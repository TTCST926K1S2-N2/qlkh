<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Customer 360°</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/customer-360.css">
</head>

<body>

<div class="customer360-container">

    <div class="page-header">
        <div>
            <a href="${pageContext.request.contextPath}/customers"
               class="back-link">
                Danh sách khách hàng
            </a>

            <h1>Customer 360°</h1>
            <p>Thông tin tổng quan và hoạt động của khách hàng</p>
        </div>
    </div>

    <div id="loading" class="state-box">
        Đang tải thông tin khách hàng...
    </div>

    <div id="error" class="state-box error-state hidden">
        <h3>Không thể tải dữ liệu</h3>
        <p id="error-message"></p>
    </div>

    <main id="customer-content" class="hidden">

        <!-- THÔNG TIN CÔNG TY -->
        <section class="card company-card">
            <div class="card-header">
                <div>
                    <span class="section-label">THÔNG TIN CÔNG TY</span>
                    <h2 id="company-name">-</h2>
                </div>

                <span id="company-status" class="status-badge">-</span>
            </div>

            <div class="company-grid">

                <div class="info-item">
                    <span>Mã số thuế</span>
                    <strong id="tax-code">-</strong>
                </div>

                <div class="info-item">
                    <span>Ngành nghề</span>
                    <strong id="industry">-</strong>
                </div>

                <div class="info-item">
                    <span>Quy mô</span>
                    <strong id="company-size">-</strong>
                </div>

                <div class="info-item">
                    <span>Website</span>
                    <strong id="website">-</strong>
                </div>

                <div class="info-item full-width">
                    <span>Địa chỉ</span>
                    <strong id="address">-</strong>
                </div>

            </div>
        </section>


        <!-- NGƯỜI LIÊN HỆ -->
        <section class="card">

            <div class="card-header">
                <div>
                    <span class="section-label">CONTACTS</span>
                    <h2>Người liên hệ</h2>
                </div>
            </div>

            <div id="contacts-section" class="empty-state">Đang tải danh sách người liên hệ...</div>

        </section>


        <!-- CƠ HỘI KINH DOANH -->
        <section class="card">

            <div class="card-header">
                <div>
                    <span class="section-label">OPPORTUNITIES</span>
                    <h2>Cơ hội kinh doanh</h2>
                </div>
            </div>

            <div id="opportunities-section" class="empty-state">Dang tai du lieu...</div>

        </section>


        <!-- DÒNG THỜI GIAN -->
        <section class="card">

            <div class="card-header">
                <div>
                    <span class="section-label">TIMELINE</span>
                    <h2>Dòng thời gian hoạt động</h2>
                </div>
            </div>

            <div id="activities-section" class="empty-state">Dang tai du lieu...</div>

        </section>


        <!-- TỆP ĐÍNH KÈM -->
        <section class="card">

            <div class="card-header">
                <div>
                    <span class="section-label">ATTACHMENTS</span>
                    <h2>Tệp đính kèm</h2>
                </div>
            </div>

            <div id="attachments-section" class="empty-state">Dang tai du lieu...</div>

        </section>

    </main>

</div>

<script>
    window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>

<script src="${pageContext.request.contextPath}/assets/js/customer-360.js"></script>

</body>
</html>