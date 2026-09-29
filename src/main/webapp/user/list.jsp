<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý tài khoản - Danh sách</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container mt-4">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Danh sách tài khoản hệ thống</h2>

        <a href="${pageContext.request.contextPath}/users/create"
           class="btn btn-primary">
             Thêm tài khoản mới
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">

            <table class="table table-striped table-hover align-middle">

                <thead class="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>Số điện thoại</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>

                    <!-- Danh sách tài khoản được Backend truyền sang JSP -->
                    <c:forEach var="user" items="${users}">
                        <tr>

                            <td>
                                <c:out value="${user.id}" />
                            </td>

                            <td>
                                <c:out value="${user.fullName}" />
                            </td>

                            <td>
                                <c:out value="${user.email}" />
                            </td>

                            <td>
                                <c:out value="${user.phone}" />
                            </td>

                            <td>
                                <c:out value="${user.status}" default="-" />
                            </td>

                            <td>

                                <a href="${pageContext.request.contextPath}/users/edit?id=${user.id}"
                                   class="btn btn-sm btn-warning">
                                    Sửa
                                </a>

                                <a href="${pageContext.request.contextPath}/users/role-group?id=${user.id}"
                                   class="btn btn-sm btn-info">
                                    Phân quyền
                                </a>

                                <a href="${pageContext.request.contextPath}/users/handover?id=${user.id}"
                                   class="btn btn-sm btn-secondary">
                                    Bàn giao/Khóa
                                </a>

                            </td>

                        </tr>
                    </c:forEach>

                    <!-- Hiển thị khi Backend chưa trả về tài khoản nào -->
                    <c:if test="${empty users}">
                        <tr>
                            <td colspan="6"
                                class="text-center text-muted py-4">
                                Chưa có tài khoản nào.
                            </td>
                        </tr>
                    </c:if>

                </tbody>

            </table>

        </div>
    </div>

</div>

</body>
</html>