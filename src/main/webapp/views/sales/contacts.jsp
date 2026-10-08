<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý người liên hệ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/contacts.css">
</head>
<body>
    <details class="contacts-mobile-menu">
        <summary>Menu điều hướng</summary>
        <nav aria-label="Điều hướng nhanh">
            <a href="${pageContext.request.contextPath}/">Trang chủ</a>
            <a href="${pageContext.request.contextPath}/customers">Quản lý khách hàng</a>
            <a href="${pageContext.request.contextPath}/views/sales/contacts.jsp">Quản lý người liên hệ</a>
        </nav>
    </details>
    <jsp:include page="/components/sidebar.jsp"/>

    <main class="contacts-main">
        <header class="contacts-header">
            <div>
                <h1>Quản lý người liên hệ</h1>
                <p>Quản lý người liên hệ và vai trò trong quyết định mua</p>
            </div>
            <button type="button" id="addContact">Thêm người liên hệ</button>
        </header>

        <section class="contacts-panel">
            <div class="contacts-filters">
                <label>
                    Khách hàng doanh nghiệp
                    <select id="customerFilter">
                        <option value="">Chọn khách hàng</option>
                    </select>
                </label>

                <label>
                    Tìm kiếm
                    <input id="contactSearch"
                           placeholder="Tên, email, số điện thoại...">
                </label>
            </div>

            <p id="contactMessage" role="status" aria-live="polite"></p>

            <div class="contacts-table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>Họ và tên</th>
                            <th>Chức danh</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th>Vai trò quyết định</th>
                            <th>Đầu mối chính</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody id="contactRows"></tbody>
                </table>
            </div>
        </section>

        <dialog id="contactDialog">
            <form id="contactForm">
                <h2 id="contactFormTitle">Thông tin người liên hệ</h2>

                <p id="contactFormError"
                   role="alert"
                   aria-live="assertive"
                   hidden></p>

                <label>
                    Khách hàng doanh nghiệp
                    <select name="customerId" required></select>
                </label>

                <label>
                    Họ và tên
                    <input name="fullName" maxlength="255" required>
                </label>

                <label>
                    Chức danh
                    <input name="jobTitle" maxlength="150">
                </label>

                <label>
                    Email
                    <input name="email" type="email" maxlength="255">
                </label>

                <label>
                    Số điện thoại
                    <input name="phone" type="tel" maxlength="30">
                </label>

                <label>
                    Vai trò trong quyết định mua
                    <select name="decisionRole">
                        <option value="">Chưa xác định</option>
                        <option value="DECISION_MAKER">Người quyết định</option>
                        <option value="INFLUENCER">Người ảnh hưởng</option>
                        <option value="END_USER">Người dùng cuối</option>
                        <option value="BLOCKER">Người cản trở</option>
                    </select>
                </label>

                <label class="contacts-checkbox">
                    <input name="isPrimary" type="checkbox">
                    Đánh dấu là đầu mối chính
                </label>

                <p class="contacts-hint">
                    Khi chuyển sang khách hàng khác, hệ thống sẽ lưu
                    lịch sử chuyển người liên hệ.
                </p>

                <div class="contacts-actions">
                    <button type="button" id="cancelContact">Đóng</button>
                    <button type="submit" id="saveContact">Lưu</button>
                </div>
            </form>
        </dialog>
    </main>

    <script>
        window.contactContextPath = "${pageContext.request.contextPath}";
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/contacts.js" defer></script>
</body>
</html>