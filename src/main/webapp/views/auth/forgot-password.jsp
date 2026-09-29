<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Quên mật khẩu - Quản Lý Khách Hàng</title>

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(
                135deg,
                #0f172a 0%,
                #1e293b 100%
            );
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
            box-shadow:
                0 20px 25px -5px rgba(0, 0, 0, 0.25);
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
            transition: all 0.2s;
        }

        .form-group input:focus {
            border-color: #4f46e5;
            box-shadow:
                0 0 0 3px rgba(79, 70, 229, 0.15);
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
            <h2 class="title">
                Quên Mật Khẩu
            </h2>

            <p class="subtitle">
                Nhập email tài khoản để nhận liên kết
                khôi phục mật khẩu
            </p>
        </div>

        <!-- Thông báo từ Backend -->
        <div id="alert-message"
             class="alert-box
             ${not empty errorMessage ? 'alert-error' : ''}
             ${not empty successMessage ? 'alert-success' : ''}">

            ${not empty errorMessage
                ? errorMessage
                : successMessage}

        </div>

        <!--
            Khi dữ liệu hợp lệ, form sẽ POST tới Backend.
            Không tạo token giả ở Frontend.
        -->
        <form id="forgot-form"
              method="POST"
              action="${pageContext.request.contextPath}/forgot-password"
              novalidate>

            <div class="form-group">

                <label for="email">
                    Email đã đăng ký
                </label>

                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="example@domain.com"
                    autocomplete="email"
                    required
                />

                <span
                    class="field-error"
                    id="email-error">
                </span>

            </div>

            <button
                type="submit"
                class="btn-submit">

                Gửi yêu cầu đặt lại

            </button>

            <div class="auth-footer">

                <a href="${pageContext.request.contextPath}/views/auth/login.jsp">
                    ← Quay lại trang Đăng nhập
                </a>

            </div>

        </form>

    </div>

</div>

<script>
document.addEventListener("DOMContentLoaded", () => {

    const form =
        document.getElementById("forgot-form");

    const emailInput =
document.getElementById("email");

    const emailErr =
        document.getElementById("email-error");

    const alertBox =
        document.getElementById("alert-message");


    if (!form || !emailInput) {
        return;
    }


    // Xóa thông báo lỗi khi người dùng nhập lại email
    emailInput.addEventListener("input", () => {

        if (emailErr) {
            emailErr.textContent = "";
        }

        if (alertBox) {
            alertBox.className = "alert-box";
            alertBox.style.display = "none";
        }

    });


    // Kiểm tra email trước khi gửi Backend
    form.addEventListener("submit", (e) => {

        let valid = true;

        if (emailErr) {
            emailErr.textContent = "";
        }


        const email =
            emailInput.value.trim();

        const regex =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


        // Không nhập email
        if (!email) {

            if (emailErr) {
                emailErr.textContent =
                    "Vui lòng nhập email.";
            }

            valid = false;
        }

        // Email sai định dạng
        else if (!regex.test(email)) {

            if (emailErr) {
                emailErr.textContent =
                    "Định dạng email không hợp lệ.";
            }

            valid = false;
        }


        /*
         * Chỉ chặn submit khi dữ liệu không hợp lệ.
         *
         * Nếu email hợp lệ:
         * KHÔNG preventDefault()
         * -> Browser POST tới Backend /forgot-password.
         */
        if (!valid) {
            e.preventDefault();
        }

    });

});
</script>

</body>
</html>
