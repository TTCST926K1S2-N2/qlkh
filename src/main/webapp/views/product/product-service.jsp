<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Quản lý Sản phẩm / Dịch vụ & Bảng giá</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/product-service.css">
</head>

<body class="bg-light">

<div class="container-fluid py-4">

    <!-- Page Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="fw-bold mb-1">
                Sản phẩm & Dịch vụ
            </h3>

            <p class="text-muted small mb-0">
                Quản lý danh mục sản phẩm, dịch vụ và cấu hình bảng giá áp dụng
            </p>
        </div>

        <div>
            <button class="btn btn-primary me-2"
                    onclick="openAddProductModal()">
                Thêm Sản phẩm/Dịch vụ
            </button>

            <button class="btn btn-outline-primary"
                    onclick="openAddPriceListModal()">
                Thêm Bảng giá
            </button>
        </div>
    </div>


    <!-- Navigation Tabs -->
    <ul class="nav nav-tabs custom-tabs mb-3"
        id="productTab"
        role="tablist">

        <li class="nav-item" role="presentation">
            <button class="nav-link active"
                    id="products-tab"
                    data-bs-toggle="tab"
                    data-bs-target="#products-pane"
                    type="button">

                Danh sách Sản phẩm / Dịch vụ

            </button>
        </li>

        <li class="nav-item" role="presentation">
            <button class="nav-link"
                    id="pricelists-tab"
                    data-bs-toggle="tab"
                    data-bs-target="#pricelists-pane"
                    type="button">

                Quản lý Bảng giá

            </button>
        </li>

    </ul>


    <div class="tab-content" id="productTabContent">

        <!-- TAB 1: DANH SÁCH SẢN PHẨM / DỊCH VỤ -->
        <div class="tab-pane fade show active"
             id="products-pane"
             role="tabpanel">

            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body">

                    <!-- Filter Bar -->
                    <form id="filterForm"
                          class="row g-3 align-items-center">

                        <div class="col-md-4">
                            <div class="input-group">

                                <input type="text"
                                       id="searchKeyword"
                                       class="form-control"
                                       placeholder="Tìm kiếm theo mã, tên...">

                            </div>
                        </div>


                        <div class="col-md-3">
                            <select id="filterType"
                                    class="form-select">

                                <option value="">
                                    -- Tất cả loại --
                                </option>

                                <option value="PRODUCT">
                                    Sản phẩm
                                </option>

                                <option value="SERVICE">
                                    Dịch vụ
                                </option>

                            </select>
                        </div>


                        <div class="col-md-3">
                            <select id="filterStatus"
                                    class="form-select">

                                <option value="">
                                    -- Tất cả trạng thái --
                                </option>

                                <option value="ACTIVE">
                                    Đang kinh doanh
                                </option>

                                <option value="INACTIVE">
                                    Ngừng kinh doanh
                                </option>

                            </select>
                        </div>


                        <div class="col-md-2 d-flex gap-2">

                            <button type="button"
                                    class="btn btn-secondary w-100"
                                    onclick="handleSearch()">

                                Lọc

                            </button>

                            <button type="button"
                                    class="btn btn-light border w-100"
                                    onclick="resetFilter()">

                                Đặt lại

                            </button>

                        </div>

                    </form>

                </div>
            </div>


            <!-- Product Table -->
            <div class="card border-0 shadow-sm">

                <div class="card-body p-0">

                    <div class="table-responsive">

                        <table class="table table-hover align-middle mb-0">

                            <thead class="table-light">
                            <tr>

                                <th>STT</th>
                                <th>Mã SP/DV</th>
                                <th>Tên Sản phẩm / Dịch vụ</th>
                                <th>Loại</th>
                                <th>Đơn vị tính</th>
                                <th>Giá niêm yết</th>
                                <th>Trạng thái</th>
                                <th class="text-center">Thao tác</th>

                            </tr>
                            </thead>

                            <tbody id="productTableBody">

                                <!-- Data rendered via JS -->

                            </tbody>

                        </table>

                    </div>

                </div>

            </div>

        </div>


        <!-- TAB 2: QUẢN LÝ BẢNG GIÁ -->
        <div class="tab-pane fade"
             id="pricelists-pane"
             role="tabpanel">

            <div class="card border-0 shadow-sm">

                <div class="card-body">

                    <h5 class="fw-bold mb-3">
                        Danh sách Bảng giá đang áp dụng
                    </h5>

                    <div class="table-responsive">

                        <table class="table table-hover align-middle mb-0">

                            <thead class="table-light">
                            <tr>

                                <th>Mã bảng giá</th>
                                <th>Tên bảng giá</th>
                                <th>Ngày áp dụng</th>
                                <th>Trạng thái</th>
                                <th class="text-center">Thao tác</th>

                            </tr>
                            </thead>

                            <tbody id="priceListTableBody">

                                <!-- Data rendered via JS -->

                            </tbody>

                        </table>

                    </div>

                </div>

            </div>

        </div>

    </div>

</div>


