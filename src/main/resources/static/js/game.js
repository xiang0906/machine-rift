// Canvas tower-defense engine.
// ---------- Game constants ----------
const CELL = 50, COLS = 14, ROWS = 8;
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

function routeForStage(stage) {
  if (!stage || !Array.isArray(stage.path)) return [];
  return [...stage.path]
    .sort((a, b) => a.pointOrder - b.pointOrder)
    .map(point => ({ c: point.gridCol, r: point.gridRow }));
}

function computePathCells(route) {
  const cells = new Set();
  for (let i = 0; i < route.length - 1; i++) {
    const a = route[i], b = route[i + 1];
    if (a.r === b.r) {
      for (let c = Math.min(a.c, b.c); c <= Math.max(a.c, b.c); c++) cells.add(c + ',' + a.r);
    } else {
      for (let r = Math.min(a.r, b.r); r <= Math.max(a.r, b.r); r++) cells.add(a.c + ',' + r);
    }
  }
  return cells;
}

function buildPathPoints(route) {
  const gridPts = route.map(w => ({ x: w.c * CELL + CELL / 2, y: w.r * CELL + CELL / 2 }));
  // Extend a short spawn/exit segment beyond the first and last waypoint so
  // enemies enter and leave from just off the visible grid.
  const first = gridPts[0], second = gridPts[1];
  const d1 = Math.hypot(first.x - second.x, first.y - second.y) || 1;
  const entry = { x: first.x + (first.x - second.x) / d1 * 40, y: first.y + (first.y - second.y) / d1 * 40 };
  const last = gridPts[gridPts.length - 1], prev = gridPts[gridPts.length - 2];
  const d2 = Math.hypot(last.x - prev.x, last.y - prev.y) || 1;
  const exit = { x: last.x + (last.x - prev.x) / d2 * 40, y: last.y + (last.y - prev.y) / d2 * 40 };
  const waypoints = [entry, ...gridPts, exit];

  const pts = [];
  for (let i = 0; i < waypoints.length - 1; i++) {
    const a = waypoints[i], b = waypoints[i + 1];
    const d = Math.hypot(b.x - a.x, b.y - a.y);
    const steps = Math.max(1, Math.ceil(d / 4));
    for (let s = 0; s < steps; s++) {
      pts.push({ x: a.x + (b.x - a.x) * s / steps, y: a.y + (b.y - a.y) * s / steps });
    }
  }
  pts.push(waypoints[waypoints.length - 1]);
  return pts;
}

const START_GOLD = 300;
const START_BASE_HP = 10;
const PROJECTILE_SPEED = 320;
const HIT_RADIUS = 8;
const TOWER_TURN_SPEED = 7;

let game = null;
let rafId = null;

function stopCurrentGame() {
  if (rafId) cancelAnimationFrame(rafId);
  rafId = null;
  game = null;
  document.getElementById('overlay-result').hidden = true;
}

function startGame() {
  const stage = state.selectedStage;
  const route = routeForStage(stage);
  const waves = [...stage.waves].sort((a, b) => a.waveNumber - b.waveNumber);
  const totalEnemyCount = waves.reduce((total, wave) => total + wave.enemyCount, 0);
  game = {
    gold: START_GOLD,
    baseHp: START_BASE_HP,
    score: 0,
    towers: [],
    enemies: [],
    projectiles: [],
    effects: [],
    selectedTowerType: null,
    hoverCell: null,
    waveActive: false,
    spawned: 0,
    spawnedInWave: 0,
    enemyCount: totalEnemyCount,
    waves,
    waveIndex: 0,
    spawnTimer: 0,
    lastTs: null,
    startedAt: null,
    ended: false,
    shakeUntil: 0,
    victoryAt: null,
    route,
    pathCells: computePathCells(route),
    pathPoints: buildPathPoints(route),
  };
  document.getElementById('gameMissionTitle').textContent =
    `任務 ${String(missionNumberForStage(stage.stageId)).padStart(2, '0')}｜${stage.stageName}`;
  renderTowerPanel();
  updateHud();
  document.getElementById('gameHint').textContent =
    '操作：選擇塔種 → 點擊非路徑格建造 → 完成配置後開始波次。';
  document.getElementById('btnStartWave').disabled = false;
  showScreen('game');
  draw();
  if (rafId) cancelAnimationFrame(rafId);
  rafId = requestAnimationFrame(loop);
}

document.getElementById('btnQuitGame').addEventListener('click', async () => {
  if (!game || game.confirmPausedAt) return;
  game.confirmPausedAt = performance.now();
  const shouldQuit = await confirmAction({
    badge: '戰場指令',
    title: '確定放棄本局？',
    message: '離開後，本局分數、遊戲進度與戰場金幣都不會保存。',
    confirmLabel: '放棄並離開',
    cancelLabel: '繼續戰鬥',
    tone: 'danger',
  });
  if (!game) return;
  if (game.startedAt) game.startedAt += performance.now() - game.confirmPausedAt;
  game.confirmPausedAt = null;
  game.lastTs = performance.now();
  if (!shouldQuit) return;
  stopCurrentGame();
  showScreen('stages', { replaceRoute: true });
});

document.getElementById('towerSortSelect').addEventListener('change', event => {
  const selectedSort = event.target.value;
  if (!TOWER_SORT_OPTIONS.has(selectedSort)) return;
  state.towerSort = selectedSort;
  if (state.playerId != null) {
    localStorage.setItem(`machineRiftTowerSort:${state.playerId}`, selectedSort);
  }
  renderTowerPanel();
});

