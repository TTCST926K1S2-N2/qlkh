<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Win/Loss & Competitors</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/sidebar.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/win-loss-competitors.css">
</head>

<body>

<jsp:include page="/components/sidebar.jsp"/>

<main class="wl-page">

    <section class="wl-header">
        <div>
            <p class="wl-eyebrow">QUẢN LÝ BÁN HÀNG</p>
            <h1>Win/Loss & Competitors</h1>
            <p class="wl-description">
                Theo dõi kết quả cơ hội bán hàng và quản lý thông tin đối thủ cạnh tranh.
            </p>
        </div>

        <div class="wl-header-actions">
            <button type="button"
                    class="wl-button wl-button-secondary"
                    id="openCompetitorButton">
                Thêm đối thủ
            </button>

            <button type="button"
                    class="wl-button wl-button-primary"
                    id="openResultButton">
                Ghi nhận kết quả
            </button>
        </div>
    </section>

    <section class="wl-summary">

        <article class="wl-summary-card">
            <div class="wl-summary-label">Tổng cơ hội</div>
            <div class="wl-summary-value" id="totalDeals">0</div>
            <div class="wl-summary-note">Các cơ hội đã ghi nhận kết quả</div>
        </article>

        <article class="wl-summary-card wl-summary-card-success">
            <div class="wl-summary-label">Win</div>
            <div class="wl-summary-value" id="wonDeals">0</div>
            <div class="wl-summary-note" id="wonRate">0% tổng cơ hội</div>
        </article>

        <article class="wl-summary-card wl-summary-card-danger">
            <div class="wl-summary-label">Loss</div>
            <div class="wl-summary-value" id="lostDeals">0</div>
            <div class="wl-summary-note" id="lostRate">0% tổng cơ hội</div>
        </article>

        <article class="wl-summary-card">
            <div class="wl-summary-label">Đối thủ</div>
            <div class="wl-summary-value" id="competitorCount">0</div>
            <div class="wl-summary-note">Đối thủ đang được quản lý</div>
        </article>

    </section>

    <section class="wl-tabs">
        <button type="button"
                class="wl-tab active"
                data-tab="results">
            Kết quả Win/Loss
        </button>

        <button type="button"
                class="wl-tab"
                data-tab="competitors">
            Đối thủ cạnh tranh
        </button>
    </section>

    <section class="wl-panel" id="resultsPanel">

        <div class="wl-panel-header">
            <div>
                <h2>Lịch sử Win/Loss</h2>
                <p>Theo dõi các cơ hội đã thắng hoặc thất bại và nguyên nhân tương ứng.</p>
            </div>

            <div class="wl-filter-group">

                <div class="wl-search">
                    <input
                        type="text"
                        id="resultSearch"
                        placeholder="Tìm theo cơ hội, khách hàng..."
                        autocomplete="off">
                </div>

                <select id="resultStatusFilter" class="wl-select">
                    <option value="all">Tất cả kết quả</option>
                    <option value="won">Win</option>
                    <option value="lost">Loss</option>
                </select>
            </div>
        </div>

        <div class="wl-table-wrapper">
            <table class="wl-table">
                <thead>
                <tr>
                    <th>Cơ hội</th>
                    <th>Khách hàng</th>
                    <th>Giá trị</th>
                    <th>Kết quả</th>
                    <th>Đối thủ</th>
                    <th>Ngày ghi nhận</th>
                    <th>Nguyên nhân</th>
                    <th>Thao tác</th>
                </tr>
                </thead>

                <tbody id="resultsTableBody"></tbody>
            </table>
        </div>

        <div class="wl-empty" id="resultsEmpty">
            <h3>Chưa có dữ liệu</h3>
            <p>Hãy ghi nhận kết quả Win/Loss đầu tiên để bắt đầu quản lý.</p>
        </div>

    </section>

    <section class="wl-panel wl-hidden" id="competitorsPanel">

        <div class="wl-panel-header">
            <div>
                <h2>Đối thủ cạnh tranh</h2>
                <p>Quản lý danh sách đối thủ thường xuất hiện trong các cơ hội bán hàng.</p>
            </div>

            <div class="wl-filter-group">
                <div class="wl-search">
                    <input
                        type="text"
                        id="competitorSearch"
                        placeholder="Tìm tên hoặc mã đối thủ..."
                        autocomplete="off">
                </div>
            </div>
        </div>

        <div class="wl-table-wrapper">
            <table class="wl-table">
                <thead>
                <tr>
                    <th>Tên đối thủ</th>
                    <th>Mã đối thủ</th>
                    <th>Lĩnh vực</th>
                    <th>Mức độ cạnh tranh</th>
                    <th>Trạng thái</th>
                    <th>Ghi chú</th>
                    <th>Thao tác</th>
                </tr>
                </thead>

                <tbody id="competitorsTableBody"></tbody>
            </table>
        </div>

        <div class="wl-empty" id="competitorsEmpty">
            <h3>Chưa có đối thủ</h3>
            <p>Thêm đối thủ để xây dựng danh sách cạnh tranh của doanh nghiệp.</p>
        </div>

    </section>

</main>

