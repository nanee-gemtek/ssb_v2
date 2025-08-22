// src/main/resources/static/js/category_menu.js
(function () {
  'use strict';

  if (window.__CAT_MENU_INIT__) return;
  window.__CAT_MENU_INIT__ = true;

  const STORAGE_KEY = 'catMenuState'; // { rootId, maxHeight, preserve }

  function init() {
    const menu = document.querySelector('.categories-menu');
    if (!menu) return;

    if (menu.dataset.bound === 'true') return;
    menu.dataset.bound = 'true';

    const ONE_OPEN = (menu.dataset.oneOpen || 'false') === 'true';
    const sections = menu.querySelectorAll('.cat-section');

    function setOpen(section, open) {
      const title = section.querySelector('.cat-title');
      const list  = section.querySelector('.cat-list');
      if (!title || !list) return;
      section.classList.toggle('open', open);
      title.setAttribute('aria-expanded', String(open));
      list.style.maxHeight = open ? (list.scrollHeight + 'px') : '0';
      list.style.opacity   = open ? '1' : '0';
    }

    // 2-1) 페이지 로드 시, 저장된 상태가 있고 같은 루트면 먼저 복원
    (function restoreIfNeeded() {
      try {
        const raw = sessionStorage.getItem(STORAGE_KEY);
        if (!raw) return;
        const state = JSON.parse(raw);
        if (!state || !state.preserve || !state.rootId) return;

        const target = Array.from(sections)
          .find(s => s.dataset.rootId === String(state.rootId));
        if (!target) return;

        const list = target.querySelector('.cat-list');
        if (!list) return;

        // 즉시 펼쳐 보이도록
        target.classList.add('open');
        target.querySelector('.cat-title')?.setAttribute('aria-expanded', 'true');
        list.style.maxHeight = (state.maxHeight ? state.maxHeight + 'px' : list.scrollHeight + 'px');
        list.style.opacity = '1';
      } finally {
        // 한 번만 쓰고 비움
        sessionStorage.removeItem(STORAGE_KEY);
      }
    })();

    // 초기 펼침 (data-open="true")
    sections.forEach(section => {
      const initiallyOpen = section.getAttribute('data-open') === 'true';
      // 이미 restore로 open 붙었으면 스킵
      if (section.classList.contains('open')) return;
      setOpen(section, initiallyOpen);
    });

    // 2-2) 제목 클릭 → 토글
    function onTitleClick(titleEl) {
      const section = titleEl.closest('.cat-section');
      if (!section) return;
      const willOpen = !section.classList.contains('open');
      if (ONE_OPEN && willOpen) {
        sections.forEach(s => { if (s !== section) setOpen(s, false); });
      }
      setOpen(section, willOpen);
    }

    // 2-3) 링크 클릭 → 같은 루트면 높이 저장 후 내비게이션
    function onLinkClick(linkEl) {
      const section = linkEl.closest('.cat-section');
      if (!section) return;

      const targetRootId = section.dataset.rootId;
      const openSection  = menu.querySelector('.cat-section.open');
      const openRootId   = openSection?.dataset.rootId;

      if (openSection && targetRootId && openRootId === targetRootId) {
        const list = openSection.querySelector('.cat-list');
        const h = list ? list.scrollHeight : 0;
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
          rootId: openRootId, maxHeight: h, preserve: true
        }));
      } else {
        sessionStorage.removeItem(STORAGE_KEY);
      }
      // 링크 본연의 이동은 그대로 진행
    }

    // 이벤트 위임
    menu.addEventListener('click', function (e) {
      const title = e.target.closest('.cat-title');
      if (title && menu.contains(title)) {
        e.preventDefault();
        onTitleClick(title);
        return;
      }
      const link = e.target.closest('.cat-list a');
      if (link && menu.contains(link)) {
        onLinkClick(link);
        // a 클릭은 기본 동작(이동) 유지
        return;
      }
    });

    // 리사이즈 시 열린 섹션 높이 재계산
    window.addEventListener('resize', function () {
      sections.forEach(section => {
        if (section.classList.contains('open')) {
          const list = section.querySelector('.cat-list');
          if (list) list.style.maxHeight = list.scrollHeight + 'px';
        }
      });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
