<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Gán vai trò & Nhóm kinh doanh</title>

    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
        rel="stylesheet">
</head>

<body class="bg-light">

<div class="container mt-5">

    <div class="row justify-content-center">

        <div class="col-lg-8 col-md-10">

            <div class="card shadow">

                <!-- HEADER -->
                <div class="card-header bg-dark text-white py-3">

                    <h4 class="mb-1">
                        Phân quyền & Nhóm kinh doanh
                    </h4>

                    <div class="small">
                        Tài khoản:
                        <strong>
                            <c:out value="${user.fullName}" />
                        </strong>
                    </div>

                </div>

                <!-- BODY -->
                <div class="card-body p-4">

                    <!-- THÔNG BÁO THÀNH CÔNG -->
                    <c:if test="${not empty successMessage}">

                        <div class="alert alert-success"
                             role="alert">

                            <c:out value="${successMessage}" />

                        </div>

                    </c:if>


                    <!-- THÔNG BÁO LỖI -->
                    <c:if test="${not empty errorMessage}">

                        <div class="alert alert-danger"
                             role="alert">

                            <c:out value="${errorMessage}" />

                        </div>

                    </c:if>


                    <!-- THÔNG TIN TÀI KHOẢN -->
                    <div class="mb-4">

                        <div class="text-muted small">
                            Họ và tên
                        </div>

                        <div class="fw-semibold">
                            <c:out value="${user.fullName}" />
                        </div>

                        <div class="text-muted small mt-2">
                            Email
                        </div>

                        <div>
                            <c:out value="${user.email}" />
                        </div>

                    </div>


                    <hr>


                    <!-- FORM -->
                    <form
                        action="${pageContext.request.contextPath}/users/role-group"
                        method="POST">

                        <!-- USER ID -->
                        <input
                            type="hidden"
                            name="userId"
                            value="${user.id}">


                        <!-- ========================= -->
                        <!-- VAI TRÒ -->
                        <!-- ========================= -->

                        <div class="mb-4">

                            <h5 class="text-primary mb-3">
                                1. Gán vai trò
                            </h5>

                            <p class="text-muted">
                                Một người dùng có thể giữ nhiều vai trò cùng lúc.
                            </p>


                            <!-- ADMIN -->
                            <div class="form-check mb-2">

                                <input
                                    class="form-check-input"
                                    type="checkbox"
                                    name="roles"
                                    value="ADMIN"
                                    id="roleAdmin"
                                    ${not empty selectedRoles
                                      && selectedRoles.contains('ADMIN')
                                      ? 'checked'
                                      : ''}>

                                <label
                                    class="form-check-label"
                                    for="roleAdmin">

                                    <strong>
                                        Quản trị hệ thống
                                    </strong>

                                    <span class="text-muted">
                                        (ADMIN)
                                    </span>

                                </label>

                            </div>


                            <!-- MANAGER -->
                            <div class="form-check mb-2">

                                <input
                                    class="form-check-input"
                                    type="checkbox"
                                    name="roles"
                                    value="MANAGER"
                                    id="roleManager"
                                    ${not empty selectedRoles
                                      && selectedRoles.contains('MANAGER')
                                      ? 'checked'
                                      : ''}>

                                <label
                                    class="form-check-label"
                                    for="roleManager">

                                    <strong>
                                        Quản lý kinh doanh
                                    </strong>

                                    <span class="text-muted">
                                        (MANAGER)
                                    </span>

                                </label>

                            </div>


                            <!-- SALES -->
                            <div class="form-check mb-2">

                                <input
                                    class="form-check-input"
                                    type="checkbox"
                                    name="roles"
                                    value="SALES"
                                    id="roleSales"
                                    ${not empty selectedRoles
                                      && selectedRoles.contains('SALES')
                                      ? 'checked'
                                      : ''}>

                                <label
                                    class="form-check-label"
                                    for="roleSales">

                                    <strong>
                                        Nhân viên kinh doanh
                                    </strong>

                                    <span class="text-muted">
                                        (SALES)
                                    </span>

                                </label>

                            </div>

                        </div>


                        <hr>


                        <!-- ========================= -->
                        <!-- NHÓM KINH DOANH -->
                        <!-- ========================= -->

                        <div class="mb-4">

                            <h5 class="text-primary mb-3">
                                2. Gán nhóm kinh doanh
                            </h5>

                            <p class="text-muted">
                                Người giữ vai trò Quản lý kinh doanh
                                phải được gán một nhóm kinh doanh cụ thể.
                            </p>


                            <label
                                for="groupId"
                                class="form-label fw-semibold">

                                Nhóm kinh doanh

                            </label>


                            <select
                                class="form-select"
                                id="groupId"
                                name="groupId">

                                <option value="">
                                    -- Không chọn nhóm --
                                </option>

                                <c:forEach
                                    var="group"
                                    items="${businessGroups}">

                                    <option
                                        value="${group.id}"
                                        ${group.id == selectedGroupId
                                          ? 'selected'
                                          : ''}>

                                        <c:out value="${group.name}" />

                                    </option>

                                </c:forEach>

                            </select>


                            <!-- KHÔNG CÓ NHÓM -->
                            <c:if test="${empty businessGroups}">

                                <div class="alert alert-warning mt-3 mb-0">

                                    Hiện chưa có nhóm kinh doanh
                                    trong hệ thống.

                                </div>

                            </c:if>

                        </div>


                        <hr>


                        <!-- ========================= -->
                        <!-- BUTTON -->
                        <!-- ========================= -->

                        <div class="d-flex justify-content-between mt-4">

                            <a
                                href="${pageContext.request.contextPath}/users"
                                class="btn btn-secondary">

                                Quay lại

                            </a>


                            <button
                                type="submit"
                                class="btn btn-primary">

                                Lưu cấu hình

                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </div>

    </div>

</div>


<!-- BOOTSTRAP -->
<script
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
</script>

</body>

</html>