// Login, registration, lobby, workshop, stage selection, and records center views.
// ---------- Account login ----------
document.getElementById('btnLogin').addEventListener('click', async () => {
  const username = document.getElementById('usernameInput').value.trim();
  const password = document.getElementById('passwordInput').value;
  const errEl = document.getElementById('startError');
  errEl.textContent = '';
  if (!username || !password) { errEl.textContent = '請輸入帳號與密碼'; return; }
  try {
    await enterGame(await api('POST', '/api/auth/login', { username, password }));
  } catch (e) {
    errEl.textContent = '登入失敗：' + e.message;
  }
});
document.getElementById('passwordInput').addEventListener('keydown', event => {
  if (event.key === 'Enter') document.getElementById('btnLogin').click();
});

document.getElementById('btnGoRegister').addEventListener('click', () => {
  document.getElementById('registerUsernameInput').value =
    document.getElementById('usernameInput').value.trim();
  document.getElementById('registerError').textContent = '';
  showScreen('register');
});

document.getElementById('btnBackToLogin').addEventListener('click', () => {
  document.getElementById('startError').textContent = '';
  showScreen('start');
});

document.getElementById('btnCreateAccount').addEventListener('click', async () => {
  const username = document.getElementById('registerUsernameInput').value.trim();
  const password = document.getElementById('registerPasswordInput').value;
  const passwordConfirm = document.getElementById('registerPasswordConfirmInput').value;
  const playerName = document.getElementById('registerPlayerNameInput').value.trim();
  const errEl = document.getElementById('registerError');
  errEl.textContent = '';
  if (!username || !password || !playerName) {
    errEl.textContent = '請填寫顯示名稱、帳號與密碼';
    return;
  }
  if (password !== passwordConfirm) {
    errEl.textContent = '兩次輸入的密碼不一致';
    return;
  }
  try {
    await enterGame(await api('POST', '/api/auth/register', { username, password, playerName }));
  } catch (e) {
    errEl.textContent = '註冊失敗：' + e.message;
  }
});
document.getElementById('registerPasswordConfirmInput').addEventListener('keydown', event => {
  if (event.key === 'Enter') document.getElementById('btnCreateAccount').click();
});

function renderLobby() {
  const progress = state.progress;
  if (!progress) return;
  document.getElementById('lobbyPlayerName').textContent = state.playerName;
  document.getElementById('lobbyLevel').textContent = progress.level;
  document.getElementById('lobbyGold').textContent = progress.gold.toLocaleString();
  document.getElementById('lobbyStages').textContent =
    `${progress.completedStages} / ${state.stages.length}`;
  document.getElementById('lobbyTowers').textContent =
    `${progress.unlockedTowers.length} / ${state.totalTowerCount}`;
}

document.getElementById('btnLobbyStages').addEventListener('click', () => {
  renderStageList();
  showScreen('stages');
});
document.getElementById('btnLobbyWorkshop').addEventListener('click', () => {
  renderTowerWorkshop();
  showScreen('workshop');
});
document.getElementById('btnLobbyRecords').addEventListener('click', async () => {
  await openRecordsCenter('history');
});
document.getElementById('btnBackFromRecords').addEventListener('click', () => {
  showScreen(state.accessToken ? 'lobby' : 'start');
});
document.getElementById('btnBackFromWorkshop').addEventListener('click', () => {
  renderLobby();
  showScreen('lobby');
});
document.getElementById('btnBackToLobby').addEventListener('click', () => {
  showScreen('lobby');
});
document.getElementById('btnLogout').addEventListener('click', async () => {
  try {
    await api('POST', '/api/auth/logout');
  } catch (ignored) {
    // Local logout still succeeds when the server session has already expired.
  }
  clearSession();
  showScreen('start');
});

function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function formatPlayTime(seconds) {
  const safeSeconds = Math.max(0, Number(seconds) || 0);
  const minutes = Math.floor(safeSeconds / 60);
  const remainder = safeSeconds % 60;
  return minutes > 0 ? `${minutes}分 ${remainder}秒` : `${remainder}秒`;
}

function formatRecordDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '時間未知';
  return new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
}

