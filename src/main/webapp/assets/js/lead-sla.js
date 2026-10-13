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
                acceptButton.disabled = true;
                acceptButton.title = "Chờ tích hợp API Backend";

                const rejectButton = document.createElement("button");
                rejectButton.type = "button";
                rejectButton.className = "sla-action";
                rejectButton.textContent = "Từ chối";
                rejectButton.disabled = true;
                rejectButton.title = "Chờ tích hợp API Backend";

                actionCell.append(acceptButton, rejectButton);
            } else {
                actionCell.textContent = "—";
            }

            tableBody.appendChild(row);
        });
    }

    function refreshLeads() {
        // Không gọi API khi chưa thống nhất hợp đồng Backend.
        dataLoaded = false;
        leads = [];

        renderLeads();

        showMessage(
            "Chưa tích hợp API Backend S4-07. " +
            "Danh sách và thao tác tiếp nhận/từ chối chưa khả dụng."
        );
    }

    searchInput.addEventListener("input", renderLeads);
    slaFilter.addEventListener("change", renderLeads);
    statusFilter.addEventListener("change", renderLeads);
    refreshButton.addEventListener("click", refreshLeads);

    refreshLeads();
});