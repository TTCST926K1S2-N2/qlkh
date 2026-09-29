document.addEventListener("DOMContentLoaded", function () {
    const container = document.getElementById("permission-container");

    if (!container) {
        return;
    }

    const scope = container.dataset.scope || "";

    let scopeText = "Chưa xác định";

    switch (scope) {
        case "MY":
            scopeText = "Của tôi";
            break;

        case "TEAM":
            scopeText = "Của nhóm tôi";
            break;

        case "ALL":
            scopeText = "Tất cả";
            break;

        default:
            scopeText = "Chưa xác định";
            break;
    }

    const labels = document.querySelectorAll(".scope-label");

    labels.forEach(function (label) {
        label.textContent = scopeText;
    });
});