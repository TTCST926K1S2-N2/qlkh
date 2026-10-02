<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Import người dùng từ Excel</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/users.css">
</head>

<body>

    <!-- HEADER -->
    <header class="page-header">

        <div class="page-header-inner">

            <div>
                <div class="breadcrumb">
                    Quản lý hệ thống
                </div>

                <h1>Import người dùng từ Excel</h1>

                <p>
                    Nhập danh sách người dùng hàng loạt từ tệp Excel.
                </p>
            </div>

            <a
                href="${pageContext.request.contextPath}/users"
                class="btn btn-light">
                Quay lại danh sách
            </a>

        </div>

    </header>


    <!-- MAIN -->
    <main class="main">

        <!-- IMPORT CARD -->
        <section class="import-card">

            <h2>Chọn tệp Excel</h2>

            <p class="description">
                Chọn tệp Excel chứa danh sách người dùng cần import.
                Hệ thống hỗ trợ định dạng .xlsx và .xls.
            </p>

            <form
                id="importForm"
                enctype="multipart/form-data">

                <div class="file-box">

                    <label
                        for="excelFile"
                        class="file-label">
                        Chọn tệp Excel
                    </label>

                    <input
                        type="file"
                        id="excelFile"
                        name="file"
                        class="file-input"
                        accept=".xlsx,.xls">

                    <p
                        id="selectedFileName"
                        class="file-name">
                        Chưa chọn tệp
                    </p>

                    <p
                        id="fileError"
                        class="file-error">
                    </p>

                </div>

                <button type="submit" id="importButton" class="btn btn-primary" disabled>Xem trước dữ liệu</button><button type="button" id="confirmImportButton" class="btn btn-primary" hidden>Xác nhận Import</button>

            </form>

        </section>


        <!-- KẾT QUẢ IMPORT -->
        <section
            id="importResult"
            class="result-card"
            hidden>

            <h2>Kết quả Import</h2>

            <p class="description">
                Kết quả xử lý danh sách người dùng từ tệp Excel.
            </p>

            <div class="result-summary">

                <div class="result-item">
                    <p>Thành công</p>

                    <strong id="successCount">
                        0
                    </strong>
                </div>

                <div class="result-item">
                    <p>Thất bại</p>

                    <strong id="failedCount">
                        0
                    </strong>
                </div>

            </div>

            <!-- DANH SÁCH LỖI BACKEND TRẢ VỀ -->
            <div
                id="errorList"
                class="error-list">
            </div>

        </section>

    </main>


    <!-- JAVASCRIPT -->
    <script
        src="${pageContext.request.contextPath}/assets/js/users.js">
    </script>

</body>

</html>