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

    async function loadCustomer() {

        const customerId = getCustomerId();

        if (!customerId) {
            showError("Không tìm thấy ID khách hàng trên URL.");
            return;
        }

        try {

            const response = await fetch(
                `${contextPath}/api/v1/customers/${encodeURIComponent(customerId)}`,
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

            renderCustomer(data);
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