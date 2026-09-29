

document.addEventListener("DOMContentLoaded", function () {
    initPermissionUI();
});

function initPermissionUI() {
    const container = document.getElementById("permission-container");
    if (!container) return;

    const currentRole = container.getAttribute("data-role") || "GUEST";

    if (currentRole === "GUEST") {
        console.log("[HTQLKH-5 FE] Chưa đăng nhập (GUEST). Hiển thị màn hình báo yêu cầu đăng nhập.");
        return;
    }

    const disabledButtons = document.querySelectorAll(".disabled-action-btn");
    disabledButtons.forEach(function (btn) {
        btn.setAttribute("aria-disabled", "true");
        btn.style.cursor = "not-allowed";
        btn.style.opacity = "0.65";
    });

    console.log("[HTQLKH-5 FE] Khởi tạo giao diện phân quyền cho vai trò:", currentRole);
}

function triggerRestrictedNotice(actionName) {
    const alertBox = document.getElementById("permission-denied-alert");
    const alertMsg = document.getElementById("permission-alert-message");

    if (alertBox && alertMsg) {
        alertMsg.innerHTML = `<strong>Cảnh báo phân quyền:</strong> Tài khoản của bạn không có quyền thực hiện thao tác "<em>${actionName}</em>". Vui lòng liên hệ Quản trị viên (ADMIN) để biết thêm chi tiết.`;
        alertBox.classList.remove("d-none");
        alertBox.scrollIntoView({ behavior: "smooth", block: "center" });
    } else {
        alert(`[CẢNH BÁO PHÂN QUYỀN]\nTài khoản của bạn không có quyền thực hiện thao tác "${actionName}".`);
    }
}

function hidePermissionAlert() {
    const alertBox = document.getElementById("permission-denied-alert");
    if (alertBox) {
        alertBox.classList.add("d-none");
    }
}

function checkPermissionAction(actionCode) {
    alert(`Thao tác [${actionCode}] hợp lệ! Quyền truy cập đã được xác nhận.`);
}