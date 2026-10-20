<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Sản phẩm & Dịch vụ</title>

    <link
            href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
            rel="stylesheet">

    <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/assets/css/product-service.css">
</head>

<body data-context-path="${pageContext.request.contextPath}" data-user-role="${sessionScope.userRole}">

<div class="system-layout">

    <jsp:include page="/components/sidebar.jsp"/>

    <main class="main-content">

        <div class="container-fluid py-4">

            <!-- HEADER -->
            <div class="page-header mb-4">
                <div>
                    <h2 class="page-title">Sản phẩm & Dịch vụ</h2>
                    <p class="page-subtitle">
                        Quản lý sản phẩm, dịch vụ và bảng giá
                    </p>
                </div>

                <div class="page-actions">
                    <button
                            type="button"
                            class="btn btn-primary"
                            id="btnAddProduct">
                        Thêm sản phẩm / dịch vụ
                    </button>

                    <button
                            type="button"
                            class="btn btn-outline-primary"
                            id="btnAddPriceList">
                        Thêm bảng giá
                    </button>
                </div>
            </div>

            <!-- TABS -->
            <ul class="nav custom-tabs mb-4" id="productPriceTabs">
                <li class="nav-item">
                    <button
                            type="button"
                            class="nav-link active"
                            id="productTab"
                            data-bs-toggle="tab"
                            data-bs-target="#productPanel">
                        Danh sách Sản phẩm / Dịch vụ
                    </button>
                </li>

                <li class="nav-item">
                    <button
                            type="button"
                            class="nav-link"
                            id="priceListTab"
                            data-bs-toggle="tab"
                            data-bs-target="#priceListPanel">
                        Quản lý Bảng giá
                    </button>
                </li>
            </ul>

            <div class="tab-content">

                <div
                        class="tab-pane fade show active"
                        id="productPanel">

                    <div class="card shadow-sm border-0">

                        <div class="card-body">

                            <!-- FILTER -->
                            <form id="filterForm">

                                <div class="row g-3 align-items-end">

                                    <div class="col-md-5">
                                        <label
                                                for="searchKeyword"
                                                class="form-label">
                                            Tìm kiếm
                                        </label>

                                        <input
                                                type="text"
                                                class="form-control"
                                                id="searchKeyword"
                                                placeholder="Nhập mã hoặc tên sản phẩm / dịch vụ">
                                    </div>

                                    <div class="col-md-2">
                                        <label
                                                for="filterType"
                                                class="form-label">
                                            Loại
                                        </label>

                                        <select
                                                class="form-select"
                                                id="filterType">

                                            <option value="">Tất cả</option>
                                            <option value="PRODUCT">
                                                Sản phẩm
                                            </option>
                                            <option value="SERVICE">
                                                Dịch vụ
                                            </option>

                                        </select>
                                    </div>

                                    <div class="col-md-2">
                                        <label
                                                for="filterStatus"
                                                class="form-label">
                                            Trạng thái
                                        </label>

                                        <select
                                                class="form-select"
                                                id="filterStatus">

                                            <option value="">Tất cả</option>

                                            <option value="ACTIVE">
                                                Đang kinh doanh
                                            </option>

                                            <option value="INACTIVE">
                                                Ngừng kinh doanh
                                            </option>

                                        </select>
                                    </div>

                                    <div class="col-md-3 filter-actions">

                                        <button
                                                type="submit"
                                                class="btn btn-primary">
                                            Tìm kiếm
                                        </button>

                                        <button
                                                type="button"
                                                class="btn btn-outline-secondary"
                                                id="btnResetFilter">
                                            Đặt lại
                                        </button>

                                    </div>

                                </div>

                            </form>

                            <!-- PRODUCT TABLE -->
                            <div class="table-responsive mt-4">

                                <table class="table table-hover align-middle">

                                    <thead>
                                    <tr>
                                        <th>STT</th>
                                        <th>Mã SP/DV</th>
                                        <th>Tên</th>
                                        <th>Loại</th>
                                        <th>Đơn vị tính</th>
                                        <th>Giá niêm yết</th>
                                        <th>Giá sàn</th>
                                <th class="manager-only-cost">Giá vốn</th>
                                        <th>Trạng thái</th>
                                        <th class="text-center">
                                            Thao tác
                                        </th>
                                    </tr>
                                    </thead>

                                    <tbody id="productTableBody"></tbody>

                                </table>

                            </div>

                        </div>

                    </div>

                </div>

                <div
                        class="tab-pane fade"
                        id="priceListPanel">

                    <div class="card shadow-sm border-0">

                        <div class="card-body">

                            <div class="table-responsive">

                                <table class="table table-hover align-middle">

                                    <thead>
                                    <tr>
                                        <th>Mã bảng giá</th>
                                        <th>Tên bảng giá</th>
                                        <th>Thời gian áp dụng</th>
                                        <th>Trạng thái</th>
                                        <th class="text-center">
                                            Thao tác
                                        </th>
                                    </tr>
                                    </thead>

                                    <tbody id="priceListTableBody"></tbody>

                                </table>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    </main>

