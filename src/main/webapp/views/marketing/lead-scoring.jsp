
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
    <a class="s405-back-home"
       href="${pageContext.request.contextPath}/home">
        Quay lại trang chủ
    </a>
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
        <label for="industryWeight">Ngành nghề phù hợp (%)</label>
        <input type="number" id="industryWeight"
               name="industryWeight" min="0" max="100"
               step="1" value="30" required>
    </div>

    <div class="form-group">
        <label for="companySizeWeight">Quy mô công ty (%)</label>
        <input type="number" id="companySizeWeight"
               name="companySizeWeight" min="0" max="100"
               step="1" value="25" required>
    </div>

    <div class="form-group">
        <label for="sourceWeight">Nguồn Lead (%)</label>
        <input type="number" id="sourceWeight"
               name="sourceWeight" min="0" max="100"
               step="1" value="20" required>
    </div>

    <div class="form-group">
        <label for="interestWeight">Mức độ quan tâm (%)</label>
        <input type="number" id="interestWeight"
               name="interestWeight" min="0" max="100"
               step="1" value="25" required>
    </div>

    <div class="form-group">
        <label for="hotThreshold">Ngưỡng Lead Nóng</label>
        <input type="number" id="hotThreshold"
               name="hotThreshold" min="0" max="100"
               step="1" value="70" required>
    </div>

    <div class="form-group">
        <label for="warmThreshold">Ngưỡng Lead Ấm</label>
        <input type="number" id="warmThreshold"
               name="warmThreshold" min="0" max="100"
               step="1" value="40" required>
    </div>
</div>
<div class="form-group">
    <label for="targetIndustries">Danh sách ngành nghề mục tiêu</label>
    <textarea id="targetIndustries"
              name="targetIndustries"
              rows="4"
              placeholder="Mỗi ngành nghề một dòng"></textarea>
    <small>Nhập mỗi ngành nghề trên một dòng.</small>
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
