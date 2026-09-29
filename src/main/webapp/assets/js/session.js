/**
 * Quản lý phiên làm việc & Đăng xuất (HTQLKH-2)
 * Quy chuẩn: 15 phút không hoạt động, popup cảnh báo 60 giây cuối.
 */
(function () {
  // Cấu hình thời gian (tính theo giây)
  const TOTAL_IDLE_SECONDS = 15 * 60; // 15 phút = 900s
  const WARNING_SECONDS = 60;         // Cảnh báo 60s cuối
  const IDLE_BEFORE_MODAL = TOTAL_IDLE_SECONDS - WARNING_SECONDS; // 14 phút = 840s

  let idleTimer = null;
  let countdownTimer = null;
  let remainingSeconds = WARNING_SECONDS;
  let isWarningActive = false;

  // Tự động nhận diện context path (/qlkh hoặc rỗng)
  function getContextPath() {
    const pathName = window.location.pathname;
    if (pathName.startsWith("/qlkh")) {
      return "/qlkh";
    }
    return "";
  }

  const contextPath = getContextPath();

  // Hàm thực hiện đăng xuất & hủy HttpSession tại backend
  function performLogout() {
    clearTimeout(idleTimer);
    clearInterval(countdownTimer);

    // Xóa storage client
    sessionStorage.clear();
    localStorage.removeItem("user_token");

    const loginRedirectUrl = contextPath + "/views/auth/login.jsp";

    // Gọi backend /logout để hủy session
    fetch(contextPath + "/logout", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" }
    })
      .catch(() => {})
      .finally(() => {
        window.location.href = loginRedirectUrl;
      });
  }

  // Đếm lùi 60 giây cuối cùng
  function startWarningCountdown() {
    isWarningActive = true;
    const modal = document.getElementById("session-modal");
    const secondsSpan = document.getElementById("session-seconds");

    if (modal) modal.classList.add("active");

    remainingSeconds = WARNING_SECONDS;
    if (secondsSpan) secondsSpan.textContent = remainingSeconds;

    if (countdownTimer) clearInterval(countdownTimer);

    countdownTimer = setInterval(() => {
      remainingSeconds--;
      if (secondsSpan) secondsSpan.textContent = remainingSeconds;

      if (remainingSeconds <= 0) {
        clearInterval(countdownTimer);
        performLogout();
      }
    }, 1000);
  }

  // Khởi động lại bộ đếm khi có hoạt động của người dùng
  function resetIdleTimer() {
    // Nếu popup đang hiện thì không tự động reset bằng cử chỉ chuột/phím,
    // người dùng phải chủ động bấm "Duy trì đăng nhập"
    if (isWarningActive) return;

    clearTimeout(idleTimer);
    idleTimer = setTimeout(() => {
      startWarningCountdown();
    }, IDLE_BEFORE_MODAL * 1000);
  }

  // Gia hạn phiên làm việc
  function extendSession() {
    const modal = document.getElementById("session-modal");
    if (countdownTimer) clearInterval(countdownTimer);
    if (modal) modal.classList.remove("active");
    isWarningActive = false;

    // Gửi tín hiệu gia hạn HttpSession tới server nếu backend có hỗ trợ
    fetch(contextPath + "/extend-session", { method: "POST" }).catch(() => {});

    // Đặt lại bộ đếm từ đầu
    resetIdleTimer();
  }

  document.addEventListener("DOMContentLoaded", () => {
    const btnExtend = document.getElementById("btn-extend-session");
    const btnLogout = document.getElementById("btn-confirm-logout");

    if (btnExtend) {
      btnExtend.addEventListener("click", (e) => {
        e.preventDefault();
        extendSession();
      });
    }

    if (btnLogout) {
      btnLogout.addEventListener("click", (e) => {
        e.preventDefault();
        performLogout();
      });
    }

    // Các sự kiện tương tác của người dùng để xác định còn đang hoạt động
    const activityEvents = ["mousemove", "mousedown", "keydown", "scroll", "touchstart"];
    activityEvents.forEach((ev) => {
      window.addEventListener(ev, resetIdleTimer, { passive: true });
    });

    // Bắt đầu đếm thời gian không hoạt động
    resetIdleTimer();
  });

  // Export để gọi nếu cần kiểm thử trên Console
  window.performLogout = performLogout;
  window.extendSession = extendSession;
  window.startWarningCountdown = startWarningCountdown;
})();