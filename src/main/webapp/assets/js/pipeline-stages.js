(function () {
    "use strict";

    const stages = [
        {
            id: 1,
            name: "Tiềm năng",
            code: "POTENTIAL",
            status: "ACTIVE",
            description: "Khách hàng mới được ghi nhận."
        },
        {
            id: 2,
            name: "Đang liên hệ",
            code: "CONTACTING",
            status: "ACTIVE",
            description: "Đang thực hiện liên hệ với khách hàng."
        },
        {
            id: 3,
            name: "Đã tư vấn",
            code: "CONSULTED",
            status: "ACTIVE",
            description: "Khách hàng đã được tư vấn."
        },
        {
            id: 4,
            name: "Đề xuất",
            code: "PROPOSAL",
            status: "ACTIVE",
            description: "Đã gửi đề xuất cho khách hàng."
        },
        {
            id: 5,
            name: "Thành công",
            code: "WON",
            status: "ACTIVE",
            description: "Khách hàng đã hoàn tất giao dịch."
        },
        {
            id: 6,
            name: "Không thành công",
            code: "LOST",
            status: "INACTIVE",
            description: "Cơ hội không tiếp tục."
        }
    ];

    let editingId = null;

    const tableBody = document.getElementById("stageTableBody");
    const emptyState = document.getElementById("emptyState");
    const searchInput = document.getElementById("stageSearch");

    const modal = document.getElementById("stageModal");
    const modalTitle = document.getElementById("stageModalTitle");
    const stageForm = document.getElementById("stageForm");

    const stageId = document.getElementById("stageId");
    const stageName = document.getElementById("stageName");
    const stageCode = document.getElementById("stageCode");
    const stageStatus = document.getElementById("stageStatus");
    const stageDescription = document.getElementById("stageDescription");

    const openAddStageBtn = document.getElementById("openAddStageBtn");
    const closeStageModal = document.getElementById("closeStageModal");
    const cancelStageBtn = document.getElementById("cancelStageBtn");
    const modalOverlay = document.getElementById("modalOverlay");

    const totalStages = document.getElementById("totalStages");
    const activeStages = document.getElementById("activeStages");
    const inactiveStages = document.getElementById("inactiveStages");
    const stageCountLabel = document.getElementById("stageCountLabel");

    const stageNameError = document.getElementById("stageNameError");
    const stageCodeError = document.getElementById("stageCodeError");

    const toast = document.getElementById("pipelineToast");

    function render() {
        const keyword = searchInput.value.trim().toLowerCase();

        const filtered = stages.filter(function (stage) {
            return (
                stage.name.toLowerCase().includes(keyword) ||
                stage.code.toLowerCase().includes(keyword)
            );
        });

        tableBody.innerHTML = "";

        filtered.forEach(function (stage, index) {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>
                    <span class="stage-order">${index + 1}</span>
                </td>

                <td>
                    <span class="stage-name">${escapeHtml(stage.name)}</span>
                </td>

                <td>
                    <span class="stage-code">${escapeHtml(stage.code)}</span>
                </td>

                <td>
                    <span class="stage-status ${stage.status === "ACTIVE" ? "active" : "inactive"}">
                        ${stage.status === "ACTIVE" ? "Đang hoạt động" : "Không hoạt động"}
                    </span>
                </td>

                <td>
                    <div class="stage-actions">
                        <button
                            type="button"
                            class="stage-action-btn"
                            data-action="edit"
                            data-id="${stage.id}">
                            Sửa
                        </button>

                        <button
                            type="button"
                            class="stage-action-btn delete"
                            data-action="delete"
                            data-id="${stage.id}">
                            Xóa
                        </button>
                    </div>
                </td>
            `;

            tableBody.appendChild(row);
        });

        emptyState.classList.toggle("visible", filtered.length === 0);

        totalStages.textContent = stages.length;
        activeStages.textContent =
            stages.filter(function (stage) {
                return stage.status === "ACTIVE";
            }).length;

        inactiveStages.textContent =
            stages.filter(function (stage) {
                return stage.status === "INACTIVE";
            }).length;

        stageCountLabel.textContent =
            filtered.length + " giai đoạn";
    }

    function openAddModal() {
        editingId = null;

        modalTitle.textContent = "Thêm giai đoạn";

        stageId.value = "";
        stageName.value = "";
        stageCode.value = "";
        stageStatus.value = "ACTIVE";
        stageDescription.value = "";

        clearErrors();

        modal.classList.add("open");
        modal.setAttribute("aria-hidden", "false");

        setTimeout(function () {
            stageName.focus();
        }, 50);
    }

    function openEditModal(id) {
        const stage = stages.find(function (item) {
            return item.id === id;
        });

        if (!stage) {
            return;
        }

        editingId = id;

        modalTitle.textContent = "Chỉnh sửa giai đoạn";

        stageId.value = stage.id;
        stageName.value = stage.name;
        stageCode.value = stage.code;
        stageStatus.value = stage.status;
        stageDescription.value = stage.description || "";

        clearErrors();

        modal.classList.add("open");
        modal.setAttribute("aria-hidden", "false");

        setTimeout(function () {
            stageName.focus();
        }, 50);
    }

    function closeModal() {
        modal.classList.remove("open");
        modal.setAttribute("aria-hidden", "true");
    }

    function validateForm() {
        clearErrors();

        let valid = true;

        const name = stageName.value.trim();
        const code = stageCode.value.trim().toUpperCase();

        if (!name) {
            stageNameError.textContent = "Vui lòng nhập tên giai đoạn.";
            valid = false;
        }

        if (!code) {
            stageCodeError.textContent = "Vui lòng nhập mã giai đoạn.";
            valid = false;
        }

        const duplicate = stages.some(function (stage) {
            return (
                stage.code.toLowerCase() === code.toLowerCase() &&
                stage.id !== editingId
            );
        });

        if (duplicate) {
            stageCodeError.textContent = "Mã giai đoạn đã tồn tại.";
            valid = false;
        }

        return valid;
    }

    function saveStage(event) {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        const name = stageName.value.trim();
        const code = stageCode.value.trim().toUpperCase();
        const status = stageStatus.value;
        const description = stageDescription.value.trim();

        if (editingId === null) {
            const nextId =
                stages.length > 0
                    ? Math.max.apply(
                          null,
                          stages.map(function (item) {
                              return item.id;
                          })
                      ) + 1
                    : 1;

            stages.push({
                id: nextId,
                name: name,
                code: code,
                status: status,
                description: description
            });

            showToast("Đã thêm giai đoạn mới.");
        } else {
            const stage = stages.find(function (item) {
                return item.id === editingId;
            });

            if (stage) {
                stage.name = name;
                stage.code = code;
                stage.status = status;
                stage.description = description;
            }

            showToast("Đã cập nhật giai đoạn.");
        }

        closeModal();
        render();
    }

    function deleteStage(id) {
        const stage = stages.find(function (item) {
            return item.id === id;
        });

        if (!stage) {
            return;
        }

        const confirmed = window.confirm(
            'Bạn có chắc muốn xóa giai đoạn "' + stage.name + '" không?'
        );

        if (!confirmed) {
            return;
        }

        const index = stages.findIndex(function (item) {
            return item.id === id;
        });

        if (index !== -1) {
            stages.splice(index, 1);
            render();
            showToast("Đã xóa giai đoạn.");
        }
    }

    function clearErrors() {
        stageNameError.textContent = "";
        stageCodeError.textContent = "";
    }

    function showToast(message) {
        toast.textContent = message;
        toast.classList.add("show");

        window.clearTimeout(showToast.timer);

        showToast.timer = window.setTimeout(function () {
            toast.classList.remove("show");
        }, 2400);
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    openAddStageBtn.addEventListener("click", openAddModal);
    closeStageModal.addEventListener("click", closeModal);
    cancelStageBtn.addEventListener("click", closeModal);
    modalOverlay.addEventListener("click", closeModal);

    searchInput.addEventListener("input", render);

    stageForm.addEventListener("submit", saveStage);

    tableBody.addEventListener("click", function (event) {
        const button = event.target.closest("[data-action]");

        if (!button) {
            return;
        }

        const id = Number(button.dataset.id);
        const action = button.dataset.action;

        if (action === "edit") {
            openEditModal(id);
        }

        if (action === "delete") {
            deleteStage(id);
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && modal.classList.contains("open")) {
            closeModal();
        }
    });

    render();
})();
