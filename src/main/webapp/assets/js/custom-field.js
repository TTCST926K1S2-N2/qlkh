(function () {
    "use strict";

    const modal = document.getElementById("fieldModal");
    const form = document.getElementById("fieldForm");
    const tableBody = document.getElementById("fieldTableBody");
    const searchInput = document.getElementById("searchInput");
    const recordCount = document.getElementById("recordCount");

    const openCreateModal = document.getElementById("openCreateModal");
    const emptyCreateButton = document.getElementById("emptyCreateButton");
    const closeModal = document.getElementById("closeModal");
    const cancelModal = document.getElementById("cancelModal");

    const modalTitle = document.getElementById("modalTitle");
    const editingId = document.getElementById("editingId");
    const fieldName = document.getElementById("fieldName");
    const fieldCode = document.getElementById("fieldCode");
    const fieldType = document.getElementById("fieldType");
    const fieldTarget = document.getElementById("fieldTarget");
    const fieldRequired = document.getElementById("fieldRequired");
    const formError = document.getElementById("formError");

    let fields = [
        {
            id: 1,
            name: "Ngày sinh",
            code: "ngay_sinh",
            type: "DATE",
            target: "CUSTOMER",
            required: false,
            active: true
        },
        {
            id: 2,
            name: "Mức độ ưu tiên",
            code: "muc_do_uu_tien",
            type: "SELECT",
            target: "CUSTOMER",
            required: true,
            active: true
        },
        {
            id: 3,
            name: "Số điểm tích lũy",
            code: "diem_tich_luy",
            type: "NUMBER",
            target: "CUSTOMER",
            required: false,
            active: true
        }
    ];

    let nextId = 4;

    const typeLabels = {
        TEXT: "Văn bản",
        NUMBER: "Số",
        DATE: "Ngày tháng",
        BOOLEAN: "Có / Không",
        SELECT: "Danh sách lựa chọn"
    };

    const targetLabels = {
        CUSTOMER: "Khách hàng",
        LEAD: "Khách hàng tiềm năng"
    };


    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }


    function updateRecordCount(count) {
        recordCount.textContent = count + " trường";
    }


    function renderTable() {
        const keyword = searchInput.value.trim().toLowerCase();

        const filteredFields = fields.filter(function (field) {
            return (
                field.name.toLowerCase().includes(keyword) ||
                field.code.toLowerCase().includes(keyword)
            );
        });

        updateRecordCount(filteredFields.length);

        if (filteredFields.length === 0) {
            tableBody.innerHTML = `
                <tr class="empty-row">
                    <td colspan="6">
                        <div class="empty-state">
                            <strong>Không tìm thấy trường phù hợp</strong>
                            <p>Hãy thử tìm kiếm bằng tên hoặc mã trường khác.</p>
                            <button type="button"
                                    class="btn-secondary"
                                    id="emptyCreateButton">
                                Thêm trường
                            </button>
                        </div>
                    </td>
                </tr>
            `;

            const newEmptyButton =
                document.getElementById("emptyCreateButton");

            if (newEmptyButton) {
                newEmptyButton.addEventListener("click", openCreateForm);
            }

            return;
        }


        tableBody.innerHTML = filteredFields.map(function (field) {

            const requiredText = field.required
                ? "Bắt buộc"
                : "Không bắt buộc";

            const statusClass = field.active
                ? "status-active"
                : "status-inactive";

            const statusText = field.active
                ? "Đang sử dụng"
                : "Ngừng sử dụng";

            return `
                <tr>

                    <td>
                        <span class="field-name">
                            ${escapeHtml(field.name)}
                        </span>
                    </td>

                    <td>
                        <span class="field-code">
                            ${escapeHtml(field.code)}
                        </span>
                    </td>

                    <td>
                        ${escapeHtml(typeLabels[field.type] || field.type)}
                    </td>

                    <td>
                        ${escapeHtml(targetLabels[field.target] || field.target)}
                    </td>

                    <td>
                        <span class="status ${statusClass}">
                            ${statusText}
                        </span>
                    </td>

                    <td>
                        <div class="action-group">

                            <button type="button"
                                    class="action-button"
                                    data-action="edit"
                                    data-id="${field.id}">
                                Sửa
                            </button>

                            <button type="button"
                                    class="action-button delete"
                                    data-action="delete"
                                    data-id="${field.id}">
                                Xóa
                            </button>

                        </div>
                    </td>

                </tr>
            `;
        }).join("");
    }


    function clearForm() {
        form.reset();

        editingId.value = "";

        modalTitle.textContent = "Thêm trường tùy chỉnh";

        formError.textContent = "";
        formError.classList.remove("show");
    }


    function openCreateForm() {
        clearForm();

        modal.classList.add("show");
        modal.setAttribute("aria-hidden", "false");

        setTimeout(function () {
            fieldName.focus();
        }, 50);
    }


    function openEditForm(id) {
        const field = fields.find(function (item) {
            return item.id === id;
        });

        if (!field) {
            return;
        }

        editingId.value = field.id;
        fieldName.value = field.name;
        fieldCode.value = field.code;
        fieldType.value = field.type;
        fieldTarget.value = field.target;
        fieldRequired.checked = field.required;

        modalTitle.textContent = "Chỉnh sửa trường tùy chỉnh";

        formError.textContent = "";
        formError.classList.remove("show");

        modal.classList.add("show");
        modal.setAttribute("aria-hidden", "false");

        setTimeout(function () {
            fieldName.focus();
        }, 50);
    }


    function closeFieldModal() {
        modal.classList.remove("show");
        modal.setAttribute("aria-hidden", "true");
        clearForm();
    }


    function showError(message) {
        formError.textContent = message;
        formError.classList.add("show");
    }


    function validateForm() {
        const name = fieldName.value.trim();
        const code = fieldCode.value.trim().toLowerCase();

        if (!name) {
            showError("Vui lòng nhập tên trường.");
            fieldName.focus();
            return false;
        }

        if (!code) {
            showError("Vui lòng nhập mã trường.");
            fieldCode.focus();
            return false;
        }

        if (!/^[a-z0-9_]+$/.test(code)) {
            showError(
                "Mã trường chỉ được sử dụng chữ thường, số và dấu gạch dưới."
            );
            fieldCode.focus();
            return false;
        }

        const currentId = editingId.value
            ? Number(editingId.value)
            : null;

        const duplicated = fields.some(function (field) {
            return (
                field.code.toLowerCase() === code &&
                field.id !== currentId
            );
        });

        if (duplicated) {
            showError("Mã trường này đã tồn tại.");
            fieldCode.focus();
            return false;
        }

        if (!fieldType.value) {
            showError("Vui lòng chọn kiểu dữ liệu.");
            fieldType.focus();
            return false;
        }

        if (!fieldTarget.value) {
            showError("Vui lòng chọn đối tượng áp dụng.");
            fieldTarget.focus();
            return false;
        }

        formError.textContent = "";
        formError.classList.remove("show");

        return true;
    }


    function saveField(event) {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        const name = fieldName.value.trim();
        const code = fieldCode.value.trim().toLowerCase();
        const type = fieldType.value;
        const target = fieldTarget.value;
        const required = fieldRequired.checked;

        const currentId = editingId.value
            ? Number(editingId.value)
            : null;


        if (currentId) {

            const field = fields.find(function (item) {
                return item.id === currentId;
            });

            if (field) {
                field.name = name;
                field.code = code;
                field.type = type;
                field.target = target;
                field.required = required;
            }

        } else {

            fields.push({
                id: nextId++,
                name: name,
                code: code,
                type: type,
                target: target,
                required: required,
                active: true
            });

        }


        renderTable();
        closeFieldModal();
    }


    function deleteField(id) {
        const field = fields.find(function (item) {
            return item.id === id;
        });

        if (!field) {
            return;
        }

        const confirmed = window.confirm(
            'Bạn có chắc muốn xóa trường "' +
            field.name +
            '" không?'
        );

        if (!confirmed) {
            return;
        }

        fields = fields.filter(function (item) {
            return item.id !== id;
        });

        renderTable();
    }


    openCreateModal.addEventListener("click", openCreateForm);

    if (emptyCreateButton) {
        emptyCreateButton.addEventListener(
            "click",
            openCreateForm
        );
    }


    closeModal.addEventListener(
        "click",
        closeFieldModal
    );


    cancelModal.addEventListener(
        "click",
        closeFieldModal
    );


    modal.addEventListener("click", function (event) {
        if (event.target === modal) {
            closeFieldModal();
        }
    });


    form.addEventListener(
        "submit",
        saveField
    );


    searchInput.addEventListener(
        "input",
        renderTable
    );


    tableBody.addEventListener("click", function (event) {

        const button = event.target.closest(
            "[data-action]"
        );

        if (!button) {
            return;
        }

        const action = button.dataset.action;
        const id = Number(button.dataset.id);

        if (action === "edit") {
            openEditForm(id);
        }

        if (action === "delete") {
            deleteField(id);
        }
    });


    document.addEventListener("keydown", function (event) {

        if (
            event.key === "Escape" &&
            modal.classList.contains("show")
        ) {
            closeFieldModal();
        }

    });


    renderTable();

})();
