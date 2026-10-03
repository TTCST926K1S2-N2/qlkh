(function () {
    "use strict";

    const contextPath =
        document.body.dataset.contextPath || "";

    const API_URL =
        contextPath + "/stages";

    let stages = [];
    let editingId = null;


    const tableBody =
        document.getElementById("stageTableBody");

    const emptyState =
        document.getElementById("emptyState");

    const searchInput =
        document.getElementById("stageSearch");

    const modal =
        document.getElementById("stageModal");

    const modalTitle =
        document.getElementById("stageModalTitle");

    const stageForm =
        document.getElementById("stageForm");

    const stageId =
        document.getElementById("stageId");

    const stageName =
        document.getElementById("stageName");

    const stageCode =
        document.getElementById("stageCode");

    const stageOrder =
        document.getElementById("stageOrder");

    const winProbability =
        document.getElementById("winProbability");

    const exitCondition =
        document.getElementById("exitCondition");

    const stageStatus =
        document.getElementById("stageStatus");

    const stageDescription =
        document.getElementById("stageDescription");

    const saveStageBtn =
        document.getElementById("saveStageBtn");

    const openAddStageBtn =
        document.getElementById("openAddStageBtn");

    const closeStageModal =
        document.getElementById("closeStageModal");

    const cancelStageBtn =
        document.getElementById("cancelStageBtn");

    const modalOverlay =
        document.getElementById("modalOverlay");

    const totalStages =
        document.getElementById("totalStages");

    const activeStages =
        document.getElementById("activeStages");

    const inactiveStages =
        document.getElementById("inactiveStages");

    const stageCountLabel =
        document.getElementById("stageCountLabel");

    const stageNameError =
        document.getElementById("stageNameError");

    const stageCodeError =
        document.getElementById("stageCodeError");

    const stageOrderError =
        document.getElementById("stageOrderError");

    const winProbabilityError =
        document.getElementById("winProbabilityError");

    const toast =
        document.getElementById("pipelineToast");


    async function apiRequest(
            url,
            options) {

        const response =
            await fetch(
                url,
                Object.assign(
                    {
                        credentials: "same-origin"
                    },
                    options || {}
                )
            );

        if (
            response.redirected &&
            response.url.includes("/login")
        ) {
            window.location.href =
                response.url;

            throw new Error(
                "Phiên đăng nhập đã hết hạn."
            );
        }

        if (response.status === 204) {
            return null;
        }

        const text =
            await response.text();

        let data = null;

        if (text) {
            try {
                data = JSON.parse(text);
            } catch (error) {

                if (response.ok) {
                    throw new Error(
                        "Phản hồi từ máy chủ không hợp lệ."
                    );
                }
            }
        }

        if (!response.ok) {

            const message =
                data && data.message
                    ? data.message
                    : "Yêu cầu thất bại (HTTP "
                        + response.status
                        + ").";

            throw new Error(message);
        }

        return data;
    }


    async function loadStages() {

        try {

            const data =
                await apiRequest(
                    API_URL,
                    {
                        method: "GET"
                    }
                );

            stages =
                Array.isArray(data)
                    ? data
                    : [];

            render();

        } catch (error) {

            console.error(error);

            showToast(
                error.message ||
                "Không thể tải danh sách giai đoạn.",
                true
            );
        }
    }


    function render() {

        const keyword =
            searchInput.value
                .trim()
                .toLowerCase();

        const ordered =
            [...stages].sort(
                function (a, b) {
                    return Number(a.stageOrder)
                        - Number(b.stageOrder);
                }
            );

        const filtered =
            ordered.filter(
                function (stage) {

                    const name =
                        String(stage.name || "")
                            .toLowerCase();

                    const code =
                        String(stage.code || "")
                            .toLowerCase();

                    const condition =
                        String(
                            stage.exitCondition || ""
                        ).toLowerCase();

                    return (
                        name.includes(keyword) ||
                        code.includes(keyword) ||
                        condition.includes(keyword)
                    );
                }
            );


        tableBody.innerHTML = "";


        filtered.forEach(
            function (stage) {

                const row =
                    document.createElement("tr");

                const probability =
                    Number(
                        stage.winProbability || 0
                    );

                row.innerHTML = `
                    <td>
                        <span class="stage-order">
                            ${escapeHtml(stage.stageOrder)}
                        </span>
                    </td>

                    <td>
                        <span class="stage-name">
                            ${escapeHtml(stage.name)}
                        </span>
                    </td>

                    <td>
                        <span class="stage-code">
                            ${escapeHtml(stage.code)}
                        </span>
                    </td>

                    <td>
                        <span class="probability-badge">
                            ${escapeHtml(formatProbability(probability))}%
                        </span>
                    </td>

                    <td>
                        <span class="condition-text"
                              title="${escapeHtml(stage.exitCondition || "")}">
                            ${escapeHtml(
                                stage.exitCondition ||
                                "Không yêu cầu"
                            )}
                        </span>
                    </td>

                    <td>
                        <span class="stage-status ${
                            stage.status === "ACTIVE"
                                ? "active"
                                : "inactive"
                        }">
                            ${
                                stage.status === "ACTIVE"
                                    ? "Đang hoạt động"
                                    : "Ngừng hoạt động"
                            }
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

                            ${
                                stage.status === "ACTIVE"
                                    ? `
                                    <button
                                        type="button"
                                        class="stage-action-btn deactivate"
                                        data-action="deactivate"
                                        data-id="${stage.id}">
                                        Ngừng
                                    </button>
                                    `
                                    : ""
                            }

                        </div>
                    </td>
                `;

                tableBody.appendChild(row);
            }
        );


        emptyState.classList.toggle(
            "visible",
            filtered.length === 0
        );


        totalStages.textContent =
            stages.length;


        activeStages.textContent =
            stages.filter(
                function (stage) {
                    return stage.status === "ACTIVE";
                }
            ).length;


        inactiveStages.textContent =
            stages.filter(
                function (stage) {
                    return stage.status === "INACTIVE";
                }
            ).length;


        stageCountLabel.textContent =
            filtered.length + " giai đoạn";
    }


    function openAddModal() {

        editingId = null;

        modalTitle.textContent =
            "Thêm giai đoạn";

        stageId.value = "";

        stageName.value = "";

        stageCode.value = "";

        stageOrder.value =
            getNextStageOrder();

        winProbability.value = "0";

        exitCondition.value = "";

        stageStatus.value =
            "ACTIVE";

        stageDescription.value = "";

        clearErrors();

        modal.classList.add("open");

        modal.setAttribute(
            "aria-hidden",
            "false"
        );

        setTimeout(
            function () {
                stageName.focus();
            },
            50
        );
    }


    function openEditModal(id) {

        const stage =
            stages.find(
                function (item) {
                    return Number(item.id)
                        === Number(id);
                }
            );

        if (!stage) {
            return;
        }

        editingId =
            Number(stage.id);

        modalTitle.textContent =
            "Chỉnh sửa giai đoạn";

        stageId.value =
            stage.id;

        stageName.value =
            stage.name || "";

        stageCode.value =
            stage.code || "";

        stageOrder.value =
            stage.stageOrder;

        winProbability.value =
            stage.winProbability;

        exitCondition.value =
            stage.exitCondition || "";

        stageStatus.value =
            stage.status || "ACTIVE";

        stageDescription.value =
            stage.description || "";

        clearErrors();

        modal.classList.add("open");

        modal.setAttribute(
            "aria-hidden",
            "false"
        );

        setTimeout(
            function () {
                stageName.focus();
            },
            50
        );
    }


    function closeModal() {

        modal.classList.remove("open");

        modal.setAttribute(
            "aria-hidden",
            "true"
        );

        editingId = null;
    }


    function clearErrors() {

        stageNameError.textContent = "";
        stageCodeError.textContent = "";
        stageOrderError.textContent = "";
        winProbabilityError.textContent = "";
    }


    function validateForm() {

        clearErrors();

        let valid = true;

        const name =
            stageName.value.trim();

        const code =
            stageCode.value
                .trim()
                .toUpperCase();

        const order =
            Number(stageOrder.value);

        const probability =
            Number(winProbability.value);


        if (!name) {

            stageNameError.textContent =
                "Vui lòng nhập tên giai đoạn.";

            valid = false;
        }


        if (!code) {

            stageCodeError.textContent =
                "Vui lòng nhập mã giai đoạn.";

            valid = false;

        } else if (
            !/^[A-Z][A-Z0-9_]*$/.test(code)
        ) {

            stageCodeError.textContent =
                "Mã chỉ gồm A-Z, 0-9, _ và phải bắt đầu bằng chữ.";

            valid = false;
        }


        const duplicateCode =
            stages.some(
                function (stage) {

                    return (
                        String(stage.code)
                            .toUpperCase()
                            === code
                        &&
                        Number(stage.id)
                            !== Number(editingId)
                    );
                }
            );

        if (duplicateCode) {

            stageCodeError.textContent =
                "Mã giai đoạn đã tồn tại.";

            valid = false;
        }


        if (
            !Number.isInteger(order) ||
            order <= 0
        ) {

            stageOrderError.textContent =
                "Thứ tự phải là số nguyên lớn hơn 0.";

            valid = false;

        } else {

            const duplicateOrder =
                stages.some(
                    function (stage) {

                        return (
                            Number(stage.stageOrder)
                                === order
                            &&
                            Number(stage.id)
                                !== Number(editingId)
                        );
                    }
                );

            if (duplicateOrder) {

                stageOrderError.textContent =
                    "Thứ tự giai đoạn đã tồn tại.";

                valid = false;
            }
        }


        if (
            Number.isNaN(probability) ||
            probability < 0 ||
            probability > 100
        ) {

            winProbabilityError.textContent =
                "Xác suất thắng phải từ 0 đến 100.";

            valid = false;
        }


        return valid;
    }


    async function saveStage(event) {

        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        const payload = {
            name:
                stageName.value.trim(),

            code:
                stageCode.value
                    .trim()
                    .toUpperCase(),

            stageOrder:
                Number(stageOrder.value),

            winProbability:
                Number(winProbability.value),

            exitCondition:
                normalizeOptional(
                    exitCondition.value
                ),

            status:
                stageStatus.value,

            description:
                normalizeOptional(
                    stageDescription.value
                )
        };


        const isEditing =
            editingId !== null;

        const url =
            isEditing
                ? API_URL + "/" + editingId
                : API_URL;

        const method =
            isEditing
                ? "PUT"
                : "POST";


        try {

            setSaving(true);

            await apiRequest(
                url,
                {
                    method: method,

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(payload)
                }
            );


            closeModal();

            await loadStages();


            showToast(
                isEditing
                    ? "Đã cập nhật giai đoạn."
                    : "Đã thêm giai đoạn mới."
            );

        } catch (error) {

            console.error(error);

            showToast(
                error.message ||
                "Không thể lưu giai đoạn.",
                true
            );

        } finally {

            setSaving(false);
        }
    }


    async function deactivateStage(id) {

        const stage =
            stages.find(
                function (item) {
                    return Number(item.id)
                        === Number(id);
                }
            );

        if (!stage) {
            return;
        }


        const confirmed =
            window.confirm(
                'Ngừng hoạt động giai đoạn "'
                + stage.name
                + '"?\n\n'
                + "Dữ liệu cũ vẫn được giữ lại."
            );

        if (!confirmed) {
            return;
        }


        try {

            await apiRequest(
                API_URL + "/" + id,
                {
                    method: "DELETE"
                }
            );

            await loadStages();

            showToast(
                "Đã ngừng hoạt động giai đoạn."
            );

        } catch (error) {

            console.error(error);

            showToast(
                error.message ||
                "Không thể ngừng hoạt động giai đoạn.",
                true
            );
        }
    }


    function setSaving(saving) {

        saveStageBtn.disabled =
            saving;

        saveStageBtn.textContent =
            saving
                ? "Đang lưu..."
                : "Lưu giai đoạn";
    }


    function getNextStageOrder() {

        if (stages.length === 0) {
            return 1;
        }

        const maxOrder =
            Math.max.apply(
                null,
                stages.map(
                    function (stage) {
                        return Number(
                            stage.stageOrder || 0
                        );
                    }
                )
            );

        return maxOrder + 1;
    }


    function normalizeOptional(value) {

        const normalized =
            String(value || "").trim();

        return normalized
            ? normalized
            : null;
    }


    function formatProbability(value) {

        if (Number.isInteger(value)) {
            return String(value);
        }

        return value.toFixed(2)
            .replace(/0+$/, "")
            .replace(/\.$/, "");
    }


    function showToast(
            message,
            isError) {

        toast.textContent =
            message;

        toast.classList.toggle(
            "error",
            Boolean(isError)
        );

        toast.classList.add("show");

        window.clearTimeout(
            showToast.timer
        );

        showToast.timer =
            window.setTimeout(
                function () {
                    toast.classList.remove(
                        "show"
                    );
                },
                3000
            );
    }


    function escapeHtml(value) {

        return String(
            value == null
                ? ""
                : value
        )
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }


    openAddStageBtn.addEventListener(
        "click",
        openAddModal
    );


    closeStageModal.addEventListener(
        "click",
        closeModal
    );


    cancelStageBtn.addEventListener(
        "click",
        closeModal
    );


    modalOverlay.addEventListener(
        "click",
        closeModal
    );


    searchInput.addEventListener(
        "input",
        render
    );


    stageForm.addEventListener(
        "submit",
        saveStage
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

            const id =
                Number(
                    button.dataset.id
                );

            const action =
                button.dataset.action;


            if (action === "edit") {
                openEditModal(id);
            }


            if (action === "deactivate") {
                deactivateStage(id);
            }
        }
    );


    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape" &&
                modal.classList.contains("open")
            ) {
                closeModal();
            }
        }
    );


    loadStages();
})();
