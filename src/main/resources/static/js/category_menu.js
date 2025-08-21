// src/main/resources/static/js/category_menu.js
(function () {
  'use strict';

  // 전역 가드: 파일이 중복 로드되거나 다시 실행돼도 한 번만 초기화
  if (window.__CAT_MENU_INIT__) {
    // console.debug('[cat-menu] already initialized');
    return;
  }
  window.__CAT_MENU_INIT__ = true;

  function init() {
    const menu = document.querySelector('.categories-menu');
    if (!menu) return;

    // 메뉴마다 개별 가드(동일 DOM에 또 초기화 방지)
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

    // 초기 펼침 (data-open="true")
    sections.forEach(section => {
      const initiallyOpen = section.getAttribute('data-open') === 'true';
      setOpen(section, initiallyOpen);
    });

    // 이벤트 위임: 타이틀 클릭 → 토글
    function onClick(e) {
      const title = e.target.closest('.cat-title');
      if (!title || !menu.contains(title)) return;

      e.preventDefault(); // a/button 혼용 대비

      const section = title.closest('.cat-section');
      if (!section) return;

      const willOpen = !section.classList.contains('open');

      if (ONE_OPEN && willOpen) {
        sections.forEach(s => { if (s !== section) setOpen(s, false); });
      }
      setOpen(section, willOpen);
    }

    function onResize() {
      sections.forEach(section => {
        if (section.classList.contains('open')) {
          const list = section.querySelector('.cat-list');
          if (list) list.style.maxHeight = list.scrollHeight + 'px';
        }
      });
    }

    menu.addEventListener('click', onClick);
    window.addEventListener('resize', onResize);

    // console.debug('[cat-menu] initialized');
  }

  // DOM 준비 후 1회만 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
