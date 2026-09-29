<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Khóa tài khoản & Bàn giao dữ liệu</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow border-danger">
                <div class="card-header bg-danger text-white">
                    <h4 class="mb-0">Khóa tài khoản & Bàn giao dữ liệu khách hàng</h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/users/handover" method="POST">
                        <input type="hidden" name="userId" value="1">
                        
                        <div class="alert alert-warning" role="alert">
                            Bạn đang thực hiện khóa tài khoản của nhân sự: <strong>Nguyễn Văn A</strong>. Vui lòng chọn người nhận bàn giao toàn bộ dữ liệu khách hàng hiện tại của nhân sự này trước khi khóa.
                        </div>

                        <div class="mb-3">
                            <label class="form-label font-weight-bold">Trạng thái tài khoản</label>
                            <select class="form-select" name="status">
                                <option value="LOCKED" selected>Khóa tài khoản (Lock)</option>
                                <option value="ACTIVE">Mở khóa (Active)</option>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="form-label font-weight-bold">Chọn nhân sự nhận bàn giao dữ liệu (Khách hàng, Hợp đồng...)</label>
                            <select class="form-select" name="targetUserId" required>
                                <option value="">-- Chọn nhân sự thay thế --</option>
                                <option value="2">Trần Thị B</option>
                                <option value="3">Lê Văn C</option>
                            </select>
                        </div>

                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/users" class="btn btn-secondary">Quay lại</a>
                            <button type="submit" class="btn btn-danger">Xác nhận khóa và bàn giao</