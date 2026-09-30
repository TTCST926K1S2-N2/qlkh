<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String role = (String) session.getAttribute("userRole");

    if (role == null) {
        role = "GUEST";
    }

    // Lấy URL hiện tại để xử lý class "active"
    String currentURI = request.getRequestURI();
    String ctxPath = request.getContextPath();
%>

<!-- Nhúng file CSS đã tách -->
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/sidebar.css">

<div class="sidebar">

    <!-- HEADER -->
    <div class="sidebar-header">
        HỆ THỐNG QLKH
    </div>


    <!-- ===================================================== -->
    <!-- NHÓM TỔNG QUAN -->
    <!-- ===================================================== -->

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

                Trang chủ
            </a>
        </li>

    </ul>


    <!-- ===================================================== -->
    <!-- NHÓM QUẢN LÝ -->
    <!-- ===================================================== -->

    <%
        if ("EMPLOYEE".equals(role)
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

                    Quản lý khách hàng
                </a>
            </li>

        </ul>

    <%
        }
    %>


    <!-- ===================================================== -->
    <!-- NHÓM QUẢN TRỊ -->
    <!-- ===================================================== -->

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

                    Quản lý tài khoản
                </a>
            </li>


            <li>
                <a href="${pageContext.request.contextPath}/roles"
                   class="<%= currentURI.contains("/roles")
                           ? "active"
                           : "" %>">

                    Phân quyền & Nhóm nghiệp vụ
                </a>
            </li>


            <li>
                <a href="${pageContext.request.contextPath}/lock-transfer"
                   class="<%= currentURI.contains("/lock-transfer")
                           ? "active"
                           : "" %>">

                    Khóa tài khoản & Bàn giao dữ liệu
                </a>
            </li>

        </ul>

    <%
        }
    %>


    <!-- ===================================================== -->
    <!-- NHÓM TÀI KHOẢN -->
    <!-- ===================================================== -->

    <div class="menu-category">
        Tài khoản
    </div>


    <ul class="sidebar-menu">

        <%
            if (!"GUEST".equals(role)) {
        %>


            <!-- HỒ SƠ CÁ NHÂN -->

            <li>
                <a href="${pageContext.request.contextPath}/profile"
                   class="<%= currentURI.contains("/profile")
                           ? "active"
                           : "" %>">

                    Hồ sơ cá nhân
                </a>
            </li>


            <!-- ĐỔI MẬT KHẨU -->

            <li>
                <a href="${pageContext.request.contextPath}/change-password"
                   class="<%= currentURI.contains("/change-password")
                           ? "active"
                           : "" %>">

                    Đổi mật khẩu
                </a>
            </li>


            <!-- ================================================= -->
            <!-- ĐĂNG XUẤT -->
            <!--
                HTQLKH-2:

                Không sử dụng:
                    <a href="/logout">

                vì thẻ <a> sẽ gửi GET /logout.

                Logout phải sử dụng POST để thay đổi trạng thái
                phiên đăng nhập phía server.
            -->
            <!-- ================================================= -->

            <li>

                <a href="#"
                   onclick="
                       event.preventDefault();
                       document.getElementById(
                           'sidebar-logout-form'
                       ).submit();
                   ">

                    Đăng xuất
                </a>


                <!--
                    Form ẩn gửi POST tới LogoutServlet.
                -->

                <form id="sidebar-logout-form"
                      action="${pageContext.request.contextPath}/logout"
                      method="post"
                      style="display: none;">
                </form>

            </li>


        <%
            } else {
        %>


            <!-- ĐĂNG NHẬP -->

            <li>
                <a href="${pageContext.request.contextPath}/login"
                   class="<%= currentURI.contains("/login")
                           ? "active"
                           : "" %>">

                    Đăng nhập
                </a>
            </li>


        <%
            }
        %>

    </ul>

</div>