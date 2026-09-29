<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Gán Vai Trò & Nhóm Kinh Doanh</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow">
                <div class="card-header bg-dark text-white">
                    <h4 class="mb-0">Phân quyền & Nhóm kinh doanh cho tài khoản: Nguyễn Văn A</h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/users/role-group" method="POST">
                        <input type="hidden" name="userId" value="1">
                        
                        <div class="mb-4">
                            <h5 class="text-primary">1. Gán vai trò (Roles)</h5>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="roles" value="ADMIN" id="roleAdmin">
                                <label class="form-check-label" for="roleAdmin">Quản trị hệ thống (Admin)</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="roles" value="MANAGER" id="roleManager" checked>
                                <label class="form-check-label" for="roleManager">Quản lý kinh doanh (Manager)</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="roles" value="SALES" id="roleSales" checked>
                                <label class="form-check-label" for="roleSales">Nhân viên kinh doanh (Sales)</label>
                            </div>
                        </div>

                        <div class="mb-4">
                            <h5 class="text-primary">2. Gán nhóm kinh doanh (Business Groups)</h5>
                            <select class="form-select" name="groupId" required>
                                <option value="">-- Chọn nhóm kinh doanh --</option>
                                <option value="1" selected>Nhóm Miền Bắc</option>
                                <option value="2">Nhóm Miền Nam</option>
                                <option value="3">Nhóm Key Account</option>
                            </select>
                        </div>

                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/users" class="btn btn-secondary">Quay lại</a>
                            <button type="submit" class="btn btn-primary">Lưu cấu hình</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>