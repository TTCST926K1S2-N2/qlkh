<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String homeUserName =
            (String) session.getAttribute("userName");

    String homeRole =
            (String) session.getAttribute("userRole");

    if (homeUserName == null || homeUserName.isBlank()) {
        homeUserName = "Người dùng";
    }

    String homeRoleName = "";

    if ("ADMIN".equals(homeRole)) {
        homeRoleName = "Quản trị hệ thống";
    } else if ("MANAGER".equals(homeRole)) {
        homeRoleName = "Quản lý kinh doanh";
    } else if ("SALES".equals(homeRole)) {
        homeRoleName = "Nhân viên kinh doanh";
    }
%>

<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Hệ thống quản lý khách hàng</title>


    <!-- Session / Logout -->

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/session.css">


    <style>

        * {
            box-sizing: border-box;
        }


        html,
        body {
            margin: 0;
            padding: 0;

            min-height: 100%;
        }


        body {
            min-height: 100vh;

            display: flex;

            background: #f4f7fa;

            color: #1e293b;

            font-family:
                "Segoe UI",
                Tahoma,
                Geneva,
                Verdana,
                sans-serif;
        }


        .main-content {
            flex: 1;

            min-width: 0;

            padding: 48px;
        }


        .welcome-container {
            width: 100%;
            max-width: 1100px;

            margin: 0 auto;
        }


        .welcome-header {
            margin-bottom: 28px;
        }


        .welcome-label {
            margin-bottom: 8px;

            color: #3498db;

            font-size: 12px;
            font-weight: 700;

            letter-spacing: 1px;

            text-transform: uppercase;
        }


        .welcome-header h1 {
            margin: 0;

            color: #172b3a;

            font-size: 30px;
            font-weight: 700;
        }


        .welcome-description {
            margin-top: 10px;

            color: #64748b;

            font-size: 14px;
        }


        .welcome-card {
            padding: 28px;

            background: #ffffff;

            border:
                1px solid #e6ebf0;

            border-radius: 14px;

            box-shadow:
                0 6px 24px rgba(15, 23, 42, 0.06);
        }


        .welcome-card-title {
            margin: 0 0 8px;

            color: #1e293b;

            font-size: 20px;
            font-weight: 650;
        }


        .welcome-card p {
            margin:
                6px
                0;

            color: #64748b;

            font-size: 14px;

            line-height: 1.6;
        }


        .role-badge {
            display: inline-flex;

            margin-top: 16px;

            padding:
                7px
                12px;

            border-radius: 20px;

            background: #edf7fd;

            color: #2388c7;

            font-size: 12px;
            font-weight: 600;
        }


        .message {
            margin-top: 18px;

            color: #475569;

            font-size: 14px;
        }


        @media (max-width: 768px) {

            body {
                display: block;
            }


            .main-content {
                padding: 26px 18px;
            }


            .welcome-header h1 {
                font-size: 25px;
            }


            .welcome-card {
                padding: 20px;
            }
        }


        @media (max-width: 360px) {

            .main-content {
                padding: 20px 14px;
            }


            .welcome-header h1 {
                font-size: 22px;
            }
        }

    </style>

</head>


<body>


    <!-- SIDEBAR -->

    <jsp:include page="/components/sidebar.jsp" />


    <!-- MAIN CONTENT -->

    <main class="main-content">

        <div class="welcome-container">

            <div class="welcome-header">

                <div class="welcome-label">
                    Tổng quan
                </div>

                <h1>
                    Hệ thống quản lý khách hàng
                </h1>

                <div class="welcome-description">
                    Quản lý thông tin khách hàng và quyền truy cập
                    theo vai trò người dùng.
                </div>

            </div>


            <section class="welcome-card">

                <% if (homeRole != null) { %>

                    <h2 class="welcome-card-title">
                        Xin chào, <%= homeUserName %>
                    </h2>

                    <p>
                        Bạn đã đăng nhập thành công vào hệ thống.
                    </p>

                    <span class="role-badge">
                        <%= homeRoleName %>
                    </span>

                <% } else { %>

                    <h2 class="welcome-card-title">
                        Chào mừng đến với hệ thống
                    </h2>

                    <p>
                        Vui lòng đăng nhập để sử dụng các chức năng
                        được cấp quyền.
                    </p>

                <% } %>


                <% if (request.getAttribute("message") != null) { %>

                    <div class="message">
                        ${message}
                    </div>

                <% } %>

            </section>

        </div>

    </main>


    <!-- SESSION -->

    <% if (session.getAttribute("userRole") != null) { %>

        <jsp:include page="/components/session-modal.jsp" />

        <script
            src="${pageContext.request.contextPath}/assets/js/session.js">
        </script>

    <% } %>


</body>

</html>