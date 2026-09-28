/**
 * Logic quản lý phiên và cảnh báo hết hạn (HTQLKH-2)
 */
const SESSION_TIMEOUT_MS = 15 * 60 * 1000;
const WARNING_BEFORE_MS = 1 * 60 * 1000;

let idleTimer = null;
let warningTimer = null;
let countdownInterval = null;

function resetSessionTimer() {
  clearTimeout(idleTimer);
  clearTimeout(warningTimer);
  clearInterval(countdownInterval);

  warningTimer = setTimeout(triggerSessionWarning, SESSION_TIMEOUT_MS - WARNING_BEFORE_MS);

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
    await fetch(window.location.origin + "/logout", { method: "POST" });
  } catch (e) {
    console.warn("Lỗi khi gửi yêu cầu logout:", e);
  } finally {
    localStorage.clear();
    sessionStorage.clear();

    if (isExpired) {
      alert("Phiên đăng nhập đã hết hạn do không có hoạt động. Vui lòng đăng nhập lại.");
    }
    window.location.href = window.location.origin + "/views/auth/login.jsp";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const userActions = ["mousedown", "keydown", "scroll", "touchstart"];
  userActions.forEach(action => {
    window.addEventListener(action, resetSessionTimer, false);
  });
  resetSessionTimer();

  const btnExtend = document.getElementById("btn-extend-session");
  const btnConfirm = document.getElementById("btn-confirm-logout");
  const btnNavLogout = document.getElementById("btn-nav-logout");

  if (btnExtend) btnExtend.addEventListener("click", dismissSessionWarning);
  if (btnConfirm) btnConfirm.addEventListener("click", () => performLogout(false));
  if (btnNavLogout) btnNavLogout.addEventListener("click", () => performLogout(false));
});