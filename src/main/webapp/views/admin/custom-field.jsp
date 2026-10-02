<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý trường tùy chỉnh</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/custom-field.css">
</head>

<body>

<div class="custom-field-page">

    <div class="page-heading">

        <div>
            <p class="page-eyebrow">QUẢN LÝ DỮ LIỆU</p>

            <h1>Trường tùy chỉnh</h1>

            <p class="page-description">
                Tạo và quản lý các trường thông tin riêng cho khách hàng.
            </p>
        </div>

        <button type="button"
                class="btn-primary"
                id="openCreateModal">
            Thêm trường mới
        </button>

    </div>


    <section class="content-card">

        <div class="toolbar">

            <div class="toolbar-title">

                <h2>Danh sách trường</h2>

                <span class="record-count"
                      id="recordCount">
                    0 trường
                </span>

            </div>


            <div class="search-box">

                <input type="search"
                       id="searchInput"
                       placeholder="Tìm theo tên hoặc mã trường..."
                       aria-label="Tìm kiếm trường tùy chỉnh">

            </div>

        </div>


        <div class="table-wrapper">

            <table>

                <thead>

                <tr>

                    <th>Tên trường</th>

                    <th>Mã trường</th>

                    <th>Kiểu dữ liệu</th>

                    <th>Đối tượng</th>

                    <th>Trạng thái</th>

                    <th class="action-column">
                        Thao tác
                    </th>

                </tr>

                </thead>


                <tbody id="fieldTableBody">

                <tr class="empty-row">

                    <td colspan="6">

                        <div class="empty-state">

                            <strong>
                                Chưa có trường tùy chỉnh
                            </strong>

                            <p>
                                Hãy thêm trường đầu tiên để bắt đầu quản lý.
                            </p>

                            <button type="button"
                                    class="btn-secondary"
                                    id="emptyCreateButton">
                                Thêm trường
                            </button>

                        </div>

                    </td>

                </tr>

                </tbody>

            </table>

        </div>

    </section>

</div>


<div class="modal-backdrop"
     id="fieldModal"
     aria-hidden="true">

    <section class="modal-panel"
             role="dialog"
             aria-modal="true"
             aria-labelledby="modalTitle">

        <div class="modal-heading">

            <div>

                <p class="page-eyebrow">
                    CẤU HÌNH THÔNG TIN
                </p>

                <h2 id="modalTitle">
                    Thêm trường tùy chỉnh
                </h2>

                <p class="modal-description">
                    Nhập thông tin để tạo trường dữ liệu mới.
                </p>

            </div>


            <button type="button"
                    class="modal-close"
                    id="closeModal"
                    aria-label="Đóng">
                Đóng
            </button>

        </div>


        <form id="fieldForm">

            <input type="hidden"
                   id="editingId">


            <div class="form-group">

                <label for="fieldName">
                    Tên trường <span>*</span>
                </label>

                <input type="text"
                       id="fieldName"
                       maxlength="100"
                       placeholder="Ví dụ: Ngày sinh"
                       required>

                <small>
                    Tên hiển thị của trường thông tin.
                </small>

            </div>


            <div class="form-group">

                <label for="fieldCode">
                    Mã trường <span>*</span>
                </label>

                <input type="text"
                       id="fieldCode"
                       maxlength="50"
                       placeholder="Ví dụ: ngay_sinh"
                       required>

                <small>
                    Dùng chữ thường, số và dấu gạch dưới.
                </small>

            </div>


            <div class="form-row">

                <div class="form-group">

                    <label for="fieldType">
                        Kiểu dữ liệu <span>*</span>
                    </label>

                    <select id="fieldType"
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
                            Ngày tháng
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

                    <select id="fieldTarget"
                            required>

                        <option value="">
                            Chọn đối tượng
                        </option>

                        <option value="CUSTOMER">
                            Khách hàng
                        </option>

                        <option value="LEAD">
                            Khách hàng tiềm năng
                        </option>

                    </select>

                </div>

            </div>


            <div class="form-group">

                <label class="checkbox-label">

                    <input type="checkbox"
                           id="fieldRequired">

                    <span>
                        Bắt buộc nhập thông tin
                    </span>

                </label>

            </div>


            <div class="form-error"
                 id="formError"
                 role="alert">
            </div>


            <div class="modal-actions">

                <button type="button"
                        class="btn-secondary"
                        id="cancelModal">
                    Hủy
                </button>

                <button type="submit"
                        class="btn-primary"
                        id="saveField">
                    Lưu trường
                </button>

            </div>

        </form>

    </section>

</div>


<script src="${pageContext.request.contextPath}/assets/js/custom-field.js"></script>

</body>
</html>
