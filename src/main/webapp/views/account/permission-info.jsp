<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    String role = (String) request.getAttribute("userRole");

    if (role == null || role.trim().isEmpty()) {
        role = (String) session.getAttribute("userRole");
    }

    if (role == null || role.trim().isEmpty()) {
        role = (String) session.getAttribute("role");
    }

    if (role == null || role.trim().isEmpty()) {
        role = "GUEST";
    } else {
        role = role.trim().toUpperCase();
    }

    String username = (String) session.getAttribute("username");

    if (username == null || username.trim().isEmpty()) {
        username = (String) request.getAttribute("username");
    }

    /*
     * dataScope do Backend xác định.
     *
     * Giá trị dự kiến:
     * MY   = Của tôi
     * TEAM = Của nhóm tôi
     * ALL  = Tất cả
     *
     * FE KHÔNG tự suy ra dataScope từ role.
     */
    String dataScope = (String) request.getAttribute("dataScope");

    if (dataScope == null || dataScope.trim().isEmpty()) {
        dataScope = (String) session.getAttribute("dataScope");
    }

    if (dataScope != null) {
        dataScope = dataScope.trim().toUpperCase();
    }

    request.setAttribute("currentRole", role);
    request.setAttribute("currentUsername", username);
    request.setAttribute("currentDataScope", dataScope);
