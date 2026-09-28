<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div id="session-modal" class="session-modal-overlay">
  <div class="session-modal-box">
    <h3>⚠️ Cảnh báo phiên làm việc</h3>
    <p>
      Phiên đăng nhập của bạn sắp hết hạn sau 
      <span id="session-seconds" class="session-countdown">60</span> giây do không có thao tác.
    </p>
    <div class="session-actions">
      <button type="button" id="btn-extend-session" class="btn-extend-session">Duy trì đăng nhập</button>
      <button type="button" id="btn-confirm-logout" class="btn-logout-confirm">Đăng xuất ngay</button>
    </div>
  </div>
</div>