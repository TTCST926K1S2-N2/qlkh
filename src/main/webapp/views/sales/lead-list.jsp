<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Lead</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lead-list.css">
</head>
<body>
<div class="page-container">
    <header class="page-header">
        <div><h1>Quản lý Lead</h1><p>Danh sách khách hàng tiềm năng và trạng thái xử lý</p></div>
        <button type="button" class="btn btn-primary" id="refreshBtn">Làm mới danh sách</button>
    </header>

    <section class="filter-panel" aria-label="Bộ lọc danh sách Lead">
        <div class="filter-grid">
            <div class="form-group"><label for="keyword">Tìm kiếm</label><input id="keyword" type="search" placeholder="Tên, email, số điện thoại..." autocomplete="off"></div>
            <div class="form-group"><label for="status">Trạng thái</label>
                <select id="status">
                    <option value="">Tất cả trạng thái</option><option value="NEW">Mới</option><option value="CONTACTED">Đã liên hệ</option><option value="QUALIFIED">Tiềm năng</option><option value="CONVERTED">Đã chuyển đổi</option><option value="LOST">Không thành công</option>
                </select>
            </div>
            <div class="form-group"><label for="source">Nguồn Lead</label>
                <select id="source">
                    <option value="">Tất cả nguồn</option><option value="WEBSITE">Website</option><option value="FACEBOOK">Facebook</option><option value="REFERRAL">Giới thiệu</option><option value="OTHER">Khác</option>
                </select>
            </div>
            <div class="form-group"><label for="assignedTo">Người phụ trách (ID)</label><input id="assignedTo" type="number" min="1" step="1" placeholder="Nhập ID nhân viên"></div>
            <div class="form-group"><label for="createdFrom">Ngày tạo từ</label><input id="createdFrom" type="date"></div>
            <div class="form-group"><label for="createdTo">Ngày tạo đến</label><input id="createdTo" type="date"></div>
            <div class="form-group"><label for="sortBy">Sắp xếp theo</label>
                <select id="sortBy"><option value="createdAt">Ngày tạo</option><option value="name">Tên Lead</option><option value="status">Trạng thái</option></select>
            </div>
            <div class="form-group"><label for="sortDirection">Thứ tự</label>
                <select id="sortDirection"><option value="desc">Mới nhất / Z-A</option><option value="asc">Cũ nhất / A-Z</option></select>
            </div>
        </div>
        <div class="filter-actions">
            <button type="button" class="btn btn-primary" id="searchBtn">Tìm kiếm</button>
            <button type="button" class="btn btn-secondary" id="resetBtn">Xóa bộ lọc</button>
            <button type="button" class="btn btn-secondary" id="saveFilterBtn">Lưu bộ lọc</button>
            <select id="savedFilters" aria-label="Bộ lọc đã lưu"><option value="">Chọn bộ lọc đã lưu</option></select>
        </div>
    </section>

    <div id="message" class="message" role="status" aria-live="polite" hidden></div>
    <section class="table-panel">
        <div class="table-heading"><h2>Danh sách Lead</h2><span id="totalCount">0 kết quả</span></div>
        <div class="table-wrapper">
            <table>
                <thead><tr><th>Lead</th><th>Liên hệ</th><th>Nguồn</th><th>Trạng thái</th><th>Người phụ trách</th><th>SLA</th><th>Thao tác</th></tr></thead>
                <tbody id="leadTableBody"><tr><td colspan="7" class="empty-cell">Chưa tải dữ liệu.</td></tr></tbody>
            </table>
        </div>
        <div class="pagination">
            <label for="pageSize">Số dòng:</label>
            <select id="pageSize"><option value="10">10</option><option value="20">20</option><option value="50">50</option></select>
            <span id="pageInfo">Trang 1 / 1</span>
            <button type="button" class="btn btn-secondary" id="prevBtn" disabled>Trước</button>
            <button type="button" class="btn btn-secondary" id="nextBtn" disabled>Tiếp</button>
        </div>
    </section>
</div>

<dialog id="leadDialog" class="lead-dialog">
    <div class="dialog-header"><h2>Chi tiết Lead</h2><button type="button" id="closeDialogBtn" class="btn btn-secondary">Đóng</button></div>
    <div id="leadDetail" class="lead-detail"></div>
</dialog>
<script>window.contextPath = "${pageContext.request.contextPath}";</script>
<script src="${pageContext.request.contextPath}/assets/js/lead-list.js"></script>
</body>
</html>
