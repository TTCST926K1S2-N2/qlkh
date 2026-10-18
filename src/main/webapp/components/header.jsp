<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/shared-header.css">
<div class="qlkh-profile-header qlkh-shared-header">
  <form class="qlkh-header-search" id="qlkhHeaderSearch" role="search">
    <span aria-hidden="true">⌕</span>
    <input id="qlkhHeaderSearchInput" type="search" placeholder="Tìm khách hàng, liên hệ..." aria-label="Tìm kiếm khách hàng" autocomplete="off">
    <button type="submit" class="qlkh-search-submit">Tìm</button>
  </form>
  <div class="qlkh-header-right">
    <div class="qlkh-header-popover-wrap">
      <button type="button" class="qlkh-header-icon-button" id="qlkhNotificationButton" aria-label="Thông báo" aria-expanded="false">♧</button>
      <div class="qlkh-header-dropdown" id="qlkhNotificationPanel" hidden><strong>Thông báo</strong><p>Chưa tích hợp dữ liệu thông báo trên trang này.</p></div>
    </div>
    <div class="qlkh-header-popover-wrap">
      <button type="button" class="qlkh-header-user" id="qlkhUserButton" aria-expanded="false">
        <span class="qlkh-header-avatar" id="qlkhHeaderAvatar"><span class="qlkh-avatar-initial">${not empty sessionScope.userName ? sessionScope.userName.substring(0,1) : 'U'}</span></span>
        <span class="qlkh-header-user-text"><strong id="qlkhHeaderUserName">${sessionScope.userName}</strong><small id="qlkhHeaderUserRole">Tài khoản</small></span>
        <span aria-hidden="true">⌄</span>
      </button>
      <div class="qlkh-header-dropdown" id="qlkhUserPanel" hidden>
        <a href="${pageContext.request.contextPath}/user/profile.jsp">Hồ sơ cá nhân</a>
        <a href="${pageContext.request.contextPath}/change-password">Đổi mật khẩu</a>
        <button type="button" id="qlkhHeaderLogout">Đăng xuất</button>
      </div>
    </div>
  </div>
</div>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/notification-ui.css">
<script src="${pageContext.request.contextPath}/assets/js/shared-header.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/notification-ui.js" defer></script>
