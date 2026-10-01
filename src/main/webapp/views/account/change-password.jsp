<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Đổi Mật Khẩu - Hệ Thống QLKH</title>

    <!-- Bootstrap 5 CSS -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
        rel="stylesheet">

    <!-- Font Awesome Icons -->
    <link
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
        rel="stylesheet">

    <!-- CSS riêng cho HTQLKH-4 -->
    <link
        href="${pageContext.request.contextPath}/assets/css/change-password.css"
        rel="stylesheet">
</head>

<body>

<div class="container">

    <div class="card change-password-card p-4 bg-white">

        <h3 class="text-center mb-2 text-primary fw-bold">
            ĐỔI MẬT KHẨU
        </h3>

        <p class="text-muted text-center small mb-4">
            Quản lý và cập nhật mật khẩu tài khoản
        </p>


        <!-- ============================== -->
        <!-- THÔNG BÁO LỖI TỪ JAVASCRIPT -->
        <!-- ============================== -->

        <div
            id="jsErrorAlert"
            class="alert alert-danger alert-dismissible fade show d-none"
            role="alert">

            <i class="fas fa-exclamation-triangle me-2"></i>

            <span id="jsErrorMessage"></span>

        </div>


        <!-- ============================== -->
        <!-- THÔNG BÁO LỖI TỪ BACKEND -->
        <!-- ============================== -->

        <%
            String error =
                    (String) request.getAttribute("errorMessage");
        %>

        <% if (error != null) { %>

            <div
                id="serverErrorAlert"
                class="alert alert-danger text-center"
                role="alert">

                <i class="fas fa-exclamation-circle me-1"></i>

                <%= error %>

            </div>

        <% } %>


        <!-- ============================== -->
        <!-- THÔNG BÁO THÀNH CÔNG BACKEND -->
        <!-- ============================== -->

        <%
            String success =
                    (String) request.getAttribute("successMessage");
        %>

        <% if (success != null) { %>

            <div
                class="alert alert-success text-center"
                role="alert">

                <i class="fas fa-check-circle me-1"></i>

                <%= success %>

                <div class="mt-3">

                    <a
                        href="${pageContext.request.contextPath}/"
                        class="btn btn-sm btn-outline-success">

                        Về trang chủ

                    </a>

                </div>

            </div>

        <% } else { %>


        <!-- ============================== -->
        <!-- FORM ĐỔI MẬT KHẨU -->
        <!-- ============================== -->

        <form
            id="changePasswordForm"
            action="${pageContext.request.contextPath}/change-password"
            method="post"
            novalidate>


            <!-- ========================== -->
            <!-- MẬT KHẨU HIỆN TẠI -->
            <!-- ========================== -->

            <div class="mb-3">

                <label
                    for="currentPassword"
                    class="form-label fw-semibold">

                    Mật khẩu hiện tại
                    <span class="text-danger">*</span>

                </label>

                <input
                    type="password"
                    class="form-control"
                    id="currentPassword"
                    name="currentPassword"
                    placeholder="Nhập mật khẩu đang sử dụng"
                    autocomplete="current-password">

                <div class="invalid-feedback">
                    Vui lòng nhập mật khẩu hiện tại.
                </div>

            </div>


            <!-- ========================== -->
            <!-- MẬT KHẨU MỚI -->
            <!-- ========================== -->

            <div class="mb-3">

                <label
                    for="newPassword"
                    class="form-label fw-semibold">

                    Mật khẩu mới
                    <span class="text-danger">*</span>

                </label>

                <input
                    type="password"
                    class="form-control"
                    id="newPassword"
                    name="newPassword"
                    placeholder="Nhập mật khẩu mới"
                    autocomplete="new-password">

                <div class="invalid-feedback">
                    Mật khẩu mới không đạt yêu cầu.
                </div>


                <!-- Checklist quy tắc mật khẩu -->

                <ul class="password-checklist mt-2 ms-1">

                    <li
                        id="rule-length"
                        class="invalid">

                        <i class="fas fa-times-circle me-1"></i>
                        Tối thiểu 8 ký tự

                    </li>

                    <li
                        id="rule-letter"
                        class="invalid">

                        <i class="fas fa-times-circle me-1"></i>
                        Chứa ít nhất 1 chữ cái (a-z, A-Z)

                    </li>

                    <li
                        id="rule-number"
                        class="invalid">

                        <i class="fas fa-times-circle me-1"></i>
                        Chứa ít nhất 1 chữ số (0-9)

                    </li>

                </ul>

            </div>


            <!-- ========================== -->
            <!-- XÁC NHẬN MẬT KHẨU -->
            <!-- ========================== -->

            <div class="mb-3">

                <label
                    for="confirmPassword"
                    class="form-label fw-semibold">

                    Xác nhận mật khẩu mới
                    <span class="text-danger">*</span>

                </label>

                <input
                    type="password"
                    class="form-control"
                    id="confirmPassword"
                    name="confirmPassword"
                    placeholder="Nhập lại mật khẩu mới"
                    autocomplete="new-password">

                <div class="invalid-feedback">
                    Mật khẩu xác nhận không trùng khớp.
                </div>

            </div>


            <!-- ========================== -->
            <!-- NÚT THAO TÁC -->
            <!-- ========================== -->

            <div class="d-grid gap-2 mt-4">

                <button
                    type="submit"
                    class="btn btn-primary py-2 fw-semibold">

                    Cập nhật mật khẩu

                </button>


                <!--
                    HTQLKH-4:
                    Trang chủ hiện tại sử dụng context root.
                    Không sử dụng /dashboard vì repo
                    hiện chưa có route này.
                -->

                <a
                    href="${pageContext.request.contextPath}/"
                    class="btn btn-light border py-2 text-secondary">

                    Hủy bỏ

                </a>

            </div>

        </form>

        <% } %>

    </div>

</div>


<!-- Bootstrap 5 JS -->

<script
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
</script>


<!-- JS riêng cho HTQLKH-4 -->

<script
    src="${pageContext.request.contextPath}/assets/js/change-password.js">
</script>

</body>

</html>