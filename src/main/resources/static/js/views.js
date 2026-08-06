// Login, registration, stage selection, and ranking views.
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

document.getElementById('btnShowRanking').addEventListener('click', async () => {
  await openRanking();
});
document.getElementById('btnShowHistory').addEventListener('click', async () => {
  await openHistory();
});
document.getElementById('btnBackFromRanking').addEventListener('click', () => {
  showScreen(state.accessToken ? 'stages' : 'start');
});
document.getElementById('btnBackFromHistory').addEventListener('click', () => {
  showScreen(state.accessToken ? 'stages' : 'start');
});
document.getElementById('btnBackToStart').addEventListener('click', async () => {
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

async function openHistory() {
  const listEl = document.getElementById('historyList');
  listEl.innerHTML = '<div class="history-empty">正在載入個人歷史戰績...</div>';
  showScreen('history');
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
            <span>任務 ${String(record.stageId).padStart(2, '0')}</span>
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

async function openRanking() {
  const listEl = document.getElementById('rankList');
  const podiumEl = document.getElementById('rankPodium');
  podiumEl.innerHTML = '';
  listEl.innerHTML = '<div class="ranking-empty">正在載入排行榜...</div>';
  showScreen('ranking');
  try {
    const ranking = await api('GET', '/api/rankings');
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
    showScreen(authenticated ? 'stages' : 'start', { replaceRoute: true });
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
    showScreen('stages', { replaceRoute: true });
    return;
  }

  if (requestedScreen === 'game' && !game) {
    showScreen('stages', { replaceRoute: true });
    return;
  }

  if (requestedScreen === 'ranking') {
    if (currentScreenName !== 'ranking') await openRanking();
    return;
  }

  if (requestedScreen === 'history') {
    if (currentScreenName !== 'history') await openHistory();
    return;
  }

  showScreen(requestedScreen, { updateRoute: false });
}

window.addEventListener('popstate', applyRouteFromLocation);
window.addEventListener('hashchange', applyRouteFromLocation);

// Restore the same player after refreshing or reopening the game.
(async function restoreSession() {
  const requestedScreen = screenNameFromHash();
  if (!state.accessToken) {
    await applyRouteFromLocation();
    return;
  }
  const errEl = document.getElementById('startError');
  errEl.textContent = '正在恢復登入狀態...';
  try {
    await enterGame(await api('GET', '/api/auth/me'), { updateRoute: false });
    errEl.textContent = '';
    if (requestedScreen === 'ranking') {
      await openRanking();
    } else if (requestedScreen === 'history') {
      await openHistory();
    } else {
      showScreen('stages', {
        updateRoute: true,
        replaceRoute: requestedScreen !== 'stages',
      });
    }
  } catch (error) {
    clearSession();
    errEl.textContent = '登入已過期，請重新登入。';
    showScreen('start', { replaceRoute: true });
  }
})();
