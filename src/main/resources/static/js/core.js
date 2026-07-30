// Shared screen state, API access, session handling, and player content.
const screens = {
  start: document.getElementById('screen-start'),
  register: document.getElementById('screen-register'),
  stages: document.getElementById('screen-stages'),
  ranking: document.getElementById('screen-ranking'),
  game: document.getElementById('screen-game'),
};
function showScreen(name) {
  Object.values(screens).forEach(s => s.hidden = true);
  screens[name].hidden = false;
  document.body.classList.toggle('game-active', name === 'game');
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

async function enterGame(authData) {
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
  showScreen('stages');
}

async function loadPlayerContent() {
  const [stages, towers, progress] = await Promise.all([
    api('GET', '/api/stages'),
    api('GET', '/api/towers'),
    api('GET', `/api/players/${state.playerId}/progress`),
  ]);
  const stageProgressById = new Map(progress.stages.map(item => [item.stageId, item]));
  const unlockedTowerIds = new Set(
    progress.unlockedTowers.map(item => item.towerId)
  );

  state.progress = progress;
  state.stages = stages
    .map(stage => ({ ...stage, progress: stageProgressById.get(stage.stageId) }));
  state.towers = towers.filter(tower => unlockedTowerIds.has(tower.towerId));
}
