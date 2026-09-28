document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("login-form");
  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");
  const btnTogglePassword = document.getElementById("btn-toggle-password");
  const usernameError = document.getElementById("username-error");
  const passwordError = document.getElementById("password-error");
  const alertMessage = document.getElementById("alert-message");

  // Icon SVG mắt mở
  const eyeOpenSvg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
      <circle cx="12" cy="12" r="3"></circle>
    </svg>
  `;

  // Icon SVG mắt gạch chéo
  const eyeClosedSvg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
      <line x1="1" y1="1" x2="23" y2="23"></line>
    </svg>
  `;

  // 1. Chức năng ẩn / hiện mật khẩu
  if (btnTogglePassword && passwordInput) {
    btnTogglePassword.addEventListener("click", () => {
      const isPassword = passwordInput.getAttribute("type") === "password";
      passwordInput.setAttribute("type", isPassword ? "text" : "password");
      btnTogglePassword.innerHTML = isPassword ? eyeClosedSvg : eyeOpenSvg;
    });
  }

  // 2. Xóa lỗi khi gõ ký tự
  if (usernameInput) {
    usernameInput.addEventListener("input", () => {
      if (usernameError) usernameError.textContent = "";
      if (alertMessage) alertMessage.classList.remove("alert-error");
    });
  }

  if (passwordInput) {
    passwordInput.addEventListener("input", () => {
      if (passwordError) passwordError.textContent = "";
      if (alertMessage) alertMessage.classList.remove("alert-error");
    });
  }

  // 3. Kiểm tra tính hợp lệ dữ liệu khi gửi form
  if (loginForm) {
    loginForm.addEventListener("submit", (e) => {
      let isValid = true;

      if (usernameError) usernameError.textContent = "";
      if (passwordError) passwordError.textContent = "";
      if (alertMessage) {
        alertMessage.textContent = "";
        alertMessage.classList.remove("alert-error");
      }

      const usernameVal = usernameInput ? usernameInput.value.trim() : "";
      const passwordVal = passwordInput ? passwordInput.value : "";

      if (!usernameVal) {
        if (usernameError) usernameError.textContent = "Vui lòng nhập tên đăng nhập hoặc email.";
        isValid = false;
      }

      if (!passwordVal) {
        if (passwordError) passwordError.textContent = "Vui lòng nhập mật khẩu.";
        isValid = false;
      } else if (passwordVal.length < 6) {
        if (passwordError) passwordError.textContent = "Mật khẩu phải có tối thiểu 6 ký tự.";
        isValid = false;
      }

      if (!isValid) {
        e.preventDefault();
      }
    });
  }
});