
(() => {
    "use strict";

    const contextPath = window.contextPath || "";
    const apiUrl = `${contextPath}/api/v1/leads`;
    const $ = id => document.getElementById(id);

    const el = {
        keyword: $("keyword"),
        status: $("status"),
        source: $("source"),
        assignedTo: $("assignedTo"),
        createdFrom: $("createdFrom"),
        createdTo: $("createdTo"),
        sortBy: $("sortBy"),
        sortDirection: $("sortDirection"),
        pageSize: $("pageSize"),
        tableBody: $("leadTableBody"),
        totalCount: $("totalCount"),
        pageInfo: $("pageInfo"),
        prevBtn: $("prevBtn"),
        nextBtn: $("nextBtn"),
        searchBtn: $("searchBtn"),
        resetBtn: $("resetBtn"),
        refreshBtn: $("refreshBtn"),
        saveFilterBtn: $("saveFilterBtn"),
        savedFilters: $("savedFilters"),
        message: $("message"),
        dialog: $("leadDialog"),
        leadDetail: $("leadDetail"),
        closeDialogBtn: $("closeDialogBtn")
    };

    const state = {
        page: 1,
        size: 10,
        totalPages: 1,
        totalItems: 0,
        leads: [],
        loading: false
    };

    const statusLabels = {
        NEW: "Mới",
        CONTACTED: "Đã liên hệ",
        QUALIFIED: "Tiềm năng",
        CONVERTED: "Đã chuyển đổi",
        LOST: "Không thành công"
    };

    const sourceLabels = {
        WEBSITE: "Website",
        FACEBOOK: "Facebook",
        REFERRAL: "Giới thiệu",
        OTHER: "Khác"
    };

    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[char]);
    }

    function showMessage(message, type = "error") {
        el.message.textContent = message;
        el.message.className = `message message-${type}`;
        el.message.hidden = false;
    }

    function hideMessage() {
        el.message.hidden = true;
        el.message.textContent = "";
    }

    function getFilters() {
        return {
            keyword: el.keyword.value.trim(),
            status: el.status.value,
            source: el.source.value,
            assignedTo: el.assignedTo.value.trim(),
            createdFrom: el.createdFrom.value,
            createdTo: el.createdTo.value,
            sortBy: el.sortBy.value,
            sortDirection: el.sortDirection.value
        };
    }

    function validateFilters() {
        [
            el.assignedTo,
            el.createdFrom,
            el.createdTo
        ].forEach(input => input.removeAttribute("aria-invalid"));

        if (
            el.assignedTo.value &&
            (!Number.isInteger(Number(el.assignedTo.value)) ||
                Number(el.assignedTo.value) < 1)
        ) {
            el.assignedTo.setAttribute("aria-invalid", "true");
            showMessage("ID người phụ trách phải là số nguyên lớn hơn 0.");
            el.assignedTo.focus();
            return false;
        }

        if (
            el.createdFrom.value &&
            el.createdTo.value &&
            el.createdFrom.value > el.createdTo.value
        ) {
            el.createdFrom.setAttribute("aria-invalid", "true");
            el.createdTo.setAttribute("aria-invalid", "true");
            showMessage("Ngày bắt đầu không được sau ngày kết thúc.");
            el.createdFrom.focus();
            return false;
        }

        return true;
    }

    function buildUrl() {
        const params = new URLSearchParams();

        params.set("page", String(state.page));
        params.set("size", String(state.size));

        Object.entries(getFilters()).forEach(([key, value]) => {
            if (value !== "") {
                params.set(key, value);
            }
        });

        return `${apiUrl}?${params.toString()}`;
    }

    function getLeadId(lead) {
        return lead.id ?? lead.leadId ?? "";
    }

    function getLeadName(lead) {
        return lead.name || lead.fullName ||
            lead.leadName || "Chưa có tên";
    }

    function renderStatus(status) {
        const css = {
            NEW: "status-new",
            CONTACTED: "status-contacted",
            QUALIFIED: "status-qualified",
            CONVERTED: "status-converted",
            LOST: "status-lost"
        }[status] || "status-new";

        return `<span class="status-badge ${css}">
            ${escapeHtml(statusLabels[status] || status || "Chưa xác định")}
        </span>`;
    }

    function renderSla(lead) {
        const status = String(
            lead.slaStatus || lead.sla_status || ""
        ).toUpperCase();

        const remaining = lead.slaRemaining ?? lead.sla_remaining;

        if (!status && remaining == null) {
            return '<span class="sla-badge sla-unknown">Chưa có dữ liệu</span>';
        }

        let css = "sla-ok";
        let label = "Trong hạn";

        if (["OVERDUE", "BREACHED", "EXPIRED"].includes(status)) {
            css = "sla-overdue";
            label = "Quá hạn";
        } else if (["WARNING", "NEAR_DEADLINE"].includes(status)) {
            css = "sla-warning";
            label = "Sắp đến hạn";
        } else if (status && !["OK", "ON_TIME", "WITHIN_SLA"].includes(status)) {
            label = status;
        }

        if (remaining != null) {
            label += ` (${remaining})`;
        }

        return `<span class="sla-badge ${css}">${escapeHtml(label)}</span>`;
    }

    function renderLeadRow(lead) {
        const id = getLeadId(lead);
        const source = lead.source || "";
        const status = lead.status || "NEW";

        return `
            <tr>
                <td>
                    <div class="lead-name">${escapeHtml(getLeadName(lead))}</div>
                    <div class="lead-company">${escapeHtml(lead.companyName || lead.company || "")}</div>
                </td>
                <td>
                    <div>${escapeHtml(lead.email || "Chưa có email")}</div>
                    <div class="secondary-text">${escapeHtml(lead.phone || lead.phoneNumber || "Chưa có số điện thoại")}</div>
                </td>
                <td>${escapeHtml(sourceLabels[source] || source || "Khác")}</td>
                <td>${renderStatus(status)}</td>
                <td>${escapeHtml(lead.assignedToName || lead.assignedName || lead.assignedTo || "Chưa phân công")}</td>
                <td>${renderSla(lead)}</td>
                <td>
                    <button type="button" class="btn btn-secondary view-btn"
                        data-action="view" data-id="${escapeHtml(id)}">
                        Chi tiết
                    </button>
                </td>
            </tr>`;
    }

    function renderTable() {
        if (state.loading) {
            el.tableBody.innerHTML =
                '<tr><td colspan="7" class="empty-cell">Đang tải dữ liệu...</td></tr>';
        } else if (!state.leads.length) {
            el.tableBody.innerHTML =
                '<tr><td colspan="7" class="empty-cell">Không tìm thấy Lead phù hợp.</td></tr>';
        } else {
            el.tableBody.innerHTML = state.leads.map(renderLeadRow).join("");
        }

        el.totalCount.textContent = `${state.totalItems} kết quả`;
        el.pageInfo.textContent = `Trang ${state.page} / ${state.totalPages}`;
        el.prevBtn.disabled = state.loading || state.page <= 1;
        el.nextBtn.disabled = state.loading || state.page >= state.totalPages;
        el.searchBtn.disabled = state.loading;
        el.refreshBtn.disabled = state.loading;
    }

    async function loadLeads() {
        if (!validateFilters()) return;

        hideMessage();
        state.loading = true;
        renderTable();

        try {
            const response = await fetch(buildUrl(), {
                method: "GET",
                headers: { Accept: "application/json" },
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error(
                    response.status === 401 || response.status === 403
                        ? "Bạn cần đăng nhập hoặc không có quyền xem danh sách Lead."
                        : `Không thể tải danh sách Lead (HTTP ${response.status}).`
                );
            }

            const result = await response.json();

            if (result.success === false) {
                throw new Error(result.message || "API trả về lỗi.");
            }

            const data = Array.isArray(result)
                ? result
                : Array.isArray(result.data)
                    ? result.data
                    : Array.isArray(result.leads)
                        ? result.leads
                        : [];

            const pagination = result.pagination || {};

            state.leads = data;
            state.totalItems = Number(
                pagination.totalItems ?? pagination.total ?? result.totalItems ?? data.length
            );
            state.totalPages = Math.max(1, Number(
                pagination.totalPages ??
                result.totalPages ??
                Math.ceil(state.totalItems / state.size)
            ));
            state.page = Number(pagination.page ?? state.page);
            state.size = Number(pagination.size ?? state.size);

        } catch (error) {
            state.leads = [];
            state.totalItems = 0;
            state.totalPages = 1;

            showMessage(
                `${error.message || "Đã xảy ra lỗi khi tải dữ liệu."} Kiểm tra máy chủ, API và quyền đăng nhập.`
            );
        } finally {
            state.loading = false;
            renderTable();
        }
    }

    function resetFilters() {
        el.keyword.value = "";
        el.status.value = "";
        el.source.value = "";
        el.assignedTo.value = "";
        el.createdFrom.value = "";
        el.createdTo.value = "";
        el.sortBy.value = "createdAt";
        el.sortDirection.value = "desc";

        state.page = 1;
        loadLeads();
    }

    function readSavedFilters() {
        try {
            return JSON.parse(localStorage.getItem("leadSavedFilters") || "[]");
        } catch {
            return [];
        }
    }

    function renderSavedFilters() {
        el.savedFilters.innerHTML =
            '<option value="">Chọn bộ lọc đã lưu</option>' +
            readSavedFilters().map((item, index) =>
                `<option value="${index}">${escapeHtml(item.name)}</option>`
            ).join("");
    }

    function saveFilter() {
        const name = window.prompt("Nhập tên bộ lọc:");
        if (!name || !name.trim()) return;

        const filters = readSavedFilters();
        filters.push({ name: name.trim(), values: getFilters() });

        try {
            localStorage.setItem("leadSavedFilters", JSON.stringify(filters));
            renderSavedFilters();
            showMessage("Đã lưu bộ lọc trên trình duyệt.", "success");
        } catch {
            showMessage("Không thể lưu bộ lọc trên trình duyệt.");
        }
    }

    function applySavedFilter() {
        const index = el.savedFilters.value;
        if (index === "") return;

        const saved = readSavedFilters()[Number(index)];
        el.savedFilters.value = "";

        if (!saved || !saved.values) return;

        const values = saved.values;
        el.keyword.value = values.keyword || "";
        el.status.value = values.status || "";
        el.source.value = values.source || "";
        el.assignedTo.value = values.assignedTo || "";
        el.createdFrom.value = values.createdFrom || "";
        el.createdTo.value = values.createdTo || "";
        el.sortBy.value = values.sortBy || "createdAt";
        el.sortDirection.value = values.sortDirection || "desc";

        state.page = 1;
        loadLeads();
    }

    function openLeadDetail(lead) {
        const fields = [
            ["Mã Lead", getLeadId(lead)],
            ["Họ tên", getLeadName(lead)],
            ["Công ty", lead.companyName || lead.company],
            ["Email", lead.email],
            ["Số điện thoại", lead.phone || lead.phoneNumber],
            ["Nguồn", sourceLabels[lead.source] || lead.source],
            ["Trạng thái", statusLabels[lead.status] || lead.status],
            ["Người phụ trách", lead.assignedToName || lead.assignedName || lead.assignedTo],
            ["Ngày tạo", lead.createdAt || lead.created_at],
            ["SLA", lead.slaStatus || lead.sla_status]
        ];

        el.leadDetail.innerHTML = fields
            .filter(([, value]) => value != null && value !== "")
            .map(([label, value]) => `
                <div class="detail-row">
                    <div class="detail-label">${escapeHtml(label)}</div>
                    <div class="detail-value">${escapeHtml(value)}</div>
                </div>
            `).join("");

        if (typeof el.dialog.showModal === "function") {
            el.dialog.showModal();
        } else {
            showMessage("Trình duyệt không hỗ trợ cửa sổ chi tiết.");
        }
    }

    el.searchBtn.addEventListener("click", () => {
        state.page = 1;
        loadLeads();
    });

    el.resetBtn.addEventListener("click", resetFilters);
    el.refreshBtn.addEventListener("click", loadLeads);
    el.saveFilterBtn.addEventListener("click", saveFilter);
    el.savedFilters.addEventListener("change", applySavedFilter);

    el.pageSize.addEventListener("change", () => {
        state.size = Number(el.pageSize.value) || 10;
        state.page = 1;
        loadLeads();
    });

    el.prevBtn.addEventListener("click", () => {
        if (state.page > 1 && !state.loading) {
            state.page--;
            loadLeads();
        }
    });

    el.nextBtn.addEventListener("click", () => {
        if (state.page < state.totalPages && !state.loading) {
            state.page++;
            loadLeads();
        }
    });

    el.keyword.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            state.page = 1;
            loadLeads();
        }
    });

    el.tableBody.addEventListener("click", event => {
        const button = event.target.closest('[data-action="view"]');
        if (!button) return;

        const lead = state.leads.find(
            item => String(getLeadId(item)) === button.dataset.id
        );

        if (lead) openLeadDetail(lead);
    });

    el.closeDialogBtn.addEventListener("click", () => el.dialog.close());

    renderSavedFilters();
    renderTable();
    loadLeads();
})();
