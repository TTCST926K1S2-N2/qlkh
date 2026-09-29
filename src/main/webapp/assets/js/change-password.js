
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("changePasswordForm");
    const currentPasswordInput = document.getElementById("currentPassword");
    const newPasswordInput = document.getElementById("newPassword");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const generalErrorAlert = document.getElementById("jsErrorAlert");
    const generalErrorMessage = document.getElementById("jsErrorMessage");

    // Các thẻ hiển thị tiêu chuẩn độ mạnh mật khẩu (Checklist)
    const ruleLength = document.getElementById("ruleLength");
    const ruleLetter = document.getElementById("ruleLetter");
    const ruleNumber = document.getElementById("ruleNumber");

    if (!form) return;

    // Lắng nghe sự kiện gõ phím trên mật khẩu mới để cập nhật checklist trực quan
    newPasswordInput.addEventListener("input", function () {
        const val = newPasswordInput.value;
        
        // Điều kiện 1: >= 8 ký tự
        const hasMinLength = val.length >= 8;
        updateRuleStatus(ruleLength, hasMinLength);

        // Điều kiện 2: Có ít nhất 1 chữ cái (a-z, A-Z)
        const hasLetter = /[a-zA-Z]/.test(val);
        updateRuleStatus(ruleLetter, hasLetter);

        // Điều kiện 3: Có ít nhất 1 chữ số (0-9)
        const hasNumber = /[0-9]/.test(val);
        updateRuleStatus(ruleNumber, hasNumber);
    });

    // Bắt sự kiện SUBMIT và chặn nếu không hợp lệ
    form.addEventListener("submit", function (e) {
        hideErrorAlert();
        clearInputErrors();

        let isValid = true;
        let errorMsgs = [];

        const currentVal = currentPasswordInput.value.trim();
        const newVal = newPasswordInput.value;
        const confirmVal = confirmPasswordInput.value;

        // 1. Validate Mật khẩu hiện tại
        if (currentVal === "") {
            markInvalid(currentPasswordInput, "Vui lòng nhập mật khẩu hiện tại!");
            errorMsgs.push("Mật khẩu hiện tại không được để trống.");
            isValid = false;
        }

        // 2. Validate Mật khẩu mới (>= 8 ký tự + chữ + số)
        const hasMinLength = newVal.length >= 8;
        const hasLetter = /[a-zA-Z]/.test(newVal);
        const hasNumber = /[0-9]/.test(newVal);

        if (!hasMinLength || !hasLetter || !hasNumber) {
            markInvalid(newPasswordInput, "Mật khẩu mới phải từ 8 ký tự, gồm cả chữ và số!");
            errorMsgs.push("Mật khẩu mới chưa đạt chuẩn quy định (tối thiểu 8 ký tự, bao gồm cả chữ và số).");
            isValid = false;
        }

        // 3. Validate Mật khẩu xác nhận trùng khớp
        if (confirmVal === "" || confirmVal !== newVal) {
            markInvalid(confirmPasswordInput, "Xác nhận mật khẩu không trùng khớp với mật khẩu mới!");
            errorMsgs.push("Xác nhận mật khẩu không khớp.");
            isValid = false;
        }

        // CHẶN SUBMIT nếu phát hiện lỗi
        if (!isValid) {
            e.preventDefault();
            e.stopPropagation();
            showErrorAlert(errorMsgs.join("<br>"));
        }
    });

    function updateRuleStatus(element, isValid) {
        if (!element) return;
        if (isValid) {
            element.classList.remove("invalid", "text-muted");
            element.classList.add("valid");
            element.querySelector("i").className = "fas fa-check-circle me-1";
        } else {
            element.classList.remove("valid");
            element.classList.add("invalid");
            element.querySelector("i").className = "fas fa-times-circle me-1";
        }
    }

    function markInvalid(inputElement, message) {
        inputElement.classList.add("is-invalid");
        const feedback = inputElement.nextElementSibling;
        if (feedback && feedback.classList.contains("invalid-feedback")) {
            feedback.textContent = message;
        }
    }

    function clearInputErrors() {
        const inputs = [currentPasswordInput, newPasswordInput, confirmPasswordInput];
        inputs.forEach(input => {
            if (input) input.classList.remove("is-invalid");
        });
    }

    function showErrorAlert(messageHtml) {
        if (generalErrorAlert && generalErrorMessage) {
            generalErrorMessage.innerHTML = messageHtml;
            generalErrorAlert.classList.remove("d-none");
            generalErrorAlert.scrollIntoView({ behavior: "smooth", block: "center" });
        }
    }

    function hideErrorAlert() {
        if (generalErrorAlert) {
            generalErrorAlert.classList.add("d-none");
        }
    }
});