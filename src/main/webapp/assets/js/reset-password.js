/**
 * Xử lý logic Đặt lại mật khẩu (HTQLKH-3)
 */
document.addEventListener("DOMContentLoaded", () => {

    const form =
        document.getElementById("reset-password-form");

    const newPass =
        document.getElementById("newPassword");

    const confirmPass =
        document.getElementById("confirmPassword");

    const newPassErr =
        document.getElementById("newPassword-error");

    const confirmPassErr =
        document.getElementById("confirmPassword-error");

    const alertBox =
        document.getElementById("alert-message");

    const toggleEyeBtns =
        document.querySelectorAll(".btn-toggle-eye");


    // =========================================================
    // SVG ICON MẮT MỞ
    // =========================================================

    const eyeOpenSvg = `
        <svg xmlns="http://www.w3.org/2000/svg"
             width="18"
             height="18"
             viewBox="0 0 24 24"
             fill="none"
             stroke="currentColor"
             stroke-width="2"
             stroke-linecap="round"
             stroke-linejoin="round">

            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>

            <circle
                cx="12"
                cy="12"
                r="3">
            </circle>
        </svg>
    `;


    // =========================================================
    // SVG ICON MẮT ĐÓNG
    // =========================================================

    const eyeClosedSvg = `
        <svg xmlns="http://www.w3.org/2000/svg"
             width="18"
             height="18"
             viewBox="0 0 24 24"
             fill="none"
             stroke="currentColor"
             stroke-width="2"
             stroke-linecap="round"
             stroke-linejoin="round">

            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>

            <line
                x1="1"
                y1="1"
                x2="23"
                y2="23">
            </line>
        </svg>
    `;


    // =========================================================
    // 1. BẬT / TẮT HIỂN THỊ MẬT KHẨU
    // =========================================================

    toggleEyeBtns.forEach((btn) => {

        btn.addEventListener("click", () => {

            const targetId =
                btn.getAttribute("data-target");

            const input =
                document.getElementById(targetId);

            if (!input) {
                return;
            }

            const isPassword =
                input.getAttribute("type") === "password";

            input.setAttribute(
                "type",
                isPassword
                    ? "text"
                    : "password"
            );

            btn.innerHTML =
                isPassword
                    ? eyeClosedSvg
                    : eyeOpenSvg;
        });
    });


    // =========================================================
    // 2. XÓA THÔNG BÁO LỖI KHI NGƯỜI DÙNG NHẬP LẠI
    // =========================================================

    [newPass, confirmPass].forEach((input) => {

        if (!input) {
            return;
        }

        input.addEventListener("input", () => {

            if (newPassErr) {
                newPassErr.textContent = "";
            }

            if (confirmPassErr) {
                confirmPassErr.textContent = "";
            }

            if (alertBox) {
                alertBox.style.display = "none";
                alertBox.className = "alert-box";
            }
        });
    });


    // =========================================================
    // 3. KIỂM TRA TÍNH HỢP LỆ CỦA MẬT KHẨU
    // =========================================================

    function validatePassword(pass) {

        if (pass.length < 8) {
            return "Mật khẩu phải có tối thiểu 8 ký tự.";
        }

        if (!/[A-Z]/.test(pass)) {
            return "Mật khẩu phải có ít nhất 1 chữ hoa.";
        }

        if (!/[a-z]/.test(pass)) {
            return "Mật khẩu phải có ít nhất 1 chữ thường.";
        }

        if (!/[0-9]/.test(pass)) {
            return "Mật khẩu phải có ít nhất 1 chữ số.";
        }

        return "";
    }


    // =========================================================
    // 4. KIỂM TRA FORM VÀ CHỐNG DOUBLE SUBMIT
    // =========================================================

    if (form) {

        /*
         * false:
         * form chưa được gửi.
         *
         * true:
         * request đang được gửi tới Backend.
         *
         * Biến này giúp ngăn người dùng bấm nút nhiều lần
         * làm gửi nhiều request với cùng reset token.
         */
        let isSubmitting = false;


        form.addEventListener("submit", (e) => {

            // -------------------------------------------------
            // CHẶN REQUEST THỨ HAI
            // -------------------------------------------------

            if (isSubmitting) {

                e.preventDefault();

                return;
            }


            let isValid = true;


            const passVal =
                newPass
                    ? newPass.value
                    : "";


            const confirmVal =
                confirmPass
                    ? confirmPass.value
                    : "";


            // -------------------------------------------------
            // XÓA LỖI CŨ
            // -------------------------------------------------

            if (newPassErr) {
                newPassErr.textContent = "";
            }

            if (confirmPassErr) {
                confirmPassErr.textContent = "";
            }


            // -------------------------------------------------
            // KIỂM TRA MẬT KHẨU MỚI
            // -------------------------------------------------

            if (!passVal) {

                if (newPassErr) {

                    newPassErr.textContent =
                        "Vui lòng nhập mật khẩu mới.";
                }

                isValid = false;

            } else {

                const validationMsg =
                    validatePassword(passVal);

                if (validationMsg) {

                    if (newPassErr) {

                        newPassErr.textContent =
                            validationMsg;
                    }

                    isValid = false;
                }
            }


            // -------------------------------------------------
            // KIỂM TRA XÁC NHẬN MẬT KHẨU
            // -------------------------------------------------

            if (!confirmVal) {

                if (confirmPassErr) {

                    confirmPassErr.textContent =
                        "Vui lòng xác nhận mật khẩu mới.";
                }

                isValid = false;

            } else if (
                passVal &&
                passVal !== confirmVal
            ) {

                if (confirmPassErr) {

                    confirmPassErr.textContent =
                        "Mật khẩu xác nhận không khớp.";
                }

                isValid = false;
            }


            // -------------------------------------------------
            // NẾU DỮ LIỆU KHÔNG HỢP LỆ → KHÔNG GỬI FORM
            // -------------------------------------------------

            if (!isValid) {

                e.preventDefault();

                return;
            }


            // -------------------------------------------------
            // DỮ LIỆU HỢP LỆ
            // KHÓA FORM ĐỂ KHÔNG GỬI REQUEST LẦN 2
            // -------------------------------------------------

            isSubmitting = true;


            const submitButton =
                form.querySelector(
                    'button[type="submit"], input[type="submit"]'
                );


            if (submitButton) {

                submitButton.disabled = true;

                if (
                    submitButton.tagName === "BUTTON"
                ) {

                    submitButton.textContent =
                        "Đang xử lý...";
                }
            }

            /*
             * KHÔNG gọi:
             *
             * form.submit()
             * fetch()
             *
             * ở đây.
             *
             * Trình duyệt sẽ tự POST form đúng 1 lần
             * tới:
             *
             * /reset-password
             */
        });
    }
});