<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- Modal cảnh báo hết phiên làm việc -->
<div id="session-warning-modal" 
     class="session-modal session-modal--hidden" 
     data-context-path="${pageContext.request.contextPath}"
     role="dialog" 
     aria-modal="true" 
     aria-labelledby="session-modal-title">
    <div class="session-modal__backdrop"></div>
    <div class="session-modal__box">
        <div class="session-modal__header">
            <h3 id="session-modal-title" class="session-modal__title">Cảnh báo hết phiên làm việc</h3>
        </div>
        <div class="session-modal__body">
            <p class="session-modal__desc">Phiên làm việc của bạn sắp kết thúc do không có thao tác.</p>
            <p class="session-modal__timer">Tự động đăng xuất sau: <span id="session-countdown" class="session-modal__countdown">60</span>s</p>
            <div id="session-modal-alert" class="session-modal__alert session-modal--hidden" role="alert"></div>
        </div>
        <div class="session-modal__footer">
            <button type="button" id="btn-extend-session" class="session-btn session-btn--primary">Duy trì đăng nhập</button>
            <button type="button" id="btn-logout-session" class="session-btn session-btn--secondary">Đăng xuất</button>
        </div>
    </div>
</div>