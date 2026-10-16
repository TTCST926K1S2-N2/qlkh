<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Customer 360° | QLKH</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer-360.css?v=3">
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
        <section class="card c360-contracts" id="contracts-section">
  <div class="c360-contract-heading">
    <div><span class="section-label">H&#7906;P &#272;&#7890;NG KH&#193;CH H&#192;NG</span><h2>Gi&#225; tr&#7883; h&#7907;p &#273;&#7891;ng</h2><p>Qu&#7843;n l&#253; gi&#225; tr&#7883; v&#224; t&#236;nh tr&#7841;ng h&#7907;p &#273;&#7891;ng</p></div>
  </div>
  <div id="contract-summary" class="c360-contract-summary" role="status">&#272;ang t&#7843;i th&#7889;ng k&#234;...</div>
  <div class="c360-contract-subheading"><h3>Danh s&#225;ch h&#7907;p &#273;&#7891;ng</h3></div>
  <div id="contract-list" class="c360-contract-list">&#272;ang t&#7843;i danh s&#225;ch...</div>
  <form id="contract-form" class="c360-contract-form" style="display:none">
    <div class="c360-contract-form-head"><h3>Th&#244;ng tin h&#7907;p &#273;&#7891;ng</h3><p>Nh&#7853;p th&#244;ng tin v&#224; l&#432;u h&#7907;p &#273;&#7891;ng cho kh&#225;ch h&#224;ng</p></div>
    <input type="hidden" name="contractId" id="contract-id">
    <label>M&#227; h&#7907;p &#273;&#7891;ng <input name="contractCode" required maxlength="80" placeholder="HD2026-001"></label>
    <label>T&#234;n h&#7907;p &#273;&#7891;ng <input name="contractName" required maxlength="255" placeholder="Nh&#7853;p t&#234;n h&#7907;p &#273;&#7891;ng"></label>
    <label>Gi&#225; tr&#7883; (VN&#272;) <input name="contractValue" type="number" min="0" step="0.01" required placeholder="0"></label>
    <label>Tr&#7841;ng th&#225;i <select name="status"><option value="DRAFT">Nh&#225;p</option><option value="PENDING">Ch&#7901; k&#253;</option><option value="SIGNED">&#272;&#227; k&#253;</option><option value="CANCELLED">&#272;&#227; h&#7911;y</option></select></label>
    <label>Ng&#224;y k&#253; <input name="signedDate" type="date"></label>
    <div class="c360-contract-actions"><button type="button" id="contract-reset" class="c360-contract-reset">Nh&#7853;p m&#7899;i</button><button type="submit" class="c360-contract-save">L&#432;u h&#7907;p &#273;&#7891;ng</button></div>
  </form>
  <p id="contract-message" class="c360-contract-message" role="status"></p>
</section>
        <section class="card" id="s308-support-section" style="margin:20px 0;padding:20px;">
          <div class="card-header">
            <div>
              <span class="section-label">CHĂM SÓC KHÁCH HÀNG</span>
              <h2>Lịch sử yêu cầu hỗ trợ</h2>
              <p>Theo dõi các yêu cầu hỗ trợ và tình trạng xử lý</p>
            </div>
          </div>
          <div id="s308-risk" role="status" style="margin:14px 0;"></div>
          <div id="s308-support-list">Đang tải lịch sử hỗ trợ...</div>
          <a id="s308-support-link"
             href="${pageContext.request.contextPath}/views/sales/customer-care.jsp"
             style="display:inline-block;margin-top:16px;padding:10px 14px;background:#2563eb;color:#fff;border-radius:6px;text-decoration:none;">
             Xem và quản lý yêu cầu hỗ trợ
          </a>
        </section>
        <div class="dashboard-grid">
          <div class="dashboard-primary">
            <section class="card" id="contacts-card"><div class="card-header"><div><span class="section-label">CONTACTS</span><h2>Người liên hệ</h2><p>Danh sách người liên hệ thuộc khách hàng</p></div></div><div id="contacts-section" class="section-body">Đang tải...</div></section>
            <section class="card" id="timeline-card"><div class="card-header"><div><span class="section-label">ACTIVITY TIMELINE</span><h2>Dòng thời gian hoạt động</h2><p>Lịch sử tương tác và các thay đổi đã ghi nhận</p></div></div><div id="activities-section" class="section-body">Đang tải...</div></section>
          </div>
          <div class="dashboard-secondary">
            <section class="card"><div class="card-header"><div><span class="section-label">COMPANY PROFILE</span><h2>Thông tin doanh nghiệp</h2></div></div><div class="company-grid"><div class="info-item"><span>Ngành nghề</span><strong id="industry">-</strong></div><div class="info-item"><span>Quy mô</span><strong id="company-size">-</strong></div><div class="info-item"><span>Website</span><strong id="website">-</strong></div><div class="info-item"><span>Địa chỉ</span><strong id="address">-</strong></div></div></section>
            <section class="card"><div class="card-header"><div><span class="section-label">OPPORTUNITIES</span><h2>Cơ hội kinh doanh</h2></div></div><div id="opportunities-section" class="section-body">Đang tải...</div>

