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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/avatar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile-redesign.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile-header.css">
</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="../components/sidebar.jsp" />

<main class="profile-page profile-redesign">
  <jsp:include page="/components/header.jsp" />
  <header class="redesign-heading">
    <div><span class="redesign-kicker">TÀI KHOẢN</span><h1>Hồ sơ cá nhân</h1><p>Quản lý thông tin cá nhân, ảnh đại diện và chữ ký email của bạn.</p></div>
    <div class="redesign-breadcrumb">Trang chủ &nbsp; / &nbsp; Hồ sơ cá nhân</div>
  </header>
  <div class="redesign-grid">
    <div class="redesign-left">
      <div class="redesign-card redesign-avatar-card">
        <div class="redesign-card-heading"><span class="redesign-heading-icon">◎</span><div><h2>Ảnh đại diện</h2><p>Cập nhật ảnh đại diện của bạn.</p></div></div>
        <section class="avatar-card">

            <div class="avatar-card-header">
                <h2>Cập nhật ảnh đại diện</h2>

                <p>
                    Chọn một ảnh từ máy tính để xem trước trước khi tải lên.
                </p>
            </div>


            <!-- THÔNG BÁO -->
            <div
                id="avatarMessage"
                class="avatar-message"
                hidden>
            </div>


            <form
                id="avatarForm"
                action="${pageContext.request.contextPath}/api/users/avatar"
                method="post"
                enctype="multipart/form-data">

                <!-- PREVIEW -->
                <div class="avatar-preview-wrapper">

                    <div class="avatar-preview">

                        <img
                            id="avatarPreview"
                            src=""
                            alt="Ảnh đại diện xem trước"
                            hidden>

                        <div
                            id="avatarPlaceholder"
                            class="avatar-placeholder">

                            <span>
                                Ảnh đại diện
                            </span>

                        </div>

                    </div>

                    <div class="avatar-user-info">

                        <strong>
                            ${sessionScope.userName}
                        </strong>

                        <span>
                            ${sessionScope.userEmail}
                        </span>

                    </div>

                </div>


                <!-- CHỌN ẢNH -->
                <div class="upload-area">

                    <label
                        for="avatarFile"
                        class="upload-label">

                        Chọn ảnh từ máy
                    </label>

                    <input
                        type="file"
                        id="avatarFile"
                        name="avatar"
                        class="file-input"
                        accept=".jpg,.jpeg,.png">

                    <p
                        id="selectedFileName"
                        class="file-name">

                        Chưa chọn ảnh

                    </p>

                    <p class="upload-note">
                        Hỗ trợ định dạng JPG, JPEG và PNG.
                    </p>

                    <p
                        id="avatarError"
                        class="field-error">
                    </p>

                </div>


                <!-- ACTION -->
                <div class="form-actions">

                    <button
                        type="button"
                        id="removeAvatarButton"
                        class="btn btn-light"
                        disabled>

                        Bỏ ảnh đã chọn

                    </button>

                    <button
                        type="submit"
                        id="uploadAvatarButton"
                        class="btn btn-primary"
                        disabled>

                        Tải ảnh đại diện

                    </button>

                </div>

            </form>

        </section>
      </div>
    </div>
    <div class="redesign-right">
      <div class="redesign-card redesign-profile-card">
        <div class="redesign-card-heading"><span class="redesign-heading-icon">♙</span><div><h2>Thông tin cá nhân</h2><p>Cập nhật họ tên, số điện thoại và chữ ký email.</p></div></div>
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
      </div>
      <div class="redesign-card redesign-summary">
        <div class="redesign-card-heading"><span class="redesign-heading-icon">ⓘ</span><div><h2>Thông tin tài khoản</h2><p>Thông tin được lấy từ tài khoản hiện tại.</p></div></div>
        <div class="redesign-summary-grid"><div><small>Vai trò</small><strong id="redesignRole">Đang tải...</strong></div><div><small>Email</small><strong id="redesignEmail">Đang tải...</strong></div></div>
      </div>
    </div>
  </div>
  <div class="redesign-note">Ảnh đại diện được dùng để nhận diện tài khoản trong hệ thống. Chỉ chấp nhận JPG, JPEG hoặc PNG, tối đa 2 MB.</div>
</main>


<script
    src="${pageContext.request.contextPath}/assets/js/profile.js"
    defer>
</script>

    <script src="${pageContext.request.contextPath}/assets/js/avatar.js" defer></script>
<script>
document.addEventListener('DOMContentLoaded', function () {
  const name = document.getElementById('fullName');
  const email = document.getElementById('email');
  const role = document.getElementById('role');
  const nameLabel = document.getElementById('redesignAvatarName');
  const emailLabel = document.getElementById('redesignAvatarEmail');
  const roleLabel = document.getElementById('redesignRole');
  const emailSummary = document.getElementById('redesignEmail');
  function sync() {
    if (nameLabel && name) nameLabel.textContent = name.value || 'Người dùng';
    if (emailLabel && email) emailLabel.textContent = email.value || '';
    if (roleLabel && role) roleLabel.textContent = role.value || 'Chưa xác định';
    if (emailSummary && email) emailSummary.textContent = email.value || 'Chưa có';
  }
  if (name) name.addEventListener('input', sync);
  const observer = new MutationObserver(sync);
  const content = document.getElementById('profileFormContent');
  if (content) observer.observe(content, {attributes: true, attributeFilter: ['hidden']});
  sync();
});
</script>

</body>
</html>