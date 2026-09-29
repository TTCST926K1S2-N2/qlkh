<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Lấy dữ liệu vai trò và tên người dùng từ Request/Session do Backend cung cấp
    String role = (String) request.getAttribute("userRole");
    if (role == null || role.trim().isEmpty()) {
        role = (String) session.getAttribute("role");
    }
    if (role == null || role.trim().isEmpty()) {
        role = (String) session.getAttribute("userRole");
    }
    if (role == null || role.trim().isEmpty()) {
        role = "EMPLOYEE"; // Mặc định nếu Backend chưa truyền
    }
    role = role.toUpperCase();

    String username = (String) session.getAttribute("username");
    if (username == null || username.trim().isEmpty()) {
        username = (String) request.getAttribute("username");
    }
    if (username == null) {
        username = "Nguoidung";
    }

    boolean isAdmin = "ADMIN".equals(role);
    boolean isManager = "MANAGER".equals(role);
    boolean isEmployee = "EMPLOYEE".equals(role);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông Tin Phân Quyền - QLKH</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome Icons -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <!-- File CSS Tùy Chỉnh của HTQLKH-5 -->
    <link href="${pageContext.request.contextPath}/assets/css/permission.css" rel="stylesheet">
</head>
<body>

<div class="container my-4" id="permission-container" data-role="<%= role %>">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
        <div>
            <h3 class="text-primary fw-bold mb-1">
                <i class="fas fa-user-shield me-2"></i>Thông Tin Quyền & Phạm Vi Dữ Liệu
            </h3>
            <p class="text-muted mb-0">HTQLKH-5: Hiển thị giao diện theo vai trò người dùng và phạm vi sở hữu dữ liệu</p>
        </div>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline-secondary btn-sm">
            <i class="fas fa-arrow-left me-1"></i> Quay lại Dashboard
        </a>
    </div>

    <!-- Khung thông báo khi không có quyền thao tác (Do JavaScript điều khiển) -->
    <div id="permission-denied-alert" class="alert alert-danger alert-dismissible fade show d-none" role="alert">
        <i class="fas fa-exclamation-triangle me-2"></i>
        <span id="permission-alert-message">Tài khoản của bạn không có quyền thực hiện thao tác này!</span>
        <button type="button" class="btn-close" onclick="hidePermissionAlert()"></button>
    </div>

    <!-- Block 1: Thông tin tài khoản & Vai trò -->
    <div class="card shadow-sm mb-4">
        <div class="card-header bg-white py-3">
            <h5 class="card-title mb-0 fw-bold text-dark">
                <i class="fas fa-id-card text-primary me-2"></i>Tài Khoản Hiện Tại
            </h5>
        </div>
        <div class="card-body">
            <div class="row align-items-center">
                <div class="col-md-6 mb-3 mb-md-0">
                    <div class="d-flex align-items-center">
                        <div class="avatar-circle me-3 bg-light text-primary">
                            <i class="fas fa-user fa-2x"></i>
                        </div>
                        <div>
                            <h6 class="mb-1 text-muted">Tên đăng nhập</h6>
                            <h5 class="fw-bold mb-0"><%= username %></h5>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <h6 class="mb-1 text-muted">Vai trò hệ thống (Role)</h6>
                    <% if (isAdmin) { %>
                        <span class="badge bg-danger fs-6 px-3 py-2"><i class="fas fa-user-tie me-1"></i> ADMIN (Quản trị viên)</span>
                    <% } else if (isManager) { %>
                        <span class="badge bg-warning text-dark fs-6 px-3 py-2"><i class="fas fa-user-tag me-1"></i> MANAGER (Quản lý)</span>
                    <% } else { %>
                        <span class="badge bg-success fs-6 px-3 py-2"><i class="fas fa-user me-1"></i> EMPLOYEE (Nhân viên)</span>
                    <% } %>
                </div>
            </div>
        </div>
    </div>

    <!-- Block 2: Quyền hạn theo phạm vi sở hữu dữ liệu (Data Ownership) -->
    <div class="card shadow-sm mb-4">
        <div class="card-header bg-white py-3">
            <h5 class="card-title mb-0 fw-bold text-dark">
                <i class="fas fa-database text-primary me-2"></i>Phạm Vi Dữ Liệu Được Phép Truy Cập
            </h5>
        </div>
        <div class="card-body">
            <% if (isAdmin) { %>
                <div class="p-3 border-start border-danger border-4 bg-light rounded">
                    <h6 class="fw-bold text-danger mb-2"><i class="fas fa-globe me-2"></i>Phạm vi: Toàn hệ thống (System-Wide)</h6>
                    <p class="mb-0 text-secondary">
                        Tài khoản <strong>ADMIN</strong> có toàn quyền quản trị, xem, thêm, sửa, xóa tất cả các bản ghi dữ liệu khách hàng, báo cáo và cấu hình thuộc hệ thống.
                    </p>
                </div>
            <% } else if (isManager) { %>
                <div class="p-3 border-start border-warning border-4 bg-light rounded">
                    <h6 class="fw-bold text-dark mb-2"><i class="fas fa-sitemap me-2 text-warning"></i>Phạm vi: Theo đơn vị / Chi nhánh (Manager Scope)</h6>
                    <p class="mb-0 text-secondary">
                        Tài khoản <strong>MANAGER</strong> có quyền quản lý và theo dõi toàn bộ dữ liệu khách hàng và báo cáo doanh số thuộc chi nhánh/phòng ban do Backend phân công.
                    </p>
                </div>
            <% } else { %>
                <div class="p-3 border-start border-success border-4 bg-light rounded">
                    <h6 class="fw-bold text-success mb-2"><i class="fas fa-user-check me-2"></i>Phạm vi: Dữ liệu cá nhân sở hữu (Owner Scope)</h6>
                    <p class="mb-0 text-secondary">
                        Tài khoản <strong>EMPLOYEE</strong> chỉ được phép truy cập, chỉnh sửa và quản lý các dữ liệu khách hàng do chính mình tạo ra hoặc được phân công sở hữu trực tiếp.
                    </p>
                </div>
            <% } %>
        </div>
    </div>

    <!-- Block 3: Bảng chức năng & Trạng thái UI theo quyền -->
    <div class="card shadow-sm mb-4">
        <div class="card-header bg-white py-3">
            <h5 class="card-title mb-0 fw-bold text-dark">
                <i class="fas fa-tasks text-primary me-2"></i>Danh Sách Quyền & Chức Năng Được Phép Sử Dụng
            </h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Chức năng</th>
                            <th>Mô tả quy định</th>
                            <th>Yêu cầu vai trò</th>
                            <th>Trạng thái UI</th>
                            <th class="text-center">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Chức năng 1: Xem khách hàng -->
                        <tr>
                            <td class="fw-semibold"><i class="fas fa-address-book me-2 text-primary"></i>Xem danh sách khách hàng</td>
                            <td class="text-muted">Xem dữ liệu khách hàng theo phạm vi sở hữu</td>
                            <td><span class="badge bg-secondary">TẤT CẢ</span></td>
                            <td><span class="badge bg-success"><i class="fas fa-check me-1"></i>Được phép</span></td>
                            <td class="text-center">
                                <a href="${pageContext.request.contextPath}/customers" class="btn btn-sm btn-outline-primary">Truy cập</a>
                            </td>
                        </tr>

                        <!-- Chức năng 2: Thêm/Sửa khách hàng -->
                        <tr>
                            <td class="fw-semibold"><i class="fas fa-user-edit me-2 text-info"></i>Thêm / Chỉnh sửa khách hàng</td>
                            <td class="text-muted">Tạo hoặc cập nhật hồ sơ trong phạm vi sở hữu</td>
                            <td><span class="badge bg-secondary">TẤT CẢ</span></td>
                            <td><span class="badge bg-success"><i class="fas fa-check me-1"></i>Được phép</span></td>
                            <td class="text-center">
                                <button type="button" class="btn btn-sm btn-outline-info" onclick="checkPermissionAction('EDIT_CUSTOMER')">Thực hiện</button>
                            </td>
                        </tr>

                        <!-- Chức năng 3: Xem báo cáo doanh số -->
                        <tr>
                            <td class="fw-semibold"><i class="fas fa-chart-bar me-2 text-warning"></i>Xem báo cáo doanh số</td>
                            <td class="text-muted">Xem báo cáo tổng hợp doanh thu phòng ban/hệ thống</td>
                            <td><span class="badge bg-warning text-dark">MANAGER</span> <span class="badge bg-danger">ADMIN</span></td>
                            <td>
                                <% if (isAdmin || isManager) { %>
                                    <span class="badge bg-success"><i class="fas fa-check me-1"></i>Được phép</span>
                                <% } else { %>
                                    <span class="badge bg-secondary"><i class="fas fa-lock me-1"></i>Không có quyền</span>
                                <% } %>
                            </td>
                            <td class="text-center">
                                <% if (isAdmin || isManager) { %>
                                    <button type="button" class="btn btn-sm btn-outline-warning text-dark" onclick="checkPermissionAction('VIEW_REPORT')">Xem báo cáo</button>
                                <% } else { %>
                                    <button type="button" class="btn btn-sm btn-outline-secondary disabled-action-btn" onclick="triggerRestrictedNotice('Xem báo cáo doanh số')">
                                        <i class="fas fa-lock me-1"></i> Bị khóa
                                    </button>
                                <% } %>
                            </td>
                        </tr>

                        <!-- Chức năng 4: Quản lý người dùng -->
                        <tr>
                            <td class="fw-semibold"><i class="fas fa-users-cog me-2 text-danger"></i>Quản lý tài khoản hệ thống</td>
                            <td class="text-muted">Tạo mới, sửa, phân quyền tài khoản người dùng</td>
                            <td><span class="badge bg-danger">ADMIN</span></td>
                            <td>
                                <% if (isAdmin) { %>
                                    <span class="badge bg-success"><i class="fas fa-check me-1"></i>Được phép</span>
                                <% } else { %>
                                    <span class="badge bg-secondary"><i class="fas fa-lock me-1"></i>Không có quyền</span>
                                <% } %>
                            </td>
                            <td class="text-center">
                                <% if (isAdmin) { %>
                                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="checkPermissionAction('MANAGE_USERS')">Quản lý</button>
                                <% } else { %>
                                    <button type="button" class="btn btn-sm btn-outline-secondary disabled-action-btn" onclick="triggerRestrictedNotice('Quản lý tài khoản hệ thống')">
                                        <i class="fas fa-lock me-1"></i> Bị khóa
                                    </button>
                                <% } %>
                            </td>
                        </tr>

                        <!-- Chức năng 5: Xóa dữ liệu -->
                        <tr>
                            <td class="fw-semibold"><i class="fas fa-trash-alt me-2 text-danger"></i>Xóa dữ liệu quan trọng</td>
                            <td class="text-muted">Xóa bản ghi dữ liệu khách hàng hoặc hợp đồng</td>
                            <td><span class="badge bg-danger">ADMIN</span></td>
                            <td>
                                <% if (isAdmin) { %>
                                    <span class="badge bg-success"><i class="fas fa-check me-1"></i>Được phép</span>
                                <% } else { %>
                                    <span class="badge bg-secondary"><i class="fas fa-lock me-1"></i>Không có quyền</span>
                                <% } %>
                            </td>
                            <td class="text-center">
                                <% if (isAdmin) { %>
                                    <button type="button" class="btn btn-sm btn-danger" onclick="checkPermissionAction('DELETE_DATA')">Xóa dữ liệu</button>
                                <% } else { %>
                                    <button type="button" class="btn btn-sm btn-secondary disabled-action-btn" onclick="triggerRestrictedNotice('Xóa dữ liệu quan trọng')">
                                        <i class="fas fa-ban me-1"></i> Không có quyền
                                    </button>
                                <% } %>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- JS Bootstrap 5 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- File JavaScript Phân Quyền UI HTQLKH-5 -->
<script src="${pageContext.request.contextPath}/assets/js/permission.js"></script>

</body>
</html>