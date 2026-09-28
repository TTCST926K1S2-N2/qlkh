<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Đặt lại mật khẩu - Quản Lý Khách Hàng</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/password.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css" />
</head>
<body>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="title">Đặt Lại Mật Khẩu</h2>
        <p class="subtitle">Nhập mã xác minh (OTP) và mật khẩu mới của bạn</p>
      </div>

      <div id="alert-box" class="alert-box ${not empty errorMessage ? 'alert-error' : ''}">
        ${errorMessage}
      </div>

      <form id="reset-form" action="${pageContext.request.contextPath}/reset-password" method="POST" novalidate>
        <div class="form-group">
          <label for="otp">Mã xác thực (OTP)</label>
          <input type="text" id="otp" name="otp" placeholder="Nhập mã xác thực" required />
          <span class="field-error" id="otp-error"></span>
        </div>

        <div class="form-group">
          <label for="newPassword">Mật khẩu mới</label>
          <div class="password-wrapper">
            <input type="password" id="newPassword" name="newPassword" placeholder="Tối thiểu 6 ký tự" required />
            <button type="button" id="btn-toggle-new-pass" class="btn-toggle-eye">👁️</button>
          </div>
          <span class="field-error" id="new-password-error"></span>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Xác nhận mật khẩu mới</label>
          <div class="password-wrapper">
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu mới" required />
            <button type="button" id="btn-toggle-confirm-pass" class="btn-toggle-eye">👁️</button>
          </div>
          <span class="field-error" id="confirm-password-error"></span>
        </div>

        <button type="submit" id="btn-submit" class="btn-login">Cập Nhật Mật Khẩu</button>
      </form>

      <div class="form-options" style="justify-content: center; margin-top: 20px;">
        <a href="${pageContext.request.contextPath}/views/auth/login.jsp" class="forgot-link">← Quay lại Đăng nhập</a>
      </div>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/assets/js/reset-password.js"></script>
</body>
</html>