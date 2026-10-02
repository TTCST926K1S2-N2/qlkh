<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="true" %>

<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Hồ sơ cá nhân - Hệ thống QLKH</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/sidebar.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/profile.css">
</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="../components/sidebar.jsp" />

<main class="profile-page">

    <section class="profile-hero">

        <div class="profile-eyebrow">
            Tài khoản
        </div>

        <h1>Hồ sơ cá nhân</h1>

        <p>
            Quản lý thông tin cá nhân và chữ ký email của bạn.
        </p>

    </section>


    <section class="profile-content">

        <div
            id="profileAlert"
            class="profile-alert"
            role="status"
            aria-live="polite"
            hidden>
        </div>


        <article class="profile-card">

            <header class="profile-card-header">

                <h2>Thông tin cá nhân</h2>

                <p>
                    Cập nhật họ tên, số điện thoại và chữ ký
                    dùng khi gửi email.
                </p>

            </header>


            <div class="profile-card-body">

                <div
                    id="profileLoading"
                    class="profile-loading">

                    Đang tải thông tin hồ sơ...

                </div>


                <div id="profileFormContent" hidden>

                    <form id="profileForm" novalidate>

                        <div class="profile-grid">


                            <!-- HO TEN -->

                            <div class="profile-field">

                                <label
                                    class="profile-label"
                                    for="fullName">

                                    Họ và tên

                                    <span class="profile-required">
                                        *
                                    </span>

                                </label>

                                <input
                                    class="profile-input"
                                    type="text"
                                    id="fullName"
                                    name="fullName"
                                    maxlength="255"
                                    autocomplete="name"
                                    placeholder="Nhập họ và tên">

                                <div
                                    id="fullNameError"
                                    class="profile-error">
                                </div>

                            </div>


                            <!-- EMAIL -->

                            <div class="profile-field">

                                <label
                                    class="profile-label"
                                    for="email">

                                    Email

                                </label>

                                <input
                                    class="profile-input"
                                    type="email"
                                    id="email"
                                    readonly>

                                <span class="profile-help">
                                    Email không thể thay đổi tại trang hồ sơ.
                                </span>

                            </div>


                            <!-- PHONE -->

                            <div class="profile-field">

                                <label
                                    class="profile-label"
                                    for="phone">

                                    Số điện thoại

                                </label>

                                <input
                                    class="profile-input"
                                    type="tel"
                                    id="phone"
                                    name="phone"
                                    maxlength="20"
                                    autocomplete="tel"
                                    placeholder="Ví dụ: 0912345678">

                                <div
                                    id="phoneError"
                                    class="profile-error">
                                </div>

                            </div>


                            <!-- ROLE -->

                            <div class="profile-field">

                                <label
                                    class="profile-label"
                                    for="role">

                                    Vai trò

                                </label>

                                <input
                                    class="profile-input"
                                    type="text"
                                    id="role"
                                    readonly>

                                <span class="profile-help">
                                    Vai trò do quản trị viên quản lý.
                                </span>

                            </div>


                            <!-- EMAIL SIGNATURE -->

                            <div class="profile-field full-width">

                                <label
                                    class="profile-label"
                                    for="emailSignature">

                                    Chữ ký email

                                </label>

                                <textarea
                                    class="profile-textarea"
                                    id="emailSignature"
                                    name="emailSignature"
                                    placeholder="Nhập chữ ký dùng khi gửi email..."></textarea>

                                <span class="profile-help">
                                    Chữ ký này sẽ được sử dụng trong email của bạn.
                                </span>

                            </div>

                        </div>


                        <div class="profile-actions">

                            <button
                                class="profile-btn profile-btn-secondary"
                                type="button"
                                id="cancelProfileButton">

                                Hủy

                            </button>


                            <button
                                class="profile-btn profile-btn-primary"
                                type="submit"
                                id="saveProfileButton">

                                Lưu thay đổi

                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </article>

    </section>

</main>


<script
    src="${pageContext.request.contextPath}/assets/js/profile.js"
    defer>
</script>

</body>
</html>