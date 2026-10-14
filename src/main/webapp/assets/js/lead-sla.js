"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("leadSearch");
    const slaFilter = document.getElementById("slaStatusFilter");
    const statusFilter = document.getElementById("leadStatusFilter");
    const refreshButton = document.getElementById("refreshLeads");
    const messageBox = document.getElementById("leadSlaMessage");
    const tableBody = document.getElementById("leadSlaRows");

    const counters = {
        total: document.getElementById("totalLeads"),
        onTime: document.getElementById("onTimeLeads"),
        urgent: document.getElementById("urgentLeads"),
        overdue: document.getElementById("overdueLeads")
    };

    // Chưa có API Backend: không sử dụng dữ liệu giả.
    let leads = [];
    let dataLoaded = false;

    const slaLabels = {
        on_time: "Trong hạn",
        urgent: "Sắp quá hạn",
        overdue: "Đã quá hạn"
    };

    const statusLabels = {
        pending: "Chờ tiếp nhận",
        accepted: "Đã tiếp nhận",
        rejected: "Đã từ chối"
    };

    function showMessage(text, type = "") {
        messageBox.textContent = text;
        messageBox.className = type;
    }

    function setCounters() {
        if (!dataLoaded) {
            Object.values(counters).forEach((element) => {
                element.textContent = "--";
            });
            return;
        }

        counters.total.textContent = String(leads.length);
        counters.onTime.textContent = String(
            leads.filter((lead) => lead.slaStatus === "on_time").length
        );
        counters.urgent.textContent = String(
            leads.filter((lead) => lead.slaStatus === "urgent").length
        );
        counters.overdue.textContent = String(
            leads.filter((lead) => lead.slaStatus === "overdue").length
        );
    }

    function formatDate(value) {
        if (!value) return "Chưa xác định";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return "Không hợp lệ";
        }

        return new Intl.DateTimeFormat("vi-VN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }).format(date);
    }

    function createCell(row, value) {
        const cell = document.createElement("td");
        cell.textContent = value == null ? "" : String(value);
        row.appendChild(cell);
        return cell;
    }

    function renderEmpty(text) {
        tableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.className = "lead-sla-empty";
        cell.textContent = text;

        row.appendChild(cell);
        tableBody.appendChild(row);
    }

    function getFilteredLeads() {
        const keyword = searchInput.value.trim().toLocaleLowerCase("vi");
        const selectedSla = slaFilter.value;
        const selectedStatus = statusFilter.value;

        return leads.filter((lead) => {
            const searchable = [
                lead.id,
                lead.fullName,
                lead.email,
                lead.phone
            ].map((value) => String(value ?? "")).join(" ")
             .toLocaleLowerCase("vi");

            return (
                (!keyword || searchable.includes(keyword)) &&
                (selectedSla === "all" || lead.slaStatus === selectedSla) &&
                (selectedStatus === "all" || lead.status === selectedStatus)
            );
        });
    }

    function renderLeads() {
        setCounters();

        if (!dataLoaded) {
            renderEmpty("Chưa có dữ liệu từ Backend S4-07.");
            return;
        }

        const filtered = getFilteredLeads();

        if (filtered.length === 0) {
            renderEmpty("Không tìm thấy Lead phù hợp.");
            return;
        }

        tableBody.replaceChildren();

        filtered.forEach((lead) => {
            const row = document.createElement("tr");

            createCell(row, lead.id);
            const customerCell = createCell(row, lead.fullName || "Chưa có tên");

            if (lead.email) {
                const email = document.createElement("small");
                email.textContent = lead.email;
                email.style.display = "block";
                email.style.marginTop = "5px";
                customerCell.appendChild(email);
            }

            createCell(row, lead.assignee || "Chưa phân công");
            createCell(row, formatDate(lead.deadline));

            const slaCell = createCell(row, "");
            const badge = document.createElement("span");
            badge.className = "sla-badge";

            if (Object.hasOwn(slaLabels, lead.slaStatus)) {
                badge.classList.add(
                    lead.slaStatus === "on_time"
                        ? "on-time"
                        : lead.slaStatus
                );
            }

            badge.textContent = slaLabels[lead.slaStatus] || "Chưa xác định";
            slaCell.appendChild(badge);

            createCell(
                row,
                statusLabels[lead.status] || "Chưa xác định"
            );

            const actionCell = createCell(row, "");

            if (lead.status === "pending") {
                const acceptButton = document.createElement("button");
                acceptButton.type = "button";
                acceptButton.className = "sla-action primary";
                acceptButton.textContent = "Tiếp nhận";
                acceptButton.addEventListener("click", () => {
                    submitLeadAction(lead.id, "accept");
                });

                const rejectButton = document.createElement("button");
                rejectButton.type = "button";
                rejectButton.className = "sla-action";
                rejectButton.textContent = "Từ chối";
                rejectButton.addEventListener("click", () => {
                    submitLeadAction(lead.id, "reject");
                });

                actionCell.append(acceptButton, rejectButton);
            } else {
                actionCell.textContent = "—";
            }

            tableBody.appendChild(row);
        });
    }

    let actionPending = false;

    async function submitLeadAction(leadId, action) {
        if (actionPending) return;

        if (!Number.isSafeInteger(Number(leadId)) ||
            Number(leadId) <= 0) {
            showMessage("ID Lead không hợp lệ.", "error");
            return;
        }

        if (action !== "accept" && action !== "reject") return;

        let reason = "";

        if (action === "reject") {
            const input = window.prompt(
                "Nhập lý do từ chối Lead (tối đa 1000 ký tự):"
            );

            if (input === null) return;

            reason = input.trim();

            if (!reason || reason.length > 1000) {
                showMessage(
                    "Lý do từ chối phải có từ 1 đến 1000 ký tự.",
                    "error"
                );
                return;
            }
        } else {
            if (!window.confirm(
                "Bạn có chắc muốn tiếp nhận Lead #" + leadId + "?"
            )) {
                return;
            }
        }

        actionPending = true;
        refreshButton.disabled = true;

        tableBody.querySelectorAll("button").forEach(button => {
            button.disabled = true;
        });

        showMessage("Đang xử lý Lead #" + leadId + "...");

        try {
            const body = new URLSearchParams();

            if (action === "reject") {
                body.set("reason", reason);
            }

            const response = await fetch(
                "/qlkh/api/v1/leads/" +
                encodeURIComponent(leadId) +
                "/" + action,
                {
                    method: "POST",
                    credentials: "same-origin",
                    headers: {
                        "Accept": "application/json",
                        "Content-Type":
                            "application/x-www-form-urlencoded;charset=UTF-8"
                    },
                    body: body.toString()
                }
            );

            const contentType =
                response.headers.get("content-type") || "";

            if (!contentType.toLowerCase().includes("application/json")) {
                throw new Error(
                    "Phiên đăng nhập hết hạn hoặc API trả về HTML."
                );
            }

            const result = await response.json();

            if (!response.ok || result.success !== true) {
                throw new Error(
                    result.message || "Không thể xử lý Lead."
                );
            }

            await refreshLeads();

        } catch (error) {
            showMessage(error.message, "error");
            renderLeads();
        } finally {
            actionPending = false;
            refreshButton.disabled = false;
        }
    }
    async function refreshLeads() {
        refreshButton.disabled = true;
        showMessage("Đang tải danh sách Lead...");

        try {
            const response = await fetch(
                "/qlkh/api/v1/leads/sla",
                {
                    method: "GET",
                    credentials: "same-origin",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            const contentType =
                response.headers.get("content-type") || "";

            if (!contentType.toLowerCase().includes("application/json")) {
                throw new Error(
                    "Phiên đăng nhập hết hạn hoặc API trả về HTML."
                );
            }

            const result = await response.json();

            if (!response.ok || result.success !== true) {
                throw new Error(
                    result.message || "Không thể tải danh sách Lead."
                );
            }

            if (!Array.isArray(result.leads)) {
                throw new Error("Dữ liệu Lead không hợp lệ.");
            }

            leads = result.leads;
            dataLoaded = true;

            renderLeads();

            showMessage(
                leads.length === 0
                    ? "Chưa có Lead được phân công cho tài khoản này."
                    : "Đã tải " + leads.length + " Lead.",
                "success"
            );
        } catch (error) {
            leads = [];
            dataLoaded = false;

            renderLeads();
            showMessage(error.message, "error");
        } finally {
            refreshButton.disabled = false;
        }
    }

    searchInput.addEventListener("input", renderLeads);
    slaFilter.addEventListener("change", renderLeads);
    statusFilter.addEventListener("change", renderLeads);
    refreshButton.addEventListener("click", refreshLeads);

    refreshLeads();
});