</div>

<div
        class="modal fade"
        id="productModal"
        tabindex="-1"
        aria-hidden="true">

    <div class="modal-dialog modal-lg modal-dialog-centered">

        <div class="modal-content">

            <form id="productForm">

                <div class="modal-header">

                    <h5
                            class="modal-title"
                            id="productModalTitle">
                        Thêm mới Sản phẩm / Dịch vụ
                    </h5>

                    <button
                            type="button"
                            class="btn btn-sm btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Đóng
                    </button>

                </div>

                <div class="modal-body">

                    <input
                            type="hidden"
                            id="productId">

                    <div class="row g-3">

                        <div class="col-md-6">

                            <label
                                    for="productCode"
                                    class="form-label required">
                                Mã sản phẩm / dịch vụ
                            </label>

                            <input
                                    type="text"
                                    class="form-control"
                                    id="productCode"
                                    maxlength="50"
                                    required>

                            <div class="invalid-feedback">
                                Vui lòng nhập mã.
                            </div>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="productType"
                                    class="form-label required">
                                Loại
                            </label>

                            <select
                                    class="form-select"
                                    id="productType"
                                    required>

                                <option value="">
                                    -- Chọn loại --
                                </option>

                                <option value="PRODUCT">
                                    Sản phẩm
                                </option>

                                <option value="SERVICE">
                                    Dịch vụ
                                </option>

                            </select>

                            <div class="invalid-feedback">
                                Vui lòng chọn loại.
                            </div>

                        </div>

                        <div class="col-md-8">

                            <label
                                    for="productName"
                                    class="form-label required">
                                Tên sản phẩm / dịch vụ
                            </label>

                            <input
                                    type="text"
                                    class="form-control"
                                    id="productName"
                                    maxlength="255"
                                    required>

                            <div class="invalid-feedback">
                                Vui lòng nhập tên.
                            </div>

                        </div>

                        <div class="col-md-4">

                            <label
                                    for="unit"
                                    class="form-label required">
                                Đơn vị tính
                            </label>

                            <input
                                    type="text"
                                    class="form-control"
                                    id="unit"
                                    maxlength="50"
                                    required>

                            <div class="invalid-feedback">
                                Vui lòng nhập đơn vị tính.
                            </div>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="basePrice"
                                    class="form-label required">
                                Giá niêm yết
                            </label>

                            <input
                                    type="number"
                                    class="form-control"
                                    id="basePrice"
                                    min="0"
                                    step="0.01"
                                    required>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="floorPrice"
                                    class="form-label required">
                                Giá sàn
                            </label>

                            <input
                                    type="number"
                                    class="form-control"
                                    id="floorPrice"
                                    min="0"
                                    step="0.01"
                                    required>

                            <div class="form-text">
                                Giá sàn không được lớn hơn giá niêm yết.
                            </div>

                        </div>

                        <div class="col-md-6 manager-only-cost">

                            <label
                                    for="costPrice"
                                    class="form-label">
                                Giá vốn
                            </label>

                            <input
                                    type="number"
                                    class="form-control"
                                    id="costPrice"
                                    min="0"
                                    step="0.01">

                            <div class="form-text">
                                Chỉ Giám đốc kinh doanh được xem và sửa giá vốn.
                            </div>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="status"
                                    class="form-label required">
                                Trạng thái
                            </label>

                            <select
                                    class="form-select"
                                    id="status"
                                    required>

                                <option value="ACTIVE">
                                    Đang kinh doanh
                                </option>

                                <option value="INACTIVE">
                                    Ngừng kinh doanh
                                </option>

                            </select>

                        </div>

                        <div class="col-12">

                            <label
                                    for="description"
                                    class="form-label">
                                Mô tả
                            </label>

                            <textarea
                                    class="form-control"
                                    id="description"
                                    rows="3"
                                    maxlength="1000"></textarea>

                        </div>

                    </div>

                </div>

                <div class="modal-footer">

                    <button
                            type="button"
                            class="btn btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Hủy
                    </button>

                    <button
                            type="submit"
                            class="btn btn-primary"
                            id="btnSaveProduct">
                        Lưu
                    </button>

                </div>

            </form>

        </div>

    </div>

