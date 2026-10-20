

document.addEventListener("DOMContentLoaded", function () {

    const API_URL =
        `${window.location.origin}${window.contextPath || ""}/api/audit-logs`;

    const searchForm =
        document.getElementById("searchForm");

    const usernameFilter =
        document.getElementById("usernameFilter");

    const targetObjectFilter =
        document.getElementById("targetObjectFilter");

    const actionFilter =
        document.getElementById("actionFilter");

    const startDateInput =
        document.getElementById("startDate");

    const endDateInput =
        document.getElementById("endDate");

    const btnReset =
        document.getElementById("btnReset");

    const btnRetry =
        document.getElementById("btnRetry");

    const tableContainer =
        document.getElementById("tableContainer");

    const logTableBody =
        document.getElementById("logTableBody");

    const loadingState =
        document.getElementById("loadingState");

    const emptyState =
        document.getElementById("emptyState");

    const errorState =
        document.getElementById("errorState");

    const errorMessageText =
        document.getElementById("errorMessageText");


    // =========================
    // Initial load
    // =========================

    fetchAuditLogs();

    if (searchForm) {

        searchForm.addEventListener("submit", function (event) {

            event.preventDefault();

            fetchAuditLogs();

        });
    }


    if (btnReset) {

        btnReset.addEventListener("click", function () {

            searchForm.reset();

            fetchAuditLogs();

        });
    }

    if (btnRetry) {

        btnRetry.addEventListener("click", function () {

            fetchAuditLogs();

        });
    }

    function fetchAuditLogs() {

        showLoading();

        const params = new URLSearchParams();


        // Người thực hiện
        if (usernameFilter &&
            usernameFilter.value.trim()) {

            params.append(
                "username",
                usernameFilter.value.trim()
            );
        }

        if (targetObjectFilter &&
            targetObjectFilter.value) {

            params.append(
                "targetObject",
                targetObjectFilter.value
            );
        }

        if (actionFilter &&
            actionFilter.value) {

            params.append(
                "action",
                actionFilter.value
            );
        }

        if (startDateInput &&
            startDateInput.value) {

            params.append(
                "startDate",
                startDateInput.value
            );
        }

        if (endDateInput &&
            endDateInput.value) {

            params.append(
                "endDate",
                endDateInput.value
            );
        }

        const queryString =
            params.toString();

        const requestUrl =
            queryString
                ? `${API_URL}?${queryString}`
                : API_URL;

        fetch(requestUrl, {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        })
        .then(function (response) {

            if (!response.ok) {

                return response.json()
                    .catch(function () {
                        return {};
                    })
                    .then(function (errorData) {

                        throw new Error(
                            errorData.error ||
                            `Lỗi kết nối máy chủ (${response.status})`
                        );

                    });
            }

            return response.json();

        })
        .then(function (data) {

            if (!Array.isArray(data)) {

                throw new Error(
                    "Dữ liệu API không đúng định dạng."
                );
            }


            if (data.length === 0) {

                showEmptyState();

                return;
            }

            renderLogTable(data);

        })
        .catch(function (error) {

            console.error(
                "Audit Log API Error:",
                error
            );

            showErrorState(
                error.message ||
                "Không thể tải danh sách nhật ký. Vui lòng thử lại sau."
            );

        });
    }


    function renderLogTable(logs) {

        logTableBody.innerHTML = "";

        logs.forEach(function (log, index) {

            const tr =
                document.createElement("tr");

            tr.innerHTML = `

                <td class="text-center align-middle">
                    ${index + 1}
                </td>

                <td class="align-middle">

                    <div class="fw-semibold text-dark">
                        ${escapeHtml(
                            log.executor || "Hệ thống"
                        )}
                    </div>

                </td>

                <td class="align-middle text-center">
                    ${getActionBadge(log.action)}
                </td>

                <td class="align-middle">

                    <span class="badge bg-light text-dark border">
                        ${escapeHtml(
                            log.targetObject || "N/A"
                        )}
                    </span>

                </td>

                <td class="align-middle">
                    ${renderValue(
                        log.oldValue
                    )}
                </td>

                <td class="align-middle">
                    ${renderValue(
                        log.newValue
                    )}
                </td>

                <td class="align-middle text-nowrap">
                    ${formatDateTime(
                        log.timestamp
                    )}
                </td>

            `;

            logTableBody.appendChild(tr);

        });

        showTable();
    }

    function renderValue(value) {

        if (
            value === null ||
            value === undefined ||
            String(value).trim() === ""
        ) {

            return `
                <span class="text-muted">
                    Không có
                </span>
            `;
        }


        return `
            <div class="log-value-box">
                ${escapeHtml(value)}
            </div>
        `;
    }

    function getActionBadge(action) {

        const act =
            String(action || "")
                .trim()
                .toUpperCase();

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
                        ${escapeHtml(
                            action || "Khác"
                        )}
                    </span>
                `;
        }
    }

    function formatDateTime(isoString) {

        if (!isoString) {

            return "N/A";
        }

        const date =
            new Date(isoString);

        if (isNaN(date.getTime())) {

            return escapeHtml(
                isoString
            );
        }

        return date.toLocaleString(
            "vi-VN",
            {
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit",
                day: "2-digit",
                month: "2-digit",
                year: "numeric"
            }
        );
    }

    function showLoading() {

        loadingState.classList.remove("d-none");

        tableContainer.classList.add("d-none");

        emptyState.classList.add("d-none");

        errorState.classList.add("d-none");
    }

    function showTable() {

        loadingState.classList.add("d-none");

        tableContainer.classList.remove("d-none");

        emptyState.classList.add("d-none");

        errorState.classList.add("d-none");
    }


    function showEmptyState() {

        loadingState.classList.add("d-none");

        tableContainer.classList.add("d-none");

        emptyState.classList.remove("d-none");

        errorState.classList.add("d-none");
    }

    function showErrorState(message) {

        loadingState.classList.add("d-none");

        tableContainer.classList.add("d-none");

        emptyState.classList.add("d-none");

        errorState.classList.remove("d-none");

        errorMessageText.textContent =
            message;
    }

    function escapeHtml(value) {

        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

});