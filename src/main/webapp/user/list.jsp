<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý tài khoản - Danh sách</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Danh sách tài khoản hệ thống</h2>
        <a href="${pageContext.request.contextPath}/users/create" class="btn btn-primary">+ Thêm tài khoản mới</a>
    </div>
    <div class="card shadow-sm">
        <div class="card-body">
            <table class="table table-striped table-hover align-middle">
                <thead class="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>Số điện thoại</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>1</td>
                        <td>Nguyễn Văn A</td>
                        <td>admin@ictu.edu.vn</td>
                        <td>0912345678</td>
                        <td><span class="badge bg-success">Hoạt động</span></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/users/edit?id=1" class="btn btn-sm btn-warning">Sửa</a>
                            <a href="${pageContext.request.contextPath}/users/role-group?id=1" class="btn btn-sm btn-info">Phân quyền</a>
                            <a href="${pageContext.request.contextPath}/users/handover?id=1" class="btn btn-sm btn-secondary">Bàn giao/Khóa</a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>