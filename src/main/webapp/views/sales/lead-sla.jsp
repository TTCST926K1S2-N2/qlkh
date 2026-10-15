<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý SLA Lead | QLKH</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/lead-sla.css">
</head>
<body>
<div class="lead-sla-layout">

    <jsp:include page="/components/sidebar.jsp" />

    <div class="lead-sla-main">
        <jsp:include page="/components/header.jsp" />

        <main class="lead-sla-content">
            <div class="lead-sla-heading">
                <div>
                    <h1>Quản lý SLA Lead</h1>
                    <p>Theo dõi thời hạn xử lý và tiếp nhận Lead được phân công.</p>
                </div>
                <button type="button" id="refreshLeads">
                    Làm mới
                </button>
            </div>

            <section class="lead-sla-summary" aria-label="Tổng quan SLA">
                <article>
                    <span>Tổng Lead</span>
                    <strong id="totalLeads">--</strong>
                </article>
                <article>
                    <span>Trong hạn</span>
                    <strong id="onTimeLeads">--</strong>
                </article>
                <article>
                    <span>Sắp quá hạn</span>
                    <strong id="urgentLeads">--</strong>
                </article>
                <article>
                    <span>Đã quá hạn</span>
                    <strong id="overdueLeads">--</strong>
                </article>
            </section>

            <section class="lead-sla-panel">
                <div class="lead-sla-panel-heading">
                    <h2>Danh sách Lead được phân công</h2>
                    <p>Tra cứu và theo dõi trạng thái xử lý SLA.</p>
                </div>

                <div class="lead-sla-filters">
                    <label for="leadSearch">
                        Tìm kiếm Lead
                        <input type="search"
                               id="leadSearch"
                               placeholder="Tên, email hoặc số điện thoại">
                    </label>

                    <label for="slaStatusFilter">
                        Trạng thái SLA
                        <select id="slaStatusFilter">
                            <option value="all">Tất cả</option>
                            <option value="on_time">Trong hạn</option>
                            <option value="urgent">Sắp quá hạn</option>
                            <option value="overdue">Đã quá hạn</option>
                        </select>
                    </label>

                    <label for="leadStatusFilter">
                        Trạng thái tiếp nhận
                        <select id="leadStatusFilter">
                            <option value="all">Tất cả</option>
                            <option value="pending">Chờ tiếp nhận</option>
                            <option value="accepted">Đã tiếp nhận</option>
                            <option value="rejected">Đã từ chối</option>
                        </select>
                    </label>
                </div>

                <div id="leadSlaMessage" role="status" aria-live="polite">
                    Đang chờ tích hợp API Backend S4-07.
                </div>

                <div class="lead-sla-table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th>Mã Lead</th>
                            <th>Khách hàng tiềm năng</th>
                            <th>Người phụ trách</th>
                            <th>Hạn xử lý</th>
                            <th>Trạng thái SLA</th>
                            <th>Tiếp nhận</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody id="leadSlaRows">
                        <tr>
                            <td colspan="7" class="lead-sla-empty">
                                Chưa có dữ liệu từ Backend.
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/lead-sla.js"
        defer></script>
</body>
</html>