</div>

<div
        class="modal fade"
        id="priceListModal"
        tabindex="-1"
        aria-hidden="true">

    <div class="modal-dialog modal-lg modal-dialog-centered">

        <div class="modal-content">

            <form id="priceListForm">

                <div class="modal-header">

                    <h5
                            class="modal-title"
                            id="priceListModalTitle">
                        Thêm bảng giá
                    </h5>

                    <button
                            type="button"
                            class="btn btn-sm btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Đóng
                    </button>

                </div>

                <div class="modal-body">

                    <input
                            type="hidden"
                            id="priceListId">

                    <div class="row g-3">

                        <div class="col-md-6">

                            <label
                                    for="priceListCode"
                                    class="form-label required">
                                Mã bảng giá
                            </label>

                            <input
                                    type="text"
                                    class="form-control"
                                    id="priceListCode"
                                    maxlength="50"
                                    required>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="priceListName"
                                    class="form-label required">
                                Tên bảng giá
                            </label>

                            <input
                                    type="text"
                                    class="form-control"
                                    id="priceListName"
                                    maxlength="255"
                                    required>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="priceListStartDate"
                                    class="form-label required">
                                Ngày bắt đầu
                            </label>

                            <input
                                    type="date"
                                    class="form-control"
                                    id="priceListStartDate"
                                    required>

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="priceListEndDate"
                                    class="form-label">
                                Ngày kết thúc
                            </label>

                            <input
                                    type="date"
                                    class="form-control"
                                    id="priceListEndDate">

                        </div>

                        <div class="col-md-6">

                            <label
                                    for="priceListStatus"
                                    class="form-label required">
                                Trạng thái
                            </label>

                            <select
                                    class="form-select"
                                    id="priceListStatus"
                                    required>

                                <option value="ACTIVE">
                                    Đang áp dụng
                                </option>

                                <option value="INACTIVE">
                                    Ngừng áp dụng
                                </option>

                            </select>

                        </div>

                        <div class="col-12">

                            <label
                                    for="priceListDescription"
                                    class="form-label">
                                Mô tả
                            </label>

                            <textarea
                                    class="form-control"
                                    id="priceListDescription"
                                    rows="3"></textarea>

                        </div>

                    </div>

                </div>

                <div class="modal-footer">

                    <button
                            type="button"
                            class="btn btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Hủy
                    </button>

                    <button
                            type="submit"
                            class="btn btn-primary"
                            id="btnSavePriceList">
                        Lưu
                    </button>

                </div>

            </form>

        </div>

    </div>

</div>

<div
        class="modal fade"
        id="priceListDetailModal"
        tabindex="-1"
        aria-hidden="true">

    <div class="modal-dialog modal-xl modal-dialog-centered">

        <div class="modal-content">

            <div class="modal-header">

                <div>

                    <h5
                            class="modal-title"
                            id="priceListDetailTitle">
                        Chi tiết bảng giá
                    </h5>

                    <div
                            class="small text-muted"
                            id="priceListDetailSubtitle">
                    </div>

                </div>

                <button
                        type="button"
                        class="btn btn-sm btn-outline-secondary"
                        data-bs-dismiss="modal">
                    Đóng
                </button>

            </div>

            <div class="modal-body">

                <div class="detail-toolbar">

                    <button
                            type="button"
                            class="btn btn-primary"
                            id="btnAddPriceListItem">
                        Thêm sản phẩm vào bảng giá
                    </button>

                </div>

                <div class="table-responsive mt-3">

                    <table class="table table-hover align-middle">

                        <thead>
                        <tr>
                            <th>STT</th>
                            <th>Mã SP/DV</th>
                            <th>Tên</th>
                            <th>Loại</th>
                            <th>Đơn vị</th>
                            <th>Giá bán</th>
                            <th>Giá sàn</th>
                            <th class="text-center">
                                Thao tác
                            </th>
                        </tr>
                        </thead>

                        <tbody id="priceListItemTableBody"></tbody>

                    </table>

                </div>

            </div>

        </div>

    </div>

