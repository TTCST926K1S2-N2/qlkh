document.addEventListener("DOMContentLoaded", () => {
  const resetForm = document.getElementById("reset-form");
  const newPass = document.getElementById("newPassword");
  const confirmPass = document.getElementById("confirmPassword");
  const newPassError = document.getElementById("new-password-error");
  const confirmPassError = document.getElementById("confirm-password-error");

  // Toggle ẩn hiện mật khẩu
  const setupToggle = (btnId, inputId) => {
    const btn = document.getElementById(btnId);
    const input = document.getElementById(inputId);
    if (btn && input) {
      btn.addEventListener("click", () => {
        const isPass = input.type === "password";
        input.type = isPass ? "text" : "password";
        btn.textContent = isPass ? "🙈" : "👁️";
      });
    }
  };

  setupToggle("btn-toggle-new-pass", "newPassword");
  setupToggle("btn-toggle-confirm-pass", "confirmPassword");

  // Validate form
  if (resetForm) {
    resetForm.addEventListener("submit", (e) => {
      let hasError = false;
      newPassError.textContent = "";
      confirmPassError.textContent = "";

      if (newPass.value.length < 6) {
        newPassError.textContent = "Mật khẩu mới phải có tối thiểu 6 ký tự.";
        hasError = true;
      }

      if (newPass.value !== confirmPass.value) {
        confirmPassError.textContent = "Mật khẩu xác nhận không khớp.";
        hasError = true;
      }

      if (hasError) {
        e.preventDefault();
      }
    });
  }
});