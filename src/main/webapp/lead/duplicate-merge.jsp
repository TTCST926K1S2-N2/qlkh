<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="vn.edu.ictu.qlkh.controller.DuplicateLeadServlet.LeadData" %>
<%
    LeadData l1 = (LeadData) request.getAttribute("lead1");
    LeadData l2 = (LeadData) request.getAttribute("lead2");
    String suggestedCustomer = (String) request.getAttribute("suggestedCustomer");
    String suggestedCustomerName = null;
    String suggestedCustomerId = null;
    if (suggestedCustomer != null && suggestedCustomer.contains("|")) {
        String[] parts = suggestedCustomer.split("\\|", 2);
        suggestedCustomerName = parts[0];
        suggestedCustomerId = parts[1];
    }
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
<style>
/* S4-04: Giao dien rieng cho trang gop Lead */
body {
    margin: 0;
    font-family: Arial, sans-serif;
}

body > .d-flex {
    display: flex !important;
    flex-wrap: nowrap !important;
    align-items: flex-start !important;
}

body > .d-flex > .flex-grow-1 {
    flex: 1 1 0 !important;
    min-width: 0 !important;
    padding: 24px !important;
    box-sizing: border-box;
}

body > .d-flex > .flex-grow-1 .row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
}

body > .d-flex > .flex-grow-1 .col-md-6 {
    width: 100% !important;
    min-width: 0;
}

body > .d-flex > .flex-grow-1 .card {
    border: 1px solid #dee2e6;
    border-radius: 10px;
    overflow: hidden;
    background: white;
}

body > .d-flex > .flex-grow-1 .card-header {
    padding: 15px;
    background: #0d6efd;
    color: white;
}

body > .d-flex > .flex-grow-1 .card-body {
    padding: 20px;
}

body > .d-flex > .flex-grow-1 .btn {
    display: inline-block;
    padding: 10px 16px;
    border-radius: 6px;
    text-decoration: none;
    cursor: pointer;
}

body > .d-flex > .flex-grow-1 .btn-danger {
    background: #dc3545;
    color: white;
    border: none;
}

body > .d-flex > .flex-grow-1 .btn-secondary {
    background: #6c757d;
    color: white;
}

@media (max-width: 900px) {
    body > .d-flex > .flex-grow-1 .row {
        grid-template-columns: 1fr;
    }
}
/* S4-04 MERGE RESULT STYLES */
body > .d-flex > .flex-grow-1 .alert-success {
    display: block;
    padding: 18px 22px;
    margin: 20px 0;
    background: #d1e7dd;
    color: #0f5132;
    border: 1px solid #badbcc;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
}

body > .d-flex > .flex-grow-1 .alert-danger {
    display: block;
    padding: 18px 22px;
    margin: 20px 0;
    background: #f8d7da;
    color: #842029;
    border: 1px solid #f5c2c7;
    border-radius: 8px;
}

body > .d-flex > .flex-grow-1 .btn-primary {
    display: inline-block;
    padding: 11px 18px;
    background: #0d6efd;
    color: #ffffff;
    border: none;
    border-radius: 7px;
    text-decoration: none;
    font-weight: 600;
}

