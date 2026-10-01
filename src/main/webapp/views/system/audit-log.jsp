<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Nhật Ký Thay Đổi - Hệ Thống QLKH</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS cho Audit Log -->
    <link href="${pageContext.request.contextPath}/assets/css/audit-log.css" rel="stylesheet">
</head>

<body class="bg-light">

<script>
    // Khai báo ContextPath toàn cục cho file JS
    window.contextPath = "${pageContext.request.contextPath}";
</script>

<div class="container-fluid py-4 px-4">

    <!-- Tiêu đề màn hình -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold text-dark mb-1">
                Nhật Ký Thay Đổi System
            </h3>

            <p class="text-muted small mb-0">
                Theo dõi chi tiết các thao tác biến động dữ liệu trong hệ thống
            </p>
        </div>
    </div>


    <!-- Khung tìm kiếm & bộ lọc -->
    <div class="card audit-log-card p-3 mb-4 bg-white">

        <form id="searchForm" class="row g-3">

            <!-- Từ khóa -->
            <div class="col-md-4">

                <label for="searchKeyword"
                       class="form-label fw-semibold small text-secondary">
                    Từ khóa tìm kiếm
                </label>

                <input type="text"
                       class="form-control bg-light"
                       id="searchKeyword"
                       placeholder="Người thực hiện, đối tượng...">

            </div>


            <!-- Hành động -->
            <div class="col-md-2">

                <label for="actionFilter"
                       class="form-label fw-semibold small text-secondary">
                    Hành động
                </label>

                <select class="form-select bg-light"
                        id="actionFilter">

                    <option value="">-- Tất cả --</option>
                    <option value="CREATE">Thêm mới</option>
                    <option value="UPDATE">Cập nhật</option>
                    <option value="DELETE">Xóa</option>
                    <option value="LOGIN">Đăng nhập</option>

                </select>

            </div>


            <!-- Từ ngày -->
            <div class="col-md-2">

                <label for="startDate"
                       class="form-label fw-semibold small text-secondary">
                    Từ ngày
                </label>

                <input type="date"
                       class="form-control bg-light"
                       id="startDate">

            </div>


            <!-- Đến ngày -->
            <div class="col-md-2">

                <label for="endDate"
                       class="form-label fw-semibold small text-secondary">
                    Đến ngày
                </label>

                <input type="date"
                       class="form-control bg-light"
                       id="endDate">

            </div>


            <!-- Nút lọc -->
            <div class="col-md-2 d-flex align-items-end gap-2">

                <button type="submit"
                        class="btn btn-primary flex-fill fw-medium">
                    Lọc
                </button>

                <button type="button"
                        id="btnReset"
                        class="btn btn-outline-secondary">
                    Đặt lại
                </button>

            </div>

        </form>

    </div>


    <!-- Khung hiển thị danh sách nhật ký -->
    <div class="card audit-log-card bg-white p-3">


        <!-- 1. Trạng thái đang tải dữ liệu -->
        <div id="loadingState"
             class="text-center py-5">

            <div class="spinner-border text-primary"
                 role="status">

                <span class="visually-hidden">
                    Đang tải...
                </span>

            </div>

            <p class="text-muted mt-2 mb-0">
                Đang lấy dữ liệu nhật ký hệ thống...
            </p>

        </div>


        <!-- 2. Bảng danh sách nhật ký -->
        <div id="tableContainer"
             class="table-responsive d-none">

            <table class="table table-hover table-audit align-middle mb-0">

                <thead>

                    <tr>

                        <th class="text-center"
                            style="width: 60px;">
                            STT
                        </th>

                        <th style="width: 220px;">
                            Người thực hiện
                        </th>

                        <th class="text-center"
                            style="width: 140px;">
                            Hành động
                        </th>

                        <th>
                            Đối tượng & Dữ liệu thay đổi
                        </th>

                        <th style="width: 180px;">
                            Thời gian
                        </th>

                    </tr>

                </thead>

                <tbody id="logTableBody">
                    <!-- Dữ liệu JS sẽ render vào đây -->
                </tbody>

            </table>

        </div>


        <!-- 3. Trạng thái không có dữ liệu -->
        <div id="emptyState"
             class="text-center py-5 d-none">

            <div class="empty-state-icon"></div>

            <h5 class="fw-bold text-secondary">
                Không tìm thấy dữ liệu nhật ký
            </h5>

            <p class="text-muted small">
                Chưa có bản ghi nhật ký nào phù hợp với bộ lọc của bạn.
            </p>

        </div>


        <!-- 4. Trạng thái lỗi API -->
        <div id="errorState"
             class="text-center py-5 d-none">

            <div class="error-state-icon"></div>

            <h5 class="fw-bold text-danger">
                Đã có lỗi xảy ra!
            </h5>

            <p id="errorMessageText"
               class="text-muted small mb-3">
                Không thể kết nối tới server.
            </p>

            <button onclick="location.reload()"
                    class="btn btn-sm btn-outline-danger">
                Thử lại
            </button>

        </div>

    </div>

</div>


<!-- JS Bootstrap 5 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- JS Audit Log -->
<script src="${pageContext.request.contextPath}/assets/js/audit-log.js"></script>

</body>
</html>