/**
 * Logic kiểm tra & ẩn hiện mật khẩu - Login (HTQLKH-1)
 */
document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("login-form");
  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");
  const usernameError = document.getElementById("username-error");
  const passwordError = document.getElementById("password-error");
  const clientAlert = document.getElementById("client-alert");
  const toggleEyeBtn = document.getElementById("toggle-password");

  if (passwordInput) {
    passwordInput.value = "";
    setTimeout(() => {
      passwordInput.value = "";
    }, 100);

    passwordInput.addEventListener("focus", () => {
      passwordInput.setAttribute("type", "password");
      passwordInput.style.webkitTextSecurity = "";
    }, { once: true });
  }

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

  // 1. Chức năng bật/tắt hiển thị mật khẩu
  if (toggleEyeBtn && passwordInput) {
    toggleEyeBtn.addEventListener("click", () => {
      const isCurrentlyMasked = 
        passwordInput.getAttribute("type") === "password" || 
        passwordInput.style.webkitTextSecurity === "disc";

      if (isCurrentlyMasked) {
        passwordInput.setAttribute("type", "text");
        passwordInput.style.webkitTextSecurity = "none";
        toggleEyeBtn.innerHTML = eyeClosedSvg;
      } else {
        passwordInput.setAttribute("type", "password");
        passwordInput.style.webkitTextSecurity = "";
        toggleEyeBtn.innerHTML = eyeOpenSvg;
      }
    });
  }

  // 2. Xóa thông báo lỗi khi gõ phím
  [usernameInput, passwordInput].forEach((input) => {
    if (input) {
      input.addEventListener("input", () => {
        if (usernameError) usernameError.textContent = "";
        if (passwordError) passwordError.textContent = "";
        if (clientAlert) {
          clientAlert.textContent = "";
          clientAlert.classList.remove("show");
        }
      });
    }
  });

  // 3. Client Validation khi submit form
  if (loginForm) {
    loginForm.addEventListener("submit", (e) => {
      let isValid = true;
      const userVal = usernameInput ? usernameInput.value.trim() : "";
      const passVal = passwordInput ? passwordInput.value : "";
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (usernameError) usernameError.textContent = "";
      if (passwordError) passwordError.textContent = "";

      // Kiểm tra tên đăng nhập hoặc email
      if (!userVal) {
        if (usernameError) usernameError.textContent = "Vui lòng nhập tên đăng nhập hoặc email.";
        isValid = false;
      } else if (userVal.includes("@")) {
        // Nếu nhập có chứa @ thì bắt buộc phải đúng định dạng email
        if (!emailRegex.test(userVal)) {
          if (usernameError) usernameError.textContent = "Định dạng email không hợp lệ (ví dụ: user@domain.com).";
          isValid = false;
        }
      } else if (userVal.length < 3) {
        // Nếu là tên đăng nhập thông thường thì tối thiểu 3 ký tự (chặn các chữ như 'd')
        if (usernameError) usernameError.textContent = "Tên đăng nhập phải có ít nhất 3 ký tự.";
        isValid = false;
      }

      // Kiểm tra mật khẩu
      if (!passVal) {
        if (passwordError) passwordError.textContent = "Vui lòng nhập mật khẩu.";
        isValid = false;
      } else if (passVal.length < 6) {
        if (passwordError) passwordError.textContent = "Mật khẩu phải có ít nhất 6 ký tự.";
        isValid = false;
      }

      if (!isValid) {
        e.preventDefault();
      }
    });
  }
});