<div class="wl-modal-overlay" id="resultModal">
    <div class="wl-modal">

        <div class="wl-modal-header">
            <div>
                <p class="wl-modal-eyebrow">WIN / LOSS</p>
                <h2 id="resultModalTitle">Ghi nhận kết quả</h2>
            </div>

            <button type="button"
                    class="wl-close-button"
                    data-close-modal="resultModal">
                Đóng
            </button>
        </div>

        <form id="resultForm">

            <input type="hidden" id="resultId">

            <div class="wl-form-grid">

                <div class="wl-form-field wl-form-field-full">
                    <label for="dealName">Tên cơ hội <span>*</span></label>
                    <input type="text"
                           id="dealName"
                           maxlength="120"
                           placeholder="Ví dụ: Triển khai CRM cho Công ty ABC">
                    <small class="wl-error" id="dealNameError"></small>
                </div>

                <div class="wl-form-field">
                    <label for="customerName">Khách hàng <span>*</span></label>
                    <input type="text"
                           id="customerName"
                           maxlength="120"
                           placeholder="Tên khách hàng">
                    <small class="wl-error" id="customerNameError"></small>
                </div>

                <div class="wl-form-field">
                    <label for="dealValue">Giá trị cơ hội</label>
                    <input type="number"
                           id="dealValue"
                           min="0"
                           step="100000"
                           placeholder="0">
                </div>

                <div class="wl-form-field">
                    <label for="resultStatus">Kết quả <span>*</span></label>
                    <select id="resultStatus">
                        <option value="won">Win</option>
                        <option value="lost">Loss</option>
                    </select>
                </div>

                <div class="wl-form-field">
                    <label for="resultCompetitor">Đối thủ</label>
                    <select id="resultCompetitor">
                        <option value="">Không xác định</option>
                    </select>
                </div>

                <div class="wl-form-field">
                    <label for="resultDate">Ngày ghi nhận <span>*</span></label>
                    <input type="date" id="resultDate">
                </div>

                <div class="wl-form-field wl-form-field-full">
                    <label for="resultReason">Nguyên nhân</label>
                    <textarea
                        id="resultReason"
                        rows="4"
                        maxlength="500"
                        placeholder="Mô tả nguyên nhân thắng hoặc thất bại..."></textarea>
                </div>

            </div>

            <div class="wl-modal-footer">
                <button type="button"
                        class="wl-button wl-button-secondary"
                        data-close-modal="resultModal">
                    Hủy
                </button>

                <button type="submit"
                        class="wl-button wl-button-primary">
                    Lưu kết quả
                </button>
            </div>

        </form>
    </div>
</div>

<div class="wl-modal-overlay" id="competitorModal">
    <div class="wl-modal">

        <div class="wl-modal-header">
            <div>
                <p class="wl-modal-eyebrow">COMPETITOR</p>
                <h2 id="competitorModalTitle">Thêm đối thủ</h2>
            </div>

            <button type="button"
                    class="wl-close-button"
                    data-close-modal="competitorModal">
                Đóng
            </button>
        </div>

        <form id="competitorForm">

            <input type="hidden" id="competitorId">

            <div class="wl-form-grid">

                <div class="wl-form-field">
                    <label for="competitorName">Tên đối thủ <span>*</span></label>
                    <input type="text"
                           id="competitorName"
                           maxlength="120"
                           placeholder="Tên doanh nghiệp">
                    <small class="wl-error" id="competitorNameError"></small>
                </div>

                <div class="wl-form-field">
                    <label for="competitorCode">Mã đối thủ <span>*</span></label>
                    <input type="text"
                           id="competitorCode"
                           maxlength="40"
                           placeholder="COMP-001">
                    <small class="wl-error" id="competitorCodeError"></small>
                </div>

                <div class="wl-form-field">
                    <label for="competitorIndustry">Lĩnh vực</label>
                    <input type="text"
                           id="competitorIndustry"
                           maxlength="100"
                           placeholder="Ví dụ: Phần mềm doanh nghiệp">
                </div>

                <div class="wl-form-field">
                    <label for="competitorLevel">Mức độ cạnh tranh</label>
                    <select id="competitorLevel">
                        <option value="low">Thấp</option>
                        <option value="medium" selected>Trung bình</option>
                        <option value="high">Cao</option>
                    </select>
                </div>

                <div class="wl-form-field">
                    <label for="competitorStatus">Trạng thái</label>
                    <select id="competitorStatus">
                        <option value="active">Đang hoạt động</option>
                        <option value="inactive">Không hoạt động</option>
                    </select>
                </div>

                <div class="wl-form-field wl-form-field-full">
                    <label for="competitorNote">Ghi chú</label>
                    <textarea
                        id="competitorNote"
                        rows="4"
                        maxlength="500"
                        placeholder="Thông tin cần lưu ý về đối thủ..."></textarea>
                </div>

            </div>

            <div class="wl-modal-footer">
                <button type="button"
                        class="wl-button wl-button-secondary"
                        data-close-modal="competitorModal">
                    Hủy
                </button>

                <button type="submit"
                        class="wl-button wl-button-primary">
                    Lưu đối thủ
                </button>
            </div>

        </form>
    </div>
</div>

<div class="wl-toast" id="wlToast"></div>

<script src="${pageContext.request.contextPath}/assets/js/win-loss-competitors.js"></script>

</body>
</html>
