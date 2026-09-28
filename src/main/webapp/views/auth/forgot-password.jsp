<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Quên mật khẩu - Quản Lý Khách Hàng</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/password.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css" />
</head>
<body>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="title">Quên Mật Khẩu</h2>
        <p class="subtitle">Nhập email tài khoản để nhận mã đặt lại mật khẩu</p>
      </div>

      <div id="alert-box" class="alert-box ${not empty errorMessage ? 'alert-error' : ''}">
        ${errorMessage}
      </div>

      <form id="forgot-form" action="${pageContext.request.contextPath}/forgot-password" method="POST" novalidate>
        <div class="form-group">
          <label for="email">Địa chỉ Email</label>
          <input type="email" id="email" name="email" placeholder="example@company.com" required />
          <span class="field-error" id="email-error"></span>
        </div>

        <button type="submit" id="btn-submit" class="btn-login">Gửi mã xác thực</button>
      </form>

      <div class="form-options" style="justify-content: center; margin-top: 20px;">
        <a href="${pageContext.request.contextPath}/views/auth/login.jsp" class="forgot-link">← Quay lại Đăng nhập</a>
      </div>
    </div>
  </div>
</body>
</html>