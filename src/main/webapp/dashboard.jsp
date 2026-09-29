<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%
    String userRole = (String) session.getAttribute("userRole");
    String username = (String) session.getAttribute("username");
    if (userRole == null) {
        userRole = "GUEST";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Khách Hàng - Phân Quyền</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; }
        .navbar { background-color: #2b6cb0; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .container { padding: 30px; max-width: 1200px; margin: auto; }
        .role-badge { background-color: #e2e8f0; color: #2d3748; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 12px; }
        .actions-bar { margin-bottom: 20px; display: flex; gap: 10px; }
        .btn { padding: 8px 16px; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; text-decoration: none; display: inline-block; }
        .btn-primary { background-color: #3182ce; color: white; }
        .btn-success { background-color: #38a169; color: white; }
        .btn-danger { background-color: #e53e3e; color: white; }
        .btn-warning { background-color: #dd6b20; color: white; }
        table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #e2e8f0; }
        th { background-color: #edf2f7; color: #2d3748; }
    </style>
</head>
<body>

<div class="navbar">
    <h2>Hệ Thống Quản Lý Khách Hàng (QLKH)</h2>
    <div>
        Xin chào, <strong><%= username != null ? username : "Người dùng" %></strong> 
        <span class="role-badge"><%= userRole %></span>
        <a href="${pageContext.request.contextPath}/change-password" class="btn btn-warning" style="margin-left: 15px;">Đổi Mật Khẩu</a>
    </div>
</div>

<div class="container">
    <h3>Danh Sách Khách Hàng</h3>
    <br>

    <!-- HIỂN THỊ NÚT CHỨC NĂNG THEO QUYỀN -->
    <div class="actions-bar">
        <% if ("ADMIN".equals(userRole) || "MANAGER".equals(userRole)) { %>
            <a href="#" class="btn btn-success">+ Thêm Khách Hàng Mới</a>
            <a href="#" class="btn btn-primary">Xuất Báo Cáo (Excel)</a>
        <% } %>

        <% if ("ADMIN".equals(userRole)) { %>
            <a href="#" class="btn btn-danger">Quản Lý Nguời Dùng / Phân Quyền</a>
        <% } %>
    </div>

    <!-- BẢNG DỮ LIỆU KHÁCH HÀNG -->
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Tên Khách Hàng</th>
                <th>Số Điện Thoại</th>
                <th>Email</th>
                <% if (!"EMPLOYEE".equals(userRole)) { %>
                    <th>Doanh Số</th>
                <% } %>
                <th>Thao Tác</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>KH001</td>
                <td>Công ty TNHH Á Châu</td>
                <td>0912345678</td>
                <td>contact@achau.vn</td>
                <% if (!"EMPLOYEE".equals(userRole)) { %>
                    <td>150,000,000 VNĐ</td>
                <% } %>
                <td>
                    <a href="#" class="btn btn-primary" style="padding: 4px 8px; font-size: 12px;">Xem</a>
                    <% if ("ADMIN".equals(userRole) || "MANAGER".equals(userRole)) { %>
                        <a href="#" class="btn btn-warning" style="padding: 4px 8px; font-size: 12px;">Sửa</a>
                    <% } %>
                    <% if ("ADMIN".equals(userRole)) { %>
                        <a href="#" class="btn btn-danger" style="padding: 4px 8px; font-size: 12px;">Xóa</a>
                    <% } %>
                </td>
            </tr>
        </tbody>
    </table>
</div>

</body>
</html>