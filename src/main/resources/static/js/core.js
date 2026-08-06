// Shared screen state, API access, session handling, and player content.
const screens = {
  start: document.getElementById('screen-start'),
  register: document.getElementById('screen-register'),
  stages: document.getElementById('screen-stages'),
  history: document.getElementById('screen-history'),
  ranking: document.getElementById('screen-ranking'),
  game: document.getElementById('screen-game'),
};

const SCREEN_ROUTES = {
  start: '/login',
  register: '/register',
  stages: '/stages',
  history: '/history',
  ranking: '/ranking',
  game: '/game',
};
const ROUTE_SCREENS = Object.fromEntries(
  Object.entries(SCREEN_ROUTES).map(([screenName, route]) => [route, screenName])
);
let currentScreenName = 'start';

function screenNameFromHash() {
  const route = window.location.hash.replace(/^#/, '') || '/login';
  return ROUTE_SCREENS[route] || null;
}

function syncRouteForScreen(name, replaceRoute = false) {
  const targetHash = `#${SCREEN_ROUTES[name]}`;
  if (window.location.hash === targetHash) return;
  const historyMethod = replaceRoute ? 'replaceState' : 'pushState';
  window.history[historyMethod](null, '', targetHash);
}

function showScreen(name, { updateRoute = true, replaceRoute = false } = {}) {
  if (!screens[name]) return;
  if (currentScreenName === 'game' && name !== 'game'
      && typeof stopCurrentGame === 'function') {
    stopCurrentGame();
  }
  Object.values(screens).forEach(s => s.hidden = true);
  screens[name].hidden = false;
  currentScreenName = name;
  document.body.classList.toggle('game-active', name === 'game');
  if (updateRoute) syncRouteForScreen(name, replaceRoute);
  if (name === 'game') requestAnimationFrame(() => window.scrollTo(0, 0));
}

async function api(method, path, body) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  if (state.accessToken) headers.Authorization = `Bearer ${state.accessToken}`;
  const res = await fetch(path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  const json = await res.json();
  if (!json.success) {
    if (res.status === 401) clearSession();
    const validationDetails = json.data && typeof json.data === 'object'
      ? Object.values(json.data).filter(Boolean).join('、')
      : '';
    throw new Error(validationDetails || json.message || '系統發生錯誤，請稍後再試');
  }
  return json.data;
}

const state = {
  accessToken: localStorage.getItem('machineRiftAccessToken'),
  playerId: null,
  playerName: '',
  stages: [],
  towers: [],
  players: [],
  progress: null,
  selectedStage: null,
  towerSort: 'cost-asc',
};

const TOWER_SORT_OPTIONS = new Set(['cost-asc', 'cost-desc', 'damage-desc', 'damage-asc']);

function loadTowerSortPreference(playerId) {
  const savedSort = localStorage.getItem(`machineRiftTowerSort:${playerId}`);
  return TOWER_SORT_OPTIONS.has(savedSort) ? savedSort : 'cost-asc';
}

function clearSession() {
  localStorage.removeItem('machineRiftAccessToken');
  state.accessToken = null;
  state.playerId = null;
  state.playerName = '';
  state.progress = null;
}

async function enterGame(authData, { updateRoute = true } = {}) {
  if (authData?.accessToken) {
    state.accessToken = authData.accessToken;
    localStorage.setItem('machineRiftAccessToken', authData.accessToken);
  }
  const player = authData?.player || authData;
  state.playerId = player.playerId;
  state.playerName = player.playerName;
  state.towerSort = loadTowerSortPreference(player.playerId);
  document.getElementById('currentPlayerName').textContent = player.playerName;
  await loadPlayerContent();
  renderStageList();
  showScreen('stages', { updateRoute });
}

async function loadPlayerContent() {
  const [stages, towers, progress] = await Promise.all([
    api('GET', '/api/stages'),
    api('GET', '/api/towers'),
    api('GET', '/api/players/me/progress'),
  ]);
  const stageProgressById = new Map(progress.stages.map(item => [item.stageId, item]));
  const unlockedTowerIds = new Set(
    progress.unlockedTowers.map(item => item.towerId)
  );

  state.progress = progress;
  state.stages = stages
    .sort((left, right) => left.stageId - right.stageId)
    .map(stage => ({ ...stage, progress: stageProgressById.get(stage.stageId) }));
  state.towers = towers.filter(tower => unlockedTowerIds.has(tower.towerId));
}

function missionNumberForStage(stageId) {
  const stageIndex = state.stages.findIndex(stage => stage.stageId === stageId);
  return stageIndex >= 0 ? stageIndex + 1 : stageId;
}
