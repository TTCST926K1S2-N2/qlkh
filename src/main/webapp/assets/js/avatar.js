document.addEventListener("DOMContentLoaded", function () {

    const avatarForm = document.getElementById("avatarForm");

    const avatarFile = document.getElementById("avatarFile");
    const avatarPreview = document.getElementById("avatarPreview");
    const avatarPlaceholder = document.getElementById("avatarPlaceholder");

    const selectedFileName = document.getElementById("selectedFileName");
    const avatarError = document.getElementById("avatarError");

    const removeAvatarButton = document.getElementById("removeAvatarButton");
    const uploadAvatarButton = document.getElementById("uploadAvatarButton");

    let previewUrl = null;


    // =====================================
    // RESET ẢNH ĐÃ CHỌN
    // =====================================

    function resetSelectedAvatar() {

        avatarFile.value = "";

        selectedFileName.textContent = "Chưa chọn ảnh";
        avatarError.textContent = "";

        avatarPreview.src = "";
        avatarPreview.hidden = true;

        avatarPlaceholder.hidden = false;

        removeAvatarButton.disabled = true;
        uploadAvatarButton.disabled = true;

        if (previewUrl !== null) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
    }


    // =====================================
    // KIỂM TRA FILE ẢNH
    // =====================================

    function isValidImage(file) {

        const fileName = file.name.toLowerCase();

        return (
            fileName.endsWith(".jpg") ||
            fileName.endsWith(".jpeg") ||
            fileName.endsWith(".png")
        );
    }


    // =====================================
    // CHỌN ẢNH
    // =====================================

    avatarFile.addEventListener("change", function () {

        avatarError.textContent = "";

        const file = avatarFile.files[0];

        if (!file) {
            resetSelectedAvatar();
            return;
        }

        selectedFileName.textContent = file.name;


        // Kiểm tra định dạng
        if (!isValidImage(file)) {

            avatarError.textContent =
                "Ảnh không hợp lệ. Vui lòng chọn file JPG, JPEG hoặc PNG.";

            avatarPreview.src = "";
            avatarPreview.hidden = true;
            avatarPlaceholder.hidden = false;

            removeAvatarButton.disabled = false;
            uploadAvatarButton.disabled = true;

            return;
        }


        // Xóa URL preview cũ nếu có
        if (previewUrl !== null) {
            URL.revokeObjectURL(previewUrl);
        }


        // Tạo preview mới
        previewUrl = URL.createObjectURL(file);

        avatarPreview.src = previewUrl;
        avatarPreview.hidden = false;

        avatarPlaceholder.hidden = true;

        removeAvatarButton.disabled = false;
        uploadAvatarButton.disabled = false;
    });


    // =====================================
    // BỎ ẢNH ĐÃ CHỌN
    // =====================================

    removeAvatarButton.addEventListener("click", function () {
        resetSelectedAvatar();
    });


    // =====================================
    // SUBMIT
    // =====================================

    avatarForm.addEventListener("submit", function (event) {

        event.preventDefault();

        const file = avatarFile.files[0];

        if (!file) {

            avatarError.textContent =
                "Vui lòng chọn ảnh đại diện.";

            return;
        }


        if (!isValidImage(file)) {

            avatarError.textContent =
                "Ảnh không hợp lệ. Vui lòng chọn file JPG, JPEG hoặc PNG.";

            uploadAvatarButton.disabled = true;

            return;
        }


        /*
         * Phần gọi API upload ảnh sẽ được tích hợp
         * khi Backend cung cấp endpoint.
         *
         * FE hiện tại chỉ xử lý chọn file,
         * kiểm tra định dạng và xem trước ảnh.
         */

        uploadAvatarButton.disabled = true;
        removeAvatarButton.disabled = true;

        uploadAvatarButton.textContent = "Đang tải...";


        // Tạm trả lại trạng thái vì chưa tích hợp API.
        setTimeout(function () {

            uploadAvatarButton.disabled = false;
            removeAvatarButton.disabled = false;

            uploadAvatarButton.textContent = "Tải ảnh đại diện";

        }, 500);
    });


    // =====================================
    // DỌN URL KHI RỜI TRANG
    // =====================================

    window.addEventListener("beforeunload", function () {

        if (previewUrl !== null) {
            URL.revokeObjectURL(previewUrl);
        }
    });

});