function renderTowerWorkshop(message = '', messageType = '', purchasing = false) {
  if (!state.progress) return;
  const gold = Number(state.progress.gold) || 0;
  const unlockedTowerIds = new Set(
    state.progress.unlockedTowers.map(tower => tower.towerId)
  );
  document.getElementById('workshopGold').textContent = gold.toLocaleString();
  const noticeEl = document.getElementById('workshopNotice');
  noticeEl.textContent = message;
  noticeEl.className = `workshop-notice${messageType ? ` ${messageType}` : ''}`;

  const listEl = document.getElementById('workshopTowerList');
  listEl.innerHTML = state.allTowers.map(tower => {
    const unlocked = unlockedTowerIds.has(tower.towerId);
    const affordable = gold >= tower.unlockCost;
    const actionText = unlocked
      ? '✓ 已永久解鎖'
      : affordable
      ? `永久解鎖 ${tower.unlockCost.toLocaleString()} G`
      : `尚缺 ${(tower.unlockCost - gold).toLocaleString()} G`;
    return `
      <article class="workshop-card${unlocked ? ' unlocked' : ''}${!affordable ? ' unaffordable' : ''}"
        style="--tower-color:${towerColor(tower.towerType)}">
        <div class="workshop-tower-head">
          <span class="workshop-tower-icon" aria-hidden="true">${towerIconLabel(tower.towerType)}</span>
          <div>
            <span>${towerRoleLabel(tower.towerType)}</span>
            <strong>${escapeHtml(tower.towerName)}</strong>
          </div>
          <span class="workshop-state">${unlocked ? '已解鎖' : '未解鎖'}</span>
        </div>
        <div class="workshop-stats">
          <span><small>傷害</small><b>${tower.damage}</b></span>
          <span><small>攻速</small><b>${tower.attackSpeed}</b></span>
          <span><small>射程</small><b>${tower.attackRange}</b></span>
          <span><small>戰場造價</small><b>${tower.cost} G</b></span>
        </div>
        <div class="workshop-purchase-row">
          <div><small>永久解鎖價格</small><b>${tower.unlockCost === 0 ? '起始配發' : `${tower.unlockCost.toLocaleString()} G`}</b></div>
          <button class="workshop-buy" data-id="${tower.towerId}"
            ${unlocked || !affordable || purchasing ? 'disabled' : ''}>${actionText}</button>
        </div>
      </article>
    `;
  }).join('');

  listEl.querySelectorAll('.workshop-buy:not(:disabled)').forEach(button => {
    button.addEventListener('click', () => purchaseTower(Number(button.dataset.id)));
  });
}

async function purchaseTower(towerId) {
  const tower = state.allTowers.find(item => item.towerId === towerId);
  if (!tower) return;
  const confirmed = await confirmAction({
    badge: '防禦塔工坊',
    title: `解鎖「${tower.towerName}」`,
    message: `將使用 ${tower.unlockCost.toLocaleString()} G 永久戰備金，解鎖後即可在所有關卡配置這座塔。`,
    confirmLabel: '確認解鎖',
    cancelLabel: '返回工坊',
  });
  if (!confirmed) return;

  renderTowerWorkshop('正在處理解鎖，請稍候...', 'pending', true);
  try {
    const result = await api('POST', `/api/towers/${towerId}/unlock`);
    await loadPlayerContent();
    renderLobby();
    renderTowerWorkshop(
      `「${result.towerName}」已永久解鎖，剩餘 ${result.remainingGold.toLocaleString()} G。`,
      'success'
    );
  } catch (error) {
    if (!state.accessToken) {
      showScreen('start', { replaceRoute: true });
      return;
    }
    renderTowerWorkshop(`解鎖失敗：${error.message}`, 'error');
  }
}

function selectRecordsTab(tab) {
  const rankingSelected = tab === 'ranking';
  const historyButton = document.getElementById('btnRecordsHistory');
  const rankingButton = document.getElementById('btnRecordsRanking');
  historyButton.classList.toggle('active', !rankingSelected);
  rankingButton.classList.toggle('active', rankingSelected);
  historyButton.setAttribute('aria-selected', String(!rankingSelected));
  rankingButton.setAttribute('aria-selected', String(rankingSelected));
  historyButton.tabIndex = rankingSelected ? -1 : 0;
  rankingButton.tabIndex = rankingSelected ? 0 : -1;
  document.getElementById('recordsHistoryPanel').hidden = rankingSelected;
  document.getElementById('recordsRankingPanel').hidden = !rankingSelected;
}

