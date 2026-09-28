/**
 * Logic quản lý phiên làm việc, cảnh báo đếm ngược và đăng xuất an toàn (HTQLKH-2)
 */

// Cấu hình thời gian: Phiên 15 phút, cảnh báo trước 1 phút (60 giây)
const SESSION_TIMEOUT_MS = 15 * 60 * 1000;
const WARNING_BEFORE_MS = 1 * 60 * 1000;

let idleTimer = null;
let warningTimer = null;
let countdownInterval = null;

// Làm mới bộ đếm thời gian khi có thao tác người dùng
function resetSessionTimer() {
  clearTimeout(idleTimer);
  clearTimeout(warningTimer);
  clearInterval(countdownInterval);

  // Hẹn giờ hiển thị cảnh báo trước khi hết hạn
  warningTimer = setTimeout(triggerSessionWarning, SESSION_TIMEOUT_MS - WARNING_BEFORE_MS);

  // Hẹn giờ tự động đăng xuất khi hết hạn hoàn toàn
  idleTimer = setTimeout(() => {
    performLogout(true);
  }, SESSION_TIMEOUT_MS);
}

// Bật popup cảnh báo đếm ngược
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

// Tắt popup cảnh báo và duy trì phiên
function dismissSessionWarning() {
  const modal = document.getElementById("session-modal");
  if (modal) modal.classList.remove("active");
  clearInterval(countdownInterval);
  resetSessionTimer();
}

// Thực hiện đăng xuất an toàn (xóa client cache + redirect về login)
async function performLogout(isExpired = false) {
  try {
    // Gửi request thông báo cho Backend hủy session (nếu có endpoint /logout)
    await fetch(window.location.origin + "/logout", { method: "POST" });
  } catch (e) {
    console.warn("Lỗi khi gửi yêu cầu logout:", e);
  } finally {
    // Xóa toàn bộ token / dữ liệu tạm ở client
    localStorage.clear();
    sessionStorage.clear();

    if (isExpired) {
      alert("Phiên đăng nhập đã hết hạn do không có hoạt động. Vui lòng đăng nhập lại.");
    }
    window.location.href = window.location.origin + "/views/auth/login.jsp";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  // Lắng nghe tương tác người dùng để reset timer
  const userActions = ["mousedown", "keydown", "scroll", "touchstart"];
  userActions.forEach(action => {
    window.addEventListener(action, resetSessionTimer, false);
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