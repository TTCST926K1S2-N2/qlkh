(function () {
  'use strict';
  const script = document.currentScript;
  const context = script ? new URL(script.src).pathname.replace(/\/assets\/js\/notification-ui\.js$/, '') : '';
  const api = context + '/api/notifications';
  const button = document.getElementById('qlkhNotificationButton');
  const panel = document.getElementById('qlkhNotificationPanel');
  if (!button || !panel) return;

  const badge = document.createElement('span');
  badge.className = 'qlkh-notification-badge';
  badge.hidden = true;
  button.appendChild(badge);
  panel.classList.add('qlkh-notification-panel');

  const heading = document.createElement('div');
  heading.className = 'qlkh-notification-heading';
  const title = document.createElement('strong');
  title.textContent = 'Thông báo';
  const markAll = document.createElement('button');
  markAll.type = 'button';
  markAll.textContent = 'Đọc tất cả';
  markAll.className = 'qlkh-notification-read-all';
  heading.append(title, markAll);
  const list = document.createElement('div');
  list.className = 'qlkh-notification-list';
  panel.replaceChildren(heading, list);

  let items = [];
  let loading = false;
  function count(value) {
    const n = Number(value) || 0;
    badge.hidden = n <= 0;
    badge.textContent = n > 99 ? '99+' : String(n);
    button.setAttribute('aria-label', n ? `Thông báo, ${n} chưa đọc` : 'Thông báo');
    markAll.hidden = n === 0;
  }
  async function jsonResponse(url, options) {
    const response = await fetch(url, Object.assign({credentials: 'same-origin', cache: 'no-store', headers: {Accept: 'application/json'}}, options || {}));
    if (!response.ok) throw new Error('HTTP ' + response.status);
    const data = await response.json();
    if (!data || data.success !== true) throw new Error('API response unsuccessful');
    return data;
  }
  function showMessage(message) {
    const p = document.createElement('p');
    p.className = 'qlkh-notification-empty';
    p.textContent = message;
    list.replaceChildren(p);
  }
  function safeLink(target) {
    if (typeof target !== 'string' || !target.startsWith('/') || target.startsWith('//') || target.includes('\\')) return null;
    try {
      const parsed = new URL(target, location.origin);
      if (parsed.origin !== location.origin || !(parsed.pathname === context || parsed.pathname.startsWith(context + '/'))) return null;
      return parsed.href;
    } catch (_) { return null; }
  }
  function render() {
    list.replaceChildren();
    if (!items.length) { showMessage('Chưa có thông báo.'); return; }
    for (const item of items) {
      const row = document.createElement('div');
      row.className = 'qlkh-notification-item' + (item.read ? '' : ' is-unread');
      const name = document.createElement('strong');
      name.textContent = item.title || 'Thông báo';
      const content = document.createElement('p');
      content.textContent = item.content || '';
      row.append(name, content);
      if (item.createdAt) {
        const time = document.createElement('small');
        const date = new Date(item.createdAt);
        time.textContent = Number.isNaN(date.getTime()) ? String(item.createdAt) : date.toLocaleString('vi-VN');
        row.appendChild(time);
      }
      const actions = document.createElement('div');
      actions.className = 'qlkh-notification-actions';
      if (!item.read) {
        const read = document.createElement('button');
        read.type = 'button';
        read.textContent = 'Đánh dấu đã đọc';
        read.addEventListener('click', async () => {
          read.disabled = true;
          try {
            await jsonResponse(api + '/read', {method: 'POST', headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8', Accept: 'application/json'}, body: new URLSearchParams({id: String(item.id)}).toString()});
            await load();
          } catch (_) { read.disabled = false; showMessage('Không thể cập nhật thông báo. Vui lòng thử lại.'); }
        });
        actions.appendChild(read);
      }
      const link = safeLink(item.targetUrl);
      if (link) {
        const anchor = document.createElement('a');
        anchor.href = link;
        anchor.textContent = 'Xem chi tiết';
        actions.appendChild(anchor);
      }
      row.appendChild(actions);
      list.appendChild(row);
    }
  }
  async function load() {
    if (loading) return;
    loading = true;
    try {
      const result = await jsonResponse(api);
      items = Array.isArray(result.data) ? result.data : [];
      count(result.unreadCount);
      render();
    } catch (error) {
      if (!panel.hidden) showMessage('Không tải được thông báo. Vui lòng thử lại.');
      console.warn('Notification load:', error);
    } finally { loading = false; }
  }
  markAll.addEventListener('click', async () => {
    markAll.disabled = true;
    try {
      await jsonResponse(api + '/read-all', {method: 'POST'});
      await load();
    } catch (_) { showMessage('Không thể đánh dấu tất cả đã đọc.'); }
    finally { markAll.disabled = false; }
  });
  button.addEventListener('click', () => { if (!panel.hidden) load(); });
  load();
  window.setInterval(() => { if (!document.hidden) load(); }, 15000);
})();
