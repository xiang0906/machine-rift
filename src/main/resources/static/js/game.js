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

let game = null;
let rafId = null;

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

document.getElementById('btnQuitGame').addEventListener('click', () => {
  const confirmationStartedAt = performance.now();
  const shouldQuit = window.confirm('確定放棄本局？\n本局分數與進度不會保存。');
  if (!shouldQuit) {
    if (game?.startedAt) {
      game.startedAt += performance.now() - confirmationStartedAt;
    }
    return;
  }
  if (rafId) cancelAnimationFrame(rafId);
  rafId = null;
  game = null;
  showScreen('stages');
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
  document.getElementById('towerCount').textContent = `已解鎖 ${state.towers.length} 座`;
  panel.innerHTML = sortedTowers().map(t => {
    const selected = game?.selectedTowerType === t.towerId;
    const unaffordable = game && game.gold < t.cost;
    return `
    <button class="tower-btn${selected ? ' selected' : ''}${unaffordable ? ' unaffordable' : ''}" data-id="${t.towerId}" style="border-color:${towerColor(t.towerType)}55">
      <span class="tower-title">
        <b>${t.towerName}</b>
        <span class="tower-status">
          <span class="tower-role">${towerRoleLabel(t.towerType)}</span>
          <span class="tower-selected-label">✓ 已選擇</span>
        </span>
      </span>
      <span class="tower-details">
        <span class="tower-cost">${t.cost}G</span>
        <span>傷 ${t.damage}</span>
        <span>速 ${t.attackSpeed}</span>
        <span>射程 ${t.attackRange}</span>
      </span>
    </button>
  `;
  }).join('');
  panel.querySelectorAll('.tower-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = Number(btn.dataset.id);
      game.selectedTowerType = game.selectedTowerType === id ? null : id;
      const selectedTower = state.towers.find(t => t.towerId === game.selectedTowerType);
      panel.querySelectorAll('.tower-btn').forEach(b => {
        b.classList.toggle('selected', Number(b.dataset.id) === game.selectedTowerType);
      });
      document.getElementById('gameHint').textContent = selectedTower
        ? `已選擇「${selectedTower.towerName}」：將游標移到地圖，綠色可建造、紅色不可建造。`
        : '請選擇一座防禦塔，再點擊非路徑格子建造。';
    });
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
  });
  game.selectedTowerType = null;
  document.querySelectorAll('#towerPanel .tower-btn').forEach(button => {
    button.classList.remove('selected');
  });
  document.getElementById('gameHint').textContent =
    `「${cfg.towerName}」建造完成，剩餘 ${game.gold}G。請重新選塔以建造下一座。`;
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
      if (t.cooldown > 0) return;
      let target = null, bestProgress = -1;
      game.enemies.forEach(e => {
        const d = Math.hypot(e.x - t.x, e.y - t.y);
        if (d <= t.range && e.pathIdx > bestProgress) { target = e; bestProgress = e.pathIdx; }
      });
      if (target) {
        game.projectiles.push({ x: t.x, y: t.y, target, damage: t.damage });
        t.cooldown = 1000 / t.attackSpeed;
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

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.save();
  if (game && performance.now() < game.shakeUntil) {
    ctx.translate((Math.random() - 0.5) * 7, (Math.random() - 0.5) * 7);
  }
  for (let r = 0; r < ROWS; r++) {
    for (let c = 0; c < COLS; c++) {
      const onPath = game && game.pathCells.has(c + ',' + r);
      ctx.fillStyle = onPath ? '#2c4468' : ((r + c) % 2 === 0 ? '#12213a' : '#0f1c32');
      ctx.fillRect(c * CELL, r * CELL, CELL, CELL);
    }
  }
  ctx.strokeStyle = 'rgba(255,255,255,0.06)';
  for (let c = 0; c <= COLS; c++) { ctx.beginPath(); ctx.moveTo(c * CELL, 0); ctx.lineTo(c * CELL, ROWS * CELL); ctx.stroke(); }
  for (let r = 0; r <= ROWS; r++) { ctx.beginPath(); ctx.moveTo(0, r * CELL); ctx.lineTo(COLS * CELL, r * CELL); ctx.stroke(); }

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
      ctx.fillStyle = towerColor(t.type);
      ctx.beginPath(); ctx.arc(t.x, t.y, 14, 0, Math.PI * 2); ctx.fill();
      ctx.strokeStyle = `${towerColor(t.type)}33`;
      ctx.beginPath(); ctx.arc(t.x, t.y, t.range, 0, Math.PI * 2); ctx.stroke();
    });
    game.enemies.forEach(e => {
      const healthBarWidth = Math.max(24, e.radius * 2);
      const healthBarX = e.x - healthBarWidth / 2;
      const healthBarY = e.y - e.radius - 8;
      ctx.fillStyle = e.hitFlashUntil > performance.now() ? '#ffffff' : enemyColor(e.name);
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
  ctx.restore();
}

function drawTowerPlacementPreview() {
  if (!game.hoverCell || !game.selectedTowerType) return;
  const { col, row } = game.hoverCell;
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

  ctx.globalAlpha = 0.75;
  ctx.fillStyle = towerColor(placement.cfg.towerType);
  ctx.beginPath();
  ctx.arc(x, y, 14, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
}

function updateHud() {
  const displayedWave = game.waveActive || game.spawned > 0 ? game.waveIndex + 1 : 0;
  const elapsedSeconds = game.startedAt
    ? Math.floor((performance.now() - game.startedAt) / 1000)
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
      playerId: state.playerId,
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
  document.getElementById('overlay-result').hidden = true;
  if (rafId) cancelAnimationFrame(rafId);
  rafId = null;
  game = null;
  showScreen('stages');
});
