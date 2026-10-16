<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.edu.ictu.qlkh.controller.DuplicateLeadServlet.LeadData" %>
<%
    LeadData l1 = (LeadData) request.getAttribute("lead1");
    LeadData l2 = (LeadData) request.getAttribute("lead2");
    String suggestedCustomer = (String) request.getAttribute("suggestedCustomer");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String errorParam = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cảnh báo và xử lý Lead trùng lặp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/sidebar.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="d-flex">
    <jsp:include page="/common/sidebar.jsp" />
    <div class="flex-grow-1 p-4">
        <h2 class="mb-4 text-danger">⚠️ Phát hiện Lead trùng theo Email, SĐT và Tên công ty</h2>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger"><%= errorMessage %></div>
        <% } %>

        <% if (suggestedCustomer != null) { %>
            <div class="alert alert-info d-flex justify-content-between align-items-center">
                <div>
                    <strong>💡 Gợi ý hệ thống:</strong> Lead này có thông tin trùng với Khách hàng đã có sẵn: <strong><%= suggestedCustomer %></strong>. Bạn có thể gắn thẳng lead này vào khách hàng đó.
                </div>
                <form action="${pageContext.request.contextPath}/leads/link-customer" method="POST" class="m-0">
                    <input type="hidden" name="leadId" value="<%= l1 != null ? l1.getId() : 1 %>">
                    <input type="hidden" name="customerId" value="1">
                    <button type="submit" class="btn btn-sm btn-dark">Gắn thẳng vào Khách hàng</button>
                </form>
            </div>
        <% } %>

        <% if (l1 != null && l2 != null) { %>
        <form action="${pageContext.request.contextPath}/leads/merge" method="POST" id="mergeForm">
            <input type="hidden" id="primaryId" name="primaryId" value="<%= l1.getId() %>">
            <input type="hidden" id="duplicateId" name="duplicateId" value="<%= l2.getId() %>">

            <div class="row">
                <div class="col-md-6">
                    <div class="card shadow-sm border-primary">
                        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                            <span>Bản ghi Lead A (ID: <%= l1.getId() %>)</span>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="choosePrimary" id="radio1" value="<%= l1.getId() %>" checked onchange="updateMergeIds()">
                                <label class="form-check-label text-white" for="radio1">Chọn làm chính</label>
                            </div>
                        </div>
                        <div class="card-body">
                            <p><strong>Họ tên:</strong> <span class="<%= !l1.getFullName().equalsIgnoreCase(l2.getFullName()) ? "text-danger fw-bold" : "" %>"><%= l1.getFullName() %></span></p>
                            <p><strong>Email:</strong> <span class="<%= !l1.getEmail().equalsIgnoreCase(l2.getEmail()) ? "text-danger fw-bold" : "" %>"><%= l1.getEmail() %></span></p>
                            <p><strong>Số điện thoại:</strong> <span class="<%= !l1.getPhone().equals(l2.getPhone()) ? "text-danger fw-bold" : "" %>"><%= l1.getPhone() %></span></p>
                            <p><strong>Công ty:</strong> <span class="<%= !l1.getCompany().equalsIgnoreCase(l2.getCompany()) ? "text-danger fw-bold" : "" %>"><%= l1.getCompany() %></span></p>
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="card shadow-sm border-secondary">
                        <div class="card-header bg-secondary text-white d-flex justify-content-between align-items-center">
                            <span>Bản ghi Lead B (ID: <%= l2.getId() %>)</span>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="choosePrimary" id="radio2" value="<%= l2.getId() %>" onchange="updateMergeIds()">
                                <label class="form-check-label text-white" for="radio2">Chọn làm chính</label>
                            </div>
                        </div>
                        <div class="card-body">
                            <p><strong>Họ tên:</strong> <span class="<%= !l1.getFullName().equalsIgnoreCase(l2.getFullName()) ? "text-danger fw-bold" : "" %>"><%= l2.getFullName() %></span></p>
                            <p><strong>Email:</strong> <span class="<%= !l1.getEmail().equalsIgnoreCase(l2.getEmail()) ? "text-danger fw-bold" : "" %>"><%= l2.getEmail() %></span></p>
                            <p><strong>Số điện thoại:</strong> <span class="<%= !l1.getPhone().equals(l2.getPhone()) ? "text-danger fw-bold" : "" %>"><%= l2.getPhone() %></span></p>
                            <p><strong>Công ty:</strong> <span class="<%= !l1.getCompany().equalsIgnoreCase(l2.getCompany()) ? "text-danger fw-bold" : "" %>"><%= l2.getCompany() %></span></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-4 d-flex justify-content-end gap-2">
                <a href="${pageContext.request.contextPath}/leads" class="btn btn-secondary">Hủy bỏ</a>
                <button type="submit" class="btn btn-danger" onclick="return confirm('Xác nhận gộp các bản ghi lead này?')">Xác nhận gộp Lead</button>
            </div>
        </form>
        <% } else { %>
            <div class="alert alert-warning">Không có bản ghi lead trùng lặp nào được chọn.</div>
            <a href="${pageContext.request.contextPath}/leads" class="btn btn-primary">Quay lại danh sách Lead</a>
        <% } %>
    </div>
</div>
<script>
    function updateMergeIds() {
        const radio1 = document.getElementById('radio1');
        const primaryIdInput = document.getElementById('primaryId');
        const duplicateIdInput = document.getElementById('duplicateId');
        const id1 = "<%= l1 != null ? l1.getId() : "" %>";
        const id2 = "<%= l2 != null ? l2.getId() : "" %>";
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