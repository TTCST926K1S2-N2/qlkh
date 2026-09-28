<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đặt lại mật khẩu - Quản Lý Khách Hàng</title>
  
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
      max-width: 420px;
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
      margin-bottom: 8px;
    }

    .auth-header .subtitle {
      color: #64748b;
      font-size: 0.9rem;
      line-height: 1.4;
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

    .password-wrapper input {
      width: 100%;
      padding: 11px 44px 11px 14px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    .password-wrapper input:focus {
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
      margin-top: 24px;
      text-align: center;
    }

    .auth-footer a {
      color: #64748b;
      text-decoration: none;
      font-size: 0.88rem;
      font-weight: 500;
      transition: color 0.2s;
    }

    .auth-footer a:hover {
      color: #4f46e5;
      text-decoration: underline;
    }
  </style>
</head>
<body>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2 class="title">Đặt Lại Mật Khẩu</h2>
        <p class="subtitle">Vui lòng thiết lập mật khẩu mới cho tài khoản của bạn</p>
      </div>

      <div id="alert-message" class="alert-box ${not empty errorMessage ? 'alert-error' : ''}">
        ${errorMessage}
      </div>

      <form id="reset-form" method="POST" action="reset-password" novalidate>
        <input type="hidden" name="token" value="${param.token}" />

        <div class="form-group">
          <label for="new-password">Mật khẩu mới</label>
          <div class="password-wrapper">
            <input 
              type="password" 
              id="new-password" 
              name="newPassword" 
              placeholder="Nhập tối thiểu 6 ký tự" 
              autocomplete="new-password" 
              required 
            />
            <button type="button" class="btn-toggle-eye" data-target="new-password" aria-label="Ẩn hiện mật khẩu">
              <svg class="eye-open" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span class="field-error" id="new-password-error"></span>
        </div>

        <div class="form-group">
          <label for="confirm-password">Xác nhận mật khẩu mới</label>
          <div class="password-wrapper">
            <input 
              type="password" 
              id="confirm-password" 
              name="confirmPassword" 
              placeholder="Nhập lại mật khẩu mới" 
              autocomplete="new-password" 
              required 
            />
            <button type="button" class="btn-toggle-eye" data-target="confirm-password" aria-label="Ẩn hiện mật khẩu">
              <svg class="eye-open" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <span class="field-error" id="confirm-password-error"></span>
        </div>

        <button type="submit" class="btn-submit">Cập Nhật Mật Khẩu</button>

        <div class="auth-footer">
          <a href="login.jsp">← Quay lại Đăng nhập</a>
        </div>
      </form>
    </div>
  </div>

  <script>
    document.addEventListener("DOMContentLoaded", () => {
      const eyeOpenSvg = `
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
          <circle cx="12" cy="12" r="3"></circle>
        </svg>
      `;

      const eyeClosedSvg = `
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
          <line x1="1" y1="1" x2="23" y2="23"></line>
        </svg>
      `;

      // 1. Toggle eye cho cả 2 trường mật khẩu
      document.querySelectorAll(".btn-toggle-eye").forEach((btn) => {
        btn.addEventListener("click", () => {
          const targetId = btn.getAttribute("data-target");
          const input = document.getElementById(targetId);
          if (input) {
            const isPassword = input.getAttribute("type") === "password";
            input.setAttribute("type", isPassword ? "text" : "password");
            btn.innerHTML = isPassword ? eyeClosedSvg : eyeOpenSvg;
          }
        });
      });

      // 2. Validate form và xử lý frontend
      const form = document.getElementById("reset-form");
      const p1Input = document.getElementById("new-password");
      const p2Input = document.getElementById("confirm-password");
      const err1 = document.getElementById("new-password-error");
      const err2 = document.getElementById("confirm-password-error");
      const alertBox = document.getElementById("alert-message");

      [p1Input, p2Input].forEach((inp) => {
        if (inp) {
          inp.addEventListener("input", () => {
            err1.textContent = "";
            err2.textContent = "";
            alertBox.className = "alert-box";
            alertBox.style.display = "none";
          });
        }
      });

      form.addEventListener("submit", (e) => {
        e.preventDefault(); // Ngăn submit server thật để không bị 404 khi demo FE

        let valid = true;
        err1.textContent = "";
        err2.textContent = "";

        const p1 = p1Input ? p1Input.value : "";
        const p2 = p2Input ? p2Input.value : "";

        if (!p1) {
          err1.textContent = "Vui lòng nhập mật khẩu mới.";
          valid = false;
        } else if (p1.length < 6) {
          err1.textContent = "Mật khẩu phải chứa ít nhất 6 ký tự.";
          valid = false;
        }

        if (!p2) {
          err2.textContent = "Vui lòng xác nhận mật khẩu.";
          valid = false;
        } else if (p1 && p1 !== p2) {
          err2.textContent = "Mật khẩu xác nhận không khớp.";
          valid = false;
        }

        if (valid) {
          alertBox.className = "alert-box alert-success";
          alertBox.innerHTML = `Mật khẩu đã được cập nhật thành công! <a href="login.jsp" style="color: #15803d; font-weight: 700; text-decoration: underline;">Đăng nhập ngay</a>.`;
          alertBox.style.display = "block";
          p1Input.value = "";
          p2Input.value = "";
        }
      });
    });
  </script>
</body>
</html>