async function openRecordsCenter(tab = 'history', { replaceRoute = false } = {}) {
  selectRecordsTab(tab);
  showScreen('records', { replaceRoute });
  if (tab === 'ranking') await loadRanking();
  else await loadHistory();
}

document.getElementById('btnRecordsHistory').addEventListener('click', async () => {
  await openRecordsCenter('history');
});
document.getElementById('btnRecordsRanking').addEventListener('click', async () => {
  await openRecordsCenter('ranking');
});
document.querySelector('.records-tabs').addEventListener('keydown', async event => {
  if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
  event.preventDefault();
  const nextTab = document.getElementById('btnRecordsHistory').classList.contains('active')
    ? 'ranking'
    : 'history';
  await openRecordsCenter(nextTab);
  document.getElementById(nextTab === 'ranking' ? 'btnRecordsRanking' : 'btnRecordsHistory').focus();
});

async function loadHistory() {
  const listEl = document.getElementById('historyList');
  listEl.innerHTML = '<div class="history-empty">正在載入個人歷史戰績...</div>';
  try {
    const records = await api('GET', '/api/game-records/me');
    if (records.length === 0) {
      listEl.innerHTML = '<div class="history-empty">目前還沒有戰績，完成一場任務後就會留下紀錄。</div>';
      return;
    }

    listEl.innerHTML = records.map((record, index) => {
      const won = record.result.toUpperCase() === 'WIN';
      return `
        <article class="history-row">
          <div class="history-sequence">${String(index + 1).padStart(2, '0')}</div>
          <div class="history-main">
            <time class="history-date">${escapeHtml(formatRecordDate(record.createdAt))}</time>
            <strong>${escapeHtml(record.stageName)}</strong>
            <span>任務 ${String(missionNumberForStage(record.stageId)).padStart(2, '0')}</span>
          </div>
          <span class="history-result ${won ? 'win' : 'lose'}">${won ? '勝利' : '失敗'}</span>
          <div class="history-metrics">
            <span><small>分數</small><b>${Number(record.score).toLocaleString()}</b></span>
            <span><small>遊戲時間</small><b>${formatPlayTime(record.playTime)}</b></span>
          </div>
        </article>
      `;
    }).join('');
  } catch (e) {
    listEl.innerHTML = `<div class="history-empty">載入歷史戰績失敗：${escapeHtml(e.message)}</div>`;
  }
}

