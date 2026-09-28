<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Đăng nhập - Quản Lý Khách Hàng</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css" />
</head>
<body>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="title">Đăng Nhập</h2>
        <p class="subtitle">Hệ Thống Quản Lý Khách Hàng (CRM)</p>
      </div>

      <!-- Hiển thị thông báo lỗi từ server nếu có -->
      <div id="alert-message" class="alert-box ${not empty errorMessage ? 'alert-error' : ''}">
        ${errorMessage}
      </div>

      <form id="login-form" action="${pageContext.request.contextPath}/login" method="POST" novalidate>
        <div class="form-group">
          <label for="username">Tên đăng nhập hoặc Email</label>
          <input 
            type="text" 
            id="username" 
            name="username" 
            value="${param.username}"
            placeholder="Nhập tên đăng nhập hoặc email" 
            autocomplete="username"
            required 
          />
          <span class="field-error" id="username-error"></span>
        </div>

        <div class="form-group">
          <label for="password">Mật khẩu</label>
          <div class="password-wrapper">
            <input 
              type="password" 
              id="password" 
              name="password" 
              placeholder="Nhập mật khẩu" 
              autocomplete="current-password"
              required 
            />
            <button type="button" id="btn-toggle-password" class="btn-toggle-eye" aria-label="Hiện mật khẩu">👁️</button>
          </div>
          <span class="field-error" id="password-error"></span>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" id="remember-me" name="remember" />
            <span>Ghi nhớ đăng nhập</span>
          </label>
          <a href="${pageContext.request.contextPath}/views/reset-password.jsp" class="forgot-link">Quên mật khẩu?</a>
        </div>

        <button type="submit" id="btn-submit" class="btn-login">Đăng Nhập</button>
      </form>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/assets/js/login.js"></script>
</body>
</html>