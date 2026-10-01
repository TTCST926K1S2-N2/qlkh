document.addEventListener("DOMContentLoaded", function () {

    const profileForm = document.getElementById("profileForm");

    const fullNameInput = document.getElementById("fullName");
    const phoneInput = document.getElementById("phone");
    const signatureInput = document.getElementById("emailSignature");

    const fullNameError = document.getElementById("fullNameError");
    const phoneError = document.getElementById("phoneError");

    const signatureCount = document.getElementById("signatureCount");
    const saveButton = document.getElementById("saveProfileButton");


    // ==============================
    // ĐẾM KÝ TỰ CHỮ KÝ EMAIL
    // ==============================

    function updateSignatureCount() {
        const length = signatureInput.value.length;

        signatureCount.textContent = length + "/1000";
    }

    updateSignatureCount();

    signatureInput.addEventListener("input", updateSignatureCount);


    // ==============================
    // VALIDATE HỌ TÊN
    // ==============================

    function validateFullName() {

        const fullName = fullNameInput.value.trim();

        fullNameError.textContent = "";

        if (fullName === "") {
            fullNameError.textContent = "Vui lòng nhập họ và tên.";
            return false;
        }

        if (fullName.length < 2) {
            fullNameError.textContent =
                "Họ và tên phải có ít nhất 2 ký tự.";
            return false;
        }

        return true;
    }


    // ==============================
    // VALIDATE SỐ ĐIỆN THOẠI VIỆT NAM
    // ==============================

    function validatePhone() {

        const phone = phoneInput.value.trim();

        phoneError.textContent = "";

        if (phone === "") {
            phoneError.textContent =
                "Vui lòng nhập số điện thoại.";
            return false;
        }

        /*
         * Số điện thoại di động Việt Nam:
         * - Bắt đầu bằng 0
         * - Đầu số 03, 05, 07, 08 hoặc 09
         * - Tổng cộng 10 chữ số
         */
        const vietnamPhoneRegex = /^0(3|5|7|8|9)[0-9]{8}$/;

        if (!vietnamPhoneRegex.test(phone)) {
            phoneError.textContent =
                "Số điện thoại Việt Nam không hợp lệ.";
            return false;
        }

        return true;
    }


    // ==============================
    // VALIDATE KHI NGƯỜI DÙNG NHẬP
    // ==============================

    fullNameInput.addEventListener("blur", validateFullName);

    phoneInput.addEventListener("blur", validatePhone);

    fullNameInput.addEventListener("input", function () {
        if (fullNameError.textContent !== "") {
            validateFullName();
        }
    });

    phoneInput.addEventListener("input", function () {
        if (phoneError.textContent !== "") {
            validatePhone();
        }
    });


    // ==============================
    // SUBMIT FORM
    // ==============================

    profileForm.addEventListener("submit", function (event) {

        event.preventDefault();

        const isFullNameValid = validateFullName();
        const isPhoneValid = validatePhone();

        if (!isFullNameValid || !isPhoneValid) {
            return;
        }

        /*
         * Phần gọi API cập nhật hồ sơ sẽ được tích hợp
         * khi Backend cung cấp endpoint.
         *
         * FE hiện tại chỉ thực hiện kiểm tra dữ liệu
         * trước khi gửi.
         */

        saveButton.disabled = true;
        saveButton.textContent = "Đang lưu...";

        // Trả nút về trạng thái ban đầu vì chưa tích hợp API.
        setTimeout(function () {
            saveButton.disabled = false;
            saveButton.textContent = "Lưu thay đổi";
        }, 500);
    });

});