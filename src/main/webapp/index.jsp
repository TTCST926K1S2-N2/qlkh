<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hệ thống quản lý khách hàng</title>

    <!-- CSS Session/Logout -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/session.css">

    <style>
        /* CSS tạo bố cục chia màn hình: Sidebar bên trái, nội dung bên phải */
        body {
            margin: 0;
            padding: 0;
            display: flex;
            background-color: #f4f6f9;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .main-content {
            flex-grow: 1;
            padding: 40px;
        }
    </style>
</head>

<body>

    <!-- Sidebar -->
    <jsp:include page="/components/sidebar.jsp" />

    <!-- Nội dung chính -->
    <div class="main-content">
        <h1>Hệ thống quản lý khách hàng</h1>
        <p>Java Servlet/JSP - Project đang hoạt động.</p>
        <p>${message}</p>
    </div>

    <!--
        Chỉ khởi tạo Session Warning khi người dùng đã đăng nhập.
        Backend đăng nhập sử dụng userRole trong HttpSession.
    -->
    <% if (session.getAttribute("userRole") != null) { %>

        <!-- Modal cảnh báo hết phiên -->
        <jsp:include page="/components/session-modal.jsp" />

        <!-- Xử lý Session/Logout -->
        <script src="${pageContext.request.contextPath}/assets/js/session.js"></script>

    <% } %>

</body>
</html>