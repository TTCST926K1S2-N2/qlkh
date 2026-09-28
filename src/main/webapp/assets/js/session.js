// Thời gian hết phiên (15 phút) & cảnh báo trước 1 phút
const SESSION_TIMEOUT_MS = 15 * 60 * 1000;
const WARNING_BEFORE_MS = 1 * 60 * 1000;

let idleTimer = null;
let warningTimer = null;
let countdownInterval = null;

function resetSessionTimer() {
  clearTimeout(idleTimer);
  clearTimeout(warningTimer);
  clearInterval(countdownInterval);

  // Lên lịch cảnh báo
  warningTimer = setTimeout(triggerSessionWarning, SESSION_TIMEOUT_MS - WARNING_BEFORE_MS);

  // Lên lịch logout tự động
  idleTimer = setTimeout(() => {
    performLogout(true);
  }, SESSION_TIMEOUT_MS);
}

function triggerSessionWarning() {
  const modal = document.getElementById("session-modal");
  const countDisplay = document.getElementById("session-seconds");
  if (!modal) return;

  modal.classList.add("active");
  let remainingSeconds = 60;
  if (countDisplay) countDisplay.textContent = remainingSeconds;

  countdownInterval = setInterval(() => {
    remainingSeconds--;
    if (countDisplay) countDisplay.textContent = remainingSeconds;
    if (remainingSeconds <= 0) {
      clearInterval(countdownInterval);
    }
  }, 1000);
}

function dismissSessionWarning() {
  const modal = document.getElementById("session-modal");
  if (modal) modal.classList.remove("active");
  clearInterval(countdownInterval);
  resetSessionTimer();
}

async function performLogout(isExpired = false) {
  try {
    const token = localStorage.getItem("token");
    if (token) {
      await fetch("/api/auth/logout", {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` }
      });
    }
  } catch (e) {
    console.warn("Lỗi gọi API logout:", e);
  } finally {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    sessionStorage.clear();

    if (isExpired) {
      alert("Phiên đăng nhập đã hết hạn do không thao tác. Vui lòng đăng nhập lại.");
    }
    window.location.href = "./login.html";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  // Lắng nghe tương tác để tính phiên
  ["mousedown", "keydown", "scroll", "touchstart"].forEach(evt => {
    window.addEventListener(evt, resetSessionTimer, false);
  });
  resetSessionTimer();

  // Bắt sự kiện các nút
  const btnExtend = document.getElementById("btn-extend-session");
  const btnConfirm = document.getElementById("btn-confirm-logout");
  const btnNavLogout = document.getElementById("btn-nav-logout");

  if (btnExtend) btnExtend.addEventListener("click", dismissSessionWarning);
  if (btnConfirm) btnConfirm.addEventListener("click", () => performLogout(false));
  if (btnNavLogout) btnNavLogout.addEventListener("click", () => performLogout(false));
});