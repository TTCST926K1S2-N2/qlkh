<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi Mật Khẩu - QLKH</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        body { background-color: #f4f6f9; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .card { background: #ffffff; width: 100%; max-width: 440px; padding: 32px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
        .card h2 { margin-bottom: 8px; color: #1a202c; font-size: 22px; text-align: center; }
        .card p.subtitle { color: #718096; font-size: 14px; text-align: center; margin-bottom: 24px; }
        .alert { padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 16px; }
        .alert-danger { background-color: #fed7d7; color: #9b2c2c; border: 1px solid #feb2b2; }
        .alert-success { background-color: #c6f6d5; color: #22543d; border: 1px solid #9ae6b4; }
        .form-group { margin-bottom: 18px; }
        .form-group label { display: block; font-weight: 600; font-size: 14px; color: #2d3748; margin-bottom: 6px; }
        .input-wrapper { position: relative; display: flex; align-items: center; }
        .input-wrapper input { width: 100%; padding: 10px 40px 10px 12px; border: 1px solid #cbd5e0; border-radius: 6px; font-size: 14px; outline: none; }
        .input-wrapper input:focus { border-color: #3182ce; box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.15); }
        .toggle-btn { position: absolute; right: 12px; background: none; border: none; cursor: pointer; color: #718096; font-size: 14px; }
        .error-msg { color: #e53e3e; font-size: 12px; margin-top: 4px; display: block; }
        .checklist { list-style: none; margin-top: 8px; padding-left: 4px; }
        .checklist li { font-size: 12px; color: #a0aec0; margin-bottom: 4px; display: flex; align-items: center; gap: 6px; }
        .checklist li.valid { color: #38a169; }
        .checklist li.valid::before { content: '✓'; font-weight: bold; }
        .checklist li.invalid::before { content: '○'; }
        .form-checkbox { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; cursor: pointer; }
        .form-checkbox input { width: 16px; height: 16px; cursor: pointer; }
        .form-checkbox label { font-size: 13px; color: #4a5568; cursor: pointer; }
        .btn-submit { width: 100%; padding: 12px; background-color: #3182ce; color: #ffffff; border: none; border-radius: 6px; font-size: 15px; font-weight: 600; cursor: pointer; }
        .btn-submit:hover { background-color: #2b6cb0; }
        .btn-submit:disabled { background-color: #a0aec0; cursor: not-allowed; }
    </style>
</head>
<body>

<div class="card">
    <h2>Đổi Mật Khẩu</h2>
    <p class="subtitle">Bảo vệ tài khoản và danh mục khách hàng</p>

    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger"><%= request.getAttribute("errorMessage") %></div>
    <% } %>
    <% if (request.getAttribute("successMessage") != null) { %>
        <div class="alert alert-success"><%= request.getAttribute("successMessage") %></div>
    <% } %>

    <form id="changePasswordForm" action="${pageContext.request.contextPath}/change-password" method="POST" novalidate>
        <div class="form-group">
            <label for="currentPassword">Mật khẩu hiện tại *</label>
            <div class="input-wrapper">
                <input type="password" id="currentPassword" name="currentPassword" required />
                <button type="button" class="toggle-btn" onclick="togglePassword('currentPassword', this)">👁️</button>
            </div>
            <span class="error-msg" id="currentPasswordError"></span>
        </div>

        <div class="form-group">
            <label for="newPassword">Mật khẩu mới *</label>
            <div class="input-wrapper">
                <input type="password" id="newPassword" name="newPassword" required />
                <button type="button" class="toggle-btn" onclick="togglePassword('newPassword', this)">👁️</button>
            </div>
            <ul class="checklist">
                <li id="rule-length" class="invalid">Tối thiểu 8 ký tự</li>
                <li id="rule-letter" class="invalid">Có ít nhất 1 chữ cái (a-z, A-Z)</li>
                <li id="rule-number" class="invalid">Có ít nhất 1 chữ số (0-9)</li>
            </ul>
            <span class="error-msg" id="newPasswordError"></span>
        </div>

        <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu mới *</label>
            <div class="input-wrapper">
                <input type="password" id="confirmPassword" name="confirmPassword" required />
                <button type="button" class="toggle-btn" onclick="togglePassword('confirmPassword', this)">👁️</button>
            </div>
            <span class="error-msg" id="confirmPasswordError"></span>
        </div>

        <div class="form-checkbox">
            <input type="checkbox" id="revokeOtherSessions" name="revokeOtherSessions" value="true" checked />
            <label for="revokeOtherSessions">Đăng xuất và thu hồi các phiên đăng nhập khác</label>
        </div>

        <button type="submit" id="submitBtn" class="btn-submit">Cập Nhật Mật Khẩu</button>
    </form>
</div>

<script>
    const newPass = document.getElementById('newPassword');
    const confirmPass = document.getElementById('confirmPassword');
    const ruleLength = document.getElementById('rule-length');
    const ruleLetter = document.getElementById('rule-letter');
    const ruleNumber = document.getElementById('rule-number');

    newPass.addEventListener('input', () => {
        const val = newPass.value;
        updateRule(ruleLength, val.length >= 8);
        updateRule(ruleLetter, /[a-zA-Z]/.test(val));
        updateRule(ruleNumber, /[0-9]/.test(val));
        validateMatch();
    });

    confirmPass.addEventListener('input', validateMatch);

    function updateRule(el, isValid) {
        el.className = isValid ? 'valid' : 'invalid';
    }

    function validateMatch() {
        const errEl = document.getElementById('confirmPasswordError');
        if (confirmPass.value && confirmPass.value !== newPass.value) {
            errEl.textContent = 'Mật khẩu xác nhận không trùng khớp';
            return false;
        } else {
            errEl.textContent = '';
            return true;
        }
    }

    function togglePassword(inputId, btn) {
        const input = document.getElementById(inputId);
        if (input.type === 'password') {
            input.type = 'text';
            btn.textContent = '🔒';
        } else {
            input.type = 'password';
            btn.textContent = '👁️';
        }
    }
</script>
</body>
</html>