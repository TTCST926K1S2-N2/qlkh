(function () {

    "use strict";

    const API =
        (window.contextPath || "") + "/custom-fields";


    const modal =
        document.getElementById("fieldModal");

    const form =
        document.getElementById("fieldForm");

    const tableBody =
        document.getElementById("fieldTableBody");

    const searchInput =
        document.getElementById("searchInput");

    const recordCount =
        document.getElementById("recordCount");

    const openCreateModal =
        document.getElementById("openCreateModal");

    const closeModal =
        document.getElementById("closeModal");

    const cancelModal =
        document.getElementById("cancelModal");

    const modalTitle =
        document.getElementById("modalTitle");

    const editingId =
        document.getElementById("editingId");

    const fieldName =
        document.getElementById("fieldName");

    const fieldCode =
        document.getElementById("fieldCode");

    const fieldType =
        document.getElementById("fieldType");

    const fieldTarget =
        document.getElementById("fieldTarget");

    const fieldRequired =
        document.getElementById("fieldRequired");

    const fieldConfig =
        document.getElementById("fieldConfig");

    const selectConfigGroup =
        document.getElementById("selectConfigGroup");

    const displayOrder =
        document.getElementById("displayOrder");

    const fieldStatus =
        document.getElementById("fieldStatus");

    const formError =
        document.getElementById("formError");

    const pageMessage =
        document.getElementById("pageMessage");

    const saveFieldButton =
        document.getElementById("saveField");


    let fields = [];


    const typeLabels = {
        TEXT: "Văn bản",
        NUMBER: "Số",
        DATE: "Ngày",
        BOOLEAN: "Có / Không",
        SELECT: "Danh sách lựa chọn"
    };


    const targetLabels = {
        CUSTOMER: "Khách hàng",
        OPPORTUNITY: "Cơ hội bán hàng"
    };


    function escapeHtml(value) {

        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }


    function showPageMessage(message, isError) {

        pageMessage.hidden = false;

        pageMessage.textContent = message;

        pageMessage.classList.toggle(
            "error",
            Boolean(isError)
        );

        pageMessage.classList.toggle(
            "success",
            !isError
        );
    }


    function hidePageMessage() {

        pageMessage.hidden = true;

        pageMessage.textContent = "";

        pageMessage.classList.remove(
            "error",
            "success"
        );
    }


    function showFormError(message) {

        formError.textContent =
            message || "Có lỗi xảy ra.";

        formError.classList.add("show");
    }


    function clearFormError() {

        formError.textContent = "";

        formError.classList.remove("show");
    }


    async function parseResponse(response) {

        if (response.status === 204) {
            return null;
        }

        const text = await response.text();

        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return {
                message: text
            };
        }
    }


    async function request(url, options) {

        const response =
            await fetch(url, options || {});

        const data =
            await parseResponse(response);

        if (!response.ok) {

            const message =
                data && data.message
                    ? data.message
                    : "Không thể xử lý yêu cầu.";

            throw new Error(message);
        }

        return data;
    }


    async function loadFields() {

        tableBody.innerHTML = `
            <tr class="empty-row">
                <td colspan="7">
                    <div class="empty-state">
                        <strong>Đang tải dữ liệu...</strong>
                    </div>
                </td>
            </tr>
        `;

        try {

            const result =
                await request(API);

            fields =
                Array.isArray(result)
                    ? result
                    : [];

            renderTable();

        } catch (error) {

            fields = [];

            tableBody.innerHTML = `
                <tr class="empty-row">
                    <td colspan="7">
                        <div class="empty-state">
                            <strong>Không thể tải dữ liệu</strong>
                            <p>${escapeHtml(error.message)}</p>
                        </div>
                    </td>
                </tr>
            `;

            updateRecordCount(0);
        }
    }


    function updateRecordCount(count) {

        recordCount.textContent =
            count + " trường";
    }


    function renderTable() {

        const keyword =
            searchInput.value
                .trim()
                .toLowerCase();

        const filteredFields =
            fields.filter(function (field) {

                return (
                    String(field.fieldName || "")
                        .toLowerCase()
                        .includes(keyword)
                    ||
                    String(field.fieldKey || "")
                        .toLowerCase()
                        .includes(keyword)
                );
            });


        updateRecordCount(
            filteredFields.length
        );


        if (filteredFields.length === 0) {

            tableBody.innerHTML = `
                <tr class="empty-row">
                    <td colspan="7">
                        <div class="empty-state">
                            <strong>Không tìm thấy trường phù hợp</strong>
                            <p>
                                Hãy thử tìm kiếm bằng tên hoặc mã trường khác.
                            </p>
                        </div>
                    </td>
                </tr>
            `;

            return;
        }


        tableBody.innerHTML =
            filteredFields
                .map(function (field) {

                    const statusClass =
                        field.status
                            ? "status-active"
                            : "status-inactive";

                    const statusText =
                        field.status
                            ? "Đang sử dụng"
                            : "Ngừng sử dụng";

                    const requiredText =
                        field.required
                            ? "Có"
                            : "Không";

                    return `
                        <tr>

                            <td>
                                <span class="field-name">
                                    ${escapeHtml(field.fieldName)}
                                </span>
                            </td>

                            <td>
                                <span class="field-code">
                                    ${escapeHtml(field.fieldKey)}
                                </span>
                            </td>

                            <td>
                                ${escapeHtml(
                                    typeLabels[field.fieldType]
                                    || field.fieldType
                                )}
                            </td>

                            <td>
                                ${escapeHtml(
                                    targetLabels[field.entityType]
                                    || field.entityType
                                )}
                            </td>

                            <td>
                                ${requiredText}
                            </td>

                            <td>
                                <span class="status ${statusClass}">
                                    ${statusText}
                                </span>
                            </td>

                            <td>

                                <div class="action-group">

                                    <button
                                        type="button"
                                        class="action-button"
                                        data-action="edit"
                                        data-id="${field.id}">

                                        Sửa

                                    </button>

                                    <button
                                        type="button"
                                        class="action-button delete"
                                        data-action="delete"
                                        data-id="${field.id}">

                                        Xóa

                                    </button>

                                </div>

                            </td>

                        </tr>
                    `;
                })
                .join("");
    }


    function updateConfigVisibility() {

        const isSelect =
            fieldType.value === "SELECT";

        selectConfigGroup.hidden =
            !isSelect;

        fieldConfig.required =
            isSelect;

        if (!isSelect) {
            fieldConfig.value = "";
        }
    }


    function clearForm() {

        form.reset();

        editingId.value = "";

        displayOrder.value = "0";

        fieldStatus.value = "true";

        modalTitle.textContent =
            "Thêm trường tùy chỉnh";

        clearFormError();

        updateConfigVisibility();
    }


    function openCreateForm() {

        clearForm();

        modal.classList.add("show");

        modal.setAttribute(
            "aria-hidden",
            "false"
        );

        setTimeout(function () {
            fieldName.focus();
        }, 50);
    }


    function openEditForm(id) {

        const field =
            fields.find(function (item) {
                return Number(item.id) === Number(id);
            });

        if (!field) {
            return;
        }

        editingId.value =
            field.id;

        fieldName.value =
            field.fieldName || "";

        fieldCode.value =
            field.fieldKey || "";

        fieldType.value =
            field.fieldType || "";

        fieldTarget.value =
            field.entityType || "";

        fieldRequired.checked =
            Boolean(field.required);

        fieldConfig.value =
            field.config || "";

        displayOrder.value =
            Number(field.displayOrder || 0);

        fieldStatus.value =
            String(Boolean(field.status));

        modalTitle.textContent =
            "Chỉnh sửa trường tùy chỉnh";

        clearFormError();

        updateConfigVisibility();

        modal.classList.add("show");

        modal.setAttribute(
            "aria-hidden",
            "false"
        );
    }


    function closeFieldModal() {

        modal.classList.remove("show");

        modal.setAttribute(
            "aria-hidden",
            "true"
        );

        clearForm();
    }


    function validateForm() {

        const name =
            fieldName.value.trim();

        const code =
            fieldCode.value.trim();

        if (!name) {
            showFormError(
                "Vui lòng nhập tên trường."
            );

            fieldName.focus();

            return false;
        }

        if (!code) {
            showFormError(
                "Vui lòng nhập mã trường."
            );

            fieldCode.focus();

            return false;
        }

        if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(code)) {

            showFormError(
                "Mã trường phải bắt đầu bằng chữ và chỉ gồm chữ, số, dấu gạch dưới."
            );

            fieldCode.focus();

            return false;
        }

        if (!fieldType.value) {

            showFormError(
                "Vui lòng chọn kiểu dữ liệu."
            );

            fieldType.focus();

            return false;
        }

        if (!fieldTarget.value) {

            showFormError(
                "Vui lòng chọn đối tượng áp dụng."
            );

            fieldTarget.focus();

            return false;
        }

        if (
            fieldType.value === "SELECT"
            &&
            !fieldConfig.value.trim()
        ) {

            showFormError(
                "Vui lòng nhập danh sách lựa chọn."
            );

            fieldConfig.focus();

            return false;
        }

        clearFormError();

        return true;
    }


    function buildPayload() {

        return new URLSearchParams({
            entityType:
                fieldTarget.value,

            fieldKey:
                fieldCode.value.trim(),

            fieldName:
                fieldName.value.trim(),

            fieldType:
                fieldType.value,

            required:
                String(fieldRequired.checked),

            status:
                fieldStatus.value,

            displayOrder:
                String(
                    Number(displayOrder.value || 0)
                ),

            config:
                fieldType.value === "SELECT"
                    ? fieldConfig.value.trim()
                    : ""
        });
    }


    async function saveField(event) {

        event.preventDefault();

        hidePageMessage();

        if (!validateForm()) {
            return;
        }

        const currentId =
            editingId.value
                ? Number(editingId.value)
                : null;

        const payload =
            buildPayload();

        saveFieldButton.disabled = true;

        saveFieldButton.textContent =
            "Đang lưu...";

        try {

            if (currentId) {

                await request(
                    API + "/" + currentId,
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type":
                                "application/x-www-form-urlencoded;charset=UTF-8"
                        },
                        body: payload.toString()
                    }
                );

                showPageMessage(
                    "Cập nhật trường tùy chỉnh thành công.",
                    false
                );

            } else {

                await request(
                    API,
                    {
                        method: "POST",
                        headers: {
                            "Content-Type":
                                "application/x-www-form-urlencoded;charset=UTF-8"
                        },
                        body: payload.toString()
                    }
                );

                showPageMessage(
                    "Thêm trường tùy chỉnh thành công.",
                    false
                );
            }

            closeFieldModal();

            await loadFields();

        } catch (error) {

            showFormError(
                error.message
            );

        } finally {

            saveFieldButton.disabled = false;

            saveFieldButton.textContent =
                "Lưu trường";
        }
    }


    async function deleteField(id) {

        const field =
            fields.find(function (item) {
                return Number(item.id) === Number(id);
            });

        if (!field) {
            return;
        }

        const confirmed =
            window.confirm(
                'Bạn có chắc muốn xóa trường "' +
                field.fieldName +
                '" không?'
            );

        if (!confirmed) {
            return;
        }

        hidePageMessage();

        try {

            await request(
                API + "/" + id,
                {
                    method: "DELETE"
                }
            );

            showPageMessage(
                "Xóa trường tùy chỉnh thành công.",
                false
            );

            await loadFields();

        } catch (error) {

            showPageMessage(
                error.message,
                true
            );
        }
    }


    openCreateModal.addEventListener(
        "click",
        openCreateForm
    );


    closeModal.addEventListener(
        "click",
        closeFieldModal
    );


    cancelModal.addEventListener(
        "click",
        closeFieldModal
    );


    fieldType.addEventListener(
        "change",
        updateConfigVisibility
    );


    modal.addEventListener(
        "click",
        function (event) {

            if (event.target === modal) {
                closeFieldModal();
            }
        }
    );


    form.addEventListener(
        "submit",
        saveField
    );


    searchInput.addEventListener(
        "input",
        renderTable
    );


    tableBody.addEventListener(
        "click",
        function (event) {

            const button =
                event.target.closest(
                    "[data-action]"
                );

            if (!button) {
                return;
            }

            const action =
                button.dataset.action;

            const id =
                Number(button.dataset.id);

            if (action === "edit") {
                openEditForm(id);
            }

            if (action === "delete") {
                deleteField(id);
            }
        }
    );


    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape"
                &&
                modal.classList.contains("show")
            ) {
                closeFieldModal();
            }
        }
    );


    updateConfigVisibility();

    loadFields();

})();
