document.addEventListener("DOMContentLoaded", function () {
    const excelFile = document.getElementById("excelFile");
    const selectedFileName = document.getElementById("selectedFileName");
    const fileError = document.getElementById("fileError");
    const importButton = document.getElementById("importButton");

    if (!excelFile || !selectedFileName || !fileError || !importButton) {
        return;
    }

    importButton.disabled = true;

    excelFile.addEventListener("change", function () {
        fileError.textContent = "";

        const file = excelFile.files[0];

        if (!file) {
            selectedFileName.textContent = "Chưa chọn tệp";
            importButton.disabled = true;
            return;
        }

        // Hiển thị tên file đã chọn
        selectedFileName.textContent = file.name;

        // Kiểm tra định dạng file
        const fileName = file.name.toLowerCase();

        const validFile =
            fileName.endsWith(".xlsx") ||
            fileName.endsWith(".xls");

        if (!validFile) {
            fileError.textContent =
                "Tệp không hợp lệ. Vui lòng chọn file Excel (.xlsx hoặc .xls).";

            importButton.disabled = true;
            return;
        }

        // File hợp lệ
        importButton.disabled = false;
    });
});