<!-- MODAL THÊM / CHỈNH SỬA SẢN PHẨM / DỊCH VỤ -->
<div class="modal fade"
     id="productModal"
     tabindex="-1"
     aria-hidden="true">

    <div class="modal-dialog modal-lg modal-dialog-centered">

        <div class="modal-content">

            <div class="modal-header">

                <h5 class="modal-title fw-bold"
                    id="productModalTitle">

                    Thêm mới Sản phẩm / Dịch vụ

                </h5>

                <button type="button"
                        class="btn-close"
                        data-bs-dismiss="modal"
                        aria-label="Close">
                </button>

            </div>


            <form id="productForm" novalidate>

                <div class="modal-body">

                    <input type="hidden" id="productId">

                    <div class="row g-3">

                        <!-- Mã sản phẩm -->
                        <div class="col-md-6">

                            <label for="productCode"
                                   class="form-label required">

                                Mã SP/DV

                            </label>

                            <input type="text"
                                   class="form-control"
                                   id="productCode"
                                   placeholder="Ví dụ: SP001"
                                   required>

                            <div class="invalid-feedback">
                                Vui lòng nhập mã (không trùng lặp).
                            </div>

                        </div>


                        <!-- Loại -->
                        <div class="col-md-6">

                            <label for="productType"
                                   class="form-label required">

                                Loại

                            </label>

                            <select class="form-select"
                                    id="productType"
                                    required>

                                <option value="PRODUCT">
                                    Sản phẩm
                                </option>

                                <option value="SERVICE">
                                    Dịch vụ
                                </option>

                            </select>

                        </div>


                        <!-- Tên -->
                        <div class="col-md-12">

                            <label for="productName"
                                   class="form-label required">

                                Tên Sản phẩm / Dịch vụ

                            </label>

                            <input type="text"
                                   class="form-control"
                                   id="productName"
                                   placeholder="Nhập tên..."
                                   required>

                            <div class="invalid-feedback">
                                Vui lòng nhập tên sản phẩm/dịch vụ.
                            </div>

                        </div>


                        <!-- Đơn vị -->
                        <div class="col-md-6">

                            <label for="unit"
                                   class="form-label required">

                                Đơn vị tính

                            </label>

                            <input type="text"
                                   class="form-control"
                                   id="unit"
                                   placeholder="Cái, Hộp, Gói, Lần..."
                                   required>

                            <div class="invalid-feedback">
                                Vui lòng nhập đơn vị tính.
                            </div>

                        </div>


                        <!-- Giá -->
                        <div class="col-md-6">

                            <label for="basePrice"
                                   class="form-label required">

                                Giá niêm yết (VNĐ)

                            </label>

                            <input type="number"
                                   class="form-control"
                                   id="basePrice"
                                   min="0"
                                   step="1000"
                                   placeholder="0"
                                   required>

                            <div class="invalid-feedback">
                                Giá niêm yết phải lớn hơn hoặc bằng 0.
                            </div>

                        </div>


                        <!-- Trạng thái -->
                        <div class="col-md-12">

                            <label for="status"
                                   class="form-label">

                                Trạng thái kinh doanh

                            </label>

                            <select class="form-select"
                                    id="status">

                                <option value="ACTIVE">
                                    Đang kinh doanh
                                </option>

                                <option value="INACTIVE">
                                    Ngừng kinh doanh
                                </option>

                            </select>

                        </div>


                        <!-- Mô tả -->
                        <div class="col-md-12">

                            <label for="description"
                                   class="form-label">

                                Mô tả chi tiết

                            </label>

                            <textarea class="form-control"
                                      id="description"
                                      rows="3"
                                      placeholder="Nhập ghi chú hoặc thông tin chi tiết..."></textarea>

                        </div>

                    </div>

                </div>


                <div class="modal-footer">

                    <button type="button"
                            class="btn btn-light border"
                            data-bs-dismiss="modal">

                        Hủy

                    </button>

                    <button type="submit"
                            class="btn btn-primary"
                            id="btnSaveProduct">

                        Lưu thay đổi

                    </button>

                </div>

            </form>

        </div>

    </div>

</div>


<!-- MODAL XÁC NHẬN XÓA -->
<div class="modal fade"
     id="deleteModal"
     tabindex="-1"
     aria-hidden="true">

    <div class="modal-dialog modal-dialog-centered">

        <div class="modal-content">

            <div class="modal-header border-0">

                <h5 class="modal-title fw-bold text-danger">
                    Xác nhận xóa
                </h5>

                <button type="button"
                        class="btn-close"
                        data-bs-dismiss="modal"
                        aria-label="Close">
                </button>

            </div>


            <div class="modal-body">

                Bạn có chắc chắn muốn xóa
                <strong id="deleteTargetName"></strong>
                khỏi hệ thống?

                Thao tác này không thể hoàn tác.

            </div>


            <div class="modal-footer border-0">

                <button type="button"
                        class="btn btn-light border"
                        data-bs-dismiss="modal">

                    Hủy

                </button>

                <button type="button"
                        class="btn btn-danger"
                        id="btnConfirmDelete">

                    Xóa ngay

                </button>

            </div>

        </div>

    </div>

</div>


<!-- TOAST CONTAINER THÔNG BÁO -->
<div class="toast-container position-fixed bottom-0 end-0 p-3">

    <div id="liveToast"
         class="toast align-items-center text-white border-0"
         role="alert"
         aria-live="assertive"
         aria-atomic="true">

        <div class="d-flex">

            <div class="toast-body"
                 id="toastMessage">
            </div>

            <button type="button"
                    class="btn-close btn-close-white me-2 m-auto"
                    data-bs-dismiss="toast"
                    aria-label="Close">
            </button>

        </div>

    </div>

</div>


<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- Custom JS -->
<script src="${pageContext.request.contextPath}/assets/js/product-service.js"></script>

</body>
</html>