async function loadRanking() {
  const stageFilter = document.getElementById('rankingStageFilter');
  const previousValue = stageFilter.value;
  stageFilter.innerHTML = [
    '<option value="">全部關卡</option>',
    ...state.stages.map(stage =>
      `<option value="${stage.stageId}">任務 ${String(missionNumberForStage(stage.stageId)).padStart(2, '0')}｜${escapeHtml(stage.stageName)}</option>`
    ),
  ].join('');
  if ([...stageFilter.options].some(option => option.value === previousValue)) {
    stageFilter.value = previousValue;
  }
  const selectedStageId = Number(stageFilter.value) || null;
  const listEl = document.getElementById('rankList');
  const podiumEl = document.getElementById('rankPodium');
  podiumEl.innerHTML = '';
  listEl.innerHTML = '<div class="ranking-empty">正在載入排行榜...</div>';
  try {
    const rankingPath = selectedStageId
      ? `/api/rankings?stageId=${selectedStageId}`
      : '/api/rankings';
    const ranking = await api('GET', rankingPath);
    const ranked = ranking.entries;

    if (ranked.length === 0) {
      listEl.innerHTML = '<div class="ranking-empty">目前還沒有戰績，完成第一場遊戲就能登上榜單。</div>';
      return;
    }

    const medals = ['🥇', '🥈', '🥉'];
    podiumEl.innerHTML = ranked.slice(0, 3).map((record, index) => {
      const playerName = record.playerName;
      const initial = Array.from(playerName.trim())[0] || '?';
      const won = record.result.toUpperCase() === 'WIN';
      return `
        <div class="podium-card rank-${index + 1}">
          <div class="podium-rank-label">第 ${index + 1} 名</div>
          <div class="podium-medal">${medals[index]}</div>
          <div class="rank-avatar">${escapeHtml(initial.toUpperCase())}</div>
          <div class="podium-name" title="${escapeHtml(playerName)}">${escapeHtml(playerName)}</div>
          <div class="podium-score">${record.score.toLocaleString()} 分</div>
          <div class="podium-meta">${escapeHtml(record.stageName)}</div>
          <div class="podium-detail">
            <span>${formatPlayTime(record.playTime)}</span>
            <span class="${won ? 'win' : 'lose'}">${won ? '勝利' : '失敗'}</span>
          </div>
        </div>
      `;
    }).join('');

    const highestScore = Math.max(1, ranked[0].score);
    listEl.innerHTML = ranked.map(record => {
      const playerName = record.playerName;
      const stageName = record.stageName;
      const initial = Array.from(playerName.trim())[0] || '?';
      const won = record.result.toUpperCase() === 'WIN';
      const resultClass = won ? 'win' : 'lose';
      const resultText = won ? '勝利' : '失敗';
      const scorePercent = Math.min(100, Math.max(4, Math.round(record.score / highestScore * 100)));
      return `
      <div class="rank-row${record.rank <= 3 ? ` top-rank top-${record.rank}` : ''}">
        <div class="rank-position">#${record.rank}</div>
        <div class="rank-avatar">${escapeHtml(initial.toUpperCase())}</div>
        <div>
          <div class="rank-name">${escapeHtml(playerName)}</div>
          <div class="rank-detail">最佳紀錄 · ${formatPlayTime(record.playTime)}</div>
        </div>
        <div class="rank-badges">
          <span class="rank-tag">${escapeHtml(stageName)}</span>
          <span class="rank-tag ${resultClass}">${resultText}</span>
        </div>
        <div class="rank-score-wrap">
          <div class="rank-score">${record.score.toLocaleString()} 分</div>
          <div class="rank-score-track"><i style="width:${scorePercent}%"></i></div>
        </div>
      </div>
    `;
    }).join('');
  } catch (e) {
    podiumEl.innerHTML = '';
    listEl.innerHTML = `<div class="ranking-empty">載入排行榜失敗：${escapeHtml(e.message)}</div>`;
  }
}

document.getElementById('rankingStageFilter').addEventListener('change', loadRanking);

// ---------- Stage select ----------
function renderStageList() {
  const listEl = document.getElementById('stageList');
  listEl.innerHTML = state.stages.map(s => {
    const ready = Array.isArray(s.path) && s.path.length >= 2
      && Array.isArray(s.waves) && s.waves.length > 0;
    const unlocked = s.progress?.unlocked === true;
    const canEnter = ready && unlocked;
    const best = !unlocked
      ? '尚未解鎖'
      : s.progress?.bestScore == null
      ? '尚無紀錄'
      : `最佳 ${s.progress.bestScore} 分 / ${s.progress.bestPlayTime} 秒`;
    return `
    <div class="stage-card${canEnter ? ' can-enter' : ' locked'}" data-id="${s.stageId}"
      role="button" tabindex="${canEnter ? '0' : '-1'}" aria-disabled="${!canEnter}"
      aria-label="${escapeHtml(s.stageName)}，${canEnter ? '點擊進入任務簡報' : '目前無法進入'}">
      <div class="stage-card-header">
        <div class="name">${escapeHtml(s.stageName)}</div>
        <span class="difficulty">${escapeHtml(s.difficulty)}</span>
      </div>
      <div class="mission">${escapeHtml(storyForStage(s.stageName))}</div>
      <div class="stage-stats">
        <span>敵人 ${s.enemyCount}</span>
        <span>波次 ${s.waves?.length || 0}</span>
        <span>獎勵 ${s.rewardGold}G</span>
      </div>
      <div class="stage-card-footer">
        <span class="stage-best">${best}</span>
      </div>
    </div>
  `;
  }).join('');
  listEl.querySelectorAll('.stage-card').forEach(card => {
    const enterStage = () => {
      const id = Number(card.dataset.id);
      const stage = state.stages.find(s => s.stageId === id);
      if (!stage || stage.progress?.unlocked !== true
          || !Array.isArray(stage.path) || stage.path.length < 2
          || !Array.isArray(stage.waves) || stage.waves.length === 0) return;
      state.selectedStage = stage;
      openMissionBriefing(stage);
    };
    card.addEventListener('click', enterStage);
    card.addEventListener('keydown', event => {
      if (event.key !== 'Enter' && event.key !== ' ') return;
      event.preventDefault();
      enterStage();
    });
  });
}

