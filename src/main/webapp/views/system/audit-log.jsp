<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Nhật ký thay đổi hệ thống.</title>

    <!-- Bootstrap 5 -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
        rel="stylesheet">

    <!-- Audit Log CSS -->
    <link
        href="${pageContext.request.contextPath}/assets/css/audit-log.css"
        rel="stylesheet">
</head>

<body class="bg-light">

<script>
    window.contextPath = "${pageContext.request.contextPath}";
</script>

<div class="container-fluid py-4 px-4">

    <!-- Tiêu đề -->
    <div class="mb-4">
        <h3 class="fw-bold text-dark mb-1">
            Nhật ký thay đổi hệ thống.
        </h3>

        <p class="text-muted small mb-0">
            Theo dõi chi tiết các thao tác và biến động dữ liệu trong hệ thống.
        </p>
    </div>

    <!-- Bộ lọc -->
    <div class="card audit-log-card p-3 mb-4 bg-white">

        <form id="searchForm" class="row g-3">

            <!-- Người thực hiện -->
            <div class="col-md-3">
                <label
                    for="usernameFilter"
                    class="form-label fw-semibold small text-secondary">
                    Người thực hiện
                </label>

                <input
                    type="text"
                    class="form-control bg-light"
                    id="usernameFilter"
                    placeholder="Nhập tên người thực hiện">
            </div>

            <!-- Loại đối tượng -->
            <div class="col-md-3">
                <label
                    for="targetObjectFilter"
                    class="form-label fw-semibold small text-secondary">
                    Loại đối tượng
                </label>

                <select
                    class="form-select bg-light"
                    id="targetObjectFilter">

                    <option value="">-- Tất cả --</option>
                    <option value="USER">USER</option>
                    <option value="USER_ROLE">USER_ROLE</option>
                    <option value="ROLE">ROLE</option>
                    <option value="PRODUCT">PRODUCT</option>
                    <option value="SERVICE">SERVICE</option>
                    <option value="CUSTOMER">CUSTOMER</option>
                </select>
            </div>

            <!-- Hành động -->
            <div class="col-md-2">
                <label
                    for="actionFilter"
                    class="form-label fw-semibold small text-secondary">
                    Hành động
                </label>

                <select
                    class="form-select bg-light"
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
                <label
                    for="startDate"
                    class="form-label fw-semibold small text-secondary">
                    Từ ngày
                </label>

                <input
                    type="date"
                    class="form-control bg-light"
                    id="startDate">
            </div>

            <!-- Đến ngày -->
            <div class="col-md-2">
                <label
                    for="endDate"
                    class="form-label fw-semibold small text-secondary">
                    Đến ngày
                </label>

                <input
                    type="date"
                    class="form-control bg-light"
                    id="endDate">
            </div>

            <!-- Nút -->
            <div class="col-12 d-flex justify-content-end gap-2">

                <button
                    type="submit"
                    class="btn btn-primary fw-medium">
                    Lọc
                </button>

                <button
                    type="button"
                    id="btnReset"
                    class="btn btn-outline-secondary">
                    Đặt lại
                </button>

            </div>

        </form>
    </div>

    <!-- Danh sách nhật ký -->
    <div class="card audit-log-card bg-white p-3">

        <!-- Loading -->
        <div
            id="loadingState"
            class="text-center py-5">

            <div
                class="spinner-border text-primary"
                role="status">

                <span class="visually-hidden">
                    Đang tải...
                </span>

            </div>

            <p class="text-muted mt-2 mb-0">
                Đang lấy dữ liệu nhật ký hệ thống...
            </p>
        </div>

        <!-- Bảng -->
        <div
            id="tableContainer"
            class="table-responsive d-none">

            <table
                class="table table-hover table-audit align-middle mb-0">

                <thead>
                    <tr>

                        <th
                            class="text-center"
                            style="width: 60px;">
                            STT
                        </th>

                        <th style="width: 220px;">
                            Người thực hiện
                        </th>

                        <th
                            class="text-center"
                            style="width: 130px;">
                            Hành động
                        </th>

                        <th style="width: 180px;">
                            Loại đối tượng
                        </th>

                        <th>
                            Dữ liệu trước
                        </th>

                        <th>
                            Dữ liệu sau
                        </th>

                        <th style="width: 180px;">
                            Thời gian
                        </th>

                    </tr>
                </thead>

                <tbody id="logTableBody">
                </tbody>

            </table>
        </div>

        <!-- Không có dữ liệu -->
        <div
            id="emptyState"
            class="text-center py-5 d-none">

            <div class="empty-state-icon">
                Không có dữ liệu
            </div>

            <h5 class="fw-bold text-secondary">
                Không tìm thấy dữ liệu nhật ký
            </h5>

            <p class="text-muted small">
                Chưa có bản ghi nhật ký nào phù hợp với bộ lọc của bạn.
            </p>

        </div>

        <!-- Lỗi -->
        <div
            id="errorState"
            class="text-center py-5 d-none">

            <div class="error-state-icon">
                Lỗi
            </div>

            <h5 class="fw-bold text-danger">
                Đã có lỗi xảy ra!
            </h5>

            <p
                id="errorMessageText"
                class="text-muted small mb-3">
                Không thể kết nối tới server.
            </p>

            <button
                id="btnRetry"
                type="button"
                class="btn btn-sm btn-outline-danger">
                Thử lại
            </button>

        </div>

    </div>

</div>

<!-- Bootstrap JS -->
<script
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
</script>

<!-- Audit Log JS -->
<script
    src="${pageContext.request.contextPath}/assets/js/audit-log.js">
</script>

</body>
</html>