<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!-- Modal cảnh báo hết hạn phiên làm việc (HTQLKH-2) -->
<div id="session-modal" class="session-modal-overlay">
  <div class="session-modal-box">
    <div class="session-modal-icon">
      <svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#ea580c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
    </div>
    <h3 class="session-modal-title">Cảnh Báo Hết Hạn Phiên</h3>
    <p class="session-modal-desc">
      Phiên đăng nhập của bạn sắp hết hạn sau 
      <span id="session-seconds" class="session-countdown">60</span> giây do không có thao tác.
    </p>
    <div class="session-actions">
      <button type="button" id="btn-extend-session" class="btn-extend-session">Duy trì đăng nhập</button>
      <button type="button" id="btn-confirm-logout" class="btn-logout-confirm">Đăng xuất ngay</button>
    </div>
  </div>
</div>