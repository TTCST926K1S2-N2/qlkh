<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Customer 360° | QLKH</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer-360.css?v=2">
</head>
<body>
<div class="app-shell">

  <div class="main-shell">

    <div class="customer360-container" id="overview">
      <a class="customer360-back c360-back-right" href="${pageContext.request.contextPath}/customers">Quay lại danh sách khách hàng</a>
<div class="page-heading"><div><span class="eyebrow">HỒ SƠ KHÁCH HÀNG</span><h1>Customer 360°</h1><p>Tổng quan thông tin và lịch sử tương tác trên một màn hình</p></div><span class="live-indicator"><span></span> Dữ liệu trực tiếp</span></div>
      <div id="loading" class="state-box">Đang tải thông tin khách hàng...</div>
      <div id="error" class="state-box error-state hidden"><h3>Không thể tải dữ liệu</h3><p id="error-message"></p><a href="${pageContext.request.contextPath}/customers">Quay lại danh sách</a></div>
      <main id="customer-content" class="hidden">
        <section class="hero-card"><div class="hero-main"><div class="hero-details"><div class="hero-caption">HỒ SƠ DOANH NGHIỆP</div><h2 id="company-name">-</h2><div class="hero-meta"><span>Mã số thuế: <b id="tax-code">-</b></span><span id="company-location">Thông tin doanh nghiệp</span></div></div></div><span id="company-status" class="status-badge">-</span></section>
        <section class="stats-grid" aria-label="Thống kê tổng quan">
          <article class="stat-card"><div class="stat-top"><span>Người liên hệ</span></div><strong id="stat-contacts">—</strong><small id="stat-contacts-note">Đang tải</small></article>
          <article class="stat-card"><div class="stat-top"><span>Hoạt động</span></div><strong id="stat-activities">—</strong><small id="stat-activities-note">Đang tải</small></article>
          <article class="stat-card"><div class="stat-top"><span>Cơ hội kinh doanh</span></div><strong id="stat-opportunities">—</strong><small id="stat-opportunities-note">Chưa tích hợp</small></article>
          <article class="stat-card"><div class="stat-top"><span>Tệp đính kèm</span></div><strong id="stat-attachments">—</strong><small id="stat-attachments-note">Chưa tích hợp</small></article>
        </section>
        <div class="dashboard-grid">
          <div class="dashboard-primary">
            <section class="card" id="contacts-card"><div class="card-header"><div><span class="section-label">CONTACTS</span><h2>Người liên hệ</h2><p>Danh sách người liên hệ thuộc khách hàng</p></div></div><div id="contacts-section" class="section-body">Đang tải...</div></section>
            <section class="card" id="timeline-card"><div class="card-header"><div><span class="section-label">ACTIVITY TIMELINE</span><h2>Dòng thời gian hoạt động</h2><p>Lịch sử tương tác và các thay đổi đã ghi nhận</p></div></div><div id="activities-section" class="section-body">Đang tải...</div></section>
          </div>
          <div class="dashboard-secondary">
            <section class="card"><div class="card-header"><div><span class="section-label">COMPANY PROFILE</span><h2>Thông tin doanh nghiệp</h2></div></div><div class="company-grid"><div class="info-item"><span>Ngành nghề</span><strong id="industry">-</strong></div><div class="info-item"><span>Quy mô</span><strong id="company-size">-</strong></div><div class="info-item"><span>Website</span><strong id="website">-</strong></div><div class="info-item"><span>Địa chỉ</span><strong id="address">-</strong></div></div></section>
            <section class="card"><div class="card-header"><div><span class="section-label">OPPORTUNITIES</span><h2>Cơ hội kinh doanh</h2></div></div><div id="opportunities-section" class="section-body">Đang tải...</div></section>
            <section class="card"><div class="card-header"><div><span class="section-label">ATTACHMENTS</span><h2>Tệp đính kèm</h2></div></div><div id="attachments-section" class="section-body">Đang tải...</div></section>
          </div>
        </div>
      </main>
    </div>
  </div>
</div>
<script>window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/assets/js/customer-360.js?v=3"></script>
</body>
</html>
