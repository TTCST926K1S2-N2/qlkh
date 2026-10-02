document.addEventListener("DOMContentLoaded", function () {

    const resultModal = document.getElementById("resultModal");
    const competitorModal = document.getElementById("competitorModal");

    const resultForm = document.getElementById("resultForm");
    const competitorForm = document.getElementById("competitorForm");

    let results = [
        {
            id: 1,
            dealName: "Triển khai CRM cho ABC",
            customerName: "Công ty ABC",
            value: 85000000,
            status: "won",
            competitor: "CloudCRM",
            date: "2026-09-05",
            reason: "Đáp ứng tốt yêu cầu và có chi phí phù hợp."
        },
        {
            id: 2,
            dealName: "Hệ thống quản lý bán hàng",
            customerName: "Công ty Minh Long",
            value: 120000000,
            status: "lost",
            competitor: "FastSales",
            date: "2026-09-11",
            reason: "Khách hàng lựa chọn giải pháp có thời gian triển khai ngắn hơn."
        },
        {
            id: 3,
            dealName: "Nâng cấp hệ thống nội bộ",
            customerName: "Công ty Thành Công",
            value: 65000000,
            status: "won",
            competitor: "",
            date: "2026-09-15",
            reason: "Giải pháp phù hợp với hệ thống hiện tại."
        }
    ];

    let competitors = [
        {
            id: 1,
            name: "CloudCRM",
            code: "COMP-001",
            industry: "Phần mềm CRM",
            level: "high",
            status: "active",
            note: "Thường xuất hiện trong các cơ hội CRM."
        },
        {
            id: 2,
            name: "FastSales",
            code: "COMP-002",
            industry: "Quản lý bán hàng",
            level: "medium",
            status: "active",
            note: "Có lợi thế về thời gian triển khai."
        },
        {
            id: 3,
            name: "BizPlatform",
            code: "COMP-003",
            industry: "Giải pháp doanh nghiệp",
            level: "low",
            status: "inactive",
            note: "Ít xuất hiện trong các cơ hội gần đây."
        }
    ];

    let nextResultId = 4;
    let nextCompetitorId = 4;

    const resultSearch = document.getElementById("resultSearch");
    const resultStatusFilter = document.getElementById("resultStatusFilter");
    const competitorSearch = document.getElementById("competitorSearch");

    function escapeHtml(value) {
        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatMoney(value) {
        if (!value || Number(value) <= 0) {
            return "—";
        }

        return new Intl.NumberFormat("vi-VN").format(Number(value)) + " đ";
    }

    function formatDate(value) {
        if (!value) {
            return "—";
        }

        const parts = value.split("-");
        if (parts.length !== 3) {
            return value;
        }

        return parts[2] + "/" + parts[1] + "/" + parts[0];
    }

    function getCompetitorName(id) {
        if (!id) {
            return "Không xác định";
        }

        const competitor = competitors.find(function (item) {
            return String(item.id) === String(id);
        });

        return competitor ? competitor.name : id;
    }

    function showToast(message) {
        const toast = document.getElementById("wlToast");

        toast.textContent = message;
        toast.classList.add("show");

        window.clearTimeout(showToast.timer);

        showToast.timer = window.setTimeout(function () {
            toast.classList.remove("show");
        }, 2400);
    }

    function openModal(modal) {
        modal.classList.add("open");
        document.body.style.overflow = "hidden";
    }

    function closeModal(modal) {
        modal.classList.remove("open");

        if (!resultModal.classList.contains("open") &&
            !competitorModal.classList.contains("open")) {
            document.body.style.overflow = "";
        }
    }

    function clearErrors() {
        document.querySelectorAll(".wl-error").forEach(function (element) {
            element.textContent = "";
        });
    }

    function renderSummary() {
        const total = results.length;
        const won = results.filter(function (item) {
            return item.status === "won";
        }).length;

        const lost = results.filter(function (item) {
            return item.status === "lost";
        }).length;

        const wonRate = total ? Math.round((won / total) * 100) : 0;
        const lostRate = total ? Math.round((lost / total) * 100) : 0;

        document.getElementById("totalDeals").textContent = total;
        document.getElementById("wonDeals").textContent = won;
        document.getElementById("lostDeals").textContent = lost;
        document.getElementById("competitorCount").textContent = competitors.length;

        document.getElementById("wonRate").textContent =
            wonRate + "% tổng cơ hội";

        document.getElementById("lostRate").textContent =
            lostRate + "% tổng cơ hội";
    }

    function renderCompetitorOptions(selectedId) {
        const select = document.getElementById("resultCompetitor");

        select.innerHTML =
            '<option value="">Không xác định</option>' +
            competitors.map(function (item) {
                const selected =
                    String(item.id) === String(selectedId) ? " selected" : "";

                return '<option value="' +
                    escapeHtml(item.id) +
                    '"' +
                    selected +
                    ">" +
                    escapeHtml(item.name) +
                    "</option>";
            }).join("");
    }

    function renderResults() {
        const tbody = document.getElementById("resultsTableBody");
        const empty = document.getElementById("resultsEmpty");

        const searchValue =
            resultSearch.value.trim().toLowerCase();

        const statusValue =
            resultStatusFilter.value;

        const filtered = results.filter(function (item) {

            const searchMatch =
                !searchValue ||
                item.dealName.toLowerCase().includes(searchValue) ||
                item.customerName.toLowerCase().includes(searchValue) ||
                getCompetitorName(item.competitor).toLowerCase().includes(searchValue);

            const statusMatch =
                statusValue === "all" ||
                item.status === statusValue;

            return searchMatch && statusMatch;
        });

        tbody.innerHTML = filtered.map(function (item) {

            const statusClass =
                item.status === "won"
                    ? "wl-status-won"
                    : "wl-status-lost";

            const statusText =
                item.status === "won"
                    ? "Win"
                    : "Loss";

            return `
                <tr>
                    <td>
                        <div class="wl-primary-text">
                            ${escapeHtml(item.dealName)}
                        </div>
                    </td>

                    <td>
                        <div class="wl-primary-text">
                            ${escapeHtml(item.customerName)}
                        </div>
                    </td>

                    <td>
                        ${formatMoney(item.value)}
                    </td>

                    <td>
                        <span class="wl-status ${statusClass}">
                            ${statusText}
                        </span>
                    </td>

                    <td>
                        ${escapeHtml(getCompetitorName(item.competitor))}
                    </td>

                    <td>
                        ${formatDate(item.date)}
                    </td>

                    <td>
                        <div class="wl-secondary-text">
                            ${escapeHtml(item.reason || "—")}
                        </div>
                    </td>

                    <td>
                        <div class="wl-actions">
                            <button
                                type="button"
                                class="wl-action wl-action-edit"
                                data-edit-result="${item.id}">
                                Sửa
                            </button>

                            <button
                                type="button"
                                class="wl-action wl-action-delete"
                                data-delete-result="${item.id}">
                                Xóa
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join("");

        empty.classList.toggle("visible", filtered.length === 0);

        tbody.querySelectorAll("[data-edit-result]").forEach(function (button) {
            button.addEventListener("click", function () {
                editResult(Number(button.dataset.editResult));
            });
        });

        tbody.querySelectorAll("[data-delete-result]").forEach(function (button) {
            button.addEventListener("click", function () {
                deleteResult(Number(button.dataset.deleteResult));
            });
        });
    }

    function renderCompetitors() {
        const tbody = document.getElementById("competitorsTableBody");
        const empty = document.getElementById("competitorsEmpty");

        const searchValue =
            competitorSearch.value.trim().toLowerCase();

        const filtered = competitors.filter(function (item) {
            return !searchValue ||
                item.name.toLowerCase().includes(searchValue) ||
                item.code.toLowerCase().includes(searchValue) ||
                item.industry.toLowerCase().includes(searchValue);
        });

        tbody.innerHTML = filtered.map(function (item) {

            let levelClass = "wl-level-medium";
            let levelText = "Trung bình";

            if (item.level === "low") {
                levelClass = "wl-level-low";
                levelText = "Thấp";
            }

            if (item.level === "high") {
                levelClass = "wl-level-high";
                levelText = "Cao";
            }

            const statusClass =
                item.status === "active"
                    ? "wl-status-active"
                    : "wl-status-inactive";

            const statusText =
                item.status === "active"
                    ? "Đang hoạt động"
                    : "Không hoạt động";

            return `
                <tr>
                    <td>
                        <div class="wl-primary-text">
                            ${escapeHtml(item.name)}
                        </div>
                    </td>

                    <td>${escapeHtml(item.code)}</td>

                    <td>${escapeHtml(item.industry || "—")}</td>

                    <td>
                        <span class="wl-level ${levelClass}">
                            ${levelText}
                        </span>
                    </td>

                    <td>
                        <span class="wl-status ${statusClass}">
                            ${statusText}
                        </span>
                    </td>

                    <td>
                        <div class="wl-secondary-text">
                            ${escapeHtml(item.note || "—")}
                        </div>
                    </td>

                    <td>
                        <div class="wl-actions">
                            <button
                                type="button"
                                class="wl-action wl-action-edit"
                                data-edit-competitor="${item.id}">
                                Sửa
                            </button>

                            <button
                                type="button"
                                class="wl-action wl-action-delete"
                                data-delete-competitor="${item.id}">
                                Xóa
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join("");

        empty.classList.toggle("visible", filtered.length === 0);

        tbody.querySelectorAll("[data-edit-competitor]").forEach(function (button) {
            button.addEventListener("click", function () {
                editCompetitor(Number(button.dataset.editCompetitor));
            });
        });

        tbody.querySelectorAll("[data-delete-competitor]").forEach(function (button) {
            button.addEventListener("click", function () {
                deleteCompetitor(Number(button.dataset.deleteCompetitor));
            });
        });
    }

    function resetResultForm() {
        resultForm.reset();

        document.getElementById("resultId").value = "";
        document.getElementById("resultModalTitle").textContent =
            "Ghi nhận kết quả";

        document.getElementById("resultDate").value =
            new Date().toISOString().slice(0, 10);

        document.getElementById("resultStatus").value = "won";

        clearErrors();
        renderCompetitorOptions("");
    }

    function editResult(id) {
        const item = results.find(function (result) {
            return result.id === id;
        });

        if (!item) {
            return;
        }

        document.getElementById("resultId").value = item.id;
        document.getElementById("dealName").value = item.dealName;
        document.getElementById("customerName").value = item.customerName;
        document.getElementById("dealValue").value = item.value || "";
        document.getElementById("resultStatus").value = item.status;
        document.getElementById("resultDate").value = item.date;
        document.getElementById("resultReason").value = item.reason || "";

        renderCompetitorOptions(item.competitor);

        document.getElementById("resultModalTitle").textContent =
            "Chỉnh sửa kết quả";

        clearErrors();
        openModal(resultModal);
    }

    function deleteResult(id) {
        const item = results.find(function (result) {
            return result.id === id;
        });

        if (!item) {
            return;
        }

        if (!window.confirm("Bạn có chắc muốn xóa kết quả này?")) {
            return;
        }

        results = results.filter(function (result) {
            return result.id !== id;
        });

        renderSummary();
        renderResults();

        showToast("Đã xóa kết quả Win/Loss.");
    }

    function resetCompetitorForm() {
        competitorForm.reset();

        document.getElementById("competitorId").value = "";
        document.getElementById("competitorModalTitle").textContent =
            "Thêm đối thủ";

        document.getElementById("competitorLevel").value = "medium";
        document.getElementById("competitorStatus").value = "active";

        clearErrors();
    }

    function editCompetitor(id) {
        const item = competitors.find(function (competitor) {
            return competitor.id === id;
        });

        if (!item) {
            return;
        }

        document.getElementById("competitorId").value = item.id;
        document.getElementById("competitorName").value = item.name;
        document.getElementById("competitorCode").value = item.code;
        document.getElementById("competitorIndustry").value = item.industry || "";
        document.getElementById("competitorLevel").value = item.level;
        document.getElementById("competitorStatus").value = item.status;
        document.getElementById("competitorNote").value = item.note || "";

        document.getElementById("competitorModalTitle").textContent =
            "Chỉnh sửa đối thủ";

        clearErrors();
        openModal(competitorModal);
    }

    function deleteCompetitor(id) {
        const item = competitors.find(function (competitor) {
            return competitor.id === id;
        });

        if (!item) {
            return;
        }

        if (!window.confirm("Bạn có chắc muốn xóa đối thủ này?")) {
            return;
        }

        competitors = competitors.filter(function (competitor) {
            return competitor.id !== id;
        });

        results = results.map(function (result) {
            if (String(result.competitor) === String(id)) {
                return {
                    ...result,
                    competitor: ""
                };
            }

            return result;
        });

        renderSummary();
        renderResults();
        renderCompetitors();
        renderCompetitorOptions("");

        showToast("Đã xóa đối thủ cạnh tranh.");
    }

    document.getElementById("openResultButton")
        .addEventListener("click", function () {
            resetResultForm();
            openModal(resultModal);
        });

    document.getElementById("openCompetitorButton")
        .addEventListener("click", function () {
            resetCompetitorForm();
            openModal(competitorModal);
        });

    document.querySelectorAll("[data-close-modal]").forEach(function (button) {
        button.addEventListener("click", function () {
            const modalId = button.dataset.closeModal;
            const modal = document.getElementById(modalId);

            if (modal) {
                closeModal(modal);
            }
        });
    });

    document.querySelectorAll(".wl-modal-overlay").forEach(function (overlay) {
        overlay.addEventListener("click", function (event) {
            if (event.target === overlay) {
                closeModal(overlay);
            }
        });
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeModal(resultModal);
            closeModal(competitorModal);
        }
    });

    document.querySelectorAll(".wl-tab").forEach(function (tab) {
        tab.addEventListener("click", function () {

            document.querySelectorAll(".wl-tab").forEach(function (item) {
                item.classList.remove("active");
            });

            tab.classList.add("active");

            const tabName = tab.dataset.tab;

            document.getElementById("resultsPanel")
                .classList.toggle("wl-hidden", tabName !== "results");

            document.getElementById("competitorsPanel")
                .classList.toggle("wl-hidden", tabName !== "competitors");
        });
    });

    resultSearch.addEventListener("input", renderResults);
    resultStatusFilter.addEventListener("change", renderResults);
    competitorSearch.addEventListener("input", renderCompetitors);

    resultForm.addEventListener("submit", function (event) {
        event.preventDefault();

        clearErrors();

        const id = document.getElementById("resultId").value;
        const dealName = document.getElementById("dealName").value.trim();
        const customerName = document.getElementById("customerName").value.trim();
        const value = document.getElementById("dealValue").value;
        const status = document.getElementById("resultStatus").value;
        const competitor = document.getElementById("resultCompetitor").value;
        const date = document.getElementById("resultDate").value;
        const reason = document.getElementById("resultReason").value.trim();

        let valid = true;

        if (!dealName) {
            document.getElementById("dealNameError").textContent =
                "Vui lòng nhập tên cơ hội.";
            valid = false;
        }

        if (!customerName) {
            document.getElementById("customerNameError").textContent =
                "Vui lòng nhập khách hàng.";
            valid = false;
        }

        if (!date) {
            showToast("Vui lòng chọn ngày ghi nhận.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        const payload = {
            dealName: dealName,
            customerName: customerName,
            value: Number(value) || 0,
            status: status,
            competitor: competitor,
            date: date,
            reason: reason
        };

        if (id) {
            const index = results.findIndex(function (item) {
                return item.id === Number(id);
            });

            if (index !== -1) {
                results[index] = {
                    ...results[index],
                    ...payload
                };
            }

            showToast("Đã cập nhật kết quả.");
        } else {
            results.push({
                id: nextResultId++,
                ...payload
            });

            showToast("Đã ghi nhận kết quả mới.");
        }

        closeModal(resultModal);
        renderSummary();
        renderResults();
    });

    competitorForm.addEventListener("submit", function (event) {
        event.preventDefault();

        clearErrors();

        const id = document.getElementById("competitorId").value;
        const name = document.getElementById("competitorName").value.trim();
        const code = document.getElementById("competitorCode").value.trim();
        const industry = document.getElementById("competitorIndustry").value.trim();
        const level = document.getElementById("competitorLevel").value;
        const status = document.getElementById("competitorStatus").value;
        const note = document.getElementById("competitorNote").value.trim();

        let valid = true;

        if (!name) {
            document.getElementById("competitorNameError").textContent =
                "Vui lòng nhập tên đối thủ.";
            valid = false;
        }

        if (!code) {
            document.getElementById("competitorCodeError").textContent =
                "Vui lòng nhập mã đối thủ.";
            valid = false;
        }

        const duplicate = competitors.some(function (item) {
            return item.code.toLowerCase() === code.toLowerCase() &&
                String(item.id) !== String(id);
        });

        if (duplicate) {
            document.getElementById("competitorCodeError").textContent =
                "Mã đối thủ đã tồn tại.";
            valid = false;
        }

        if (!valid) {
            return;
        }

        const payload = {
            name: name,
            code: code,
            industry: industry,
            level: level,
            status: status,
            note: note
        };

        if (id) {
            const index = competitors.findIndex(function (item) {
                return item.id === Number(id);
            });

            if (index !== -1) {
                competitors[index] = {
                    ...competitors[index],
                    ...payload
                };
            }

            showToast("Đã cập nhật đối thủ.");
        } else {
            competitors.push({
                id: nextCompetitorId++,
                ...payload
            });

            showToast("Đã thêm đối thủ mới.");
        }

        closeModal(competitorModal);

        renderSummary();
        renderResults();
        renderCompetitors();
        renderCompetitorOptions("");
    });

    renderSummary();
    renderCompetitorOptions("");
    renderResults();
    renderCompetitors();
});