<div style="padding:16px">
  <button type="button" id="opportunity-toggle"
    style="padding:10px 16px;border:0;border-radius:6px;background:#2563eb;color:white;cursor:pointer">
    Thêm cơ hội
  </button>

  <form id="opportunity-form" hidden style="margin-top:16px">
    <label style="display:block;margin-bottom:10px">
      Tên cơ hội
      <input name="name" required maxlength="255"
        style="display:block;width:100%;padding:9px;box-sizing:border-box">
    </label>

    <label style="display:block;margin-bottom:10px">
      Giai đoạn bán hàng
      <select name="stageId" id="opportunity-stage"
        style="display:block;width:100%;padding:9px;box-sizing:border-box">
        <option value="">Chọn giai đoạn</option>
      </select>
    </label>

    <label style="display:block;margin-bottom:10px">
      Giá trị dự kiến (VNĐ)
      <input name="expectedValue" type="number" min="0" step="0.01"
        style="display:block;width:100%;padding:9px;box-sizing:border-box">
    </label>

    <label style="display:block;margin-bottom:10px">
      Ngày dự kiến chốt
      <input name="expectedCloseDate" type="date"
        style="display:block;width:100%;padding:9px;box-sizing:border-box">
    </label>

    <button type="submit"
      style="padding:10px 16px;border:0;border-radius:6px;background:#2563eb;color:white;cursor:pointer">
      Lưu cơ hội
    </button>
  </form>

  <p id="opportunity-message" role="status"></p>
</div></section>
            <section class="card"><div class="card-header"><div><span class="section-label">ATTACHMENTS</span><h2>Tệp đính kèm</h2></div></div><div id="attachments-section" class="section-body">Đang tải...</div>
<div style="padding:16px">
  <form id="attachment-upload-form" enctype="multipart/form-data">
    <label for="attachment-file">Chọn tệp (PDF, PNG, JPG, DOCX, XLSX; tối đa 10 MB)</label>
    <input id="attachment-file" name="file" type="file" accept=".pdf,.png,.jpg,.jpeg,.docx,.xlsx" required style="display:block;margin:10px 0;max-width:100%">
    <button type="submit" style="padding:10px 16px;border:0;border-radius:6px;background:#2563eb;color:white;cursor:pointer">Tải tệp lên</button>
  </form>
  <p id="attachment-upload-message" role="status"></p>
</div></section>
          </div>
        </div>
      </main>
    </div>
  </div>
</div>
<script>
window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
window.CONTRACT_CAN_WRITE = <%= ("ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("userRole"))) || "MANAGER".equalsIgnoreCase(String.valueOf(session.getAttribute("userRole")))) ? "true" : "false" %>;
</script>
<script src="${pageContext.request.contextPath}/assets/js/customer-360.js?v=7"></script>
</body>
</html>