function sortedTowers() {
  const towers = [...state.towers];
  const comparison = {
    'cost-asc': (left, right) => left.cost - right.cost,
    'cost-desc': (left, right) => right.cost - left.cost,
    'damage-desc': (left, right) => right.damage - left.damage,
    'damage-asc': (left, right) => left.damage - right.damage,
  }[state.towerSort] || ((left, right) => left.cost - right.cost);

  return towers.sort((left, right) =>
    comparison(left, right)
    || left.cost - right.cost
    || left.towerId - right.towerId
  );
}

function renderTowerPanel() {
  const panel = document.getElementById('towerPanel');
  const sortSelect = document.getElementById('towerSortSelect');
  sortSelect.value = state.towerSort;
  document.getElementById('towerCount').textContent = state.towers.length > 3
    ? `已解鎖 ${state.towers.length} 座 · 上下捲動`
    : `已解鎖 ${state.towers.length} 座`;
  panel.innerHTML = sortedTowers().map(t => {
    const selected = game?.selectedTowerType === t.towerId;
    const unaffordable = game && game.gold < t.cost;
    return `
    <button class="tower-btn${selected ? ' selected' : ''}${unaffordable ? ' unaffordable' : ''}"
      data-id="${t.towerId}" aria-pressed="${selected}" aria-label="${t.towerName}，造價 ${t.cost} 金幣"
      style="--tower-color:${towerColor(t.towerType)};border-color:${towerColor(t.towerType)}55">
      <span class="tower-identity">
        <span class="tower-card-icon" aria-hidden="true">${towerIconLabel(t.towerType)}</span>
        <span class="tower-title">
          <b>${t.towerName}</b>
          <span class="tower-status">
            <span class="tower-role">${towerRoleLabel(t.towerType)}</span>
            <span class="tower-selected-label">✓ 已選擇</span>
            <span class="tower-unaffordable-label">金幣不足</span>
          </span>
        </span>
      </span>
      <span class="tower-details">
        <span class="tower-stat tower-cost"><small>造價</small><b>${t.cost}G</b></span>
        <span class="tower-stat"><small>傷害</small><b>${t.damage}</b></span>
        <span class="tower-stat"><small>攻速</small><b>${t.attackSpeed}</b></span>
        <span class="tower-stat"><small>射程</small><b>${t.attackRange}</b></span>
      </span>
    </button>
  `;
  }).join('');
  panel.querySelectorAll('.tower-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = Number(btn.dataset.id);
      const clickedTower = state.towers.find(t => t.towerId === id);
      if (clickedTower && game.gold < clickedTower.cost) {
        game.selectedTowerType = null;
        syncSelectedTowerButtons();
        document.getElementById('gameHint').textContent =
          `金幣不足，「${clickedTower.towerName}」需要 ${clickedTower.cost}G，已取消選取。`;
        return;
      }
      game.selectedTowerType = game.selectedTowerType === id ? null : id;
      const selectedTower = state.towers.find(t => t.towerId === game.selectedTowerType);
      syncSelectedTowerButtons();
      document.getElementById('gameHint').textContent = selectedTower
        ? `已選擇「${selectedTower.towerName}」：金幣足夠時可連續建造，再點一次塔卡可取消。`
        : '請選擇一座防禦塔，再點擊非路徑格子建造。';
    });
  });
}

function syncSelectedTowerButtons() {
  if (!game) return;
  document.querySelectorAll('#towerPanel .tower-btn').forEach(button => {
    const isSelected = Number(button.dataset.id) === game.selectedTowerType;
    button.classList.toggle('selected', isSelected);
    button.setAttribute('aria-pressed', String(isSelected));
  });
}

function updateTowerAffordability() {
  if (!game) return;
  document.querySelectorAll('#towerPanel .tower-btn').forEach(button => {
    const towerId = Number(button.dataset.id);
    const tower = state.towers.find(item => item.towerId === towerId);
    button.classList.toggle('unaffordable', Boolean(tower && game.gold < tower.cost));
  });
}

function towerRoleLabel(type) {
  return ({
    RAPID: '快速型',
    GATLING: '極速型',
    BALANCED: '均衡型',
    ARC: '強襲型',
    HEAVY: '重擊型',
    SIEGE: '攻城型',
  })[type] || '標準型';
}

function towerIconLabel(type) {
  return ({
    RAPID: '脈',
    GATLING: '機',
    BALANCED: '量',
    ARC: '弧',
    HEAVY: '軌',
    SIEGE: '重',
  })[type] || '塔';
}

function towerColor(type) {
  return ({
    RAPID: '#4dd0e1',
    GATLING: '#69f0ae',
    BALANCED: '#7c4dff',
    ARC: '#ffca28',
    HEAVY: '#ff7043',
    SIEGE: '#ec407a',
  })[type] || '#4dd0e1';
}

