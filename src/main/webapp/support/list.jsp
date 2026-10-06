<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý yêu cầu hỗ trợ khách hàng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>📋 Danh sách yêu cầu hỗ trợ khách hàng</h2>
        <a href="${pageContext.request.contextPath}/supports/create" class="btn btn-primary">+ Ghi nhận yêu cầu mới</a>
    </div>

    <% String msg = request.getParameter("message");
       if ("create_success".equals(msg)) { %>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Ghi nhận yêu cầu hỗ trợ thành công!
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } else if ("update_success".equals(msg)) { %>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Cập nhật trạng thái yêu cầu thành công!
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>

    <div class="card shadow-sm">
        <div class="card-body">
            <table class="table table-hover align-middle">
                <thead class="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Tiêu đề yêu cầu</th>
                        <th>Khách hàng</th>
                        <th>Mức độ ưu tiên</th>
                        <th>Người xử lý</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>#SR-101</td>
                        <td>Lỗi đăng nhập hệ thống CRM</td>
                        <td>Nguyễn Văn An (KH-01)</td>
                        <td><span class="badge bg-danger">Cao</span></td>
                        <td>Trần Văn Quản Trị</td>
                        <td><span class="badge bg-warning text-dark">Đang xử lý</span></td>
                        <td>
                            <a href="#" class="btn btn-sm btn-outline-info">Chi tiết</a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
