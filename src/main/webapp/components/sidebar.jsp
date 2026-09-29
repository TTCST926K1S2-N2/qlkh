<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String role = (String) session.getAttribute("userRole");
    if (role == null) {
        role = "GUEST";
    }
%>

<style>
    .sidebar {
        width: 280px;
        min-height: 100vh;
        background-color: #2c3e50;
        color: #ecf0f1;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        box-shadow: 2px 0 10px rgba(0,0,0,0.1);
        display: flex;
        flex-direction: column;
    }
    .sidebar-header {
        padding: 25px 20px;
        font-size: 1.2rem;
        font-weight: bold;
        text-align: center;
        background-color: #1a252f;
        border-bottom: 1px solid #34495e;
        letter-spacing: 1px;
    }
    .menu-category {
        font-size: 0.75rem;
        color: #7f8c8d;
        text-transform: uppercase;
        letter-spacing: 1px;
        padding: 25px 20px 10px 20px;
        font-weight: 600;
    }
    .sidebar-menu {
        list-style-type: none;
        padding: 0;
        margin: 0;
    }
    .sidebar-menu li a {
        display: block;
        padding: 12px 20px;
        color: #bdc3c7;
        text-decoration: none;
        font-size: 15px;
        transition: all 0.2s ease;
    }
    .sidebar-menu li a:hover {
        background-color: #34495e;
        color: #3498db;
        padding-left: 28px; /* Hiệu ứng trượt khi hover */
    }
    /* Style cho menu đang được chọn (Active) */
    .sidebar-menu li a.active {
        background-color: #3498db;
        color: #ffffff;
        font-weight: 500;
        border-left: 4px solid #ecf0f1;
        padding-left: 16px;
    }
    .sidebar-menu li a.active:hover {
        padding-left: 16px; /* Giữ nguyên không trượt khi đang active */
    }
</style>

<div class="sidebar">
    <div class="sidebar-header">
        HỆ THỐNG QLKH
    </div>

    <!-- NHÓM TỔNG QUAN -->
    <div class="menu-category">Tổng quan</div>
    <ul class="sidebar-menu">
        <li><a href="${pageContext.request.contextPath}/" class="active">Trang chủ</a></li>
    </ul>

    <!-- NHÓM QUẢN LÝ -->
    <% if ("EMPLOYEE".equals(role) || "MANAGER".equals(role) || "ADMIN".equals(role)) { %>
        <div class="menu-category">Quản lý</div>
        <ul class="sidebar-menu">
            <li><a href="${pageContext.request.contextPath}/customers">Quản lý khách hàng</a></li>
        </ul>
    <% } %>

    <!-- NHÓM QUẢN TRỊ -->
    <% if ("ADMIN".equals(role)) { %>
        <div class="menu-category">Quản trị</div>
        <ul class="sidebar-menu">
            <li><a href="${pageContext.request.contextPath}/users">Quản lý tài khoản</a></li>
            <li><a href="${pageContext.request.contextPath}/roles">Phân quyền & Nhóm nghiệp vụ</a></li>
            <li><a href="${pageContext.request.contextPath}/lock-transfer">Khóa tài khoản & Bàn giao dữ liệu</a></li>
        </ul>
    <% } %>

    <!-- NHÓM TÀI KHOẢN -->
    <div class="menu-category">Tài khoản</div>
    <ul class="sidebar-menu">
        <% if (!"GUEST".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/profile">Hồ sơ cá nhân</a></li>
            <li><a href="${pageContext.request.contextPath}/change-password">Đổi mật khẩu</a></li>
            <li><a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></li>
        <% } else { %>
            <li><a href="${pageContext.request.contextPath}/login">Đăng nhập</a></li>
        <% } %>
    </ul>
</div>