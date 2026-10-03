<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String userRole =
            (String) session.getAttribute("userRole");

    if (
            userRole == null
            || (
                !"ADMIN".equals(userRole)
                && !"MANAGER".equals(userRole)
            )
    ) {
        response.sendRedirect(
                request.getContextPath() + "/"
        );
        return;
    }
%>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0">

    <title>
        Lý do thắng/thua & Đối thủ
    </title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/sidebar.css">

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/win-loss-competitors.css">

</head>

<body
    data-context-path="${pageContext.request.contextPath}"
    data-user-role="<%= userRole %>">

<jsp:include page="/components/sidebar.jsp"/>

<main class="wl-page">

    <section class="wl-header">

        <div>

            <p class="wl-eyebrow">
                QUẢN LÝ BÁN HÀNG
            </p>

            <h1>
                Lý do thắng/thua & Đối thủ cạnh tranh
            </h1>

            <p class="wl-description">
                Quản lý danh mục lý do thắng,
                lý do thua và đối thủ cạnh tranh
                dùng chung cho hoạt động bán hàng.
            </p>

        </div>

        <button
            type="button"
            class="wl-button wl-button-primary"
            id="btnAdd">

            + Thêm mới

        </button>

    </section>


    <section class="wl-summary">

        <article class="wl-summary-card wl-summary-card-success">

            <div class="wl-summary-label">
                Lý do thắng
            </div>

            <div
                class="wl-summary-value"
                id="wonCount">
                0
            </div>

            <div class="wl-summary-note">
                Danh mục đang khai báo
            </div>

        </article>


        <article class="wl-summary-card wl-summary-card-danger">

            <div class="wl-summary-label">
                Lý do thua
            </div>

            <div
                class="wl-summary-value"
                id="lostCount">
                0
            </div>

            <div class="wl-summary-note">
                Danh mục đang khai báo
            </div>

        </article>


        <article class="wl-summary-card">

            <div class="wl-summary-label">
                Đối thủ cạnh tranh
            </div>

            <div
                class="wl-summary-value"
                id="competitorCount">
                0
            </div>

            <div class="wl-summary-note">
                Đối thủ đã khai báo
            </div>

        </article>

    </section>


    <section class="wl-panel">

        <div class="wl-panel-header">

            <div class="wl-tabs">

                <button
                    type="button"
                    class="wl-tab active"
                    data-tab="won">

                    Lý do thắng

                </button>

                <button
                    type="button"
                    class="wl-tab"
                    data-tab="lost">

                    Lý do thua

                </button>

                <button
                    type="button"
                    class="wl-tab"
                    data-tab="competitors">

                    Đối thủ cạnh tranh

                </button>

            </div>


            <div class="wl-filter-group">

                <div class="wl-search">

                    <input
                        type="search"
                        id="searchInput"
                        placeholder="Tìm theo mã hoặc tên...">

                </div>


                <select
                    id="statusFilter"
                    class="wl-select">

                    <option value="">
                        Tất cả trạng thái
                    </option>

                    <option value="ACTIVE">
                        Đang hoạt động
                    </option>

                    <option value="INACTIVE">
                        Ngừng hoạt động
                    </option>

                </select>


                <button
                    type="button"
                    class="wl-button wl-button-secondary"
                    id="btnReload">

                    Tải lại

                </button>

            </div>

        </div>


        <div
            class="wl-loading"
            id="loadingState">

            Đang tải dữ liệu...

        </div>


        <div
            class="wl-empty"
            id="emptyState"
            hidden>

            Chưa có dữ liệu.

        </div>


        <div
            class="wl-table-wrapper"
            id="tableWrapper">

            <table class="wl-table">

                <thead>

                <tr>

                    <th>Mã</th>

                    <th>Tên</th>

                    <th>Mô tả</th>

                    <th>Trạng thái</th>

                    <th class="wl-actions-column">
                        Thao tác
                    </th>

                </tr>

                </thead>

                <tbody id="dataBody"></tbody>

            </table>

        </div>

    </section>

</main>


<div
    class="wl-modal-backdrop"
    id="editModal"
    hidden>

    <div class="wl-modal">

        <div class="wl-modal-header">

            <div>

                <p class="wl-modal-eyebrow">
                    S2-10
                </p>

                <h2 id="modalTitle">
                    Thêm mới
                </h2>

            </div>

            <button
                type="button"
                class="wl-modal-close"
                id="btnCloseModal"
                aria-label="Đóng">

                ×

            </button>

        </div>


        <form id="editForm">

            <input
                type="hidden"
                id="entityId">


            <div class="wl-form-grid">

                <div class="wl-form-field">

                    <label for="entityCode">
                        Mã
                    </label>

                    <input
                        type="text"
                        id="entityCode"
                        maxlength="100"
                        required>

                    <small>
                        Chữ in hoa, số và dấu gạch dưới.
                    </small>

                </div>


                <div class="wl-form-field">

                    <label for="entityName">
                        Tên
                    </label>

                    <input
                        type="text"
                        id="entityName"
                        maxlength="255"
                        required>

                </div>


                <div class="wl-form-field">

                    <label for="entityStatus">
                        Trạng thái
                    </label>

                    <select
                        id="entityStatus"
                        required>

                        <option value="ACTIVE">
                            Đang hoạt động
                        </option>

                        <option value="INACTIVE">
                            Ngừng hoạt động
                        </option>

                    </select>

                </div>


                <div
                    class="wl-form-field"
                    id="reasonTypeField">

                    <label for="reasonType">
                        Loại lý do
                    </label>

                    <select id="reasonType">

                        <option value="WON">
                            Lý do thắng
                        </option>

                        <option value="LOST">
                            Lý do thua
                        </option>

                    </select>

                </div>


                <div class="wl-form-field wl-form-field-full">

                    <label for="entityDescription">
                        Mô tả
                    </label>

                    <textarea
                        id="entityDescription"
                        rows="4"
                        maxlength="1000"
                        placeholder="Nhập mô tả..."></textarea>

                </div>

            </div>


            <div class="wl-modal-footer">

                <button
                    type="button"
                    class="wl-button wl-button-secondary"
                    id="btnCancel">

                    Hủy

                </button>

                <button
                    type="submit"
                    class="wl-button wl-button-primary"
                    id="btnSave">

                    Lưu

                </button>

            </div>

        </form>

    </div>

</div>


<div
    class="wl-modal-backdrop"
    id="deleteModal"
    hidden>

    <div class="wl-modal wl-modal-small">

        <div class="wl-modal-header">

            <div>

                <p class="wl-modal-eyebrow">
                    XÁC NHẬN
                </p>

                <h2>
                    Xóa dữ liệu
                </h2>

            </div>

        </div>

        <div class="wl-confirm-content">

            Bạn có chắc muốn xóa
            <strong id="deleteName"></strong>?

        </div>

        <div class="wl-modal-footer">

            <button
                type="button"
                class="wl-button wl-button-secondary"
                id="btnCancelDelete">

                Hủy

            </button>

            <button
                type="button"
                class="wl-button wl-button-danger"
                id="btnConfirmDelete">

                Xóa

            </button>

        </div>

    </div>

</div>


<div
    class="wl-toast"
    id="wlToast">
</div>


<script
    src="${pageContext.request.contextPath}/assets/js/win-loss-competitors.js">
</script>

</body>

</html>