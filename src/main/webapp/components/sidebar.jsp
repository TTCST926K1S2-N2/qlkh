<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String role = (String) session.getAttribute("userRole");
    String userName = (String) session.getAttribute("userName");
    Object businessGroupObj = session.getAttribute("businessGroupName");

    if (role == null || role.isBlank()) {
        role = "GUEST";
    }

    if (userName == null || userName.isBlank()) {
        userName = "NgÆ°á»i dĂ¹ng";
    }

    String businessGroupName =
            businessGroupObj != null
                    ? businessGroupObj.toString()
                    : null;

    String roleDisplayName;

    switch (role) {
        case "ADMIN":
            roleDisplayName = "Quáº£n trá»‹ há»‡ thá»‘ng";
            break;

        case "MANAGER":
            roleDisplayName = "Quáº£n lĂ½ kinh doanh";
            break;

        case "SALES":
            roleDisplayName = "NhĂ¢n viĂªn kinh doanh";
            break;

        default:
            roleDisplayName = "KhĂ¡ch";
            break;
    }

    String currentURI = request.getRequestURI();
    String currentView = request.getParameter("view");

    boolean isRoleGroupPage =
            currentURI.contains("/users/role-group")
                    || "role-group".equals(currentView);

    boolean isUserManagementPage =
            currentURI.contains("/users")
                    && !isRoleGroupPage;
%>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/sidebar.css">

<aside class="sidebar">

    <!-- ========================= -->
    <!-- THÆ¯Æ NG HIá»†U -->
    <!-- ========================= -->

    <div class="sidebar-brand">

        <div class="brand-mark">
            Q
        </div>

        <div class="brand-text">

            <div class="brand-title">
                Há»† THá»NG QLKH
            </div>

            <div class="brand-subtitle">
                Customer Management
            </div>

        </div>

    </div>


    <!-- ========================= -->
    <!-- THĂ”NG TIN NGÆ¯á»œI DĂ™NG -->
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

                <%
                    if (businessGroupName != null
                            && !businessGroupName.isBlank()) {
                %>

                    <div class="user-group">
                        NhĂ³m: <%= businessGroupName %>
                    </div>

                <% } %>

            </div>

        </div>

    <% } %>


    <!-- ========================= -->
    <!-- MENU -->
    <!-- ========================= -->

    <nav class="sidebar-navigation">


        <!-- ========================= -->
        <!-- Tá»”NG QUAN -->
        <!-- ========================= -->

        <div class="menu-category">
            Tá»•ng quan
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
                        Trang chá»§
                    </span>

                </a>

            </li>

        </ul>


        <!-- ========================= -->
        <!-- QUáº¢N LĂ -->
        <!-- SALES / MANAGER / ADMIN -->
        <!-- ========================= -->

        <%
            if ("SALES".equals(role)
                    || "MANAGER".equals(role)
                    || "ADMIN".equals(role)) {
        %>

            <div class="menu-category">
                Quáº£n lĂ½
            </div>

            <ul class="sidebar-menu">

                <li>

                    <a href="${pageContext.request.contextPath}/customers"
                       class="<%= currentURI.contains("/customers")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Quáº£n lĂ½ khĂ¡ch hĂ ng
                        </span>

                    </a>

                </li>

            </ul>

        <%
            }
        %>


        <!-- ========================= -->
        <!-- QUáº¢N TRá» -->
        <!-- CHá»ˆ ADMIN -->
        <!-- ========================= -->

        <%
            if ("ADMIN".equals(role)) {
        %>

            <div class="menu-category">
                Quáº£n trá»‹
            </div>

            <ul class="sidebar-menu">


                <!-- QUáº¢N LĂ TĂ€I KHOáº¢N -->

                <li>

                    <a href="${pageContext.request.contextPath}/users"
                       class="<%= isUserManagementPage
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Quáº£n lĂ½ tĂ i khoáº£n
                        </span>

                    </a>

                </li>


                <!-- HTQLKH-9 -->
                <!-- PHĂ‚N QUYá»€N & NHĂ“M KINH DOANH -->

                <li>

                    <a href="${pageContext.request.contextPath}/users?view=role-group"
                       class="<%= isRoleGroupPage
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            PhĂ¢n quyá»n & NhĂ³m kinh doanh
                        </span>

                    </a>

                </li>


                <!-- KHĂ“A TĂ€I KHOáº¢N & BĂ€N GIAO -->

                <li>

                    <a href="${pageContext.request.contextPath}/lock-transfer"
                       class="<%= currentURI.contains("/lock-transfer")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            KhĂ³a tĂ i khoáº£n & BĂ n giao
                        </span>

                    </a>

                </li>

            </ul>

        <%
            }
        %>


        <!-- ========================= -->
        <!-- TĂ€I KHOáº¢N -->
        <!-- ========================= -->

        <div class="menu-category">
            TĂ i khoáº£n
        </div>

        <ul class="sidebar-menu">

            <% if (!"GUEST".equals(role)) { %>


                <!-- Há»’ SÆ  CĂ NHĂ‚N -->

                <li>

                    <a href="${pageContext.request.contextPath}/user/profile.jsp"
                       class="<%= currentURI.contains("/profile")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Há»“ sÆ¡ cĂ¡ nhĂ¢n
                        </span>

                    </a>

                </li>


                <!-- QUYá»€N & PHáº M VI Dá»® LIá»†U -->

                <li>

                    <a href="${pageContext.request.contextPath}/permission"
                       class="<%= currentURI.contains("/permission")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Quyá»n & pháº¡m vi dá»¯ liá»‡u
                        </span>

                    </a>

                </li>


                <!-- Äá»”I Máº¬T KHáº¨U -->

                <li>

                    <a href="${pageContext.request.contextPath}/change-password"
                       class="<%= currentURI.contains("/change-password")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            Äá»•i máº­t kháº©u
                        </span>

                    </a>

                </li>


                <!-- ÄÄ‚NG XUáº¤T -->

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
                            ÄÄƒng xuáº¥t
                        </span>

                    </a>

                    <form id="sidebar-logout-form"
                          action="${pageContext.request.contextPath}/logout"
                          method="post"
                          style="display: none;">
                    </form>

                </li>


            <% } else { %>


                <!-- ÄÄ‚NG NHáº¬P -->

                <li>

                    <a href="${pageContext.request.contextPath}/login"
                       class="<%= currentURI.contains("/login")
                               ? "active"
                               : "" %>">

                        <span class="menu-indicator"></span>

                        <span class="menu-text">
                            ÄÄƒng nháº­p
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

        Há»‡ thá»‘ng Ä‘ang hoáº¡t Ä‘á»™ng

    </div>

</aside>
