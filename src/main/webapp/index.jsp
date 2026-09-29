<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hệ thống quản lý khách hàng</title>
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
   
    <jsp:include page="/components/sidebar.jsp" />

   
    <div class="main-content">
    <h1>Hệ thống quản lý khách hàng</h1>
    <p>Java Servlet/JSP - Project đang hoạt động.</p>
    <p>${message}</p>
</div>
</body>
</html>