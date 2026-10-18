
(() => {
    "use strict";

    const CONTEXT_PATH = window.contextPath || "";
    const CONFIG_API = `${CONTEXT_PATH}/api/v1/lead-scoring/config`;
    const LEADS_API = `${CONTEXT_PATH}/api/v1/lead-scoring/`;

    const DEFAULT_CONFIG = {
        industryWeight: 30,
        companySizeWeight: 25,
        sourceWeight: 20,
        interestWeight: 25,
        hotThreshold: 70,
        warmThreshold: 40,
        targetIndustries: []
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
        const config = {};

        const industryInput = $("targetIndustries");
        config.targetIndustries = industryInput
            ? [...new Set(
                industryInput.value
                    .split(/\r?\n/)
                    .map(value => value.trim())
                    .filter(Boolean)
              )]
            : [];

        for (const key of Object.keys(DEFAULT_CONFIG).filter(key => key !== "targetIndustries")) {
            const input = $(key);
            config[key] = input && input.value.trim() !== ""
                ? Number(input.value)
                : NaN;
        }

        return config;
    }

    function validateConfig(config) {
        clearFieldErrors();

        for (const key of Object.keys(DEFAULT_CONFIG).filter(key => key !== "targetIndustries")) {
            const value = config[key];

            if (!Number.isFinite(value) || value < 0 || value > 100) {
                showFieldError(
                    [key],
                    `${key} phai nam trong khoang 0-100.`
                );
                return false;
            }
        }

        const total =
            config.industryWeight +
            config.companySizeWeight +
            config.sourceWeight +
            config.interestWeight;

        if (Math.abs(total - 100) > 0.000001) {
            showMessage(
                `Tong bon trong so phai bang 100%. Hien tai: ${total}%.`,
                "danger"
            );
            return false;
        }

        if (config.warmThreshold >= config.hotThreshold) {
            showMessage(
                "Nguong Lead Am phai nho hon nguong Lead Nong.",
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
        const industryInput = $("targetIndustries");
        if (industryInput) {
            industryInput.value = Array.isArray(config.targetIndustries)
                ? config.targetIndustries.join("\n")
                : "";
        }
        for (const key of Object.keys(DEFAULT_CONFIG).filter(key => key !== "targetIndustries")) {
            const input = $(key);

            if (input && Number.isFinite(Number(config[key]))) {
                input.value = config[key];
            }
        }

        updateWeightTotal();
    }

    function updateWeightTotal() {
        const config = readConfigForm();
        const weights = [
            config.industryWeight,
            config.companySizeWeight,
            config.sourceWeight,
            config.interestWeight
        ];

        const element = getElement(
            "weightTotal",
            "totalWeight",
            "totalPercentage"
        );

        if (!element) return;

        if (weights.some((value) => !Number.isFinite(value))) {
            element.textContent = "Tong trong so: --";
            return;
        }

        const total = weights.reduce((sum, value) => sum + value, 0);
        element.textContent = `Tong trong so: ${total}%`;
        element.classList.toggle("text-danger", total !== 100);
        element.classList.toggle("text-success", total === 100);
    }

    async function loadConfig() {
        try {
            const result = await requestJson(CONFIG_API, {
                method: "GET"
            });

            const config = getPayload(result);

            currentConfig = Object.fromEntries(
                Object.entries(DEFAULT_CONFIG).map(([key, fallback]) => [
                    key,
                    key === "targetIndustries"
                        ? (Array.isArray(config[key]) ? config[key] : fallback)
                        : (config[key] == null ? fallback : Number(config[key]))
                ])
            );

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
        if (score >= currentConfig.hotThreshold) return "Nóng";
        if (score >= currentConfig.warmThreshold) return "Ấm";
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
        const resetButton = $("resetScoringConfig");

        if (resetButton) {
            resetButton.addEventListener("click", () => {
                const defaults = { ...DEFAULT_CONFIG };

                Object.entries(defaults).forEach(([id, value]) => {
                    const input = $(id);

                    if (!input) return;

                    input.value = Array.isArray(value)
                        ? value.join("\n")
                        : value;

                    input.classList.remove("is-invalid");
                });

                updateWeightTotal();
                showMessage(
                    "Đã khôi phục giá trị mặc định. Bấm Lưu cấu hình để áp dụng.",
                    "info"
                );
            });
        }

        Object.keys(DEFAULT_CONFIG).forEach((id) => {
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
