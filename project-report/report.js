const tabs = [...document.querySelectorAll('.page-tab')];
const pages = [...document.querySelectorAll('.report-page')];
const indicator = document.querySelector('#page-indicator');
const railIndicator = document.querySelector('#rail-page-indicator');
const validPages = new Set(pages.map(page => page.id));
const functionDialog = document.querySelector('#function-overview-dialog');
const openFunctionDialogButton = document.querySelector('#open-function-overview');
const closeFunctionDialogButton = document.querySelector('[data-close-function-dialog]');
const apiDialog = document.querySelector('#api-overview-dialog');
const openApiDialogButton = document.querySelector('#open-api-overview');
const closeApiDialogButton = document.querySelector('[data-close-api-dialog]');

function pageFromHash() {
  const requested = window.location.hash.replace('#', '');
  return validPages.has(requested) ? requested : 'overview';
}

function showReportPage(pageId, { updateHistory = true } = {}) {
  const nextPage = validPages.has(pageId) ? pageId : 'overview';

  pages.forEach(page => {
    const active = page.id === nextPage;
    page.hidden = !active;
    page.classList.toggle('is-active', active);
  });

  tabs.forEach(tab => {
    const active = tab.dataset.page === nextPage;
    tab.classList.toggle('is-active', active);
    tab.setAttribute('aria-selected', String(active));
  });

  const pageIndex = pages.findIndex(page => page.id === nextPage) + 1;
  const pageTotal = String(pages.length).padStart(2, '0');
  indicator.textContent = `PAGE ${String(pageIndex).padStart(2, '0')} / ${pageTotal}`;
  if (railIndicator) {
    railIndicator.textContent = `${String(pageIndex).padStart(2, '0')} / ${pageTotal}`;
  }
  document.title = `${tabs[pageIndex - 1].textContent.trim()}｜Machine Rift 專案報告`;

  if (updateHistory && window.location.hash !== `#${nextPage}`) {
    window.history.pushState({ page: nextPage }, '', `#${nextPage}`);
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

tabs.forEach(tab => {
  tab.addEventListener('click', () => showReportPage(tab.dataset.page));
});

openFunctionDialogButton.addEventListener('click', () => {
  functionDialog.showModal();
});

closeFunctionDialogButton.addEventListener('click', () => {
  functionDialog.close();
});

functionDialog.addEventListener('click', event => {
  if (event.target === functionDialog) {
    functionDialog.close();
  }
});

openApiDialogButton.addEventListener('click', () => {
  apiDialog.showModal();
});

closeApiDialogButton.addEventListener('click', () => {
  apiDialog.close();
});

apiDialog.addEventListener('click', event => {
  if (event.target === apiDialog) {
    apiDialog.close();
  }
});

window.addEventListener('popstate', () => {
  showReportPage(pageFromHash(), { updateHistory: false });
});

window.addEventListener('hashchange', () => {
  showReportPage(pageFromHash(), { updateHistory: false });
});

document.addEventListener('keydown', event => {
  if (!['ArrowLeft', 'ArrowRight'].includes(event.key)) return;

  const currentIndex = pages.findIndex(page => !page.hidden);
  const offset = event.key === 'ArrowRight' ? 1 : -1;
  const nextIndex = Math.min(Math.max(currentIndex + offset, 0), pages.length - 1);

  if (nextIndex !== currentIndex) {
    showReportPage(pages[nextIndex].id);
    tabs[nextIndex].focus();
  }
});

showReportPage(pageFromHash(), { updateHistory: false });