function openMissionBriefing(stage) {
  document.getElementById('briefingCode').textContent =
    `任務 ${String(missionNumberForStage(stage.stageId)).padStart(2, '0')} · ${stage.difficulty}`;
  document.getElementById('briefingTitle').textContent = stage.stageName;
  document.getElementById('briefingStory').textContent = storyForStage(stage.stageName);
  document.getElementById('briefingObjective').textContent =
    `守住基地，擊退 ${stage.waves.length} 波、共 ${stage.enemyCount} 名敵人`;
  document.getElementById('overlay-briefing').hidden = false;
}

document.getElementById('btnDeploy').addEventListener('click', () => {
  document.getElementById('overlay-briefing').hidden = true;
  startGame();
});

document.getElementById('btnCancelBriefing').addEventListener('click', () => {
  document.getElementById('overlay-briefing').hidden = true;
  state.selectedStage = null;
});

async function applyRouteFromLocation() {
  const requestedScreen = screenNameFromHash();
  const authenticated = Boolean(state.accessToken && state.playerId);

  if (!requestedScreen) {
    showScreen(authenticated ? 'lobby' : 'start', { replaceRoute: true });
    return;
  }

  if (!authenticated) {
    const publicScreen = requestedScreen === 'register' ? 'register' : 'start';
    showScreen(publicScreen, {
      updateRoute: true,
      replaceRoute: requestedScreen !== publicScreen,
    });
    return;
  }

  if (requestedScreen === 'start' || requestedScreen === 'register') {
    showScreen('lobby', { replaceRoute: true });
    return;
  }

  if (requestedScreen === 'game' && !game) {
    showScreen('lobby', { replaceRoute: true });
    return;
  }

  if (requestedScreen === 'records') {
    const legacyRoute = window.location.hash === '#/history'
      || window.location.hash === '#/ranking';
    await openRecordsCenter(recordTabFromHash(), { replaceRoute: legacyRoute });
    return;
  }

  showScreen(requestedScreen, { updateRoute: false });
}

window.addEventListener('popstate', applyRouteFromLocation);
window.addEventListener('hashchange', applyRouteFromLocation);

// Restore the same player after refreshing or reopening the game.
(async function restoreSession() {
  const requestedScreen = screenNameFromHash();
  const requestedRecordsTab = recordTabFromHash();
  const legacyRecordsRoute = window.location.hash === '#/history'
    || window.location.hash === '#/ranking';
  if (!state.accessToken) {
    await applyRouteFromLocation();
    return;
  }
  const errEl = document.getElementById('startError');
  errEl.textContent = '正在恢復登入狀態...';
  try {
    await enterGame(await api('GET', '/api/auth/me'), { updateRoute: false });
    errEl.textContent = '';
    if (requestedScreen === 'records') {
      await openRecordsCenter(requestedRecordsTab, { replaceRoute: legacyRecordsRoute });
    } else if (requestedScreen === 'stages') {
      showScreen('stages', { updateRoute: false });
    } else if (requestedScreen === 'workshop') {
      renderTowerWorkshop();
      showScreen('workshop', { updateRoute: false });
    } else {
      showScreen('lobby', {
        updateRoute: true,
        replaceRoute: requestedScreen !== 'lobby',
      });
    }
  } catch (error) {
    clearSession();
    errEl.textContent = '登入已過期，請重新登入。';
    showScreen('start', { replaceRoute: true });
  }
})();
