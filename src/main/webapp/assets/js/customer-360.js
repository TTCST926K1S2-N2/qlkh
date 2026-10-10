document.addEventListener("DOMContentLoaded", () => {

    const contextPath = window.APP_CONTEXT_PATH || "";

    const loading = document.getElementById("loading");
    const error = document.getElementById("error");
    const errorMessage = document.getElementById("error-message");
    const content = document.getElementById("customer-content");

    function getCustomerId() {
        const params = new URLSearchParams(window.location.search);
        return params.get("id");
    }

    function showError(message) {
        loading.classList.add("hidden");
        content.classList.add("hidden");
        error.classList.remove("hidden");

        errorMessage.textContent = message;
    }

    function showContent() {
        loading.classList.add("hidden");
        error.classList.add("hidden");
        content.classList.remove("hidden");
    }

    function displayValue(value) {
        return value === null ||
               value === undefined ||
               value === ""
            ? "-"
            : value;
    }

    function setWebsite(website) {

        const element = document.getElementById("website");

        if (!website) {
            element.textContent = "-";
            return;
        }

        const link = document.createElement("a");

        link.href = website;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        link.textContent = website;

        element.replaceChildren(link);
    }

    function renderCustomer(customer) {

        document.getElementById("company-name").textContent =
            displayValue(customer.companyName);

        document.getElementById("tax-code").textContent =
            displayValue(customer.taxCode);

        document.getElementById("industry").textContent =
            displayValue(customer.industry);

        document.getElementById("company-size").textContent =
            displayValue(customer.companySize);

        document.getElementById("address").textContent =
            displayValue(customer.address);

        document.getElementById("company-status").textContent =
            displayValue(customer.status);

        setWebsite(customer.website);
    }


    function renderContacts(data) {
        const container = document.getElementById("contacts-section");
        if (!container) return;

        container.replaceChildren();

        if (data.availability?.contacts !== true) {
            container.textContent =
                "Chức năng người liên hệ chưa có nguồn dữ liệu.";
            return;
        }

        const contacts = Array.isArray(data.contacts)
            ? data.contacts
            : [];

        if (contacts.length === 0) {
            container.textContent =
                "Khách hàng này chưa có người liên hệ.";
            return;
        }

        const list = document.createElement("div");
        list.className = "customer360-contact-list";

        contacts.forEach(contact => {
            const item = document.createElement("div");
            item.className = "customer360-contact-item";

            const name = document.createElement("h3");
            name.textContent = contact.fullName || "Chưa có tên";

            const job = document.createElement("p");
            job.textContent = contact.jobTitle || "Chưa có chức vụ";

            const email = document.createElement("p");
            email.textContent = contact.email || "Chưa có email";

            const phone = document.createElement("p");
            phone.textContent = contact.phone || "Chưa có số điện thoại";

            item.append(name, job, email, phone);

            if (contact.isPrimary === true) {
                const primary = document.createElement("span");
                primary.textContent = "Liên hệ chính";
                primary.className = "status-badge";
                item.appendChild(primary);
            }

            list.appendChild(item);
        });

        container.appendChild(list);
    }

    function renderUnavailableSections(data) {
        const sections = [
            ["opportunities-section", "opportunities", "cơ hội kinh doanh"],
            ["activities-section", "activities", "dòng thời gian"],
            ["attachments-section", "attachments", "tệp đính kèm"]
        ];

        sections.forEach(([id, key, label]) => {
            const element = document.getElementById(id);
            if (!element) return;

            if (data.availability?.[key] !== true) {
                element.textContent =
                    `Chức năng ${label} chưa có nguồn dữ liệu.`;
                return;
            }

            const items = data[key];

            if (!Array.isArray(items)) {
                element.textContent =
                    `Không thể đọc dữ liệu ${label}.`;
                return;
            }

            if (items.length === 0) {
                element.textContent =
                    `Khách hàng chưa có ${label}.`;
                return;
            }

            element.textContent =
                `Có ${items.length} bản ghi ${label}.`;
        });
    }
    async function loadCustomer() {

        const customerId = getCustomerId();

        if (!customerId) {
            showError("Không tìm thấy ID khách hàng trên URL.");
            return;
        }

        try {

            const response = await fetch(
                `${contextPath}/api/v1/customers/360/${encodeURIComponent(customerId)}`,
                {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    },
                    credentials: "same-origin"
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Không thể tải thông tin khách hàng."
                );
            }

            if (!data.customer) {
                throw new Error("API khong tra ve thong tin khach hang.");
            }
            renderCustomer(data.customer);
            renderContacts(data);
            renderUnavailableSections(data);
            showContent();

        } catch (err) {

            console.error("Customer 360 error:", err);

            showError(
                err.message ||
                "Có lỗi xảy ra khi tải dữ liệu."
            );
        }
    }

    loadCustomer();
});