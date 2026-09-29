<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đăng Nhập - Hệ Thống Quản Lý Khách Hàng</title>

  <!-- Google Font Inter -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="../../assets/css/login.css?v=1.1">

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
      padding: 40px 32px;
      border-radius: 16px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.25), 0 8px 10px -6px rgba(0, 0, 0, 0.2);
    }
    .auth-header {
      text-align: center;
      margin-bottom: 28px;
    }
    .auth-header .title {
      font-size: 1.65rem;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 6px;
    }
    .auth-header .subtitle {
      color: #64748b;
      font-size: 0.9rem;
      line-height: 1.4;
    }
    .alert-box {
      display: none;
      padding: 12px 14px;
      border-radius: 8px;
      font-size: 0.88rem;
      margin-bottom: 20px;
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
    .form-group {
      margin-bottom: 20px;
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
    .form-group input[type="text"],
    .form-group input[type="password"] {
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
      margin-top: 5px;
      min-height: 16px;
    }
    .form-actions-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 22px;
      font-size: 0.88rem;
    }
    .remember-me {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      color: #475569;
      cursor: pointer;
      user-select: none;
    }
    .remember-me input[type="checkbox"] {
      width: 16px;
      height: 16px;
      accent-color: #4f46e5;
      cursor: pointer;
    }
    .forgot-link {
      color: #4f46e5;
      text-decoration: none;
      font-weight: 500;
      transition: color 0.2s;
    }
    .forgot-link:hover {
      text-decoration: underline;
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
    }
    .btn-submit:hover {
      background-color: #4338ca;
    }
  </style>
</head>
<body>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2 class="title">Đăng Nhập</h2>
        <p class="subtitle">Hệ thống quản lý quan hệ khách hàng</p>
      </div>

      <c:if test="${not empty errorMessage}">
        <div class="alert-box alert-error show">${errorMessage}</div>
      </c:if>
      <div id="client-alert" class="alert-box alert-error"></div>

      <form id="login-form" method="POST" action="${pageContext.request.contextPath}/login" autocomplete="off" novalidate>
        <div class="form-group">
          <label for="username">Tên đăng nhập hoặc Email</label>
          <input 
            type="text" 
            id="username" 
            name="username" 
            placeholder="Nhập tên đăng nhập hoặc email" 
            autocomplete="off" 
            value="${username}" 
            required 
          />
          <span class="field-error" id="username-error"></span>
        </div>

       <div class="form-group">
          <label for="password">Mật khẩu</label>
          <div class="password-wrapper">
            <input 
              type="text" 
              id="password" 
              name="password" 
              placeholder="Nhập mật khẩu" 
              autocomplete="new-password"
              data-masked="true"
              style="-webkit-text-security: disc;"
              value="" 
              required 
            />
            <button type="button" class="btn-toggle-eye" id="toggle-password" aria-label="Ẩn/hiện mật khẩu">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span class="field-error" id="password-error"></span>
        </div>

        <div class="form-actions-row">
          <label class="remember-me">
            <input type="checkbox" name="rememberMe" id="rememberMe" value="true">
            <span>Ghi nhớ đăng nhập</span>
          </label>
          <a href="${pageContext.request.contextPath}/views/auth/forgot-password.jsp" class="forgot-link">Quên mật khẩu?</a>
        </div>

        <button type="submit" class="btn-submit" id="btn-submit">Đăng Nhập</button>
      </form>
    </div>
  </div>

  <script src="../../assets/js/login.js?v=1.1"></script>
</body>
</html>