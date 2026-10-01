<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Hồ sơ cá nhân</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/profile.css">
</head>

<body>

    <!-- HEADER -->
    <header class="page-header">
        <div class="page-header-inner">
            <div>
                <div class="breadcrumb">
                    Tài khoản
                </div>

                <h1>Hồ sơ cá nhân</h1>

                <p>
                    Xem và cập nhật thông tin cá nhân của bạn.
                </p>
            </div>
        </div>
    </header>


    <!-- MAIN -->
    <main class="main">

        <section class="profile-card">

            <div class="profile-card-header">
                <h2>Thông tin cá nhân</h2>

                <p>
                    Cập nhật thông tin hồ sơ và chữ ký email của bạn.
                </p>
            </div>


            <!-- THÔNG BÁO -->
            <div
                id="profileMessage"
                class="profile-message"
                hidden>
            </div>


            <!-- FORM HỒ SƠ -->
            <form id="profileForm">

                <!-- HỌ TÊN -->
                <div class="form-group">

                    <label for="fullName">
                        Họ và tên
                        <span class="required">*</span>
                    </label>

                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        class="form-control"
                        value="${sessionScope.userName}"
                        placeholder="Nhập họ và tên">

                    <p
                        id="fullNameError"
                        class="field-error">
                    </p>

                </div>


                <!-- EMAIL -->
                <div class="form-group">

                    <label for="email">
                        Email
                    </label>

                    <input
                        type="email"
                        id="email"
                        class="form-control readonly-field"
                        value="${sessionScope.userEmail}"
                        readonly>

                    <p class="field-note">
                        Email không thể chỉnh sửa.
                    </p>

                </div>


                <!-- SỐ ĐIỆN THOẠI -->
                <div class="form-group">

                    <label for="phone">
                        Số điện thoại
                        <span class="required">*</span>
                    </label>

                    <input
                        type="tel"
                        id="phone"
                        name="phone"
                        class="form-control"
                        value=""
                        placeholder="Ví dụ: 0912345678"
                        inputmode="numeric"
                        maxlength="10">

                    <p
                        id="phoneError"
                        class="field-error">
                    </p>

                </div>


                <!-- VAI TRÒ -->
                <div class="form-group">

                    <label for="role">
                        Vai trò
                    </label>

                    <input
                        type="text"
                        id="role"
                        class="form-control readonly-field"
                        value="${sessionScope.userRole}"
                        readonly>

                    <p class="field-note">
                        Vai trò do quản trị viên quản lý.
                    </p>

                </div>


                <!-- CHỮ KÝ EMAIL -->
                <div class="form-group">

                    <label for="emailSignature">
                        Chữ ký email
                    </label>

                    <textarea
                        id="emailSignature"
                        name="emailSignature"
                        class="form-control signature-input"
                        rows="6"
                        maxlength="1000"
                        placeholder="Nhập chữ ký sử dụng khi gửi email..."></textarea>

                    <div class="signature-footer">

                        <span>
                            Chữ ký này sẽ được sử dụng khi gửi email.
                        </span>

                        <span id="signatureCount">
                            0/1000
                        </span>

                    </div>

                </div>


                <!-- ACTION -->
                <div class="form-actions">

                    <button
                        type="submit"
                        id="saveProfileButton"
                        class="btn btn-primary">

                        Lưu thay đổi

                    </button>

                </div>

            </form>

        </section>

    </main>


    <!-- JAVASCRIPT -->
    <script
        src="${pageContext.request.contextPath}/assets/js/profile.js">
    </script>

</body>

</html>