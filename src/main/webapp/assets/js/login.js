document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("login-form");
  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");
  const btnToggleEye = document.getElementById("btn-toggle-password");
  const usernameError = document.getElementById("username-error");
  const passwordError = document.getElementById("password-error");
  const alertBox = document.getElementById("alert-message");

  // 1. Ẩn/Hiện mật khẩu
  if (btnToggleEye && passwordInput) {
    btnToggleEye.addEventListener("click", () => {
      const isPassword = passwordInput.getAttribute("type") === "password";
      passwordInput.setAttribute("type", isPassword ? "text" : "password");
      btnToggleEye.textContent = isPassword ? "🙈" : "👁️";
    });
  }

  // 2. Client-side Validation trước khi submit form về Servlet
  if (form) {
    form.addEventListener("submit", (e) => {
      let hasError = false;
      usernameError.textContent = "";
      passwordError.textContent = "";

      const username = usernameInput.value.trim();
      const password = passwordInput.value;

      if (!username) {
        usernameError.textContent = "Vui lòng nhập tên đăng nhập hoặc email.";
        hasError = true;
      }

      if (!password) {
        passwordError.textContent = "Vui lòng nhập mật khẩu.";
        hasError = true;
      }

      if (hasError) {
        e.preventDefault(); // Chặn gửi request nếu form chưa hợp lệ
      }
    });
  }
});