"use strict";

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("leadCaptureForm");
    const message = document.getElementById("leadMessage");

    if (!form || !message) {
        return;
    }

    function initializeEmbedCode() {
        const embedCode = document.getElementById("embedCode");
        const copyButton = document.getElementById("copyEmbedCode");
        const embedMessage = document.getElementById("embedMessage");
        const formIdField = document.getElementById("formId");

        if (!embedCode || !copyButton || !embedMessage || !formIdField) {
            return;
        }

        const formUrl = new URL(window.location.href);
        formUrl.search = "";
        formUrl.hash = "";
        formUrl.searchParams.set("formId", formIdField.value);

        const iframeCode =
            '<iframe src="' + formUrl.href + '"' +
            ' title="Đăng ký nhận tư vấn"' +
            ' width="100%" height="720"' +
            ' style="border:0;max-width:100%;"' +
            ' loading="lazy"></iframe>';

        embedCode.value = iframeCode;

        copyButton.addEventListener("click", async function () {
            try {
                await navigator.clipboard.writeText(embedCode.value);
                embedMessage.textContent = "Đã sao chép mã nhúng.";
            } catch (error) {
                embedCode.focus();
                embedCode.select();
                embedMessage.textContent =
                    "Không thể sao chép tự động. Hãy nhấn Ctrl+C để sao chép.";
            }
        });
    }

    initializeEmbedCode();
    const submitButton = document.getElementById("leadSubmitButton");
    const submitText = document.getElementById("leadSubmitText");

    let isSubmitting = false;

    function setSubmitState(state, text) {
        if (!submitButton || !submitText) {
            return;
        }

        isSubmitting = state === "loading";
        submitButton.disabled = isSubmitting;
        submitButton.setAttribute("aria-busy", String(isSubmitting));

        submitText.textContent = isSubmitting
            ? "Đang gửi..."
            : "Gửi yêu cầu tư vấn";

        if (state === "success") {
            message.textContent = text || "Yêu cầu đã được gửi thành công.";
            message.hidden = false;
        } else if (state === "error") {
            message.textContent = text || "Gửi yêu cầu thất bại. Vui lòng thử lại.";
            message.hidden = false;
        } else if (state === "loading") {
            message.textContent = "Hệ thống đang xử lý yêu cầu...";
            message.hidden = false;
        }
    }
    const fieldNames = [
        "fullName",
        "email",
        "phone",
        "company",
        "interest"
    ];

    const fields = {};

    fieldNames.forEach(function (name) {
        fields[name] = form.elements.namedItem(name);
    });

    function getValue(name) {
        return fields[name].value.trim();
    }

    function clearError(name) {
        const field = fields[name];
        const error = document.getElementById(name + "Error");

        field.removeAttribute("aria-invalid");
        field.removeAttribute("aria-describedby");

        if (error) {
            error.textContent = "";
            error.hidden = true;
        }
    }

    function showError(name, text) {
        const field = fields[name];
        let error = document.getElementById(name + "Error");

        if (!error) {
            error = document.createElement("div");
            error.id = name + "Error";
            error.className = "field-error";
            error.setAttribute("role", "alert");
            field.insertAdjacentElement("afterend", error);
        }

        error.textContent = text;
        error.hidden = false;

        field.setAttribute("aria-invalid", "true");
        field.setAttribute("aria-describedby", error.id);
    }

    function validateField(name) {
        const field = fields[name];
        const value = getValue(name);

        clearError(name);

        if (field.required && !value) {
            showError(name, "Vui lòng nhập thông tin bắt buộc.");
            return false;
        }

        if (!value) {
            return true;
        }

        if (field.maxLength > 0 && value.length > field.maxLength) {
            showError(name, "Nội dung vượt quá độ dài cho phép.");
            return false;
        }

        if (name === "email" && !field.checkValidity()) {
            showError(name, "Địa chỉ email không hợp lệ.");
            return false;
        }

        if (name === "phone" && !/^(0[0-9]{9}|\+84[0-9]{9})$/.test(value)) {
            showError(name, "Số điện thoại phải có dạng 0xxxxxxxxx hoặc +84xxxxxxxxx.");
            return false;
        }

        return true;
    }

    fieldNames.forEach(function (name) {
        fields[name].addEventListener("input", function () {
            message.hidden = true;

            if (fields[name].hasAttribute("aria-invalid")) {
                validateField(name);
            }
        });

        fields[name].addEventListener("blur", function () {
            validateField(name);
        });
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        if (isSubmitting) {
            return;
        }

        message.hidden = true;

        let firstInvalidField = null;

        fieldNames.forEach(function (name) {
            if (!validateField(name) && !firstInvalidField) {
                firstInvalidField = fields[name];
            }
        });

        if (firstInvalidField) {
            firstInvalidField.focus();
            return;
        }

        message.textContent =
            "Thông tin đã hợp lệ nhưng yêu cầu CHƯA được gửi. " +
            "Chức năng gửi và lưu Lead sẽ hoạt động sau khi tích hợp Backend.";

        message.hidden = false;
    });
});