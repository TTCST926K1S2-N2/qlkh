<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>áº¢nh Ä‘áº¡i diá»‡n</title>

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
                    TĂ i khoáº£n
                </div>

                <h1>áº¢nh Ä‘áº¡i diá»‡n</h1>

                <p>
                    Táº£i lĂªn vĂ  xem trÆ°á»›c áº£nh Ä‘áº¡i diá»‡n cá»§a báº¡n.
                </p>
            </div>

        </div>
    </header>


    <!-- MAIN -->
    <main class="main">

        <section class="avatar-card">

            <div class="avatar-card-header">
                <h2>Cáº­p nháº­t áº£nh Ä‘áº¡i diá»‡n</h2>

                <p>
                    Chá»n má»™t áº£nh tá»« mĂ¡y tĂ­nh Ä‘á»ƒ xem trÆ°á»›c trÆ°á»›c khi táº£i lĂªn.
                </p>
            </div>


            <!-- THĂ”NG BĂO -->
            <div
                id="avatarMessage"
                class="avatar-message"
                hidden>
            </div>


            <form
                id="avatarForm" action="${pageContext.request.contextPath}/api/users/avatar" method="post"
                enctype="multipart/form-data">

                <!-- PREVIEW -->
                <div class="avatar-preview-wrapper">

                    <div class="avatar-preview">

                        <img
                            id="avatarPreview"
                            src=""
                            alt="áº¢nh Ä‘áº¡i diá»‡n xem trÆ°á»›c"
                            hidden>

                        <div
                            id="avatarPlaceholder"
                            class="avatar-placeholder">

                            <span>
                                áº¢nh Ä‘áº¡i diá»‡n
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


                <!-- CHá»ŒN áº¢NH -->
                <div class="upload-area">

                    <label
                        for="avatarFile"
                        class="upload-label">

                        Chá»n áº£nh tá»« mĂ¡y
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

                        ChÆ°a chá»n áº£nh

                    </p>

                    <p class="upload-note">
                        Há»— trá»£ Ä‘á»‹nh dáº¡ng JPG, JPEG vĂ  PNG.
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

                        Bá» áº£nh Ä‘Ă£ chá»n

                    </button>

                    <button
                        type="submit"
                        id="uploadAvatarButton"
                        class="btn btn-primary"
                        disabled>

                        Táº£i áº£nh Ä‘áº¡i diá»‡n

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
