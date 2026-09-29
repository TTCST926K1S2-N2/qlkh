(function () {
    const modal = document.getElementById("session-warning-modal");
    if (!modal) return;

    // Lấy contextPath động từ JSP, không hard-code /qlkh
    const rawContextPath = modal.getAttribute("data-context-path");
    const contextPath = (rawContextPath && rawContextPath !== "/") ? rawContextPath : "";

    const countdownEl = document.getElementById("session-countdown");
    const alertEl = document.getElementById("session-modal-alert");
    const btnExtend = document.getElementById("btn-extend-session");
    const btnLogout = document.getElementById("btn-logout-session");

    // Cấu hình thời gian
    const IDLE_TIMEOUT_MS = 15 * 60 * 1000; // 15 phút không thao tác
    const COUNTDOWN_SECONDS = 60;           // Đếm ngược 60 giây
    let idleTimer = null;
    let countdownInterval = null;
    let secondsLeft = COUNTDOWN_SECONDS;

    function showAlert(message) {
        if (alertEl) {
            alertEl.textContent = message;
            alertEl.classList.remove("session-modal--hidden");
        }
    }

    function clearAlert() {
        if (alertEl) {
            alertEl.textContent = "";
            alertEl.classList.add("session-modal--hidden");
        }
    }

    function resetIdleTimer() {
        if (!modal.classList.contains("session-modal--hidden")) return;
        clearTimeout(idleTimer);
        idleTimer = setTimeout(showWarningModal, IDLE_TIMEOUT_MS);
    }

    function showWarningModal() {
        secondsLeft = COUNTDOWN_SECONDS;
        if (countdownEl) countdownEl.textContent = secondsLeft;
        clearAlert();
        if (btnExtend) btnExtend.disabled = false;
        if (btnLogout) btnLogout.disabled = false;

        modal.classList.remove("session-modal--hidden");

        clearInterval(countdownInterval);
        countdownInterval = setInterval(() => {
            secondsLeft--;
            if (countdownEl) countdownEl.textContent = secondsLeft;
            if (secondsLeft <= 0) {
                clearInterval(countdownInterval);
                performLogout();
            }
        }, 1000);
    }

    function hideModal() {
        modal.classList.add("session-modal--hidden");
        clearInterval(countdownInterval);
        clearAlert();
        resetIdleTimer();
    }

    // 1. Gia hạn phiên: CHỈ reset timer khi backend trả về HTTP 200 / thành công
    async function extendSession() {
        if (btnExtend) btnExtend.disabled = true;
        clearAlert();

        try {
            const response = await fetch(`${contextPath}/extend-session`, {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            });

            if (response.ok) {
                hideModal();
            } else {
                throw new Error(`Gia hạn thất bại (Mã lỗi: ${response.status}). Phiên có thể đã kết thúc.`);
            }
        } catch (error) {
            showAlert(error.message || "Không thể kết nối đến máy chủ để gia hạn phiên.");
        } finally {
            if (btnExtend && !modal.classList.contains("session-modal--hidden")) {
                btnExtend.disabled = false;
            }
        }
    }

    // 2. Xử lý Đăng xuất:
    // - Thành công -> chuyển rõ ràng về trang login bằng contextPath (không dùng response.url)
    // - Thất bại -> hiển thị lỗi, cho phép thử lại, KHÔNG tự chuyển hướng về login
    async function performLogout() {
        if (btnLogout) btnLogout.disabled = true;
        if (btnExtend) btnExtend.disabled = true;
        clearAlert();

        try {
            const response = await fetch(`${contextPath}/logout`, {
                method: "POST",
                headers: { "X-Requested-With": "XMLHttpRequest" }
            });

            if (response.ok) {
                window.location.href = `${contextPath}/views/auth/login.jsp`;
            } else {
                throw new Error(`Đăng xuất thất bại từ phía máy chủ (Mã lỗi: ${response.status}).`);
            }
        } catch (err) {
            showAlert(err.message || "Không thể kết nối máy chủ để đăng xuất. Vui lòng thử lại.");
            if (btnLogout) btnLogout.disabled = false;
            if (btnExtend) btnExtend.disabled = false;
        }
    }

    // Gắn sự kiện click nút bấm
    if (btnExtend) btnExtend.addEventListener("click", extendSession);
    if (btnLogout) btnLogout.addEventListener("click", performLogout);

    // Lắng nghe hoạt động người dùng để duy trì timer
    const userEvents = ["mousemove", "keydown", "click", "scroll", "touchstart"];
    userEvents.forEach(evt => {
        window.addEventListener(evt, resetIdleTimer, { passive: true });
    });

    // Khởi động đếm thời gian
    resetIdleTimer();
    window.showSessionWarning = showWarningModal;
})();