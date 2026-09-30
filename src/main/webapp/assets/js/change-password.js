/**
 * Validation và Xử lý Giao diện Đổi Mật Khẩu (HTQLKH-4 FE)
 */
document.addEventListener("DOMContentLoaded", function () {

    const form =
        document.getElementById("changePasswordForm");

    const currentPasswordInput =
        document.getElementById("currentPassword");

    const newPasswordInput =
        document.getElementById("newPassword");

    const confirmPasswordInput =
        document.getElementById("confirmPassword");


    // Thông báo lỗi từ JavaScript
    const generalErrorAlert =
        document.getElementById("jsErrorAlert");

    const generalErrorMessage =
        document.getElementById("jsErrorMessage");


    // Thông báo lỗi cũ do Backend trả về
    const serverErrorAlert =
        document.getElementById("serverErrorAlert");


    // Checklist quy tắc mật khẩu
    const ruleLength =
        document.getElementById("rule-length");

    const ruleLetter =
        document.getElementById("rule-letter");

    const ruleNumber =
        document.getElementById("rule-number");


    if (!form) {
        return;
    }


    /*
     * ==========================================
     * 1. KIỂM TRA CHECKLIST KHI NHẬP MẬT KHẨU
     * ==========================================
     */
    newPasswordInput.addEventListener(
        "input",
        function () {

            validatePasswordRules(
                newPasswordInput.value
            );
        }
    );


    /*
     * ==========================================
     * 2. KIỂM TRA KHI SUBMIT FORM
     * ==========================================
     */
    form.addEventListener(
        "submit",
        function (e) {

            /*
             * Xóa thông báo JavaScript của lần trước.
             */
            hideErrorAlert();

            /*
             * HTQLKH-4:
             * Ẩn lỗi Backend của lần submit trước.
             *
             * Ví dụ:
             * Lần trước nhập sai mật khẩu hiện tại
             * Backend trả:
             *
             * "Mật khẩu hiện tại không chính xác."
             *
             * Khi người dùng thử lại, thông báo cũ
             * phải được ẩn để không xuất hiện cùng
             * lỗi validation mới.
             */
            hideServerErrorAlert();

            clearInputErrors();


            let isValid = true;

            const errorMessages = [];


            const currentVal =
                currentPasswordInput.value.trim();

            const newVal =
                newPasswordInput.value;

            const confirmVal =
                confirmPasswordInput.value;


            /*
             * ======================================
             * VALIDATION 1
             * Mật khẩu hiện tại bắt buộc nhập
             * ======================================
             */
            if (currentVal === "") {

                markInvalid(
                    currentPasswordInput,
                    "Vui lòng nhập mật khẩu hiện tại."
                );

                errorMessages.push(
                    "Mật khẩu hiện tại không được để trống."
                );

                isValid = false;
            }


            /*
             * ======================================
             * VALIDATION 2
             * Mật khẩu mới:
             * - ít nhất 8 ký tự
             * - có chữ
             * - có số
             * ======================================
             */
            const hasMinLength =
                newVal.length >= 8;

            const hasLetter =
                /[a-zA-Z]/.test(newVal);

            const hasNumber =
                /[0-9]/.test(newVal);


            if (
                !hasMinLength
                || !hasLetter
                || !hasNumber
            ) {

                markInvalid(
                    newPasswordInput,
                    "Mật khẩu mới không đạt yêu cầu an toàn."
                );

                errorMessages.push(
                    "Mật khẩu mới phải từ 8 ký tự, chứa ít nhất 1 chữ cái và 1 chữ số."
                );

                isValid = false;
            }


            /*
             * ======================================
             * VALIDATION 3
             * Mật khẩu mới không được giống
             * mật khẩu hiện tại
             * ======================================
             */
            if (
                newVal !== ""
                && currentVal !== ""
                && newVal === currentVal
            ) {

                markInvalid(
                    newPasswordInput,
                    "Mật khẩu mới phải khác mật khẩu hiện tại."
                );

                errorMessages.push(
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại."
                );

                isValid = false;
            }


            /*
             * ======================================
             * VALIDATION 4
             * Xác nhận mật khẩu phải khớp
             * ======================================
             */
            if (
                confirmVal === ""
                || confirmVal !== newVal
            ) {

                markInvalid(
                    confirmPasswordInput,
                    "Mật khẩu xác nhận không trùng khớp."
                );

                errorMessages.push(
                    "Xác nhận mật khẩu mới không khớp."
                );

                isValid = false;
            }


            /*
             * ======================================
             * CHẶN SUBMIT NẾU CÓ LỖI
             * ======================================
             */
            if (!isValid) {

                e.preventDefault();
                e.stopPropagation();

                showErrorAlert(
                    errorMessages.join("<br>")
                );
            }
        }
    );


    /*
     * ==========================================
     * CHECKLIST MẬT KHẨU
     * ==========================================
     */
    function validatePasswordRules(val) {

        const hasMinLength =
            val.length >= 8;

        const hasLetter =
            /[a-zA-Z]/.test(val);

        const hasNumber =
            /[0-9]/.test(val);


        updateRuleItem(
            ruleLength,
            hasMinLength
        );

        updateRuleItem(
            ruleLetter,
            hasLetter
        );

        updateRuleItem(
            ruleNumber,
            hasNumber
        );
    }


    /*
     * Cập nhật trạng thái từng rule.
     */
    function updateRuleItem(
        element,
        isValid
    ) {

        if (!element) {
            return;
        }

        const icon =
            element.querySelector("i");


        if (isValid) {

            element.className =
                "valid";

            if (icon) {

                icon.className =
                    "fas fa-check-circle me-1";
            }

        } else {

            element.className =
                "invalid";

            if (icon) {

                icon.className =
                    "fas fa-times-circle me-1";
            }
        }
    }


    /*
     * ==========================================
     * ĐÁNH DẤU INPUT KHÔNG HỢP LỆ
     * ==========================================
     */
    function markInvalid(
        inputElement,
        message
    ) {

        if (!inputElement) {
            return;
        }

        inputElement.classList.add(
            "is-invalid"
        );


        const feedback =
            inputElement.nextElementSibling;


        if (
            feedback
            && feedback.classList.contains(
                "invalid-feedback"
            )
        ) {

            feedback.textContent =
                message;
        }
    }


    /*
     * ==========================================
     * XÓA TRẠNG THÁI LỖI INPUT
     * ==========================================
     */
    function clearInputErrors() {

        [
            currentPasswordInput,
            newPasswordInput,
            confirmPasswordInput
        ].forEach(function (input) {

            if (input) {

                input.classList.remove(
                    "is-invalid"
                );
            }
        });
    }


    /*
     * ==========================================
     * HIỂN THỊ LỖI JAVASCRIPT
     * ==========================================
     */
    function showErrorAlert(
        messageHtml
    ) {

        if (
            generalErrorAlert
            && generalErrorMessage
        ) {

            generalErrorMessage.innerHTML =
                messageHtml;

            generalErrorAlert.classList.remove(
                "d-none"
            );

            generalErrorAlert.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });
        }
    }


    /*
     * ==========================================
     * ẨN LỖI JAVASCRIPT
     * ==========================================
     */
    function hideErrorAlert() {

        if (generalErrorAlert) {

            generalErrorAlert.classList.add(
                "d-none"
            );
        }
    }


    /*
     * ==========================================
     * ẨN THÔNG BÁO BACKEND CŨ
     * ==========================================
     */
    function hideServerErrorAlert() {

        if (serverErrorAlert) {

            serverErrorAlert.classList.add(
                "d-none"
            );
        }
    }
});