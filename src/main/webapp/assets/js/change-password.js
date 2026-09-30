/**
 * Validation và Xử lý Giao diện Đổi Mật Khẩu (HTQLKH-4 FE)
 */
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("changePasswordForm");
    const currentPasswordInput = document.getElementById("currentPassword");
    const newPasswordInput = document.getElementById("newPassword");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    
    // Thẻ thông báo lỗi tổng quan
    const generalErrorAlert = document.getElementById("jsErrorAlert");
    const generalErrorMessage = document.getElementById("jsErrorMessage");

    // Thống nhất ID checklist quy tắc mật khẩu (Kebab-case)
    const ruleLength = document.getElementById("rule-length");
    const ruleLetter = document.getElementById("rule-letter");
    const ruleNumber = document.getElementById("rule-number");

    if (!form) return;

    // 1. Kiểm tra trực quan checklist khi người dùng gõ mật khẩu mới
    newPasswordInput.addEventListener("input", function () {
        validatePasswordRules(newPasswordInput.value);
    });

    // 2. Chặn Submit Form nếu dữ liệu không hợp lệ
    form.addEventListener("submit", function (e) {
        hideErrorAlert();
        clearInputErrors();

        let isValid = true;
        let errorMessages = [];

        const currentVal = currentPasswordInput.value.trim();
        const newVal = newPasswordInput.value;
        const confirmVal = confirmPasswordInput.value;

        // Validation 1: Mật khẩu hiện tại không được trống
        if (currentVal === "") {
            markInvalid(currentPasswordInput, "Vui lòng nhập mật khẩu hiện tại.");
            errorMessages.push("Mật khẩu hiện tại không được để trống.");
            isValid = false;
        }

        // Validation 2: Mật khẩu mới >= 8 ký tự, phải có cả chữ và số
        const hasMinLength = newVal.length >= 8;
        const hasLetter = /[a-zA-Z]/.test(newVal);
        const hasNumber = /[0-9]/.test(newVal);

        if (!hasMinLength || !hasLetter || !hasNumber) {
            markInvalid(newPasswordInput, "Mật khẩu mới không đạt yêu cầu an toàn.");
            errorMessages.push("Mật khẩu mới phải từ 8 ký tự, chứa ít nhất 1 chữ cái và 1 chữ số.");
            isValid = false;
        }

        // Validation 3: Mật khẩu mới phải khác mật khẩu hiện tại
        if (newVal !== "" && currentVal !== "" && newVal === currentVal) {
            markInvalid(
                newPasswordInput,
                "Mật khẩu mới phải khác mật khẩu hiện tại."
            );
            errorMessages.push(
                "Mật khẩu mới không được trùng với mật khẩu hiện tại."
            );
            isValid = false;
        }

        // Validation 4: Xác nhận mật khẩu mới phải khớp
        if (confirmVal === "" || confirmVal !== newVal) {
            markInvalid(confirmPasswordInput, "Mật khẩu xác nhận không trùng khớp.");
            errorMessages.push("Xác nhận mật khẩu mới không khớp.");
            isValid = false;
        }

        // BẮT BUỘC CHẶN SUBMIT nếu phát hiện bất kỳ lỗi nào
        if (!isValid) {
            e.preventDefault();
            e.stopPropagation();
            showErrorAlert(errorMessages.join("<br>"));
        }
    });

    // Cập nhật trạng thái biểu tượng & màu sắc của checklist
    function validatePasswordRules(val) {
        const hasMinLength = val.length >= 8;
        const hasLetter = /[a-zA-Z]/.test(val);
        const hasNumber = /[0-9]/.test(val);

        updateRuleItem(ruleLength, hasMinLength);
        updateRuleItem(ruleLetter, hasLetter);
        updateRuleItem(ruleNumber, hasNumber);
    }

    function updateRuleItem(element, isValid) {
        if (!element) return;
        const icon = element.querySelector("i");
        if (isValid) {
            element.className = "valid";
            if (icon) icon.className = "fas fa-check-circle me-1";
        } else {
            element.className = "invalid";
            if (icon) icon.className = "fas fa-times-circle me-1";
        }
    }

    function markInvalid(inputElement, message) {
        if (!inputElement) return;
        inputElement.classList.add("is-invalid");
        const feedback = inputElement.nextElementSibling;
        if (feedback && feedback.classList.contains("invalid-feedback")) {
            feedback.textContent = message;
        }
    }

    function clearInputErrors() {
        [currentPasswordInput, newPasswordInput, confirmPasswordInput].forEach(input => {
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