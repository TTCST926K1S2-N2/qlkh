<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, vn.edu.ictu.qlkh.controller.SupportRequestServlet.CustomerOption" %>
<%
    List<CustomerOption> customerList = (List<CustomerOption>) request.getAttribute("customerList");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Ghi nhận yêu cầu hỗ trợ</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/sidebar.css" rel="stylesheet">
</head>
<body>
<div class="d-flex">
    <jsp:include page="/common/sidebar.jsp" />
    <div class="flex-grow-1 p-4">
        <h2 class="mb-4">Ghi nhận yêu cầu hỗ trợ khách hàng</h2>

        <% if ("invalid_input".equals(error)) { %>
            <div class="alert alert-danger">Vui lòng nhập đầy đủ tiêu đề và chọn khách hàng!</div>
        <% } else if ("db_error".equals(error)) { %>
            <div class="alert alert-danger">Lỗi cơ sở dữ liệu khi lưu yêu cầu hỗ trợ.</div>
        <% } %>

        <div class="card shadow-sm">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/supports/create" method="POST">
                    <div class="mb-3">
                        <label for="title" class="form-label">Tiêu đề yêu cầu <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="title" name="title" required placeholder="Nhập tiêu đề vấn đề...">
                    </div>

                    <div class="mb-3">
                        <label for="customerId" class="form-label">Khách hàng <span class="text-danger">*</span></label>
                        <select class="form-select" id="customerId" name="customerId" required>
                            <option value="">-- Chọn khách hàng thực tế --</option>
                            <% if (customerList != null) {
                                for (CustomerOption c : customerList) { %>
                                <option value="<%= c.getId() %>"><%= c.getName() %></option>
                            <% } } %>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label for="priority" class="form-label">Mức độ ưu tiên</label>
                        <select class="form-select" id="priority" name="priority">
                            <option value="Thấp">Thấp</option>
                            <option value="Trung bình" selected>Trung bình</option>
                            <option value="Cao">Cao</option>
                            <option value="Khẩn cấp">Khẩn cấp</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label for="assignee" class="form-label">Người xử lý</label>
                        <input type="text" class="form-control" id="assignee" name="assignee" placeholder="Nhập tên nhân viên xử lý...">
                    </div>

                    <div class="mb-3">
                        <label for="description" class="form-label">Mô tả chi tiết</label>
                        <textarea class="form-control" id="description" name="description" rows="4" placeholder="Mô tả chi tiết vấn đề của khách hàng..."></textarea>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${pageContext.request.contextPath}/supports" class="btn btn-secondary">Hủy bỏ</a>
                        <button type="submit" class="btn btn-primary">Lưu yêu cầu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