body > .d-flex > .flex-grow-1 .btn-primary:hover {
    background: #0b5ed7;
    color: #ffffff;
}
</style>
</head>
<body class="bg-light">
<div class="d-flex" style="display:flex!important;flex-wrap:nowrap!important;align-items:flex-start;">
    <jsp:include page="/components/sidebar.jsp" />
    <div class="flex-grow-1 p-4" style="flex:1 1 0!important;min-width:0!important;max-width:100%;position:relative;z-index:1;">
        <h2 class="mb-4 text-danger">⚠️ Phát hiện Lead trùng theo Email, SĐT và Tên công ty</h2>

        <% if (errorMessage != null) { %>
            <div class="alert alert-danger"><%= escapeHtml(errorMessage) %></div>
        <% } %>

        <% if (suggestedCustomerId != null && l1 != null) { %>
            <div class="alert alert-info d-flex justify-content-between align-items-center">
                <div>
                    <strong>💡 Gợi ý hệ thống:</strong> Lead này có thông tin trùng với Khách hàng đã có sẵn: <strong><%= escapeHtml(suggestedCustomerName) %></strong>. Bạn có thể gắn thẳng lead này vào khách hàng đó.
                </div>
                <form action="${pageContext.request.contextPath}/leads/link-customer" method="POST" class="m-0">
                    <input type="hidden" name="leadId" value="<%= l1.getId() %>">
                    <input type="hidden" name="customerId" value="<%= suggestedCustomerId %>">
                    <button type="submit" class="btn btn-sm btn-dark">Gắn thẳng vào Khách hàng</button>
                </form>
            </div>
        <% } %>

        <% if (l1 != null && l2 != null) { %>
        <%
            boolean mergeSuccess = Boolean.TRUE.equals(request.getAttribute("mergeVerified"));
            boolean mergeFailed = request.getParameter("error") != null;
        %>
        <% if (mergeSuccess) { %>
            <div class="alert alert-success" role="alert">
                Gộp Lead thành công!
            </div>
            <a href="${pageContext.request.contextPath}/leads/duplicates"
               class="btn btn-primary">Trở về danh sách Lead trùng</a>
        <% } else { %>
            <% if (mergeFailed) { %>
                <div class="alert alert-danger" role="alert">
                    Không thể gộp Lead. Vui lòng kiểm tra trạng thái Lead và thử lại.
                </div>
            <% } %>
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
                            <p><strong>Họ tên:</strong> <span class="<%= !java.util.Objects.toString(l1.getFullName(), "").equalsIgnoreCase(java.util.Objects.toString(l2.getFullName(), "")) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l1.getFullName()) %></span></p>
                            <p><strong>Email:</strong> <span class="<%= !java.util.Objects.toString(l1.getEmail(), "").equalsIgnoreCase(java.util.Objects.toString(l2.getEmail(), "")) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l1.getEmail()) %></span></p>
                            <p><strong>Số điện thoại:</strong> <span class="<%= !java.util.Objects.equals(l1.getPhone(), l2.getPhone()) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l1.getPhone()) %></span></p>
                            <p><strong>Công ty:</strong> <span class="<%= !java.util.Objects.toString(l1.getCompany(), "").equalsIgnoreCase(java.util.Objects.toString(l2.getCompany(), "")) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l1.getCompany()) %></span></p>
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
                            <p><strong>Họ tên:</strong> <span class="<%= !java.util.Objects.toString(l1.getFullName(), "").equalsIgnoreCase(java.util.Objects.toString(l2.getFullName(), "")) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l2.getFullName()) %></span></p>
                            <p><strong>Email:</strong> <span class="<%= !java.util.Objects.toString(l1.getEmail(), "").equalsIgnoreCase(java.util.Objects.toString(l2.getEmail(), "")) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l2.getEmail()) %></span></p>
                            <p><strong>Số điện thoại:</strong> <span class="<%= !java.util.Objects.equals(l1.getPhone(), l2.getPhone()) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l2.getPhone()) %></span></p>
                            <p><strong>Công ty:</strong> <span class="<%= !java.util.Objects.toString(l1.getCompany(), "").equalsIgnoreCase(java.util.Objects.toString(l2.getCompany(), "")) ? "text-danger fw-bold" : "" %>"><%= escapeHtml(l2.getCompany()) %></span></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-4 d-flex justify-content-end gap-2">
                <a href="${pageContext.request.contextPath}/leads/duplicates" class="btn btn-secondary">Trở về danh sách Lead trùng</a>
                <button type="submit" class="btn btn-danger" onclick="return confirm('Xác nhận gộp các bản ghi lead này?')">Xác nhận gộp Lead</button>
            </div>
        </form>
        <% } %>
        <% } else { %>
            <div class="alert alert-warning">Không có bản ghi lead trùng lặp nào được chọn.</div>
            <a href="${pageContext.request.contextPath}/leads/duplicates" class="btn btn-primary">Trở về danh sách Lead trùng</a>
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