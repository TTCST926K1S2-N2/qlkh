
(() => {
    "use strict";

    const CONTEXT_PATH = window.contextPath || "";
    const CONFIG_API = `${CONTEXT_PATH}/api/v1/lead-scoring/config`;
    const LEADS_API = `${CONTEXT_PATH}/api/v1/lead-scoring/`;

    const DEFAULT_CONFIG = {
        expectedValueWeight: 50,
        completenessWeight: 30,
        openOpportunityWeight: 20
    };

    let currentConfig = { ...DEFAULT_CONFIG };
    let allLeads = [];

    const $ = (id) => document.getElementById(id);

    function getElement(...ids) {
        for (const id of ids) {
            const element = $(id);
            if (element) return element;
        }
        return null;
    }

    function showMessage(message, type = "success") {
        const container = getElement(
            "scoringMessage",
            "messageContainer",
            "alertMessage"
        );

        if (container) {
            container.textContent = message;
            container.className = `alert alert-${type}`;
            container.style.display = "block";

            window.setTimeout(() => {
                container.style.display = "none";
            }, 4500);
        } else {
            window.alert(message);
        }
    }

    async function requestJson(url, options = {}) {
        const response = await fetch(url, {
            credentials: "same-origin",
            ...options,
            headers: {
                ...(options.body
                    ? { "Content-Type": "application/json" }
                    : {}),
                ...options.headers
            }
        });

        const text = await response.text();
        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                throw new Error("Máy chủ trả về dữ liệu không hợp lệ.");
            }
        }

        if (!response.ok || data.success === false) {
            throw new Error(
                data.message ||
                data.error ||
                `Yêu cầu thất bại (HTTP ${response.status}).`
            );
        }

        return data;
    }

    function getPayload(data) {
        return data?.data ?? data?.config ?? data;
    }

    function readConfigForm() {
        const expectedValueWeight = Number(
            getElement("expectedValueWeight", "expectedValue")?.value
        );

        const completenessWeight = Number(
            getElement("completenessWeight", "completeness")?.value
        );

        const openOpportunityWeight = Number(
            getElement("openOpportunityWeight", "openOpportunity")?.value
        );

        return {
            expectedValueWeight,
            completenessWeight,
            openOpportunityWeight
        };
    }

    function validateConfig(config) {
        const fields = [
            {
                key: "expectedValueWeight",
                ids: ["expectedValueWeight", "expectedValue"],
                label: "Trọng số giá trị kỳ vọng"
            },
            {
                key: "completenessWeight",
                ids: ["completenessWeight", "completeness"],
                label: "Trọng số độ đầy đủ thông tin"
            },
            {
                key: "openOpportunityWeight",
                ids: ["openOpportunityWeight", "openOpportunity"],
                label: "Trọng số cơ hội đang mở"
            }
        ];

        clearFieldErrors();

        for (const field of fields) {
            const input = getElement(...field.ids);
            const rawValue = input?.value?.trim() ?? "";
            const value = config[field.key];

            if (
                rawValue === "" ||
                !Number.isFinite(value) ||
                value < 0 ||
                value > 100
            ) {
                showFieldError(
                    field.ids,
                    `${field.label} phải nằm trong khoảng 0–100.`
                );
                return false;
            }
        }

        const total =
            config.expectedValueWeight +
            config.completenessWeight +
            config.openOpportunityWeight;

        if (Math.abs(total - 100) > 0.000001) {
            showMessage(
                `Tổng trọng số hiện tại là ${total}%. Tổng ba trọng số phải bằng 100%.`,
                "danger"
            );
            return false;
        }

        return true;
    }

    function showFieldError(ids, message) {
        const input = getElement(...ids);

        if (input) {
            input.classList.add("is-invalid");
            input.setAttribute("aria-invalid", "true");

            let feedback = input.parentElement?.querySelector(
                ".invalid-feedback"
            );

            if (!feedback) {
                feedback = document.createElement("div");
                feedback.className = "invalid-feedback";
                input.insertAdjacentElement("afterend", feedback);
            }

            feedback.textContent = message;
            feedback.style.display = "block";
        }

        showMessage(message, "danger");
    }

    function clearFieldErrors() {
        document.querySelectorAll(".is-invalid").forEach((element) => {
            element.classList.remove("is-invalid");
            element.removeAttribute("aria-invalid");
        });

        document.querySelectorAll(".invalid-feedback").forEach((element) => {
            element.textContent = "";
            element.style.display = "none";
        });
    }

    function fillConfigForm(config) {
        const fields = [
            ["expectedValueWeight", "expectedValue"],
            ["completenessWeight", "completeness"],
            ["openOpportunityWeight", "openOpportunity"]
        ];

        fields.forEach(([id1, id2]) => {
            const input = getElement(id1, id2);
            const key = id1;

            if (input && Number.isFinite(Number(config[key]))) {
                input.value = config[key];
            }
        });

        updateWeightTotal();
    }

    function updateWeightTotal() {
        const config = readConfigForm();

        const values = [
            config.expectedValueWeight,
            config.completenessWeight,
            config.openOpportunityWeight
        ];

        const totalElement = getElement(
            "weightTotal",
            "totalWeight",
            "totalPercentage"
        );

        if (!totalElement) return;

        if (values.some((value) => !Number.isFinite(value))) {
            totalElement.textContent = "Tổng: --";
            return;
        }

        const total = values.reduce((sum, value) => sum + value, 0);
        totalElement.textContent = `Tổng trọng số: ${total}%`;

        totalElement.classList.toggle("text-danger", total !== 100);
        totalElement.classList.toggle("text-success", total === 100);
    }

    async function loadConfig() {
        try {
            const result = await requestJson(CONFIG_API, {
                method: "GET"
            });

            const config = getPayload(result);

            currentConfig = {
                expectedValueWeight:
                    Number(config.expectedValueWeight ??
                        DEFAULT_CONFIG.expectedValueWeight),
                completenessWeight:
                    Number(config.completenessWeight ??
                        DEFAULT_CONFIG.completenessWeight),
                openOpportunityWeight:
                    Number(config.openOpportunityWeight ??
                        DEFAULT_CONFIG.openOpportunityWeight)
            };

            fillConfigForm(currentConfig);
        } catch (error) {
            console.error("Không tải được cấu hình:", error);
            fillConfigForm(currentConfig);
            showMessage(
                `Không tải được cấu hình từ máy chủ: ${error.message}`,
                "danger"
            );
        }
    }

    async function saveConfig(event) {
        if (event) event.preventDefault();

        const config = readConfigForm();

        if (!validateConfig(config)) return;

        const saveButton = getElement(
            "saveScoringConfig",
            "btnSaveConfig",
            "btnSave"
        );

        const originalText = saveButton?.textContent;

        if (saveButton) {
            saveButton.disabled = true;
            saveButton.textContent = "Đang lưu...";
        }

        try {
            await requestJson(CONFIG_API, {
                method: "POST",
                body: JSON.stringify(config)
            });

            currentConfig = { ...config };

            showMessage("Lưu cấu hình chấm điểm thành công.", "success");
        } catch (error) {
            console.error("Không lưu được cấu hình:", error);
            showMessage(
                `Lưu cấu hình thất bại: ${error.message}`,
                "danger"
            );
        } finally {
            if (saveButton) {
                saveButton.disabled = false;
                saveButton.textContent = originalText || "Lưu cấu hình";
            }
        }
    }

    function classifyLead(score) {
        if (score >= 70) return "Nóng";
        if (score >= 40) return "Ấm";
        return "Lạnh";
    }

    function renderLeads(leads) {
        const tbody = getElement(
            "leadScoresTableBody",
            "leadsTableBody",
            "leadTableBody"
        );

        if (!tbody) return;

        tbody.replaceChildren();

        if (!leads.length) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 6;
            cell.textContent = "Chưa có dữ liệu Lead.";
            cell.className = "text-center";
            row.appendChild(cell);
            tbody.appendChild(row);
            return;
        }

        leads.forEach((lead) => {
            const score = Number(lead.score ?? lead.leadScore ?? 0);
            const row = document.createElement("tr");

            const values = [
                lead.name ?? lead.customerName ?? lead.fullName ?? "—",
                lead.email ?? "—",
                lead.phone ?? "—",
                `${score.toFixed(2)} điểm`,
                classifyLead(score)
            ];

            values.forEach((value) => {
                const cell = document.createElement("td");
                cell.textContent = String(value);
                row.appendChild(cell);
            });

            tbody.appendChild(row);
        });
    }

    function filterLeads() {
        const searchInput = getElement(
            "leadSearch",
            "searchLead",
            "searchInput"
        );

        const keyword = (searchInput?.value ?? "")
            .trim()
            .toLocaleLowerCase("vi");

        const filtered = allLeads.filter((lead) => {
            const searchable = [
                lead.name,
                lead.customerName,
                lead.fullName,
                lead.email,
                lead.phone
            ]
                .filter(Boolean)
                .join(" ")
                .toLocaleLowerCase("vi");

            return searchable.includes(keyword);
        });

        renderLeads(filtered);
    }

    async function loadLeads() {
        try {
            const result = await requestJson(LEADS_API, {
                method: "GET"
            });

            const payload = getPayload(result);

            allLeads = Array.isArray(payload)
                ? payload
                : Array.isArray(payload?.leads)
                    ? payload.leads
                    : [];

            filterLeads();
        } catch (error) {
            console.error("Không tải được danh sách Lead:", error);

            const tbody = getElement(
                "leadScoresTableBody",
                "leadsTableBody",
                "leadTableBody"
            );

            if (tbody) {
                tbody.replaceChildren();

                const row = document.createElement("tr");
                const cell = document.createElement("td");

                cell.colSpan = 6;
                cell.textContent =
                    `Không tải được danh sách Lead: ${error.message}`;
                row.appendChild(cell);
                tbody.appendChild(row);
            }
        }
    }

    function bindEvents() {
        const form = getElement(
            "scoringConfigForm",
            "leadScoringForm",
            "configForm"
        );

        if (form) {
            form.addEventListener("submit", saveConfig);
        }

        const saveButton = getElement(
            "saveScoringConfig",
            "btnSaveConfig",
            "btnSave"
        );

        if (saveButton && saveButton.type !== "submit") {
            saveButton.addEventListener("click", saveConfig);
        }

        [
            "expectedValueWeight",
            "expectedValue",
            "completenessWeight",
            "completeness",
            "openOpportunityWeight",
            "openOpportunity"
        ].forEach((id) => {
            const input = $(id);

            if (input) {
                input.addEventListener("input", updateWeightTotal);
                input.addEventListener("input", () => {
                    input.classList.remove("is-invalid");
                });
            }
        });

        const searchInput = getElement(
            "leadSearch",
            "searchLead",
            "searchInput"
        );

        if (searchInput) {
            searchInput.addEventListener("input", filterLeads);
        }

        const reloadButton = getElement(
            "refreshLeads",
            "btnRefreshLeads"
        );

        if (reloadButton) {
            reloadButton.addEventListener("click", loadLeads);
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        bindEvents();
        loadConfig();
        loadLeads();
    });

})();
