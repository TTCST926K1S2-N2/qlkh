"use strict";

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("leadCaptureForm");
    const message = document.getElementById("leadMessage");

    if (!form || !message) {
        return;
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