%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Phân quyền và phạm vi dữ liệu</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <link href="${pageContext.request.contextPath}/assets/css/permission.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container py-4"
     id="permission-container"
     data-scope="${currentDataScope}">

    <!-- Chưa đăng nhập -->
    <c:if test="${currentRole == 'GUEST'}">

        <div class="card shadow-sm mx-auto login-required-card">

            <div class="card-body text-center p-5">

                <h4 class="fw-bold mb-3">
                    Yêu cầu đăng nhập
                </h4>

                <p class="text-muted">
                    Bạn chưa đăng nhập hoặc phiên đăng nhập
                    không còn hợp lệ.
                </p>

                <a href="${pageContext.request.contextPath}/login"
                   class="btn btn-primary">

                    Đăng nhập

                </a>

            </div>

        </div>

    </c:if>


    <!-- Đã đăng nhập -->
    <c:if test="${currentRole != 'GUEST'}">

        <div class="d-flex justify-content-between
                    align-items-center
                    border-bottom
                    pb-3 mb-4">

            <div>

                <h3 class="fw-bold mb-1">
                    Phân quyền và phạm vi dữ liệu
                </h3>

                <p class="text-muted mb-0">
                    Dữ liệu hiển thị theo phạm vi được
                    Backend cấp cho tài khoản hiện tại.
                </p>

            </div>

            <a href="${pageContext.request.contextPath}/dashboard"
               class="btn btn-outline-secondary">

                Quay lại Dashboard

            </a>

        </div>


        <!-- Thông báo lỗi do Backend trả về -->
        <c:if test="${not empty permissionError}">

            <div class="alert alert-danger"
                 role="alert">

                <strong>Không thể truy cập dữ liệu.</strong>

                <c:out value="${permissionError}" />

            </div>

        </c:if>


        <!-- Thông tin tài khoản -->
        <div class="card shadow-sm mb-4">

            <div class="card-header bg-white">

                <h5 class="mb-0 fw-bold">
                    Tài khoản hiện tại
                </h5>

            </div>

            <div class="card-body">

                <div class="row g-3">

                    <div class="col-md-6">

                        <div class="text-muted small">
                            Tên đăng nhập
                        </div>

                        <div class="fw-bold">

                            <c:choose>

                                <c:when test="${not empty currentUsername}">
                                    <c:out value="${currentUsername}" />
                                </c:when>

                                <c:otherwise>
                                    Chưa có thông tin
                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>


                    <div class="col-md-6">

                        <div class="text-muted small">
                            Vai trò
                        </div>

                        <div class="fw-bold">
                            <c:out value="${currentRole}" />
                        </div>

                    </div>

                </div>

            </div>

        </div>


        <!-- Phạm vi dữ liệu -->
        <div class="card shadow-sm mb-4">

            <div class="card-header bg-white">

                <h5 class="mb-0 fw-bold">
                    Phạm vi dữ liệu
                </h5>

            </div>

            <div class="card-body">

                <div class="scope-grid">

                    <!-- MY -->
                    <div class="scope-card
                        ${currentDataScope == 'MY'
                            ? 'scope-active'
                            : ''}">

                        <h6 class="fw-bold">
                            Của tôi
                        </h6>

                        <p class="text-muted mb-0">
                            Chỉ dữ liệu thuộc sở hữu
                            của tài khoản hiện tại.
                        </p>

                    </div>


                    <!-- TEAM -->
                    <div class="scope-card
                        ${currentDataScope == 'TEAM'
                            ? 'scope-active'
                            : ''}">

                        <h6 class="fw-bold">
                            Của nhóm tôi
                        </h6>

                        <p class="text-muted mb-0">
                            Dữ liệu thuộc nhóm kinh doanh
                            của tài khoản hiện tại.
                        </p>

                    </div>


                    <!-- ALL -->
                    <div class="scope-card
                        ${currentDataScope == 'ALL'
                            ? 'scope-active'
                            : ''}">

                        <h6 class="fw-bold">
                            Tất cả
                        </h6>

                        <p class="text-muted mb-0">
                            Toàn bộ dữ liệu mà tài khoản
                            được phép truy cập.
                        </p>

                    </div>

                </div>


                <!-- BE chưa trả scope -->
                <c:if test="${empty currentDataScope}">

                    <div class="alert alert-warning mt-3 mb-0">

                        Chưa xác định được phạm vi dữ liệu
                        của tài khoản.

                    </div>

                </c:if>

            </div>

        </div>


        <!-- Các dữ liệu áp dụng phạm vi -->
        <div class="card shadow-sm mb-4">

            <div class="card-header bg-white">

                <h5 class="mb-0 fw-bold">
                    Dữ liệu áp dụng phạm vi
                </h5>

            </div>

            <div class="card-body">

                <p class="text-muted">

                    Phạm vi trên được áp dụng thống nhất
                    khi Backend truy vấn danh sách,
                    tìm kiếm và xuất Excel.

                </p>

                <div class="table-responsive">

                    <table class="table table-bordered
                                  align-middle mb-0">

                        <thead class="table-light">

                        <tr>
                            <th>Loại dữ liệu</th>
                            <th>Phạm vi áp dụng</th>
                        </tr>

                        </thead>

                        <tbody>

                        <tr>
                            <td>Khách hàng</td>

                            <td>
                                <span class="scope-label">
                                    Đang xác định
                                </span>
                            </td>
                        </tr>

                        <tr>
                            <td>Cơ hội</td>

                            <td>
                                <span class="scope-label">
                                    Đang xác định
                                </span>
                            </td>
                        </tr>

                        <tr>
                            <td>Hoạt động</td>

                            <td>
                                <span class="scope-label">
                                    Đang xác định
                                </span>
                            </td>
                        </tr>

                        <tr>
                            <td>Báo giá</td>

                            <td>
                                <span class="scope-label">
                                    Đang xác định
                                </span>
                            </td>
                        </tr>

                        </tbody>

                    </table>

                </div>

            </div>

        </div>


        <!-- Quy tắc tìm kiếm và Excel -->
        <div class="card shadow-sm">

            <div class="card-header bg-white">

                <h5 class="mb-0 fw-bold">
                    Quy tắc áp dụng
                </h5>

            </div>

            <div class="card-body">

                <ul class="mb-0">

                    <li>
                        Danh sách dữ liệu được Backend
                        lọc theo phạm vi của tài khoản.
                    </li>

                    <li>
                        Tìm kiếm sử dụng cùng phạm vi dữ liệu.
                    </li>

                    <li>
                        Xuất Excel chỉ xuất dữ liệu nằm
                        trong phạm vi được phép.
                    </li>

                    <li>
                        Khi truy cập bản ghi ngoài phạm vi,
                        hệ thống hiển thị thông báo lỗi
                        bằng tiếng Việt.
                    </li>

                </ul>

            </div>

        </div>

    </c:if>

</div>

<script src="${pageContext.request.contextPath}/assets/js/permission.js">
</script>

</body>
</html>