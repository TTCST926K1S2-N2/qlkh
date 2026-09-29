<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String role = (String) session.getAttribute("userRole");
    if (role == null) {
        role = "GUEST";
    }
%>

<div class="sidebar">
    <ul class="sidebar-menu">
        <li><a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a></li>
        
        <% if ("ADMIN".equals(role) || "MANAGER".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/customers">👥 Quản lý Khách hàng</a></li>
        <% } %>

        <% if ("ADMIN".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/users">⚙️ Quản lý Người dùng</a></li>
        <% } %>

        <% if (!"GUEST".equals(role)) { %>
            <li><a href="${pageContext.request.contextPath}/profile">👤 Hồ sơ cá nhân</a></li>
            <li><a href="${pageContext.request.contextPath}/logout">🚪 Đăng xuất</a></li>
        <% } else { %>
            <li><a href="${pageContext.request.contextPath}/login">🔑 Đăng nhập</a></li>
        <% } %>
    </ul>
</div>