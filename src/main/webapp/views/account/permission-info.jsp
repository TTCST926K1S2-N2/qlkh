<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    String role = (String) request.getAttribute("userRole");

    if (role == null || role.trim().isEmpty()) {
        role = (String) session.getAttribute("userRole");
    }

    if (role == null || role.trim().isEmpty()) {
        role = (String) session.getAttribute("role");
    }

    if (role == null || role.trim().isEmpty()) {
        role = "GUEST";
    } else {
        role = role.trim().toUpperCase();
    }

    /*
     * LoginServlet hiện lưu userName trong session.
     * Ưu tiên tên hiển thị, sau đó mới fallback sang email/username.
     */
    String username = (String) session.getAttribute("userName");

    if (username == null || username.trim().isEmpty()) {
        username = (String) session.getAttribute("userEmail");
    }

    if (username == null || username.trim().isEmpty()) {
        username = (String) session.getAttribute("username");
    }

    if (username == null || username.trim().isEmpty()) {
        username = (String) request.getAttribute("username");
    }

    /*
     * dataScope hoàn toàn do Backend xác định:
     * MY   = Của tôi
     * TEAM = Của nhóm tôi
     * ALL  = Tất cả
     *
     * FE không tự suy ra dataScope từ role.
     */
    String dataScope = (String) request.getAttribute("dataScope");

    if (dataScope == null || dataScope.trim().isEmpty()) {
        dataScope = (String) session.getAttribute("dataScope");
    }

    if (dataScope != null) {
        dataScope = dataScope.trim().toUpperCase();
    }

    request.setAttribute("currentRole", role);
    request.setAttribute("currentUsername", username);
    request.setAttribute("currentDataScope", dataScope);
%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Phân quyền và phạm vi dữ liệu</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/permission.css">
</head>

<body>

