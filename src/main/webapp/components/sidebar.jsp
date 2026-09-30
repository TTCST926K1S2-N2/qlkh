<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String role = (String) session.getAttribute("userRole");
    String userName = (String) session.getAttribute("userName");
    Object businessGroupObj = session.getAttribute("businessGroupName");

    if (role == null || role.isBlank()) {
        role = "GUEST";
    }

    if (userName == null || userName.isBlank()) {
        userName = "Người dùng";
    }

    String businessGroupName =
            businessGroupObj != null
                    ? businessGroupObj.toString()
                    : null;

    String roleDisplayName;

    switch (role) {
        case "ADMIN":
            roleDisplayName = "Quản trị hệ thống";
            break;

        case "MANAGER":
            roleDisplayName = "Quản lý kinh doanh";
            break;

        case "SALES":
            roleDisplayName = "Nhân viên kinh doanh";
            break;

        default:
            roleDisplayName = "Khách";
            break;
    }

    String currentURI = request.getRequestURI();
%>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/sidebar.css">

<aside class="sidebar">

    <!-- ========================= -->
    <!-- THƯƠNG HIỆU -->
    <!-- ========================= -->

    <div class="sidebar-brand">
        <div class="brand-mark">
            Q
        </div>

        <div class="brand-text">
            <div class="brand-title">
                HỆ THỐNG QLKH
            </div>

            <div class="brand-subtitle">
                Customer Management
            </div>
        </div>
    </div>


    <!-- ========================= -->
    <!-- THÔNG TIN NGƯỜI DÙNG -->
    <!-- ========================= -->

    <% if (!"GUEST".equals(role)) { %>

        <div class="sidebar-user">

            <div class="user-avatar">
                <%= userName.substring(0, 1).toUpperCase() %>
            </div>

            <div class="user-information">

                <div class="user-name"
                     title="<%= userName %>">
                    <%= userName %>
                </div>

                <div class="user-role">
                    <%= roleDisplayName %>
                </div>

                <% if (businessGroupName != null
                        && !businessGroupName.isBlank()) { %>

                    <div class="user-group">
                        Nhóm: <%= businessGroupName %>
                    </div>

                <% } %>

            </div>

        </div>

    <% } %>


    <!-- ========================= -->
    <!-- MENU -->
    <!-- ========================= -->

    <nav class="sidebar-navigation">

        <!-- TỔNG QUAN -->

        <div class="menu-category">
            Tổng quan
        </div>

        <ul class="sidebar-menu">

            <li>
                <a href="${pageContext.request.contextPath}/"
                   class="<%= currentURI.endsWith("/")
                           || currentURI.endsWith("/index.jsp")
                           ? "active"
                           : "" %>">

                    <span class="menu-indicator"></span>

                    <span class="menu-text">
                        Trang chủ
                    </span>

                </a>
            </li>

        </ul>


        <!-- ========================= -->
        <!-- QUẢN LÝ -->
        <!-- SALES / MANAGER / ADMIN -->
        <!-- ========================= -->

        <%
            if ("SALES".equals(role)
                    || "MANAGER".equals(role)
                    || "ADMIN".equals(role)) {
        %>

            <div class="menu-category">
                Quản lý
            </div>

            <ul class="sidebar-menu">

                <li>
                    <a href="${pageContext.request.contextPath}/customers"
                       class="<%= currentURI.contains("/customers")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Quản lý khách hàng
                        </span>

                    </a>
                </li>

            </ul>

        <%
            }
        %>


        <!-- ========================= -->
        <!-- QUẢN TRỊ -->
        <!-- CHỈ ADMIN -->
        <!-- ========================= -->

        <%
            if ("ADMIN".equals(role)) {
        %>

            <div class="menu-category">
                Quản trị
            </div>

            <ul class="sidebar-menu">

                <li>
                    <a href="${pageContext.request.contextPath}/users"
                       class="<%= currentURI.contains("/users")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Quản lý tài khoản
                        </span>

                    </a>
                </li>


                <li>
                    <a href="${pageContext.request.contextPath}/roles"
                       class="<%= currentURI.contains("/roles")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Phân quyền & Nhóm nghiệp vụ
                        </span>

                    </a>
                </li>


                <li>
                    <a href="${pageContext.request.contextPath}/lock-transfer"
                       class="<%= currentURI.contains("/lock-transfer")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Khóa tài khoản & Bàn giao
                        </span>

                    </a>
                </li>

            </ul>

        <%
            }
        %>


        <!-- ========================= -->
        <!-- TÀI KHOẢN -->
        <!-- ========================= -->

        <div class="menu-category">
            Tài khoản
        </div>

        <ul class="sidebar-menu">

            <% if (!"GUEST".equals(role)) { %>

                <li>
                    <a href="${pageContext.request.contextPath}/profile"
                       class="<%= currentURI.contains("/profile")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Hồ sơ cá nhân
                        </span>

                    </a>
                </li>


                <!--
                    Trang xem quyền và phạm vi dữ liệu.
                    Có thể sử dụng cho SALES, MANAGER và ADMIN.
                -->

                <li>
                    <a href="${pageContext.request.contextPath}/permission"
                       class="<%= currentURI.contains("/permission")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Quyền & phạm vi dữ liệu
                        </span>

                    </a>
                </li>


                <li>
                    <a href="${pageContext.request.contextPath}/change-password"
                       class="<%= currentURI.contains("/change-password")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Đổi mật khẩu
                        </span>

                    </a>
                </li>


                <!-- Logout bắt buộc dùng POST -->

                <li class="logout-item">

                    <a href="#"
                       onclick="
                           event.preventDefault();
                           document.getElementById(
                               'sidebar-logout-form'
                           ).submit();
                       ">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Đăng xuất
                        </span>

                    </a>


                    <form id="sidebar-logout-form"
                          action="${pageContext.request.contextPath}/logout"
                          method="post"
                          style="display: none;">
                    </form>

                </li>

            <% } else { %>

                <li>
                    <a href="${pageContext.request.contextPath}/login"
                       class="<%= currentURI.contains("/login")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Đăng nhập
                        </span>

                    </a>
                </li>

            <% } %>

        </ul>

    </nav>


    <!-- ========================= -->
    <!-- FOOTER -->
    <!-- ========================= -->

    <div class="sidebar-footer">
        <span class="system-status"></span>
        Hệ thống đang hoạt động
    </div>

</aside>