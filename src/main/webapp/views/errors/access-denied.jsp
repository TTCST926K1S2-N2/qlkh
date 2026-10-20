<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Không có quyền truy cập</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/error.css">
</head>
<body>
    <div class="error-container">
        <h1 class="error-code warning">403</h1>
        <h2 class="error-title">Không có quyền truy cập</h2>
        <p class="error-description">Bạn không có quyền truy cập chức năng này.</p>
        
        <div class="error-actions">
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Quay lại trang chủ</a>
        </div>
    </div>
</body>
</html>