
document.addEventListener("DOMContentLoaded", function () {
    initPermissionUI();
});

/**
 * Khởi tạo giao diện theo vai trò người dùng được truyền từ DOM
 */
function initPermissionUI() {
    const container = document.getElementById("permission-container");
    if (!container) return;

    const currentRole = container.getAttribute("data-role") || "EMPLOYEE";

    // Cấu hình giao diện các nút bị khóa
    const disabledButtons = document.querySelectorAll(".disabled-action-btn");
    disabledButtons.forEach(function (btn) {
        btn.setAttribute("aria-disabled", "true");
        btn.style.cursor = "not-allowed";
        btn.style.opacity = "0.65";
    });

    console.log("[HTQLKH-5 FE] Khởi tạo giao diện phân quyền cho vai trò:", currentRole);
}

/**
 * Hiển thị khung thông báo tiếng Việt rõ ràng khi người dùng click vào chức năng không có quyền
 * @param {string} actionName - Tên chức năng không có quyền thao tác
 */
function triggerRestrictedNotice(actionName) {
    const alertBox = document.getElementById("permission-denied-alert");
    const alertMsg = document.getElementById("permission-alert-message");

    if (alertBox && alertMsg) {
        alertMsg.innerHTML = `<strong>Cảnh báo phân quyền:</strong> Tài khoản của bạn không có quyền thực hiện thao tác "<em>${actionName}</em>". Vui lòng liên hệ Quản trị viên (ADMIN) nếu cần cấp quyền.`;
        alertBox.classList.remove("d-none");
        
        // Cuộn màn hình lên vị trí khung thông báo
        alertBox.scrollIntoView({ behavior: "smooth", block: "center" });
    } else {
        alert(`[CẢNH BÁO PHÂN QUYỀN]\nTài khoản của bạn không có quyền thực hiện thao tác "${actionName}".`);
    }
}

/**
 * Ẩn khung thông báo cảnh báo phân quyền
 */
function hidePermissionAlert() {
    const alertBox = document.getElementById("permission-denied-alert");
    if (alertBox) {
        alertBox.classList.add("d-none");
    }
}

/**
 * Thực thi phản hồi giả lập cho các thao tác được phép
 * @param {string} actionCode - Mã hành động
 */
function checkPermissionAction(actionCode) {
    alert(`Thao tác [${actionCode}] hợp lệ! Quyền truy cập đã được xác nhận.`);
}