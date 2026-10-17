
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu hình chấm điểm Lead</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/lead-scoring.css">
</head>
<body>
    <div class="page-container">
        <header class="page-header">
            <div>
                <h1>Cấu hình chấm điểm Lead</h1>
                <p>
                    Thiết lập trọng số để đánh giá mức độ tiềm năng
                    của khách hàng.
                </p>
            </div>
        </header>

        <main>
            <section class="content-card">
                <div class="section-header">
                    <h2>Trọng số chấm điểm</h2>
                    <p>
                        Tổng trọng số của ba tiêu chí phải bằng 100%.
                    </p>
                </div>

                <div id="scoringMessage"
                     class="alert"
                     role="alert"
                     aria-live="polite"
                     style="display: none;">
                </div>

                <form id="scoringConfigForm" novalidate>
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="expectedValueWeight">
                                Giá trị kỳ vọng (%)
                            </label>
                            <input
                                type="number"
                                id="expectedValueWeight"
                                name="expectedValueWeight"
                                min="0"
                                max="100"
                                step="1"
                                required>
                            <small>
                                Đánh giá giá trị kinh doanh dự kiến của Lead.
                            </small>
                        </div>

                        <div class="form-group">
                            <label for="completenessWeight">
                                Độ đầy đủ thông tin (%)
                            </label>
                            <input
                                type="number"
                                id="completenessWeight"
                                name="completenessWeight"
                                min="0"
                                max="100"
                                step="1"
                                required>
                            <small>
                                Đánh giá mức độ hoàn thiện thông tin khách hàng.
                            </small>
                        </div>

                        <div class="form-group">
                            <label for="openOpportunityWeight">
                                Cơ hội đang mở (%)
                            </label>
                            <input
                                type="number"
                                id="openOpportunityWeight"
                                name="openOpportunityWeight"
                                min="0"
                                max="100"
                                step="1"
                                required>
                            <small>
                                Đánh giá việc Lead có cơ hội kinh doanh đang mở.
                            </small>
                        </div>
                    </div>

                    <div class="weight-summary">
                        <span>Tổng trọng số</span>
                        <strong id="weightTotal">Đang tính...</strong>
                    </div>

                    <div class="form-actions">
                        <button type="button"
                                id="resetScoringConfig"
                                class="btn btn-secondary">
                            Khôi phục mặc định
                        </button>

                        <button type="submit"
                                id="saveScoringConfig"
                                class="btn btn-primary">
                            Lưu cấu hình
                        </button>
                    </div>
                </form>
            </section>

            <section class="content-card">
                <div class="section-header">
                    <h2>Danh sách Lead và điểm đánh giá</h2>
                    <p>
                        Điểm số được tải từ hệ thống chấm điểm Lead.
                    </p>
                </div>

                <div class="table-toolbar">
                    <div class="form-group search-group">
                        <label for="leadSearch">Tìm kiếm Lead</label>
                        <input
                            type="search"
                            id="leadSearch"
                            placeholder="Nhập tên, email hoặc số điện thoại">
                    </div>

                    <button type="button"
                            id="refreshLeads"
                            class="btn btn-secondary">
                        Tải lại danh sách
                    </button>
                </div>

                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Họ và tên</th>
                                <th>Email</th>
                                <th>Số điện thoại</th>
                                <th>Điểm số</th>
                                <th>Xếp loại</th>
                            </tr>
                        </thead>
                        <tbody id="leadScoresTableBody">
                            <tr>
                                <td colspan="5" class="text-center">
                                    Đang tải dữ liệu...
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>

    <script>
        window.contextPath = "${pageContext.request.contextPath}";
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/lead-scoring.js"></script>
</body>
</html>
