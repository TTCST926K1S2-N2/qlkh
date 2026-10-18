"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const root = document.querySelector(".care-container");
    const context = root.dataset.context || "";
    const api = `${context}/api/v1/customer-support`;

    const $ = (id) => document.getElementById(id);

    const priorities = {
        LOW: "Thấp",
        MEDIUM: "Trung bình",
        HIGH: "Cao",
        URGENT: "Khẩn cấp"
    };

    const statuses = {
        OPEN: "Mới",
        IN_PROGRESS: "Đang xử lý",
        RESOLVED: "Đã giải quyết",
        CLOSED: "Đã đóng"
    };

    let currentCustomerId = null;
    let requests = [];

    function message(text, isError = false) {
        const el = $("careMessage");
        el.textContent = text;
        el.style.color = isError ? "#b91c1c" : "#166534";
    }

    function positiveId(value, optional = false) {
        const text = String(value ?? "").trim();

        if (optional && text === "") {
            return null;
        }

        if (!/^[1-9]\d*$/.test(text)) {
            throw new Error("Mã phải là số nguyên dương.");
        }

        const number = Number(text);

        if (!Number.isSafeInteger(number)) {
            throw new Error("Mã không hợp lệ.");
        }

        return number;
    }

    async function request(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            cache: "no-store",
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            }
        });

        const contentType = response.headers.get("content-type") || "";

        if (!contentType.includes("application/json")) {
            throw new Error(
                "Không nhận được dữ liệu JSON. Hãy kiểm tra đăng nhập và đường dẫn API."
            );
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.error || `Yêu cầu thất bại (${response.status}).`
            );
        }

        return data;
    }

    function cell(row, value) {
        const td = document.createElement("td");
        td.textContent = value ?? "";
        row.appendChild(td);
        return td;
    }

    function renderTable() {
        const tbody = $("careTableBody");
        tbody.replaceChildren();

        if (requests.length === 0) {
            const row = document.createElement("tr");
            const td = cell(row, "Khách hàng chưa có yêu cầu hỗ trợ.");
            td.colSpan = 6;
            tbody.appendChild(row);
            return;
        }

        requests.forEach((item) => {
            const row = document.createElement("tr");

            cell(row, item.id);
            cell(row, item.title);
            cell(row, priorities[item.priority] || item.priority);
            cell(row, statuses[item.status] || item.status);
            cell(row, item.assignedTo ?? "Chưa phân công");

            const action = document.createElement("td");
            const button = document.createElement("button");

            button.type = "button";
            button.textContent = "Cập nhật";
            button.dataset.id = String(item.id);

            action.appendChild(button);
            row.appendChild(action);
            tbody.appendChild(row);
        });
    }

    async function loadRisk(customerId) {
        const warning = $("riskWarning");

        warning.hidden = true;
        warning.textContent = "";

        const data = await request(
            `${api}/risk?customerId=${encodeURIComponent(customerId)}`
        );

        if (data.atRisk) {
            warning.textContent =
                `Cảnh báo: Khách hàng có ${data.unresolvedCount} ` +
                "yêu cầu chưa giải quyết, cần ưu tiên chăm sóc.";
            warning.hidden = false;
        }
    }

    async function loadCustomer(customerId) {
        message("Đang tải dữ liệu...");

        const data = await request(
            `${api}?customerId=${encodeURIComponent(customerId)}`
        );

        if (!Array.isArray(data)) {
            throw new Error("Dữ liệu danh sách không hợp lệ.");
        }

        currentCustomerId = customerId;
        requests = data;

        $("editCarePanel").hidden = true;
        renderTable();

        try {
            await loadRisk(customerId);
            message(`Đã tải ${requests.length} yêu cầu hỗ trợ.`);
        } catch (error) {
            message(`Đã tải danh sách, nhưng lỗi cảnh báo: ${error.message}`, true);
        }
    }

    $("loadCustomer").addEventListener("click", async () => {
        try {
            const id = positiveId($("customerId").value);
            await loadCustomer(id);
        } catch (error) {
            message(error.message, true);
        }
    });

    $("createCareForm").addEventListener("submit", async (event) => {
        event.preventDefault();

        try {
            const customerId = positiveId($("customerId").value);

            if (currentCustomerId !== customerId) {
                throw new Error(
                    "Hãy bấm Xem yêu cầu để xác nhận khách hàng trước khi tạo."
                );
            }

            const payload = {
                customerId,
                title: $("careTitle").value.trim(),
                description: $("careDescription").value.trim(),
                priority: $("carePriority").value,
                assignedTo: positiveId($("careAssignee").value, true)
            };

            await request(api, {
                method: "POST",
                body: JSON.stringify(payload)
            });

            $("createCareForm").reset();
            await loadCustomer(customerId);
            message("Đã tạo yêu cầu hỗ trợ thành công.");
        } catch (error) {
            message(error.message, true);
        }
    });

    $("careTableBody").addEventListener("click", (event) => {
        const button = event.target.closest("button[data-id]");

        if (!button) {
            return;
        }

        const item = requests.find(
            (r) => String(r.id) === button.dataset.id
        );

        if (!item) {
            message("Không tìm thấy yêu cầu.", true);
            return;
        }

        $("editCareId").value = item.id;
        $("editCareStatus").value = item.status;
        $("editCarePriority").value = item.priority;
        $("editCareAssignee").value = item.assignedTo ?? "";
        $("editCareNote").value = item.resolutionNote ?? "";

        $("editCarePanel").hidden = false;
        $("editCarePanel").scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    });

    $("editCareForm").addEventListener("submit", async (event) => {
        event.preventDefault();

        try {
            const id = positiveId($("editCareId").value);

            const payload = {
                status: $("editCareStatus").value,
                priority: $("editCarePriority").value,
                assignedTo: positiveId($("editCareAssignee").value, true),
                resolutionNote: $("editCareNote").value.trim()
            };

            if (
                ["RESOLVED", "CLOSED"].includes(payload.status) &&
                !payload.resolutionNote
            ) {
                throw new Error(
                    "Cần nhập ghi chú xử lý trước khi hoàn thành yêu cầu."
                );
            }

            await request(`${api}/${id}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            await loadCustomer(currentCustomerId);
            message("Đã cập nhật yêu cầu thành công.");
        } catch (error) {
            message(error.message, true);
        }
    });

    $("cancelEdit").addEventListener("click", () => {
        $("editCarePanel").hidden = true;
    });

    const params = new URLSearchParams(window.location.search);
    const initialCustomerId = params.get("customerId");

    if (initialCustomerId) {
        $("customerId").value = initialCustomerId;
        $("loadCustomer").click();
    }
});
// s308-persist-customer
(function () {
    const storageKey = "s308-selected-customer";

    function init() {
        const customerInput = document.getElementById("customerId");
        const loadButton = document.getElementById("loadCustomer");

        if (!customerInput || !loadButton) {
            console.warn("S3-08: Khong tim thay o ma khach hang hoac nut xem yeu cau.");
            return;
        }

        const query = new URLSearchParams(window.location.search);
        const queryCustomerId = query.get("customerId");
        const savedCustomerId = sessionStorage.getItem(storageKey);

        const selectedId = queryCustomerId || savedCustomerId;

        if (selectedId && /^[1-9]\d*$/.test(selectedId)) {
            customerInput.value = selectedId;
            loadButton.click();
        }

        loadButton.addEventListener("click", function () {
            const id = customerInput.value.trim();

            if (/^[1-9]\d*$/.test(id)) {
                sessionStorage.setItem(storageKey, id);
            }
        });

        customerInput.addEventListener("change", function () {
            const id = customerInput.value.trim();

            if (/^[1-9]\d*$/.test(id)) {
                sessionStorage.setItem(storageKey, id);
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();