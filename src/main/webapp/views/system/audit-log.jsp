<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0">

    <title>Nhật ký thay đổi hệ thống</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/sidebar.css">

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/audit-log.css">
</head>

<body>

<script>
    window.contextPath = "${pageContext.request.contextPath}";
</script>

<jsp:include page="/components/sidebar.jsp" />

<div class="page-content">

    <header class="page-header">

        

        <h1>
            Nhật ký thay đổi
        </h1>

        <p>
            Theo dõi người thực hiện, thời gian và dữ liệu
            trước/sau của các thay đổi trong hệ thống.
        </p>

    </header>


    <main class="main-content">

        <section class="filter-card">

            <form id="searchForm">

                <div class="filter-grid">

                    <div class="form-group">

                        <label for="usernameFilter">
                            Người thực hiện
                        </label>

                        <input
                            type="text"
                            id="usernameFilter"
                            placeholder="Nhập email hoặc người thực hiện">

                    </div>


                    <div class="form-group">

                        <label for="targetObjectFilter">
                            Loại đối tượng
                        </label>

                        <input
                            type="text"
                            id="targetObjectFilter"
                            placeholder="Ví dụ: USER_ROLE">

                    </div>


                    <div class="form-group">

                        <label for="actionFilter">
                            Hành động
                        </label>

                        <select id="actionFilter">

                            <option value="">
                                -- Tất cả --
                            </option>

                            <option value="CREATE">
                                Thêm mới
                            </option>

                            <option value="UPDATE">
                                Cập nhật
                            </option>

                            <option value="DELETE">
                                Xóa
                            </option>

                            <option value="LOGIN">
                                Đăng nhập
                            </option>

                        </select>

                    </div>


                    <div class="form-group">

                        <label for="startDate">
                            Từ ngày
                        </label>

                        <input
                            type="date"
                            id="startDate">

                    </div>


                    <div class="form-group">

                        <label for="endDate">
                            Đến ngày
                        </label>

                        <input
                            type="date"
                            id="endDate">

                    </div>

                </div>


                <div class="filter-actions">

                    <button
                        type="button"
                        id="btnReset"
                        class="btn btn-light">

                        Đặt lại

                    </button>

                    <button
                        type="submit"
                        class="btn btn-primary">

                        Lọc dữ liệu

                    </button>

                </div>

            </form>

        </section>


        <section class="audit-log-card">

            <div
                id="loadingState"
                class="state-box">

                <div class="spinner"></div>

                <p>
                    Đang lấy dữ liệu nhật ký...
                </p>

            </div>


            <div
                id="tableContainer"
                class="table-wrapper d-none">

                <table class="table-audit">

                    <thead>

                    <tr>

                        <th>STT</th>

                        <th>
                            Người thực hiện
                        </th>

                        <th>
                            Hành động
                        </th>

                        <th>
                            Loại đối tượng
                        </th>

                        <th>
                            Dữ liệu trước
                        </th>

                        <th>
                            Dữ liệu sau
                        </th>

                        <th>
                            Thời gian
                        </th>

                    </tr>

                    </thead>

                    <tbody id="logTableBody">
                    </tbody>

                </table>

            </div>


            <div
                id="emptyState"
                class="state-box d-none">

                <h3>
                    Không tìm thấy dữ liệu nhật ký
                </h3>

                <p>
                    Chưa có bản ghi phù hợp với bộ lọc.
                </p>

            </div>


            <div
                id="errorState"
                class="state-box d-none">

                <h3 class="error-title">
                    Không thể tải nhật ký
                </h3>

                <p id="errorMessageText">
                    Không thể kết nối tới máy chủ.
                </p>

                <button
                    id="btnRetry"
                    type="button"
                    class="btn btn-light">

                    Thử lại

                </button>

            </div>

        </section>

    </main>

</div>


<script
    src="${pageContext.request.contextPath}/assets/js/audit-log.js">
</script>

</body>

</html>

