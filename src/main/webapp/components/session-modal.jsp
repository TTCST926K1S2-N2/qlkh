<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- CSS cô lập hoàn toàn cho Session Modal, không ảnh hưởng thẻ body hay trang cha -->
<style>
  .session-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.75);
    backdrop-filter: blur(4px);
    z-index: 99999;
    display: none; /* Mặc định luôn ẩn */
    align-items: center;
    justify-content: center;
    padding: 20px;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    box-sizing: border-box;
  }

  .session-modal-overlay.active {
    display: flex !important;
  }

  .session-modal-overlay .session-card {
    background: #ffffff;
    width: 100%;
    max-width: 420px;
    padding: 32px 28px;
    border-radius: 16px;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3);
    text-align: center;
    box-sizing: border-box;
    animation: sessionPopIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  }

  @keyframes sessionPopIn {
    0% { transform: scale(0.92); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  }

  .session-modal-overlay .session-header {
    margin-bottom: 20px;
  }

  .session-modal-overlay .session-title {
    font-size: 1.45rem;
    font-weight: 700;
    color: #0f172a;
    margin: 0 0 8px 0;
  }

  .session-modal-overlay .session-subtitle {
    color: #64748b;
    font-size: 0.92rem;
    line-height: 1.5;
    margin: 0;
  }

  .session-modal-overlay .session-countdown {
    color: #4f46e5;
    font-weight: 700;
    font-size: 1.05rem;
  }

  .session-modal-overlay .session-actions-group {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 24px;
  }

  .session-modal-overlay .btn-extend-main {
    width: 100%;
    padding: 12px;
    background-color: #4f46e5;
    color: #ffffff;
    border: none;
    border-radius: 8px;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }

  .session-modal-overlay .btn-extend-main:hover {
    background-color: #4338ca;
  }

  .session-modal-overlay .btn-logout-sub {
    width: 100%;
    padding: 11px;
    background-color: transparent;
    color: #64748b;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    font-size: 0.92rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .session-modal-overlay .btn-logout-sub:hover {
    background-color: #f8fafc;
    color: #0f172a;
    border-color: #94a3b8;
  }
</style>

<!-- Modal cảnh báo hết hạn phiên (mặc định không có class active) -->
<div id="session-modal" class="session-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="session-title">
  <div class="session-card">
    <div class="session-header">
      <h2 id="session-title" class="session-title">Cảnh Báo Hết Hạn Phiên</h2>
      <p class="session-subtitle">
        Phiên làm việc sắp hết hạn sau <span id="session-seconds" class="session-countdown">60</span> giây do không có hoạt động.
      </p>
    </div>

    <div class="session-actions-group">
      <button type="button" id="btn-extend-session" class="btn-extend-main">Duy trì đăng nhập</button>
      <button type="button" id="btn-confirm-logout" class="btn-logout-sub">Đăng xuất ngay</button>
    </div>
  </div>
</div>

<!-- Script xử lý session được load từ contextPath -->
<script src="${pageContext.request.contextPath}/assets/js/session.js"></script>