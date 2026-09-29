<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đặt Lại Mật Khẩu - Quản Lý Khách Hàng</title>

  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .auth-container {
      width: 100%;
      max-width: 440px;
    }

    .auth-card {
      background: #ffffff;
      padding: 36px 32px;
      border-radius: 16px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.25);
    }

    .auth-header {
      text-align: center;
      margin-bottom: 24px;
    }

    .auth-header .title {
      font-size: 1.6rem;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 6px;
    }

    .auth-header .subtitle {
      color: #64748b;
      font-size: 0.88rem;
      line-height: 1.4;
    }

    .alert-box {
      display: none;
      padding: 11px 14px;
      border-radius: 8px;
      font-size: 0.88rem;
      margin-bottom: 18px;
      line-height: 1.4;
    }

    .alert-box.show {
      display: block;
    }

    .alert-error {
      background-color: #fee2e2;
      color: #991b1b;
      border: 1px solid #fecaca;
    }

    .alert-success {
      background-color: #dcfce7;
      color: #166534;
      border: 1px solid #bbf7d0;
    }

    .form-group {
      margin-bottom: 18px;
    }

    .form-group label {
      display: block;
      font-size: 0.88rem;
      font-weight: 600;
      color: #334155;
      margin-bottom: 6px;
    }

    .password-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }

    .form-group input {
      width: 100%;
      padding: 11px 14px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    .password-wrapper input {
      padding-right: 44px;
    }

    .form-group input:focus {
      border-color: #4f46e5;
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
    }

    .btn-toggle-eye {
      position: absolute;
      right: 10px;
      background: transparent;
      border: none;
      cursor: pointer;
      padding: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #64748b;
      transition: color 0.2s;
    }

    .btn-toggle-eye:hover {
      color: #0f172a;
    }

    .field-error {
      display: block;
      color: #dc2626;
      font-size: 0.78rem;
      margin-top: 4px;
      min-height: 16px;
    }

    .btn-submit {
      width: 100%;
      padding: 12px;
      background-color: #4f46e5;
      color: #ffffff;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: background-color 0.2s;
      margin-top: 6px;
    }

    .btn-submit:hover {
      background-color: #4338ca;
    }

    .auth-footer {
      text-align: center;
      margin-top: 20px;
      font-size: 0.88rem;
    }

    .auth-footer a {
      color: #4f46e5;
      text-decoration: none;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      transition: color 0.2s;
    }

    .auth-footer a:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2 class="title">Đặt Lại Mật Khẩu</h2>
        <p class="subtitle">Vui lòng nhập mật khẩu mới cho tài khoản của bạn</p>
      </div>

      <!-- Hiển thị thông báo sạch bằng thẻ JSTL / EL chuẩn, không lồng style phức tạp -->
      <c:if test="${not empty errorMessage}">
        <div class="alert-box alert-error show">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty successMessage}">
        <div class="alert-box alert-success show">${successMessage}</div>
      </c:if>
      <div id="alert-message" class="alert-box"></div>

      <!-- Form submit -->
      <form id="reset-password-form" method="POST" action="${pageContext.request.contextPath}/reset-password" novalidate>
        <input type="hidden" name="token" id="token" value="${param.token}" />

        <div class="form-group">
          <label for="newPassword">Mật khẩu mới</label>
          <div class="password-wrapper">
            <input 
              type="password" 
              id="newPassword" 
              name="newPassword" 
              placeholder="Nhập mật khẩu mới..." 
              autocomplete="new-password" 
              required 
            />
            <button type="button" class="btn-toggle-eye" data-target="newPassword" aria-label="Ẩn/hiện mật khẩu">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span class="field-error" id="newPassword-error"></span>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Xác nhận mật khẩu mới</label>
          <div class="password-wrapper">
            <input 
              type="password" 
              id="confirmPassword" 
              name="confirmPassword" 
              placeholder="Nhập lại mật khẩu mới..." 
              autocomplete="new-password" 
              required 
            />
            <button type="button" class="btn-toggle-eye" data-target="confirmPassword" aria-label="Ẩn/hiện mật khẩu">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span class="field-error" id="confirmPassword-error"></span>
        </div>

        <button type="submit" class="btn-submit" id="btn-submit">Xác Nhận Đổi Mật Khẩu</button>
      </form>

      <!-- Đường dẫn quay lại Login chuẩn tránh lỗi 404 -->
      <div class="auth-footer">
        <a href="${pageContext.request.contextPath}/views/auth/login.jsp" id="link-back-login">
          &larr; Quay lại trang Đăng nhập
        </a>
      </div>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/assets/js/reset-password.js"></script>
</body>
</html>