<div class="permission-page"
     id="permission-container"
     data-scope="${currentDataScope}">

    <!-- CHƯA ĐĂNG NHẬP -->
    <c:if test="${currentRole == 'GUEST'}">

        <div class="login-required-card">

            <div class="login-icon"></div>

            <h2>Yêu cầu đăng nhập</h2>

            <p>
                Bạn chưa đăng nhập hoặc phiên đăng nhập
                không còn hợp lệ.
            </p>

            <a href="${pageContext.request.contextPath}/login"
               class="primary-button">
                Đăng nhập
            </a>

        </div>

    </c:if>


    <!-- ĐÃ ĐĂNG NHẬP -->
    <c:if test="${currentRole != 'GUEST'}">

        <div class="permission-wrapper">

            <!-- HEADER -->
            <header class="page-header">

                <div class="header-left">

                    <div class="header-icon">
                        <span></span>
                    </div>

                    <div>
                        <div class="eyebrow">
                            QUẢN LÝ QUYỀN TRUY CẬP
                        </div>

                        <h1>Phân quyền và phạm vi dữ liệu</h1>

                        <p>
                            Theo dõi quyền truy cập và phạm vi dữ liệu
                            được Backend cấp cho tài khoản hiện tại.
                        </p>
                    </div>

                </div>

                <a href="${pageContext.request.contextPath}/"
                   class="back-button">

                    <span class="back-arrow"></span>
                 Quay về trang chủ

                </a>

            </header>


            <!-- LỖI BACKEND -->
            <c:if test="${not empty permissionError}">

                <div class="permission-alert permission-alert-danger">

                    <div class="alert-symbol">!</div>

                    <div>
                        <strong>Không thể truy cập dữ liệu</strong>

                        <p>
                            <c:out value="${permissionError}" />
                        </p>
                    </div>

                </div>

            </c:if>


            <!-- TÀI KHOẢN HIỆN TẠI -->
            <section class="permission-panel account-panel">

                <div class="panel-heading">

                    <div>
                        <span class="section-label">TÀI KHOẢN</span>
                        <h2>Tài khoản hiện tại</h2>
                    </div>

                    <span class="status-badge">
                        <span class="status-dot"></span>
                        Đang hoạt động
                    </span>

                </div>


                <div class="account-content">

                    <div class="account-avatar"
                         id="account-avatar">
                        U
                    </div>


                    <div class="account-main">

                        <div class="account-name">

                            <c:choose>

                                <c:when test="${not empty currentUsername}">
                                    <c:out value="${currentUsername}" />
                                </c:when>

                                <c:otherwise>
                                    Chưa có thông tin
                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="account-description">
                            Tài khoản đang sử dụng hệ thống
                        </div>

                    </div>


                    <div class="account-role">

                        <span class="info-caption">
                            VAI TRÒ HỆ THỐNG
                        </span>

                        <span class="role-badge role-${currentRole}">
                            <c:out value="${currentRole}" />
                        </span>

                        <span class="role-description">

                            <c:choose>

                                <c:when test="${currentRole == 'SALES'}">
                                    Nhân viên kinh doanh
                                </c:when>

                                <c:when test="${currentRole == 'MANAGER'}">
                                    Quản lý kinh doanh
                                </c:when>

                                <c:when test="${currentRole == 'ADMIN'}">
                                    Quản trị viên
                                </c:when>

                                <c:otherwise>
                                    Người dùng hệ thống
                                </c:otherwise>

                            </c:choose>

                        </span>

                    </div>

                </div>

            </section>


            <!-- PHẠM VI -->
            <section class="permission-panel">

                <div class="panel-heading">

                    <div>
                        <span class="section-label">PHÂN QUYỀN</span>
                        <h2>Phạm vi dữ liệu</h2>
                    </div>

                    <div class="current-scope-summary">
                        Phạm vi hiện tại:
                        <strong id="current-scope-text">
                            Đang xác định
                        </strong>
                    </div>

                </div>


                <div class="scope-grid">

                    <c:if test="${currentDataScope == 'MY'}">
                    <!-- MY -->
                    <div class="scope-card
                         ${currentDataScope == 'MY'
                            ? 'scope-active'
                            : ''}"
                         data-scope-card="MY">

                        <div class="scope-top">

                            <div class="scope-icon">
                                <span>01</span>
                            </div>

                            <c:if test="${currentDataScope == 'MY'}">
                                <span class="active-badge">
                                    ✓ Đang áp dụng
                                </span>
                            </c:if>

                        </div>

                        <h3>Của tôi</h3>

                        <p>
                            Chỉ dữ liệu thuộc sở hữu của
                            tài khoản hiện tại.
                        </p>

                        <div class="scope-code">
                            MY
                        </div>

                    </div>


                    </c:if>
                    <c:if test="${currentDataScope == 'TEAM'}">
                    <!-- TEAM -->
                    <div class="scope-card
                         ${currentDataScope == 'TEAM'
                            ? 'scope-active'
                            : ''}"
                         data-scope-card="TEAM">

                        <div class="scope-top">

                            <div class="scope-icon">
                                <span>02</span>
                            </div>

                            <c:if test="${currentDataScope == 'TEAM'}">
                                <span class="active-badge">
                                    ✓ Đang áp dụng
                                </span>
                            </c:if>

                        </div>

                        <h3>Của nhóm tôi</h3>

                        <p>
                            Dữ liệu thuộc nhóm kinh doanh
                            của tài khoản hiện tại.
                        </p>

                        <div class="scope-code">
                            TEAM
                        </div>

                    </div>


                    </c:if>
                    <c:if test="${currentDataScope == 'ALL'}">
                    <!-- ALL -->
                    <div class="scope-card
                         ${currentDataScope == 'ALL'
                            ? 'scope-active'
                            : ''}"
                         data-scope-card="ALL">

                        <div class="scope-top">

                            <div class="scope-icon">
                                <span>03</span>
                            </div>

                            <c:if test="${currentDataScope == 'ALL'}">
                                <span class="active-badge">
                                    Đang áp dụng
                                </span>
                            </c:if>

                        </div>

                        <h3>Tất cả</h3>

                        <p>
                            Toàn bộ dữ liệu mà tài khoản
                            được phép truy cập.
                        </p>

                        <div class="scope-code">
                            ALL
                        </div>

                    </div>

                    </c:if>
                </div>


                <c:if test="${empty currentDataScope}">

                    <div class="permission-alert
                                permission-alert-warning">

                        <div class="alert-symbol">!</div>

                        <div>
                            <strong>Chưa xác định phạm vi</strong>

                            <p>
                                Backend chưa cung cấp phạm vi dữ liệu
                                cho tài khoản hiện tại.
                            </p>
                        </div>

                    </div>

                </c:if>

            </section>


            <!-- DỮ LIỆU ÁP DỤNG -->
            <section class="permission-panel">

                <div class="panel-heading">

                    <div>
                        <span class="section-label">DỮ LIỆU</span>
                        <h2>Dữ liệu áp dụng phạm vi</h2>
                    </div>

                </div>


                <div class="panel-description">
                    Phạm vi trên được áp dụng thống nhất khi Backend
                    truy vấn danh sách, tìm kiếm và xuất Excel.
                </div>


                <div class="data-grid">

                    <div class="data-item">

                        <div class="data-icon">
                            KH
                        </div>

                        <div class="data-info">
                            <strong>Khách hàng</strong>
                            <span>Dữ liệu khách hàng</span>
                        </div>

                        <span class="scope-label">
                            Đang xác định
                        </span>

                    </div>


                    <div class="data-item">

                        <div class="data-icon">
                            CH
                        </div>

                        <div class="data-info">
                            <strong>Cơ hội</strong>
                            <span>Cơ hội kinh doanh</span>
                        </div>

                        <span class="scope-label">
                            Đang xác định
                        </span>

                    </div>


                    <div class="data-item">

                        <div class="data-icon">
                            HĐ
                        </div>

                        <div class="data-info">
                            <strong>Hoạt động</strong>
                            <span>Hoạt động chăm sóc khách hàng</span>
                        </div>

                        <span class="scope-label">
                            Đang xác định
                        </span>

                    </div>


                    <div class="data-item">

                        <div class="data-icon">
                            BG
                        </div>

                        <div class="data-info">
                            <strong>Báo giá</strong>
                            <span>Dữ liệu báo giá</span>
                        </div>

                        <span class="scope-label">
                            Đang xác định
                        </span>

                    </div>

                </div>

            </section>


            <!-- QUY TẮC -->
            <section class="permission-panel rules-panel">

                <div class="panel-heading">

                    <div>
                        <span class="section-label">NGUYÊN TẮC</span>
                        <h2>Quy tắc áp dụng</h2>
                    </div>

                </div>


                <div class="rules-grid">

                    <div class="rule-item">

                        <span class="rule-number">01</span>

                        <div>
                            <strong>Danh sách dữ liệu</strong>

                            <p>
                                Backend lọc danh sách theo đúng
                                phạm vi của tài khoản.
                            </p>
                        </div>

                    </div>


                    <div class="rule-item">

                        <span class="rule-number">02</span>

                        <div>
                            <strong>Tìm kiếm</strong>

                            <p>
                                Kết quả tìm kiếm sử dụng cùng
                                phạm vi dữ liệu.
                            </p>
                        </div>

                    </div>


                    <div class="rule-item">

                        <span class="rule-number">03</span>

                        <div>
                            <strong>Xuất Excel</strong>

                            <p>
                                Chỉ xuất dữ liệu nằm trong phạm vi
                                được phép.
                            </p>
                        </div>

                    </div>


                    <div class="rule-item">

                        <span class="rule-number">04</span>

                        <div>
                            <strong>Truy cập ngoài phạm vi</strong>

                            <p>
                                Hệ thống hiển thị thông báo lỗi
                                bằng tiếng Việt.
                            </p>
                        </div>

                    </div>

                </div>

            </section>


            <footer class="permission-footer">
                Hệ thống quản lý khách hàng · Phân quyền dữ liệu
            </footer>

        </div>

    </c:if>

</div>

<script src="${pageContext.request.contextPath}/assets/js/permission.js">
</script>

</body>
</html>