function traceRegularPolygon(radius, sides, rotation = -Math.PI / 2) {
  ctx.beginPath();
  for (let index = 0; index < sides; index++) {
    const angle = rotation + index * Math.PI * 2 / sides;
    const x = Math.cos(angle) * radius;
    const y = Math.sin(angle) * radius;
    if (index === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  }
  ctx.closePath();
}

function rotateToward(current, target, maxStep) {
  const delta = Math.atan2(Math.sin(target - current), Math.cos(target - current));
  return current + Math.max(-maxStep, Math.min(maxStep, delta));
}

function towerBarrelLength(type) {
  return ({
    RAPID: 19,
    GATLING: 21,
    BALANCED: 21,
    ARC: 21,
    HEAVY: 23,
    SIEGE: 25,
  })[type] || 20;
}

function drawTowerMuzzleFlash(tower, color, now) {
  if (!(tower.muzzleFlashUntil > now)) return;
  const life = Math.min(1, (tower.muzzleFlashUntil - now) / 90);
  const length = towerBarrelLength(tower.type);
  const positions = tower.type === 'RAPID'
    ? [[-5, -length], [5, -length]]
    : tower.type === 'ARC'
    ? [[-6, -length], [6, -length]]
    : [[0, -length]];

  ctx.save();
  ctx.globalAlpha *= life;
  ctx.fillStyle = '#fff8cf';
  ctx.strokeStyle = color;
  ctx.lineWidth = 2;
  ctx.shadowColor = color;
  ctx.shadowBlur = 14;
  positions.forEach(([x, y]) => {
    ctx.beginPath();
    ctx.arc(x, y, 3.5 + life * 2, 0, Math.PI * 2);
    ctx.fill();
    ctx.beginPath();
    ctx.moveTo(x, y - 2);
    ctx.lineTo(x, y - 9 - life * 5);
    ctx.stroke();
  });
  ctx.restore();
}

function drawTowerBody(tower, alpha = 1, now = performance.now()) {
  const color = towerColor(tower.type);
  ctx.save();
  ctx.translate(tower.x, tower.y);
  ctx.globalAlpha = alpha;
  ctx.shadowColor = color;
  ctx.shadowBlur = 9;

  // Shared mounting plate keeps every tower anchored to the grid.
  ctx.fillStyle = '#07121f';
  ctx.strokeStyle = 'rgba(194, 230, 240, 0.34)';
  ctx.lineWidth = 1.4;
  ctx.beginPath();
  ctx.arc(0, 0, 16, 0, Math.PI * 2);
  ctx.fill();
  ctx.stroke();
  ctx.shadowBlur = 5;

  const aimAngle = Number.isFinite(tower.aimAngle) ? tower.aimAngle : -Math.PI / 2;
  const recoil = tower.recoilUntil > now
    ? Math.min(4, (tower.recoilUntil - now) / 130 * 4)
    : 0;
  ctx.save();
  ctx.rotate(aimAngle + Math.PI / 2);
  ctx.translate(0, recoil);

  switch (tower.type) {
    case 'RAPID':
      // Twin pulse emitters.
      ctx.strokeStyle = color;
      ctx.lineWidth = 3;
      ctx.lineCap = 'round';
      ctx.beginPath();
      ctx.moveTo(-5, -5); ctx.lineTo(-5, -18);
      ctx.moveTo(5, -5); ctx.lineTo(5, -18);
      ctx.stroke();
      ctx.fillStyle = color;
      ctx.beginPath(); ctx.arc(0, 1, 8, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#d8fbff';
      ctx.beginPath(); ctx.arc(0, 1, 2.8, 0, Math.PI * 2); ctx.fill();
      break;

    case 'GATLING':
      // Three slim barrels and a hexagonal motor housing.
      ctx.strokeStyle = color;
      ctx.lineWidth = 2.2;
      ctx.lineCap = 'square';
      ctx.beginPath();
      ctx.moveTo(-6, -5); ctx.lineTo(-6, -18);
      ctx.moveTo(0, -7); ctx.lineTo(0, -20);
      ctx.moveTo(6, -5); ctx.lineTo(6, -18);
      ctx.stroke();
      ctx.fillStyle = color;
      traceRegularPolygon(10, 6);
      ctx.fill();
      ctx.fillStyle = '#0c342b';
      ctx.beginPath(); ctx.arc(0, 0, 3.5, 0, Math.PI * 2); ctx.fill();
      break;

    case 'BALANCED':
      // Diamond core with one precise energy barrel.
      ctx.strokeStyle = color;
      ctx.lineWidth = 4;
      ctx.beginPath(); ctx.moveTo(0, -6); ctx.lineTo(0, -20); ctx.stroke();
      ctx.fillStyle = color;
      traceRegularPolygon(11, 4, 0);
      ctx.fill();
      ctx.fillStyle = '#dfd4ff';
      traceRegularPolygon(4, 4, 0);
      ctx.fill();
      break;

    case 'ARC':
      // Split conductors make the electric tower readable at a glance.
      ctx.strokeStyle = color;
      ctx.lineWidth = 3;
      ctx.lineCap = 'round';
      ctx.beginPath();
      ctx.moveTo(-3, 2); ctx.lineTo(-9, -13); ctx.lineTo(-6, -19);
      ctx.moveTo(3, 2); ctx.lineTo(9, -13); ctx.lineTo(6, -19);
      ctx.stroke();
      ctx.fillStyle = color;
      ctx.beginPath(); ctx.arc(0, 3, 7, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = '#fff3b0';
      ctx.beginPath(); ctx.arc(-6, -19, 2.5, 0, Math.PI * 2); ctx.fill();
      ctx.beginPath(); ctx.arc(6, -19, 2.5, 0, Math.PI * 2); ctx.fill();
      break;

    case 'HEAVY':
      // A broad square chassis and oversized single cannon.
      ctx.fillStyle = color;
      ctx.fillRect(-5, -21, 10, 16);
      ctx.fillStyle = '#8f2f18';
      ctx.fillRect(-2, -20, 4, 13);
      ctx.fillStyle = color;
      ctx.fillRect(-11, -9, 22, 18);
      ctx.strokeStyle = '#ffc2ad';
      ctx.lineWidth = 1.5;
      ctx.strokeRect(-8, -6, 16, 12);
      break;

    case 'SIEGE':
      // Large octagonal base, reinforced braces and a siege barrel.
      ctx.fillStyle = color;
      traceRegularPolygon(13, 8);
      ctx.fill();
      ctx.fillStyle = '#651536';
      traceRegularPolygon(8, 8);
      ctx.fill();
      ctx.fillStyle = color;
      ctx.fillRect(-5, -23, 10, 18);
      ctx.strokeStyle = '#ffb5d2';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(-12, 8); ctx.lineTo(-17, 13);
      ctx.moveTo(12, 8); ctx.lineTo(17, 13);
      ctx.stroke();
      ctx.fillStyle = '#ffd4e5';
      ctx.beginPath(); ctx.arc(0, 0, 3, 0, Math.PI * 2); ctx.fill();
      break;

    default:
      ctx.fillStyle = color;
      ctx.beginPath(); ctx.arc(0, 0, 10, 0, Math.PI * 2); ctx.fill();
  }

  drawTowerMuzzleFlash(tower, color, now);
  ctx.restore();
  ctx.restore();
}

function drawBuiltTowerRange(tower) {
  const color = towerColor(tower.type);
  ctx.save();
  ctx.fillStyle = `${color}0d`;
  ctx.beginPath();
  ctx.arc(tower.x, tower.y, tower.range, 0, Math.PI * 2);
  ctx.fill();
  ctx.setLineDash([5, 5]);
  ctx.strokeStyle = `${color}99`;
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  ctx.arc(tower.x, tower.y, tower.range, 0, Math.PI * 2);
  ctx.stroke();
  ctx.restore();
}

function canvasCellFromPointer(event) {
  const rect = canvas.getBoundingClientRect();
  const scaleX = canvas.width / rect.width, scaleY = canvas.height / rect.height;
  const mx = (event.clientX - rect.left) * scaleX;
  const my = (event.clientY - rect.top) * scaleY;
  const col = Math.floor(mx / CELL), row = Math.floor(my / CELL);
  return col < 0 || col >= COLS || row < 0 || row >= ROWS ? null : { col, row };
}

function towerPlacementStatus(col, row) {
  if (!game.selectedTowerType) return { valid: false, reason: '請先選擇防禦塔。' };
  const cfg = state.towers.find(t => t.towerId === game.selectedTowerType);
  if (!cfg) return { valid: false, reason: '找不到選擇的防禦塔。' };
  if (game.pathCells.has(col + ',' + row)) {
    return { valid: false, cfg, reason: '敵人行進路徑不能建造。' };
  }
  if (game.towers.some(t => t.col === col && t.row === row)) {
    return { valid: false, cfg, reason: '這個格子已經有防禦塔。' };
  }
  if (game.gold < cfg.cost) {
    return { valid: false, cfg, reason: `金幣不足，建造需要 ${cfg.cost}G。` };
  }
  return { valid: true, cfg, reason: `可以建造「${cfg.towerName}」。` };
}

canvas.addEventListener('mousemove', event => {
  if (!game || game.ended) return;
  game.hoverCell = canvasCellFromPointer(event);
});

canvas.addEventListener('mouseleave', () => {
  if (game) game.hoverCell = null;
});

canvas.addEventListener('click', event => {
  if (!game || game.ended) return;
  const cell = canvasCellFromPointer(event);
  if (!cell) return;
  const placement = towerPlacementStatus(cell.col, cell.row);
  if (!placement.valid) {
    document.getElementById('gameHint').textContent = placement.reason;
    return;
  }
  const { col, row } = cell;
  const cfg = placement.cfg;
  game.gold -= cfg.cost;
  game.towers.push({
    col, row,
    x: col * CELL + CELL / 2, y: row * CELL + CELL / 2,
    damage: cfg.damage, attackSpeed: cfg.attackSpeed, range: cfg.attackRange,
    cooldown: 0, name: cfg.towerName, type: cfg.towerType,
    aimAngle: -Math.PI / 2, muzzleFlashUntil: 0, recoilUntil: 0,
  });
  const canContinueBuilding = game.gold >= cfg.cost;
  if (!canContinueBuilding) game.selectedTowerType = null;
  syncSelectedTowerButtons();
  document.getElementById('gameHint').textContent = canContinueBuilding
    ? `「${cfg.towerName}」建造完成，剩餘 ${game.gold}G。可繼續點擊空格建造同一座塔。`
    : `「${cfg.towerName}」建造完成，剩餘 ${game.gold}G，不足以再建造同一座，已自動取消選取。`;
  updateHud();
});

document.getElementById('btnStartWave').addEventListener('click', () => {
  if (!game || game.waveActive) return;
  game.waveActive = true;
  game.startedAt = performance.now();
  document.getElementById('btnStartWave').disabled = true;
  document.getElementById('gameHint').textContent = `第 1/${game.waves.length} 波進行中...敵人抵達基地會扣血。`;
  updateHud();
});

function spawnEnemy(wave) {
  const start = game.pathPoints[0];
  const config = wave.enemy;
  game.enemies.push({
    x: start.x, y: start.y, pathIdx: 0,
    hp: config.health,
    maxHp: config.health,
    speed: config.speed,
    rewardGold: config.rewardGold,
    name: config.enemyName,
    radius: enemyRadius(config.enemyName),
    alive: true,
  });
  game.spawned++;
  game.spawnedInWave++;
}

function enemyColor(name) {
  return ({
    '偵察機': '#ff7043',
    '裝甲機': '#90a4ae',
    '裂隙核心': '#ab47bc',
    '疾風無人機': '#ffee58',
    '護盾機兵': '#42a5f5',
    '裂隙巨像': '#ec407a',
  })[name] || '#ff5252';
}

function enemyRadius(name) {
  return ({
    '疾風無人機': 7,
    '偵察機': 9,
    '裝甲機': 11,
    '護盾機兵': 13,
    '裂隙核心': 14,
    '裂隙巨像': 18,
  })[name] || 10;
}

function loop(ts) {
  if (!game) return;
  if (game.lastTs == null) game.lastTs = ts;
  if (game.confirmPausedAt) {
    game.lastTs = ts;
    draw();
    rafId = requestAnimationFrame(loop);
    return;
  }
  const dt = Math.min(50, ts - game.lastTs);
  game.lastTs = ts;

  if (game.waveActive && !game.ended) {
    const currentWave = game.waves[game.waveIndex];
    game.spawnTimer -= dt;
    if (currentWave && game.spawnTimer <= 0 && game.spawnedInWave < currentWave.enemyCount) {
      spawnEnemy(currentWave);
      game.spawnTimer = currentWave.spawnIntervalMs;
    }

    game.enemies.forEach(e => {
      if (!e.alive) return;
      let remaining = e.speed * dt / 1000;
      while (remaining > 0 && e.pathIdx < game.pathPoints.length - 1) {
        const target = game.pathPoints[e.pathIdx + 1];
        const dx = target.x - e.x, dy = target.y - e.y, d = Math.hypot(dx, dy);
        if (d <= remaining) {
          e.x = target.x; e.y = target.y; e.pathIdx++; remaining -= d;
        } else {
          e.x += dx / d * remaining; e.y += dy / d * remaining; remaining = 0;
        }
      }
      if (e.pathIdx >= game.pathPoints.length - 1) {
        e.alive = false;
        game.baseHp--;
        game.shakeUntil = performance.now() + 220;
        if (game.baseHp <= 0) endGame('LOSE');
      }
    });
    game.enemies = game.enemies.filter(e => e.alive);

    game.towers.forEach(t => {
      t.cooldown -= dt;
      let target = null, bestProgress = -1;
      game.enemies.forEach(e => {
        const d = Math.hypot(e.x - t.x, e.y - t.y);
        if (d <= t.range && e.pathIdx > bestProgress) { target = e; bestProgress = e.pathIdx; }
      });
      if (target) {
        const desiredAngle = Math.atan2(target.y - t.y, target.x - t.x);
        t.aimAngle = rotateToward(t.aimAngle, desiredAngle, TOWER_TURN_SPEED * dt / 1000);
        if (t.cooldown <= 0) {
          const muzzleDistance = towerBarrelLength(t.type);
          game.projectiles.push({
            x: t.x + Math.cos(t.aimAngle) * muzzleDistance,
            y: t.y + Math.sin(t.aimAngle) * muzzleDistance,
            target,
            damage: t.damage,
          });
          t.muzzleFlashUntil = ts + 90;
          t.recoilUntil = ts + 130;
          t.cooldown = 1000 / t.attackSpeed;
        }
      }
    });

    game.projectiles.forEach(p => {
      if (!p.target.alive) { p.dead = true; return; }
      const dx = p.target.x - p.x, dy = p.target.y - p.y, d = Math.hypot(dx, dy);
      if (d <= HIT_RADIUS) {
        p.target.hp -= p.damage;
        p.target.hitFlashUntil = performance.now() + 90;
        if (p.target.hp <= 0) {
          p.target.alive = false;
          game.gold += p.target.rewardGold;
          game.score += p.target.rewardGold;
          game.effects.push({
            x: p.target.x,
            y: p.target.y - p.target.radius,
            text: `+${p.target.rewardGold}G`,
            life: 900,
            maxLife: 900,
          });
        }
        p.dead = true;
      } else {
        p.x += dx / d * PROJECTILE_SPEED * dt / 1000;
        p.y += dy / d * PROJECTILE_SPEED * dt / 1000;
      }
    });
    game.projectiles = game.projectiles.filter(p => !p.dead);

    if (!game.ended && currentWave
        && game.spawnedInWave >= currentWave.enemyCount
        && game.enemies.length === 0) {
      if (game.waveIndex >= game.waves.length - 1) {
        game.waveActive = false;
        game.victoryAt = ts + 550;
        document.getElementById('gameHint').textContent =
          '最後目標已清除，正在確認戰場狀態...';
      } else {
        game.waveIndex++;
        game.spawnedInWave = 0;
        game.spawnTimer = 1200;
        document.getElementById('gameHint').textContent =
          `第 ${game.waveIndex + 1}/${game.waves.length} 波即將開始...`;
      }
    }
    updateHud();
  }

  game.effects.forEach(effect => {
    effect.life -= dt;
    effect.y -= 18 * dt / 1000;
  });
  game.effects = game.effects.filter(effect => effect.life > 0);

  if (!game.ended && game.victoryAt != null && ts >= game.victoryAt) {
    game.victoryAt = null;
    endGame('WIN');
  }

  draw();
  if (!game.ended) rafId = requestAnimationFrame(loop);
}

function traceRoute(route) {
  if (!route || route.length === 0) return;
  ctx.beginPath();
  route.forEach((point, index) => {
    const x = point.c * CELL + CELL / 2;
    const y = point.r * CELL + CELL / 2;
    if (index === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  });
}

function drawBattlefieldBase(now) {
  const baseGradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
  baseGradient.addColorStop(0, '#071426');
  baseGradient.addColorStop(0.55, '#0b1b31');
  baseGradient.addColorStop(1, '#07111f');
  ctx.fillStyle = baseGradient;
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  const glow = ctx.createRadialGradient(
    canvas.width * 0.55, canvas.height * 0.42, 20,
    canvas.width * 0.55, canvas.height * 0.42, canvas.width * 0.58,
  );
  glow.addColorStop(0, 'rgba(38, 115, 160, 0.13)');
  glow.addColorStop(1, 'rgba(4, 10, 20, 0)');
  ctx.fillStyle = glow;
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Faint circuit traces make the board feel like a powered machine surface.
  ctx.save();
  ctx.strokeStyle = 'rgba(77, 208, 225, 0.055)';
  ctx.fillStyle = 'rgba(77, 208, 225, 0.12)';
  ctx.lineWidth = 1;
  const pulse = 0.65 + Math.sin(now / 900) * 0.2;
  ctx.globalAlpha = pulse;
  for (let row = 0; row < ROWS; row += 2) {
    const y = row * CELL + 12;
    const offset = row % 4 === 0 ? 18 : 42;
    ctx.beginPath();
    ctx.moveTo(offset, y);
    ctx.lineTo(offset + 72, y);
    ctx.lineTo(offset + 88, y + 16);
    ctx.lineTo(offset + 142, y + 16);
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(offset + 142, y + 16, 2, 0, Math.PI * 2);
    ctx.fill();
  }
  ctx.restore();
}

function drawGridSurface() {
  for (let r = 0; r < ROWS; r++) {
    for (let c = 0; c < COLS; c++) {
      const onPath = game && game.pathCells.has(c + ',' + r);
      if (onPath) {
        ctx.fillStyle = (r + c) % 2 === 0 ? 'rgba(28, 54, 82, 0.76)' : 'rgba(24, 48, 75, 0.76)';
      } else {
        ctx.fillStyle = (r + c) % 2 === 0 ? 'rgba(18, 37, 61, 0.72)' : 'rgba(13, 29, 49, 0.72)';
      }
      ctx.fillRect(c * CELL, r * CELL, CELL, CELL);

      if (!onPath) {
        ctx.fillStyle = 'rgba(255,255,255,0.025)';
        ctx.fillRect(c * CELL + 2, r * CELL + 2, CELL - 4, 1);
        ctx.fillRect(c * CELL + 2, r * CELL + 2, 1, CELL - 4);
        ctx.fillStyle = 'rgba(0,0,0,0.09)';
        ctx.fillRect(c * CELL + 3, (r + 1) * CELL - 3, CELL - 6, 1);
      }
    }
  }
}

function drawPathLayer(now) {
  if (!game?.route?.length) return;

  ctx.save();
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';

  ctx.shadowColor = '#35b9df';
  ctx.shadowBlur = 18;
  ctx.strokeStyle = 'rgba(40, 161, 203, 0.24)';
  ctx.lineWidth = 44;
  traceRoute(game.route);
  ctx.stroke();

  ctx.shadowBlur = 0;
  ctx.strokeStyle = '#263f5d';
  ctx.lineWidth = 37;
  traceRoute(game.route);
  ctx.stroke();

  const roadGradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
  roadGradient.addColorStop(0, '#355574');
  roadGradient.addColorStop(0.5, '#2c4968');
  roadGradient.addColorStop(1, '#233d5d');
  ctx.strokeStyle = roadGradient;
  ctx.lineWidth = 29;
  traceRoute(game.route);
  ctx.stroke();

  // Deployment guidance is only needed before the first wave starts.
  if (!game.startedAt) {
    ctx.setLineDash([5, 9]);
    ctx.lineDashOffset = -(now / 55) % 14;
    ctx.strokeStyle = 'rgba(111, 219, 238, 0.48)';
    ctx.lineWidth = 1.5;
    traceRoute(game.route);
    ctx.stroke();
    ctx.setLineDash([]);

    const arrowOffset = Math.floor(now / 110) % 20;
    for (let index = 9 + arrowOffset; index < game.pathPoints.length - 5; index += 24) {
      const point = game.pathPoints[index];
      const next = game.pathPoints[Math.min(index + 3, game.pathPoints.length - 1)];
      const angle = Math.atan2(next.y - point.y, next.x - point.x);
      ctx.save();
      ctx.translate(point.x, point.y);
      ctx.rotate(angle);
      ctx.strokeStyle = 'rgba(128, 222, 234, 0.55)';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.moveTo(-5, -4);
      ctx.lineTo(1, 0);
      ctx.lineTo(-5, 4);
      ctx.stroke();
      ctx.restore();
    }
  }
  ctx.restore();
}

function drawBuildableCells() {
  if (!game) return;
  const selectingTower = game.selectedTowerType != null;

  ctx.save();
  for (let row = 0; row < ROWS; row++) {
    for (let col = 0; col < COLS; col++) {
      const occupied = game.towers.some(tower => tower.col === col && tower.row === row);
      if (game.pathCells.has(col + ',' + row) || occupied) continue;

      const x = col * CELL;
      const y = row * CELL;
      const inset = 7;
      const arm = selectingTower ? 7 : 4;
      ctx.strokeStyle = selectingTower
        ? 'rgba(105, 240, 174, 0.34)'
        : 'rgba(118, 176, 199, 0.11)';
      ctx.lineWidth = selectingTower ? 1.4 : 1;
      ctx.beginPath();
      ctx.moveTo(x + inset, y + inset + arm); ctx.lineTo(x + inset, y + inset); ctx.lineTo(x + inset + arm, y + inset);
      ctx.moveTo(x + CELL - inset - arm, y + CELL - inset); ctx.lineTo(x + CELL - inset, y + CELL - inset); ctx.lineTo(x + CELL - inset, y + CELL - inset - arm);
      ctx.stroke();

      if (selectingTower) {
        ctx.fillStyle = 'rgba(105, 240, 174, 0.035)';
        ctx.fillRect(x + 4, y + 4, CELL - 8, CELL - 8);
      }
    }
  }
  ctx.restore();
}

function draw() {
  const now = performance.now();
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.save();
  if (game && now < game.shakeUntil) {
    ctx.translate((Math.random() - 0.5) * 7, (Math.random() - 0.5) * 7);
  }
  drawBattlefieldBase(now);
  drawGridSurface();
  drawPathLayer(now);
  ctx.strokeStyle = 'rgba(157, 206, 224, 0.07)';
  ctx.lineWidth = 1;
  for (let c = 0; c <= COLS; c++) { ctx.beginPath(); ctx.moveTo(c * CELL, 0); ctx.lineTo(c * CELL, ROWS * CELL); ctx.stroke(); }
  for (let r = 0; r <= ROWS; r++) { ctx.beginPath(); ctx.moveTo(0, r * CELL); ctx.lineTo(COLS * CELL, r * CELL); ctx.stroke(); }
  drawBuildableCells();

  if (game) {
    const route = game.route;
    const toPx = w => ({ x: w.c * CELL + CELL / 2, y: w.r * CELL + CELL / 2 });
    const p0 = toPx(route[0]), p1 = toPx(route[1]);
    const pl = toPx(route[route.length - 1]);
    const angle = Math.atan2(p1.y - p0.y, p1.x - p0.x);

    // Spawn point — where enemies come from
    ctx.save();
    ctx.translate(p0.x, p0.y);
    ctx.rotate(angle);
    ctx.strokeStyle = '#ff5252'; ctx.lineWidth = 2;
    ctx.shadowColor = '#ff5252'; ctx.shadowBlur = 10;
    ctx.beginPath(); ctx.arc(0, 0, 16, 0, Math.PI * 2); ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(-4, -8); ctx.lineTo(8, 0); ctx.lineTo(-4, 8);
    ctx.closePath(); ctx.fillStyle = '#ff5252'; ctx.fill();
    ctx.restore();
    ctx.fillStyle = '#ff8a80';
    ctx.font = '10px Arial'; ctx.textAlign = 'center'; ctx.textBaseline = 'bottom';
    ctx.fillText('敵人入侵', p0.x, p0.y - 20);

    // Base — what the player is defending
    ctx.save();
    ctx.shadowColor = '#4dd0e1'; ctx.shadowBlur = 14;
    ctx.fillStyle = '#4dd0e1';
    ctx.beginPath();
    ctx.moveTo(pl.x, pl.y - 17);
    ctx.lineTo(pl.x + 15, pl.y - 7);
    ctx.lineTo(pl.x + 15, pl.y + 11);
    ctx.lineTo(pl.x, pl.y + 18);
    ctx.lineTo(pl.x - 15, pl.y + 11);
    ctx.lineTo(pl.x - 15, pl.y - 7);
    ctx.closePath(); ctx.fill();
    ctx.fillStyle = '#062028';
    ctx.beginPath(); ctx.arc(pl.x, pl.y, 6, 0, Math.PI * 2); ctx.fill();
    ctx.restore();
    ctx.fillStyle = '#80deea';
    ctx.font = '10px Arial'; ctx.textAlign = 'center'; ctx.textBaseline = 'bottom';
    ctx.fillText('主堡', pl.x, pl.y - 21);

    drawTowerPlacementPreview();

    game.towers.forEach(t => {
      const hoveringTower = game.hoverCell?.col === t.col && game.hoverCell?.row === t.row;
      if (hoveringTower) drawBuiltTowerRange(t);
      drawTowerBody(t, 1, now);
    });
    game.enemies.forEach(e => {
      const healthBarWidth = Math.max(24, e.radius * 2);
      const healthBarX = e.x - healthBarWidth / 2;
      const healthBarY = e.y - e.radius - 8;
      ctx.fillStyle = e.hitFlashUntil > now ? '#ffffff' : enemyColor(e.name);
      ctx.beginPath(); ctx.arc(e.x, e.y, e.radius, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = 'rgba(255,255,255,0.2)';
      ctx.fillRect(healthBarX, healthBarY, healthBarWidth, 4);
      ctx.fillStyle = '#4caf50';
      ctx.fillRect(healthBarX, healthBarY, healthBarWidth * (e.hp / e.maxHp), 4);
    });
    game.projectiles.forEach(p => {
      ctx.fillStyle = '#ffe082';
      ctx.beginPath(); ctx.arc(p.x, p.y, 3, 0, Math.PI * 2); ctx.fill();
    });
    game.effects.forEach(effect => {
      ctx.save();
      ctx.globalAlpha = Math.min(1, effect.life / 250);
      ctx.fillStyle = '#ffd54f';
      ctx.font = 'bold 13px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'bottom';
      ctx.shadowColor = '#000000';
      ctx.shadowBlur = 4;
      ctx.fillText(effect.text, effect.x, effect.y);
      ctx.restore();
    });
  }
  const vignette = ctx.createRadialGradient(
    canvas.width / 2, canvas.height / 2, canvas.height * 0.2,
    canvas.width / 2, canvas.height / 2, canvas.width * 0.62,
  );
  vignette.addColorStop(0, 'rgba(0,0,0,0)');
  vignette.addColorStop(1, 'rgba(0,4,12,0.34)');
  ctx.fillStyle = vignette;
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.restore();
}

function drawTowerPlacementPreview() {
  if (!game.hoverCell || !game.selectedTowerType) return;
  const { col, row } = game.hoverCell;
  if (game.towers.some(tower => tower.col === col && tower.row === row)) return;
  const placement = towerPlacementStatus(col, row);
  if (!placement.cfg) return;

  const x = col * CELL + CELL / 2;
  const y = row * CELL + CELL / 2;
  const previewColor = placement.valid ? '#69f0ae' : '#ff5252';

  ctx.save();
  ctx.fillStyle = placement.valid ? 'rgba(105,240,174,0.18)' : 'rgba(255,82,82,0.2)';
  ctx.fillRect(col * CELL + 1, row * CELL + 1, CELL - 2, CELL - 2);
  ctx.strokeStyle = previewColor;
  ctx.lineWidth = 2;
  ctx.strokeRect(col * CELL + 2, row * CELL + 2, CELL - 4, CELL - 4);

  ctx.fillStyle = placement.valid ? 'rgba(105,240,174,0.06)' : 'rgba(255,82,82,0.04)';
  ctx.beginPath();
  ctx.arc(x, y, placement.cfg.attackRange, 0, Math.PI * 2);
  ctx.fill();
  ctx.strokeStyle = placement.valid ? 'rgba(105,240,174,0.72)' : 'rgba(255,82,82,0.6)';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  drawTowerBody({ x, y, type: placement.cfg.towerType }, 0.78);
  ctx.restore();
}

function updateHud() {
  const displayedWave = game.waveActive || game.spawned > 0 ? game.waveIndex + 1 : 0;
  const elapsedSeconds = game.startedAt
    ? Math.floor(((game.confirmPausedAt || performance.now()) - game.startedAt) / 1000)
    : 0;
  const elapsedMinutes = Math.floor(elapsedSeconds / 60);
  const remainingSeconds = elapsedSeconds % 60;
  document.getElementById('hudWave').textContent = displayedWave === 0
    ? `等待部署｜共 ${game.waves.length} 波`
    : `第 ${displayedWave} / ${game.waves.length} 波`;
  document.getElementById('hudTime').textContent =
    `${String(elapsedMinutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
  document.getElementById('hudEnemies').textContent = game.spawned + '/' + game.enemyCount;
  document.getElementById('hudGold').textContent = game.gold;
  const hpEl = document.getElementById('hudHp');
  hpEl.textContent = game.baseHp;
  document.getElementById('hudHpWrap').classList.toggle('hp-low', game.baseHp <= 3);
  document.getElementById('hudScore').textContent = game.score;
  updateTowerAffordability();
}

async function endGame(result) {
  if (!game || game.ended) return;
  game.ended = true;
  game.waveActive = false;
  if (result === 'WIN') game.score += state.selectedStage.rewardGold;
  const playTime = game.startedAt ? Math.round((performance.now() - game.startedAt) / 1000) : 0;

  document.getElementById('resultTitle').textContent = result === 'WIN' ? '任務成功' : '任務失敗';
  document.getElementById('resultTitle').className = result === 'WIN' ? 'result-win' : 'result-lose';
  document.getElementById('resultMessage').textContent =
    resultStoryForStage(state.selectedStage.stageName, result);
  document.getElementById('resultDetail').textContent = `分數：${game.score}　用時：${playTime} 秒`;
  document.getElementById('overlay-result').hidden = false;

  try {
    await api('POST', '/api/game-records', {
      stageId: state.selectedStage.stageId,
      score: game.score,
      result,
      playTime,
    });
    await loadPlayerContent();
    renderStageList();
  } catch (e) {
    document.getElementById('resultDetail').textContent += '（戰績儲存失敗：' + e.message + '）';
  }
}

document.getElementById('btnPlayAgain').addEventListener('click', () => {
  stopCurrentGame();
  showScreen('stages', { replaceRoute: true });
});

document.getElementById('btnResultLobby').addEventListener('click', () => {
  stopCurrentGame();
  renderLobby();
  showScreen('lobby', { replaceRoute: true });
});
