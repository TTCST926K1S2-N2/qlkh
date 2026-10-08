<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.edu.ictu.qlkh.controller.SupportRequestServlet.SupportRequest" %>
<%
    SupportRequest s = (SupportRequest) request.getAttribute("support");
    String message = request.getParameter("message");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết yêu cầu hỗ trợ</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/sidebar.css" rel="stylesheet">
</head>
<body>
<div class="d-flex">
    <jsp:include page="/common/sidebar.jsp" />
    <div class="flex-grow-1 p-4">
        <h2 class="mb-4">Chi tiết yêu cầu hỗ trợ #SR-<%= s != null ? s.getId() : "" %></h2>

        <% if ("update_success".equals(message)) { %>
            <div class="alert alert-success">Cập nhật trạng thái thành công!</div>
        <% } %>

        <% if (s != null) { %>
        <div class="card shadow-sm mb-4">
            <div class="card-body">
                <p><strong>Tiêu đề:</strong> <%= s.getTitle() %></p>
                <p><strong>Khách hàng:</strong> <%= s.getCustomerName() != null ? s.getCustomerName() : "N/A" %></p>
                <p><strong>Mức độ ưu tiên:</strong> <span class="badge bg-secondary"><%= s.getPriority() %></span></p>
                <p><strong>Người xử lý:</strong> <%= s.getAssignee() != null ? s.getAssignee() : "Chưa phân công" %></p>
                <p><strong>Trạng thái hiện tại:</strong> <span class="badge bg-info text-dark"><%= s.getStatus() %></span></p>
                <p><strong>Mô tả chi tiết:</strong></p>
                <div class="p-3 bg-light border rounded mb-3"><%= s.getDescription() != null ? s.getDescription() : "Không có mô tả." %></div>

                <form action="${pageContext.request.contextPath}/supports/update-status" method="POST" class="row g-3 align-items-center">
                    <input type="hidden" name="id" value="<%= s.getId() %>">
                    <div class="col-auto">
                        <label for="status" class="col-form-label"><strong>Cập nhật trạng thái:</strong></label>
                    </div>
                    <div class="col-auto">
                        <select class="form-select" id="status" name="status">
                            <option value="Mới" <%= "Mới".equals(s.getStatus()) ? "selected" : "" %>>Mới</option>
                            <option value="Đang xử lý" <%= "Đang xử lý".equals(s.getStatus()) ? "selected" : "" %>>Đang xử lý</option>
                            <option value="Đã giải quyết" <%= "Đã giải quyết".equals(s.getStatus()) ? "selected" : "" %>>Đã giải quyết</option>
                            <option value="Đóng" <%= "Đóng".equals(s.getStatus()) ? "selected" : "" %>>Đóng</option>
                        </select>
                    </div>
                    <div class="col-auto">
                        <button type="submit" class="btn btn-success">Cập nhật</button>
                    </div>
                </form>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/supports" class="btn btn-secondary">Quay lại danh sách</a>
        <% } else { %>
            <div class="alert alert-warning">Không tìm thấy thông tin yêu cầu hỗ trợ.</div>
            <a href="${pageContext.request.contextPath}/supports" class="btn btn-primary">Quay lại danh sách</a>
        <% } %>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
