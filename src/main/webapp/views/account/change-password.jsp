<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi Mật Khẩu - Hệ Thống QLKH</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome Icons -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <!-- Custom CSS cho HTQLKH-4 -->
    <link href="${pageContext.request.contextPath}/assets/css/change-password.css" rel="stylesheet">
</head>
<body>

<div class="container">
    <div class="card change-password-card p-4 bg-white">
        <h3 class="text-center mb-3 text-primary fw-bold">ĐỔI MẬT KHẨU</h3>
        <p class="text-muted text-center small mb-4">Cập nhật mật khẩu định kỳ để bảo vệ tài khoản</p>

        <!-- Khung báo lỗi từ Client JS -->
        <div id="jsErrorAlert" class="alert alert-danger alert-dismissible fade show d-none" role="alert">
            <i class="fas fa-exclamation-triangle me-2"></i>
            <span id="jsErrorMessage"></span>
        </div>

        <!-- Thông báo từ Server Backend (nếu có) -->
        <% String error = (String) request.getAttribute("errorMessage"); %>
        <% if (error != null) { %>
            <div class="alert alert-danger text-center" role="alert">
                <i class="fas fa-exclamation-circle me-1"></i> <%= error %>
            </div>
        <% } %>

        <% String success = (String) request.getAttribute("successMessage"); %>
        <% if (success != null) { %>
            <div class="alert alert-success text-center" role="alert">
                <i class="fas fa-check-circle me-1"></i> <%= success %>
                <div class="mt-3">
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-sm btn-outline-success">Đến trang đăng nhập</a>
                </div>
            </div>
        <% } else { %>

        <!-- Form Đổi Mật Khẩu -->
        <form id="changePasswordForm" action="${pageContext.request.contextPath}/change-password" method="post" novalidate>
            <!-- Mật khẩu hiện tại -->
            <div class="mb-3">
                <label for="currentPassword" class="form-label fw-semibold">Mật khẩu hiện tại <span class="text-danger">*</span></label>
                <input type="password" class="form-control" id="currentPassword" name="currentPassword" placeholder="Nhập mật khẩu đang dùng">
                <div class="invalid-feedback">Vui lòng nhập mật khẩu hiện tại.</div>
            </div>

            <!-- Mật khẩu mới -->
            <div class="mb-3">
                <label for="newPassword" class="form-label fw-semibold">Mật khẩu mới <span class="text-danger">*</span></label>
                <input type="password" class="form-control" id="newPassword" name="newPassword" placeholder="Nhập mật khẩu mới">
                <div class="invalid-feedback">Mật khẩu mới chưa hợp lệ.</div>
                
                <!-- Checklist tiêu chuẩn độ mạnh mật khẩu -->
                <ul class="password-checklist mt-2">
                    <li id="ruleLength" class="text-muted"><i class="fas fa-times-circle me-1"></i>Tối thiểu 8 ký tự</li>
                    <li id="ruleLetter" class="text-muted"><i class="fas fa-times-circle me-1"></i>Chứa ít nhất 1 chữ cái (a-z, A-Z)</li>
                    <li id="ruleNumber" class="text-muted"><i class="fas fa-times-circle me-1"></i>Chứa ít nhất 1 chữ số (0-9)</li>
                </ul>
            </div>

            <!-- Xác nhận mật khẩu mới -->
            <div class="mb-3">
                <label for="confirmPassword" class="form-label fw-semibold">Xác nhận mật khẩu mới <span class="text-danger">*</span></label>
                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu mới">
                <div class="invalid-feedback">Xác nhận mật khẩu không trùng khớp.</div>
            </div>

            <!-- Nút bấm gửi / hủy -->
            <div class="d-grid gap-2 mt-4">
                <button type="submit" class="btn btn-primary py-2 fw-semibold">Cập nhật mật khẩu</button>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-light border py-2 text-secondary">Hủy bỏ</a>
            </div>
        </form>

        <% } %>
    </div>
</div>

<!-- JS Bootstrap 5 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- Custom JS cho HTQLKH-4 -->
<script src="${pageContext.request.contextPath}/assets/js/change-password.js"></script>

</body>
</html>