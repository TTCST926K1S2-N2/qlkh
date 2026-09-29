(function () {
    const modal = document.getElementById("session-warning-modal");
    if (!modal) return;

    // 1. Lấy contextPath động từ JSP, loại bỏ hard-code
    const rawContextPath = modal.getAttribute("data-context-path");
    const contextPath = (rawContextPath && rawContextPath !== "/") ? rawContextPath : "";

    const countdownEl = document.getElementById("session-countdown");
    const alertEl = document.getElementById("session-modal-alert");
    const btnExtend = document.getElementById("btn-extend-session");
    const btnLogout = document.getElementById("btn-logout-session");

    // Cấu hình thời gian
    const IDLE_TIMEOUT_MS = 15 * 60 * 1000; // 15 phút nhàn rỗi thì hiện modal
    const COUNTDOWN_SECONDS = 60;           // Đếm ngược 60s
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

    // 2. Gia hạn phiên: CHỈ reset timer khi backend trả về kết quả thành công
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
                // Thành công: đóng modal và khởi động lại timer
                hideModal();
            } else {
                throw new Error(`Gia hạn thất bại (Mã lỗi: ${response.status}). Phiên có thể đã kết thúc.`);
            }
        } catch (error) {
            // Thất bại: Giữ nguyên modal, không reset timer, hiển thị cảnh báo
            showAlert(error.message || "Không thể kết nối đến máy chủ để gia hạn phiên.");
        } finally {
            if (btnExtend && !modal.classList.contains("session-modal--hidden")) {
                btnExtend.disabled = false;
            }
        }
    }

    // 4. Xử lý logout an toàn
    async function performLogout() {
        if (btnLogout) btnLogout.disabled = true;
        if (btnExtend) btnExtend.disabled = true;

        try {
            const response = await fetch(`${contextPath}/logout`, {
                method: "POST",
                headers: { "X-Requested-With": "XMLHttpRequest" }
            });

            if (response.ok || response.redirected) {
                window.location.href = response.url || `${contextPath}/views/auth/login.jsp`;
            } else {
                throw new Error("Máy chủ từ chối yêu cầu đăng xuất.");
            }
        } catch (err) {
            // Logout thất bại hoặc lỗi mạng: fallback chuyển hướng trực tiếp về trang login
            showAlert("Đăng xuất có sự cố, đang đưa bạn về trang đăng nhập...");
            setTimeout(() => {
                window.location.href = `${contextPath}/views/auth/login.jsp`;
            }, 1000);
        }
    }

    // Gắn sự kiện click
    if (btnExtend) btnExtend.addEventListener("click", extendSession);
    if (btnLogout) btnLogout.addEventListener("click", performLogout);

    // Lắng nghe thao tác người dùng để duy trì timer khi chưa hiện popup
    const userEvents = ["mousemove", "keydown", "click", "scroll", "touchstart"];
    userEvents.forEach(evt => {
        window.addEventListener(evt, resetIdleTimer, { passive: true });
    });

    // Bắt đầu đếm thời gian
    resetIdleTimer();

    // Hỗ trợ kiểm thử thủ công qua console nếu cần
    window.showSessionWarning = showWarningModal;
})();