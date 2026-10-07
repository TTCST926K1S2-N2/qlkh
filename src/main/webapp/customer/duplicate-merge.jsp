<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.edu.ictu.qlkh.controller.DuplicateCustomerServlet.CustomerData" %>
<%
    CustomerData c1 = (CustomerData) request.getAttribute("customer1");
    CustomerData c2 = (CustomerData) request.getAttribute("customer2");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String errorParam = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cảnh báo và gộp khách hàng trùng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-4 mb-5">
    <h2 class="mb-4">⚠️ Phát hiện hồ sơ khách hàng trùng lặp</h2>

    <% if (errorMessage != null) { %>
        <div class="alert alert-danger"><%= errorMessage %></div>
    <% } %>
    <% if ("invalid_selection".equals(errorParam)) { %>
        <div class="alert alert-warning">Vui lòng chọn hai hồ sơ khác nhau để thực hiện gộp.</div>
    <% } else if ("merge_failed".equals(errorParam)) { %>
        <div class="alert alert-danger">Gộp khách hàng thất bại do lỗi hệ thống. Vui lòng thử lại.</div>
    <% } else if ("unauthorized".equals(errorParam)) { %>
        <div class="alert alert-danger">Bạn không có quyền thực hiện thao tác này (Chỉ Trưởng nhóm kinh doanh mới được gộp).</div>
    <% } %>

    <% if (c1 != null && c2 != null) { %>
    <form action="${pageContext.request.contextPath}/customers/merge" method="POST" id="mergeForm">
        <input type="hidden" id="primaryId" name="primaryId" value="<%= c1.getId() %>">
        <input type="hidden" id="duplicateId" name="duplicateId" value="<%= c2.getId() %>">

        <div class="row">
            <div class="col-md-6">
                <div class="card shadow-sm border-primary">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <span>Hồ sơ A (ID: <%= c1.getId() %>)</span>
                        <div class="form-check">
                            <input class="form-check-input primary-radio" type="radio" name="choosePrimary" id="radio1" value="<%= c1.getId() %>" checked onchange="updateMergeIds()">
                            <label class="form-check-label text-white" for="radio1">Chọn làm chính</label>
                        </div>
                    </div>
                    <div class="card-body">
                        <p><strong>Tên công ty:</strong> <%= c1.getName() %></p>
                        <p><strong>Mã số thuế:</strong> <%= c1.getTaxCode() %></p>
                        <p><strong>Ngành nghề:</strong> <%= c1.getIndustry() %></p>
                        <p><strong>Quy mô:</strong> <%= c1.getScale() %></p>
                        <p><strong>Website:</strong> <%= c1.getWebsite() %></p>
                        <p><strong>Địa chỉ:</strong> <%= c1.getAddress() %></p>
                        <p><strong>Người sở hữu:</strong> <%= c1.getOwner() %></p>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="card shadow-sm border-secondary">
                    <div class="card-header bg-secondary text-white d-flex justify-content-between align-items-center">
                        <span>Hồ sơ B (ID: <%= c2.getId() %>)</span>
                        <div class="form-check">
                            <input class="form-check-input primary-radio" type="radio" name="choosePrimary" id="radio2" value="<%= c2.getId() %>" onchange="updateMergeIds()">
                            <label class="form-check-label text-white" for="radio2">Chọn làm chính</label>
                        </div>
                    </div>
                    <div class="card-body">
                        <p><strong>Tên công ty:</strong> <%= c2.getName() %></p>
                        <p><strong>Mã số thuế:</strong> <%= c2.getTaxCode() %></p>
                        <p><strong>Ngành nghề:</strong> <%= c2.getIndustry() %></p>
                        <p><strong>Quy mô:</strong> <%= c2.getScale() %></p>
                        <p><strong>Website:</strong> <%= c2.getWebsite() %></p>
                        <p><strong>Địa chỉ:</strong> <%= c2.getAddress() %></p>
                        <p><strong>Người sở hữu:</strong> <%= c2.getOwner() %></p>
                    </div>
                </div>
            </div>
        </div>

        <div class="card shadow-sm mt-4">
            <div class="card-header bg-info text-white">
                <h5 class="mb-0">📋 Chi tiết dự kiến dữ liệu được giữ lại & chuyển đổi</h5>
            </div>
            <div class="card-body">
                <ul>
                    <li><strong>Thông tin định danh & Thuộc tính:</strong> Giữ lại toàn bộ thông tin của Hồ sơ được chọn làm chính. Hồ sơ còn lại sẽ bị vô hiệu hóa (Trạng thái: MERGED).</li>
                    <li><strong>Người liên hệ (Contacts):</strong> Toàn bộ danh sách người liên hệ thuộc hồ sơ phụ sẽ được tự động chuyển sang liên kết với Hồ sơ chính.</li>
                    <li><strong>Cơ hội kinh doanh (Opportunities):</strong> Các cơ hội đang mở/đóng của hồ sơ phụ sẽ gộp chung vào dòng thời gian của Hồ sơ chính.</li>
                    <li><strong>Hoạt động & Lịch sử chăm sóc (Activities):</strong> Lịch sử cuộc gọi, email, ghi chú sẽ được gom về một mối tại Hồ sơ chính.</li>
                </ul>
            </div>
        </div>

        <div class="mt-4 d-flex justify-content-end gap-2">
            <a href="${pageContext.request.contextPath}/customers" class="btn btn-secondary">Hủy bỏ</a>
            <button type="submit" class="btn btn-danger" onclick="return confirm('Bạn có chắc chắn muốn thực hiện gộp hồ sơ này không? Thao tác không thể hoàn tác.')">Xác nhận gộp hồ sơ</button>
        </div>
    </form>
    <% } else { %>
        <div class="alert alert-warning">Không có dữ liệu so sánh khách hàng trùng.</div>
        <a href="${pageContext.request.contextPath}/customers" class="btn btn-primary">Quay lại danh sách</a>
    <% } %>
</div>

<script>
    function updateMergeIds() {
        const radio1 = document.getElementById('radio1');
        const primaryIdInput = document.getElementById('primaryId');
        const duplicateIdInput = document.getElementById('duplicateId');

        const id1 = "<%= c1 != null ? c1.getId() : "" %>";
        const id2 = "<%= c2 != null ? c2.getId() : "" %>";

        if (radio1.checked) {
            primaryIdInput.value = id1;
            duplicateIdInput.value = id2;
        } else {
            primaryIdInput.value = id2;
            duplicateIdInput.value = id1;
        }
    }
    window.onload = updateMergeIds;
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
