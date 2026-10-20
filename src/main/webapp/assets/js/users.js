document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("importForm");
    const fileInput = document.getElementById("excelFile");
    const fileName = document.getElementById("selectedFileName");
    const fileError = document.getElementById("fileError");

    const previewButton = document.getElementById("importButton");
    const confirmButton = document.getElementById("confirmImportButton");

    const result = document.getElementById("importResult");
    const successCount = document.getElementById("successCount");
    const failedCount = document.getElementById("failedCount");
    const errorList = document.getElementById("errorList");
    const notification = document.getElementById("importNotification");

    if (!form || !fileInput || !previewButton || !confirmButton) {
        return;
    }

    const script = document.querySelector(
        'script[src*="/assets/js/users.js"]'
    );

    const contextPath = script
        ? new URL(script.src).pathname.replace(
            "/assets/js/users.js", ""
        )
        : "";

    const importUrl = contextPath + "/users/import";

    let previewReady = false;
    let busy = false;

    function notify(message, type = "info") {
        if (!notification) return;

        notification.textContent = message;
        notification.className =
            "import-notification " + type;

        notification.hidden = false;
    }

    function resetResult() {
        previewReady = false;
        confirmButton.hidden = true;
        result.hidden = true;

        if (notification) {
            notification.hidden = true;
        }

        fileError.textContent = "";
    }

    function validFile(file) {
        return file &&
            /\.(xlsx|xls)$/i.test(file.name);
    }

    fileInput.addEventListener("change", function () {
        resetResult();

        const file = fileInput.files[0];

        fileName.textContent = file
            ? file.name
            : "Chưa chọn tệp";

        if (file && !validFile(file)) {
            fileError.textContent =
                "Tệp không hợp lệ. Chỉ hỗ trợ .xlsx hoặc .xls.";

            notify(fileError.textContent, "error");
        }

        previewButton.disabled =
            !validFile(file) || busy;
    });

    form.addEventListener("submit", async function (event) {
        event.preventDefault();
        await processImport(true);
    });

    confirmButton.addEventListener("click", async function () {
        if (!previewReady || busy) return;
        await processImport(false);
    });

    async function processImport(preview) {
        const file = fileInput.files[0];

        if (busy) return;

        if (!validFile(file)) {
            notify(
                "Vui lòng chọn tệp Excel hợp lệ.",
                "error"
            );
            return;
        }

        busy = true;
        previewButton.disabled = true;
        confirmButton.disabled = true;

        previewButton.textContent = preview
            ? "Đang xem trước..."
            : "Đang Import...";

        if (notification) {
            notification.hidden = true;
        }

        fileError.textContent = "";

        const formData = new FormData();
        formData.append("file", file);

        if (preview) {
            formData.append("preview", "true");
        }

        try {
            const response = await fetch(importUrl, {
                method: "POST",
                body: formData
            });

            const data = await response.json();

            if (!response.ok || data.success === false) {
                throw new Error(
                    data.message ||
                    "Không thể xử lý tệp Excel."
                );
            }

            const success = Number(data.successCount || 0);
            const failed = Number(data.failedCount || 0);

            successCount.textContent = success;
            failedCount.textContent = failed;

            errorList.replaceChildren();

            (data.errors || []).forEach(function (item) {
                const line = document.createElement("p");

                line.textContent =
                    "Dòng " + item.row +
                    (item.email ? " - " + item.email : "") +
                    ": " + item.message;

                errorList.appendChild(line);
            });

            result.hidden = false;

            if (preview) {
                previewReady = success > 0;
                confirmButton.hidden = !previewReady;

                notify(
                    "Xem trước hoàn tất. Hợp lệ: " +
                    success + ", lỗi: " + failed + ".",
                    failed > 0 ? "warning" : "success"
                );
            } else {
                previewReady = false;
                confirmButton.hidden = true;

                fileInput.value = "";
                fileName.textContent = "Chưa chọn tệp";

                notify(
                    "Import hoàn tất. Thành công: " +
                    success + ", thất bại: " + failed + ".",
                    failed > 0 ? "warning" : "success"
                );
            }
        } catch (error) {
            fileError.textContent = error.message;

            if (preview) {
                previewReady = false;
                confirmButton.hidden = true;
            }

            notify(error.message, "error");
        } finally {
            busy = false;

            previewButton.disabled =
                !validFile(fileInput.files[0]);

            confirmButton.disabled = false;
            previewButton.textContent =
                "Xem trước dữ liệu";
        }
    }
});