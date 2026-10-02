<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>

<%
    HttpSession currentSession = request.getSession(false);

    String currentRole =
            currentSession == null
                    ? null
                    : (String) currentSession.getAttribute("userRole");

    if (!"ADMIN".equalsIgnoreCase(currentRole)) {

        response.sendRedirect(
                request.getContextPath() + "/access-denied"
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

    <title>Quản lý trường tùy chỉnh</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/sidebar.css">

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/custom-field.css">

</head>

<body>

<script>
    window.contextPath = "${pageContext.request.contextPath}";
</script>

<jsp:include page="/components/sidebar.jsp" />

<main class="custom-field-page">

    <header class="page-heading">

        <div>

            <h1>Trường tùy chỉnh</h1>

            <p class="page-description">
                Tạo và quản lý các trường thông tin riêng cho khách hàng
                và cơ hội bán hàng.
            </p>

        </div>

        <button
            type="button"
            class="btn-primary"
            id="openCreateModal">

            Thêm trường mới

        </button>

    </header>


    <section class="content-card">

        <div class="toolbar">

            <div class="toolbar-title">

                <h2>Danh sách trường</h2>

                <span
                    class="record-count"
                    id="recordCount">

                    0 trường

                </span>

            </div>


            <div class="search-box">

                <input
                    type="search"
                    id="searchInput"
                    placeholder="Tìm theo tên hoặc mã trường..."
                    aria-label="Tìm kiếm trường tùy chỉnh">

            </div>

        </div>


        <div
            id="pageMessage"
            class="page-message"
            hidden>
        </div>


        <div class="table-wrapper">

            <table>

                <thead>

                <tr>

                    <th>Tên trường</th>

                    <th>Mã trường</th>

                    <th>Kiểu dữ liệu</th>

                    <th>Đối tượng</th>

                    <th>Bắt buộc</th>

                    <th>Trạng thái</th>

                    <th class="action-column">
                        Thao tác
                    </th>

                </tr>

                </thead>


                <tbody id="fieldTableBody">

                    <tr class="empty-row">

                        <td colspan="7">

                            <div class="empty-state">

                                <strong>
                                    Đang tải dữ liệu...
                                </strong>

                            </div>

                        </td>

                    </tr>

                </tbody>

            </table>

        </div>

    </section>

</main>


<div
    class="modal-backdrop"
    id="fieldModal"
    aria-hidden="true">

    <section
        class="modal-panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modalTitle">

        <div class="modal-heading">

            <div>

                <h2 id="modalTitle">
                    Thêm trường tùy chỉnh
                </h2>

                <p class="modal-description">
                    Khai báo thông tin cho trường dữ liệu.
                </p>

            </div>

            <button
                type="button"
                class="modal-close"
                id="closeModal"
                aria-label="Đóng">

                Đóng

            </button>

        </div>


        <form id="fieldForm">

            <input
                type="hidden"
                id="editingId">


            <div class="form-group">

                <label for="fieldName">
                    Tên trường <span>*</span>
                </label>

                <input
                    type="text"
                    id="fieldName"
                    maxlength="255"
                    placeholder="Ví dụ: Ngày sinh"
                    required>

            </div>


            <div class="form-group">

                <label for="fieldCode">
                    Mã trường <span>*</span>
                </label>

                <input
                    type="text"
                    id="fieldCode"
                    maxlength="100"
                    placeholder="Ví dụ: ngay_sinh"
                    required>

                <small>
                    Bắt đầu bằng chữ, chỉ dùng chữ, số và dấu gạch dưới.
                </small>

            </div>


            <div class="form-row">

                <div class="form-group">

                    <label for="fieldType">
                        Kiểu dữ liệu <span>*</span>
                    </label>

                    <select
                        id="fieldType"
                        required>

                        <option value="">
                            Chọn kiểu dữ liệu
                        </option>

                        <option value="TEXT">
                            Văn bản
                        </option>

                        <option value="NUMBER">
                            Số
                        </option>

                        <option value="DATE">
                            Ngày
                        </option>

                        <option value="BOOLEAN">
                            Có / Không
                        </option>

                        <option value="SELECT">
                            Danh sách lựa chọn
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label for="fieldTarget">
                        Đối tượng áp dụng <span>*</span>
                    </label>

                    <select
                        id="fieldTarget"
                        required>

                        <option value="">
                            Chọn đối tượng
                        </option>

                        <option value="CUSTOMER">
                            Khách hàng
                        </option>

                        <option value="OPPORTUNITY">
                            Cơ hội bán hàng
                        </option>

                    </select>

                </div>

            </div>


            <div
                class="form-group"
                id="selectConfigGroup"
                hidden>

                <label for="fieldConfig">
                    Danh sách lựa chọn <span>*</span>
                </label>

                <textarea
                    id="fieldConfig"
                    maxlength="1000"
                    rows="3"
                    placeholder="Ví dụ: Website,Facebook,Zalo,Giới thiệu"></textarea>

                <small>
                    Các giá trị cách nhau bằng dấu phẩy.
                </small>

            </div>


            <div class="form-row">

                <div class="form-group">

                    <label for="displayOrder">
                        Thứ tự hiển thị
                    </label>

                    <input
                        type="number"
                        id="displayOrder"
                        min="0"
                        value="0">

                </div>


                <div class="form-group">

                    <label for="fieldStatus">
                        Trạng thái
                    </label>

                    <select id="fieldStatus">

                        <option value="true">
                            Đang sử dụng
                        </option>

                        <option value="false">
                            Ngừng sử dụng
                        </option>

                    </select>

                </div>

            </div>


            <div class="form-group">

                <label class="checkbox-label">

                    <input
                        type="checkbox"
                        id="fieldRequired">

                    <span>
                        Bắt buộc nhập thông tin
                    </span>

                </label>

            </div>


            <div
                class="form-error"
                id="formError"
                role="alert">
            </div>


            <div class="modal-actions">

                <button
                    type="button"
                    class="btn-secondary"
                    id="cancelModal">

                    Hủy

                </button>

                <button
                    type="submit"
                    class="btn-primary"
                    id="saveField">

                    Lưu trường

                </button>

            </div>

        </form>

    </section>

</div>


<script
    src="${pageContext.request.contextPath}/assets/js/custom-field.js?v=S208_FIX4">
</script>

</body>

</html>
