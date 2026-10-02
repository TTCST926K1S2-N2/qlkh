<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Pipeline Stages</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/sidebar.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/custom-field.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/pipeline-stages.css">
</head>

<body class="pipeline-page">

<div class="pipeline-layout">

    <aside class="pipeline-sidebar">
        <jsp:include page="/components/sidebar.jsp"/>
    </aside>

    <main class="pipeline-main">

        <section class="pipeline-header">
            <div>
                <p class="pipeline-eyebrow">QUẢN TRỊ HỆ THỐNG</p>
                <h1>Quản lý Pipeline Stages</h1>
                <p class="pipeline-description">
                    Thiết lập và quản lý các giai đoạn trong quy trình xử lý khách hàng.
                </p>
            </div>

            <button type="button"
                    class="pipeline-primary-btn"
                    id="openAddStageBtn">
                Thêm giai đoạn
            </button>
        </section>

        <section class="pipeline-summary">

            <div class="pipeline-summary-card">
                <span class="summary-label">Tổng giai đoạn</span>
                <strong id="totalStages">0</strong>
                <span class="summary-note">Các giai đoạn hiện có</span>
            </div>

            <div class="pipeline-summary-card">
                <span class="summary-label">Đang hoạt động</span>
                <strong id="activeStages">0</strong>
                <span class="summary-note">Được sử dụng trong pipeline</span>
            </div>

            <div class="pipeline-summary-card">
                <span class="summary-label">Không hoạt động</span>
                <strong id="inactiveStages">0</strong>
                <span class="summary-note">Đang tạm ẩn khỏi pipeline</span>
            </div>

        </section>

        <section class="pipeline-panel">

            <div class="pipeline-toolbar">

                <div class="pipeline-toolbar-title">
                    <h2>Danh sách giai đoạn</h2>
                    <span id="stageCountLabel">0 giai đoạn</span>
                </div>

                <div class="pipeline-search">
                    <label for="stageSearch">Tìm kiếm</label>
                    <input
                        type="text"
                        id="stageSearch"
                        placeholder="Tìm theo tên hoặc mã giai đoạn..."
                        autocomplete="off">
                </div>

            </div>

            <div class="pipeline-table-wrapper">

                <table class="pipeline-table">

                    <thead>
                    <tr>
                        <th class="order-column">Thứ tự</th>
                        <th>Tên giai đoạn</th>
                        <th>Mã giai đoạn</th>
                        <th>Trạng thái</th>
                        <th class="action-column">Thao tác</th>
                    </tr>
                    </thead>

                    <tbody id="stageTableBody"></tbody>

                </table>

                <div class="pipeline-empty" id="emptyState">
                    <h3>Chưa có giai đoạn phù hợp</h3>
                    <p>
                        Hãy thêm giai đoạn mới hoặc thay đổi từ khóa tìm kiếm.
                    </p>
                </div>

            </div>

        </section>

    </main>
</div>


<div class="pipeline-modal" id="stageModal" aria-hidden="true">

    <div class="pipeline-modal-overlay" id="modalOverlay"></div>

    <div class="pipeline-modal-dialog"
         role="dialog"
         aria-modal="true"
         aria-labelledby="stageModalTitle">

        <div class="pipeline-modal-header">
            <div>
                <p class="modal-eyebrow">PIPELINE</p>
                <h2 id="stageModalTitle">Thêm giai đoạn</h2>
            </div>

            <button type="button"
                    class="pipeline-close-btn"
                    id="closeStageModal">
                Đóng
            </button>
        </div>

        <form id="stageForm" novalidate>

            <input type="hidden" id="stageId">

            <div class="pipeline-form-grid">

                <div class="pipeline-form-group pipeline-full">
                    <label for="stageName">
                        Tên giai đoạn
                        <span>*</span>
                    </label>

                    <input
                        type="text"
                        id="stageName"
                        maxlength="100"
                        placeholder="Ví dụ: Tiềm năng"
                        autocomplete="off">

                    <small class="field-error" id="stageNameError"></small>
                </div>

                <div class="pipeline-form-group">
                    <label for="stageCode">
                        Mã giai đoạn
                        <span>*</span>
                    </label>

                    <input
                        type="text"
                        id="stageCode"
                        maxlength="50"
                        placeholder="Ví dụ: POTENTIAL"
                        autocomplete="off">

                    <small class="field-error" id="stageCodeError"></small>
                </div>

                <div class="pipeline-form-group">
                    <label for="stageStatus">
                        Trạng thái
                        <span>*</span>
                    </label>

                    <select id="stageStatus">
                        <option value="ACTIVE">Đang hoạt động</option>
                        <option value="INACTIVE">Không hoạt động</option>
                    </select>
                </div>

                <div class="pipeline-form-group pipeline-full">
                    <label for="stageDescription">
                        Mô tả
                    </label>

                    <textarea
                        id="stageDescription"
                        rows="4"
                        maxlength="300"
                        placeholder="Mô tả ngắn về giai đoạn..."></textarea>
                </div>

            </div>

            <div class="pipeline-modal-footer">

                <button type="button"
                        class="pipeline-secondary-btn"
                        id="cancelStageBtn">
                    Hủy
                </button>

                <button type="submit"
                        class="pipeline-primary-btn">
                    Lưu giai đoạn
                </button>

            </div>

        </form>

    </div>
</div>


<div class="pipeline-toast" id="pipelineToast"></div>

<script src="${pageContext.request.contextPath}/assets/js/pipeline-stages.js"></script>

</body>
</html>
