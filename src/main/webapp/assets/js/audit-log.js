/**
 * Quản lý Nhật ký Thay đổi (Audit Log) - S2-04 FE
 */
document.addEventListener("DOMContentLoaded", function () {
    const API_URL = `${window.location.origin}${window.contextPath || ''}/api/audit-logs`;

    // Elements
    const searchForm = document.getElementById("searchForm");
    const keywordInput = document.getElementById("searchKeyword");
    const actionFilter = document.getElementById("actionFilter");
    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const btnReset = document.getElementById("btnReset");

    const tableContainer = document.getElementById("tableContainer");
    const logTableBody = document.getElementById("logTableBody");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const errorState = document.getElementById("errorState");
    const errorMessageText = document.getElementById("errorMessageText");

    // Lần đầu tải trang -> Gọi API lấy dữ liệu
    fetchAuditLogs();

    // Sự kiện Tìm kiếm / Lọc
    if (searchForm) {
        searchForm.addEventListener("submit", function (e) {
            e.preventDefault();
            fetchAuditLogs();
        });
    }

    // Sự kiện Đặt lại bộ lọc
    if (btnReset) {
        btnReset.addEventListener("click", function () {
            searchForm.reset();
            fetchAuditLogs();
        });
    }

    /**
     * Hàm kết nối API Backend lấy danh sách nhật ký
     */
    function fetchAuditLogs() {
        showLoading();

        // Chuẩn bị tham số Query
        const params = new URLSearchParams();

        if (keywordInput.value.trim()) {
            params.append("keyword", keywordInput.value.trim());
        }

        if (actionFilter.value) {
            params.append("action", actionFilter.value);
        }

        if (startDateInput.value) {
            params.append("startDate", startDateInput.value);
        }

        if (endDateInput.value) {
            params.append("endDate", endDateInput.value);
        }

        fetch(`${API_URL}?${params.toString()}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(
                    `Lỗi kết nối máy chủ (${response.status}: ${response.statusText})`
                );
            }

            return response.json();
        })
        .then(data => {
            if (!data || data.length === 0) {
                showEmptyState();
            } else {
                renderLogTable(data);
            }
        })
        .catch(error => {
            console.error("Audit Log API Error:", error);

            showErrorState(
                error.message ||
                "Không thể tải danh sách nhật ký. Vui lòng thử lại sau."
            );
        });
    }

    /**
     * Render dữ liệu ra bảng HTML
     */
    function renderLogTable(logs) {
        logTableBody.innerHTML = "";

        logs.forEach((log, index) => {
            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td class="text-center align-middle">${index + 1}</td>

                <td class="align-middle">
                    <div class="fw-semibold text-dark">
                        ${escapeHtml(log.executor || "Hệ thống")}
                    </div>
                    <small class="text-muted">
                        ${escapeHtml(log.executorRole || "")}
                    </small>
                </td>

                <td class="align-middle text-center">
                    ${getActionBadge(log.action)}
                </td>

                <td class="align-middle">
                    <div class="fw-medium text-dark">
                        ${escapeHtml(log.targetObject || "N/A")}
                    </div>
                    <div class="log-details-box text-muted mt-1">
                        ${escapeHtml(log.details || "Không có chi tiết")}
                    </div>
                </td>

                <td class="align-middle text-nowrap">
                    ${formatDateTime(log.timestamp)}
                </td>
            `;

            logTableBody.appendChild(tr);
        });

        showTable();
    }

    /**
     * Hiển thị hành động
     * Không sử dụng icon
     */
    function getActionBadge(action) {
        const act = (action || "").toUpperCase();

        switch (act) {
            case "CREATE":
            case "THÊM MỚI":
                return `
                    <span class="badge badge-action-create px-2 py-1">
                        Thêm mới
                    </span>
                `;

            case "UPDATE":
            case "CẬP NHẬT":
                return `
                    <span class="badge badge-action-update px-2 py-1">
                        Cập nhật
                    </span>
                `;

            case "DELETE":
            case "XÓA":
                return `
                    <span class="badge badge-action-delete px-2 py-1">
                        Xóa
                    </span>
                `;

            case "LOGIN":
            case "ĐĂNG NHẬP":
                return `
                    <span class="badge badge-action-login px-2 py-1">
                        Đăng nhập
                    </span>
                `;

            default:
                return `
                    <span class="badge bg-secondary px-2 py-1">
                        ${escapeHtml(action || "Khác")}
                    </span>
                `;
        }
    }

    /**
     * Định dạng ngày giờ
     */
    function formatDateTime(isoString) {
        if (!isoString) {
            return "N/A";
        }

        const date = new Date(isoString);

        if (isNaN(date.getTime())) {
            return isoString;
        }

        return date.toLocaleString("vi-VN", {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            day: "2-digit",
            month: "2-digit",
            year: "numeric"
        });
    }

    /**
     * Trạng thái Loading
     */
    function showLoading() {
        loadingState.classList.remove("d-none");
        tableContainer.classList.add("d-none");
        emptyState.classList.add("d-none");
        errorState.classList.add("d-none");
    }

    /**
     * Hiển thị bảng dữ liệu
     */
    function showTable() {
        loadingState.classList.add("d-none");
        tableContainer.classList.remove("d-none");
        emptyState.classList.add("d-none");
        errorState.classList.add("d-none");
    }

    /**
     * Không có dữ liệu
     */
    function showEmptyState() {
        loadingState.classList.add("d-none");
        tableContainer.classList.add("d-none");
        emptyState.classList.remove("d-none");
        errorState.classList.add("d-none");
    }

    /**
     * Hiển thị lỗi API
     */
    function showErrorState(msg) {
        loadingState.classList.add("d-none");
        tableContainer.classList.add("d-none");
        emptyState.classList.add("d-none");
        errorState.classList.remove("d-none");

        errorMessageText.textContent = msg;
    }

    /**
     * Chống XSS khi hiển thị dữ liệu
     */
    function escapeHtml(str) {
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }
});