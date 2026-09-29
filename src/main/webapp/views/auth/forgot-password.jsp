<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Quên mật khẩu - Quản Lý Khách Hàng</title>
  
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

    .login-container {
      width: 100%;
      max-width: 420px;
    }

    .login-card {
      background: #ffffff;
      padding: 36px 32px;
      border-radius: 16px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.25);
    }

    .login-header {
      text-align: center;
      margin-bottom: 24px;
    }

    .login-header .title {
      font-size: 1.6rem;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 8px;
    }

    .login-header .subtitle {
      color: #64748b;
      font-size: 0.9rem;
      line-height: 1.5;
    }

    .alert-box {
      display: none;
      padding: 10px 14px;
      border-radius: 8px;
      font-size: 0.88rem;
      margin-bottom: 18px;
    }

    .alert-error {
      display: block !important;
      background-color: #fee2e2;
      color: #991b1b;
      border: 1px solid #fecaca;
    }

    .alert-success {
      display: block !important;
      background-color: #dcfce7;
      color: #166534;
      border: 1px solid #bbf7d0;
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

    .form-group input {
      width: 100%;
      padding: 11px 14px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    .form-group input:focus {
      border-color: #4f46e5;
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
    }

    .field-error {
      display: block;
      color: #dc2626;
      font-size: 0.78rem;
      margin-top: 4px;
      min-height: 16px;
    }

    .btn-login {
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
      margin-top: 4px;
    }

    .btn-login:hover {
      background-color: #4338ca;
    }

    .back-to-login {
      margin-top: 24px;
      text-align: center;
    }

    .back-to-login a {
      color: #64748b;
      text-decoration: none;
      font-size: 0.88rem;
      font-weight: 500;
      transition: color 0.2s;
    }

    .back-to-login a:hover {
      color: #4f46e5;
      text-decoration: underline;
    }
  </style>
</head>
<body>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="title">Quên Mật Khẩu</h2>
        <p class="subtitle">Nhập email đã đăng ký để nhận liên kết đặt lại mật khẩu</p>
      </div>

      <div id="alert-message" class="alert-box ${not empty errorMessage ? 'alert-error' : ''} ${not empty successMessage ? 'alert-success' : ''}">
        ${not empty errorMessage ? errorMessage : successMessage}
      </div>

      <form id="forgot-form" method="POST" action="${pageContext.request.contextPath}/forgot-password" novalidate>
        <div class="form-group">
          <label for="email">Email tài khoản</label>
          <input 
            type="email" 
            id="email" 
            name="email" 
            placeholder="example@domain.com" 
            autocomplete="email"
            required 
          />
          <span class="field-error" id="email-error"></span>
        </div>

        <button type="submit" id="btn-forgot-submit" class="btn-login">Gửi yêu cầu</button>

        <div class="back-to-login">
          <a href="${pageContext.request.contextPath}/views/auth/login.jsp">← Quay lại Đăng nhập</a>
        </div>
      </form>
    </div>
  </div>

 <script>
    document.addEventListener("DOMContentLoaded", () => {
      const form = document.getElementById("forgot-form");
      const emailInput = document.getElementById("email");
      const emailErr = document.getElementById("email-error");
      const alertBox = document.getElementById("alert-message");

      emailInput.addEventListener("input", () => {
        emailErr.textContent = "";
        alertBox.className = "alert-box";
        alertBox.style.display = "none";
      });

      form.addEventListener("submit", (e) => {
        e.preventDefault(); // Ngăn submit thật để không bị lỗi 404 Backend

        let valid = true;
        emailErr.textContent = "";
        const email = emailInput.value.trim();
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email) {
          emailErr.textContent = "Vui lòng nhập email.";
          valid = false;
        } else if (!regex.test(email)) {
          emailErr.textContent = "Định dạng email không hợp lệ.";
          valid = false;
        }

        if (valid) {
          // Hiển thị thông báo thành công đẹp mắt ngay trên giao diện
          alertBox.className = "alert-box alert-success";
          alertBox.innerHTML = `Yêu cầu đã được gửi tới <b>${email}</b>! Kiểm tra email hoặc bấm <a href="reset-password.jsp?token=demo123" style="color: #15803d; font-weight: 700; text-decoration: underline;">vào đây</a> để đặt lại mật khẩu.`;
          alertBox.style.display = "block";
        }
      });
    });
  </script>
</body>
</html>