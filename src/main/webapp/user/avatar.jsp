<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Ảnh đại diện</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/avatar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
</head>

<body data-context-path="${pageContext.request.contextPath}">
    <jsp:include page="../components/sidebar.jsp" />

    <!-- HEADER -->
    <header class="page-header">
        <div class="page-header-inner">

            <div>
                <div class="breadcrumb">
                    Tài khoản
                </div>

                <h1>Ảnh đại diện</h1>

                <p>
                    Tải lên và xem trước ảnh đại diện của bạn.
                </p>
            </div>

        </div>
    </header>


    <!-- MAIN -->
    <main class="main">

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

    </main>


    <script
        src="${pageContext.request.contextPath}/assets/js/avatar.js">
    </script>

</body>

</html>