</div>

<div
        class="modal fade"
        id="priceListItemModal"
        tabindex="-1"
        aria-hidden="true">

    <div class="modal-dialog modal-md modal-dialog-centered">

        <div class="modal-content">

            <form id="priceListItemForm">

                <div class="modal-header">

                    <h5
                            class="modal-title"
                            id="priceListItemModalTitle">
                        Thêm sản phẩm vào bảng giá
                    </h5>

                    <button
                            type="button"
                            class="btn btn-sm btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Đóng
                    </button>

                </div>

                <div class="modal-body">

                    <input
                            type="hidden"
                            id="priceListItemId">

                    <div class="mb-3">

                        <label
                                for="priceListItemProductId"
                                class="form-label required">
                            Sản phẩm / Dịch vụ
                        </label>

                        <select
                                class="form-select"
                                id="priceListItemProductId"
                                required>
                        </select>

                    </div>

                    <div class="mb-3">

                        <label
                                for="priceListItemListPrice"
                                class="form-label required">
                            Giá bán
                        </label>

                        <input
                                type="number"
                                class="form-control"
                                id="priceListItemListPrice"
                                min="0"
                                step="0.01"
                                required>

                    </div>

                    <div class="mb-3">

                        <label
                                for="priceListItemFloorPrice"
                                class="form-label required">
                            Giá sàn
                        </label>

                        <input
                                type="number"
                                class="form-control"
                                id="priceListItemFloorPrice"
                                min="0"
                                step="0.01"
                                required>

                    </div>

                </div>

                <div class="modal-footer">

                    <button
                            type="button"
                            class="btn btn-outline-secondary"
                            data-bs-dismiss="modal">
                        Hủy
                    </button>

                    <button
                            type="submit"
                            class="btn btn-primary"
                            id="btnSavePriceListItem">
                        Lưu
                    </button>

                </div>

            </form>

        </div>

    </div>

</div>

<div
        class="modal fade"
        id="deleteModal"
        tabindex="-1"
        aria-hidden="true">

    <div class="modal-dialog modal-sm modal-dialog-centered">

        <div class="modal-content">

            <div class="modal-header">

                <h5 class="modal-title">
                    Xác nhận
                </h5>

                <button
                        type="button"
                        class="btn btn-sm btn-outline-secondary"
                        data-bs-dismiss="modal">
                    Đóng
                </button>

            </div>

            <div class="modal-body">

                <p class="mb-0">
                    Bạn có chắc chắn muốn thực hiện thao tác với:
                </p>

                <p
                        class="fw-bold mt-2 mb-0"
                        id="deleteTargetName">
                </p>

            </div>

            <div class="modal-footer">

                <button
                        type="button"
                        class="btn btn-outline-secondary"
                        data-bs-dismiss="modal">
                    Hủy
                </button>

                <button
                        type="button"
                        class="btn btn-danger"
                        id="btnConfirmDelete">
                    Xác nhận
                </button>

            </div>

        </div>

    </div>

</div>

<div
        class="toast-container position-fixed bottom-0 end-0 p-3">

    <div
            id="liveToast"
            class="toast"
            role="alert"
            aria-live="assertive"
            aria-atomic="true">

        <div class="toast-header">

            <strong
                    class="me-auto"
                    id="toastTitle">
                Thông báo
            </strong>

            <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary"
                    data-bs-dismiss="toast">
                Đóng
            </button>

        </div>

        <div
                class="toast-body"
                id="toastMessage">
        </div>

    </div>

</div>

<script
        src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js">
</script>

<script
        src="${pageContext.request.contextPath}/assets/js/product-service.js">
</script>

</body>
</html>