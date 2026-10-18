<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%
    List<long[]> pairs = (List<long[]>) request.getAttribute("duplicatePairs");
    String error = (String) request.getAttribute("errorMessage");
    String historyError = (String) request.getAttribute("historyError");
    List<Object[]> mergeHistory =
        (List<Object[]>) request.getAttribute("mergeHistory");
    String activeTab = request.getParameter("tab");
    if (!"history".equals(activeTab)) {
        activeTab = "pending";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Xử lý Lead trùng</title>
    <style>
        .s404-page {
            flex: 1;
            min-width: 0;
            padding: 28px;
            font-family: Arial, sans-serif;
            background: #f5f7fb;
            min-height: 100vh;
            box-sizing: border-box;
        }
        .s404-page h1 { margin: 0 0 10px; font-size: 26px; }
        .s404-page p { color: #64748b; }
        .s404-table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            margin-top: 24px;
        }
        .s404-table th, .s404-table td {
            padding: 14px;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
        }
        .s404-table th { background: #eaf1ff; }
        .s404-button {
            display: inline-block;
            padding: 9px 15px;
            border-radius: 6px;
            background: #2563eb;
            color: white;
            text-decoration: none;
        }
        .s404-notice {
            padding: 16px;
            background: white;
            border-radius: 8px;
            margin-top: 20px;
        }
        .s404-error { color: #b91c1c; }
        @media (max-width: 800px) {
            .s404-page { padding: 12px; }
            .s404-table th, .s404-table td { padding: 8px; }
        }
    </style>
</head>
<body style="margin:0">
<div style="display:flex;min-height:100vh">
    <jsp:include page="/components/sidebar.jsp" />

    <main class="s404-page">
        <h1>Phát hiện và xử lý Lead trùng</h1>
        <p>Các cặp Lead có email hoặc số điện thoại giống nhau.
           Hãy kiểm tra thông tin trước khi quyết định gộp.</p>

        <style>
            .s404-tabs {
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
                margin: 22px 0;
            }
            .s404-tab {
                padding: 11px 18px;
                border: 1px solid #dbe3ef;
                border-radius: 8px;
                background: #fff;
                color: #334155;
                text-decoration: none;
                font-weight: 600;
            }
            .s404-tab.active {
                background: #2563eb;
                color: #fff;
                border-color: #2563eb;
            }
            .s404-badge {
                display: inline-block;
                padding: 3px 8px;
                border-radius: 12px;
                background: #dcfce7;
                color: #166534;
                font-size: 12px;
            }
            .s404-history-wrap {
                overflow-x: auto;
                margin-top: 18px;
            }
        </style>

        <nav class="s404-tabs">
            <a class="s404-tab <%= "pending".equals(activeTab) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/leads/duplicates?tab=pending">
                Lead chờ xử lý
            </a>
            <a class="s404-tab <%= "history".equals(activeTab) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/leads/duplicates?tab=history">
                Lịch sử đã gộp
                (<%= mergeHistory == null ? 0 : mergeHistory.size() %>)
            </a>
        </nav>

        <% if (!"history".equals(activeTab)) { %>
        <% if (error != null) { %>
            <div class="s404-notice s404-error">
                <%= error %>
            </div>
        <% } else if (pairs == null || pairs.isEmpty()) { %>
            <div class="s404-notice">
                Không tìm thấy cặp Lead nghi trùng cần xử lý.
            </div>
        <% } else { %>
            <table class="s404-table">
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>Lead A</th>
                        <th>Lead B</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                <% int stt = 1;
                   for (long[] pair : pairs) { %>
                    <tr>
                        <td><%= stt++ %></td>
                        <td>Lead #<%= pair[0] %></td>
                        <td>Lead #<%= pair[1] %></td>
                        <td>
                            <a class="s404-button"
                               href="<%= request.getContextPath() %>/leads/duplicates?id1=<%= pair[0] %>&amp;id2=<%= pair[1] %>">
                                Xem chi tiết
                            </a>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
        <% } else { %>

            <h2>Lịch sử gộp Lead</h2>
            <p>Danh sách các lần gộp đã được ghi nhận trong cơ sở dữ liệu,
               sắp xếp theo thời gian mới nhất.</p>

            <% if (historyError != null) { %>
                <div class="s404-notice s404-error">
                    <%= historyError %>
                </div>
            <% } else if (mergeHistory == null || mergeHistory.isEmpty()) { %>
                <div class="s404-notice">
                    Chưa có lịch sử gộp Lead.
                </div>
            <% } else { %>
                <div class="s404-history-wrap">
                    <table class="s404-table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>Lead đã gộp</th>
                                <th>Lead được giữ lại</th>
                                <th>Người thực hiện</th>
                                <th>Thời gian gộp</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% int historyIndex = 1;
                           for (Object[] item : mergeHistory) { %>
                            <tr>
                                <td><%= historyIndex++ %></td>
                                <td>Lead #<%= item[0] %></td>
                                <td>Lead #<%= item[1] %></td>
                                <td>ID <%= item[2] %></td>
                                <td><%= item[3] == null ? "" : item[3] %></td>
                                <td><span class="s404-badge">Đã gộp</span></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        <% } %>
    </main>
</div>
</body>
</html>