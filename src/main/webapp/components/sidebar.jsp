<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String role = (String) session.getAttribute("userRole");
    if (role == null) {
        role = "GUEST";
    }
%>

<style>
    .sidebar {
        width: 260px;
        min-height: 100vh;
        background-color: #2c3e50;
        color: #ecf0f1;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        box-shadow: 2px 0 10px rgba(0,0,0,0.1);
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
    .sidebar-menu {
        list-style-type: none;
        padding: 0;
        margin: 0;
    }
    .sidebar-menu li {
        border-bottom: 1px solid #34495e;
    }
    .sidebar-menu a {
        display: block;
        padding: 16px 20px;
        color: #bdc3c7;
        text-decoration: none;
        font-size: 15px;
        transition: all 0.3s ease;
    }
    .sidebar-menu a:hover {
        background-color: #34495e;
        padding-left: 28px; 
        color: #3498db;
    }
</style>

<div class="sidebar">
    <div class="sidebar-header">
        HỆ THỐNG QLKH
    </div>
    <ul class="sidebar-menu">
        <li><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
        
        <% if ("ADMIN".equals(role) || "MANAGER".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/customers">Quản lý Khách hàng</a></li>
        <% } %>

        <% if ("ADMIN".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/users">Quản lý Người dùng</a></li>
        <% } %>

        <% if (!"GUEST".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/profile">Hồ sơ cá nhân</a></li>
            <li><a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></li>
        <% } else { %>
            <li><a href="${pageContext.request.contextPath}/login">Đăng nhập</a></li>
        <% } %>
    </ul>
</div>