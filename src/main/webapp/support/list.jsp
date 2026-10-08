<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, vn.edu.ictu.qlkh.controller.SupportRequestServlet.SupportRequest" %>
<%
    List<SupportRequest> supportList = (List<SupportRequest>) request.getAttribute("supportList");
    String message = request.getParameter("message");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý yêu cầu hỗ trợ khách hàng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/sidebar.css" rel="stylesheet">
</head>
<body>
<div class="d-flex">
    <jsp:include page="/common/sidebar.jsp" />
    <div class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Quản lý yêu cầu hỗ trợ khách hàng</h2>
            <a href="${pageContext.request.contextPath}/supports/create" class="btn btn-primary">+ Tạo yêu cầu mới</a>
        </div>

        <% if ("create_success".equals(message)) { %>
            <div class="alert alert-success">Tạo yêu cầu hỗ trợ thành công!</div>
        <% } else if ("invalid_input".equals(error)) { %>
            <div class="alert alert-danger">Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.</div>
        <% } %>

        <div class="card shadow-sm">
            <div class="card-body">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Tiêu đề</th>
                            <th>Khách hàng</th>
                            <th>Mức độ ưu tiên</th>
                            <th>Người xử lý</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (supportList != null && !supportList.isEmpty()) {
                            for (SupportRequest s : supportList) { %>
                            <tr>
                                <td>#SR-<%= s.getId() %></td>
                                <td><%= s.getTitle() %></td>
                                <td><%= s.getCustomerName() != null ? s.getCustomerName() : "N/A" %></td>
                                <td>
                                    <span class="badge <%= "Cao".equals(s.getPriority()) ? "bg-danger" : "bg-secondary" %>">
                                        <%= s.getPriority() %>
                                    </span>
                                </td>
                                <td><%= s.getAssignee() != null ? s.getAssignee() : "Chưa phân công" %></td>
                                <td><span class="badge bg-info text-dark"><%= s.getStatus() %></span></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/supports/detail?id=<%= s.getId() %>" class="btn btn-sm btn-outline-primary">Chi tiết</a>
                                </td>
                            </tr>
                        <% } } else { %>
                            <tr>
                                <td colspan="7" class="text-center text-muted">Không có yêu cầu hỗ trợ nào trong hệ thống.</td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
