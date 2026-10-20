<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.edu.ictu.qlkh.controller.ConvertLeadServlet.LeadInfo" %>
<%
    LeadInfo lead = (LeadInfo) request.getAttribute("lead");
    Boolean isQualified = (Boolean) request.getAttribute("isQualified");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String errorParam = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chuyển đổi Lead thành Khách hàng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/sidebar.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="d-flex">
    <jsp:include page="/common/sidebar.jsp" />
    <div class="flex-grow-1 p-4">
        <h2 class="mb-4">🔄 Chuyển đổi Lead thành Khách hàng</h2>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger"><%= errorMessage %></div>
        <% } %>
        <% if ("convert_failed".equals(errorParam)) { %>
            <div class="alert alert-danger">Chuyển đổi lead thất bại do lỗi hệ thống. Vui lòng thử lại.</div>
        <% } %>

        <% if (lead != null) { %>
            <% if (isQualified != null && isQualified) { %>
                <div class="alert alert-success">Lead này <strong>đủ điều kiện</strong> để chuyển đổi thành Khách hàng doanh nghiệp.</div>

                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-primary text-white">
                        <h5 class="mb-0">📋 Thông tin Lead được chuyển đổi</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <p><strong>Họ và tên:</strong> <%= lead.getFullName() %></p>
                                <p><strong>Email:</strong> <%= lead.getEmail() %></p>
                                <p><strong>Số điện thoại:</strong> <%= lead.getPhone() %></p>
                            </div>
                            <div class="col-md-6">
                                <p><strong>Công ty:</strong> <%= lead.getCompany() %></p>
                                <p><strong>Nguồn:</strong> <%= lead.getSource() %></p>
                                <p><strong>Người phụ trách:</strong> <%= lead.getOwner() %></p>
                            </div>
                        </div>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/leads/execute-convert" method="POST">
                    <input type="hidden" name="leadId" value="<%= lead.getId() %>">
                    <div class="d-flex justify-content-end gap-2">
                        <a href="${pageContext.request.contextPath}/leads" class="btn btn-secondary">Hủy bỏ</a>
                        <button type="submit" class="btn btn-success" onclick="return confirm('Xác nhận chuyển đổi lead này thành khách hàng mới?')">Xác nhận chuyển đổi</button>
                    </div>
                </form>
            <% } else { %>
                <div class="alert alert-warning">
                    ⚠️ <strong>Cảnh báo:</strong> Lead này hiện <strong>chưa đủ điều kiện</strong> chuyển đổi (Trạng thái hiện tại: <%= lead.getStatus() %>). Vui lòng cập nhật đầy đủ thông tin trước khi thực hiện chuyển đổi.
                </div>
                <div class="mt-3">
                    <a href="${pageContext.request.contextPath}/leads" class="btn btn-secondary">Quay lại danh sách Lead</a>
                </div>
            <% } %>
        <% } else { %>
            <div class="alert alert-warning">Không tìm thấy thông tin Lead để chuyển đổi.</div>
            <a href="${pageContext.request.contextPath}/leads" class="btn btn-primary">Quay lại danh sách Lead</a>
        <% } %>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>