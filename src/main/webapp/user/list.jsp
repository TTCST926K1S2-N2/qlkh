<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Quản lý tài khoản</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #f4f7fb;
            color: #162033;
            min-height: 100vh;
        }

        /* =========================
           HEADER
        ========================= */

        .page-header {
            background: #ffffff;
            border-bottom: 1px solid #e4eaf2;
            padding: 24px 40px;
        }

        .page-header-inner {
            max-width: 1250px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
        }

        .breadcrumb {
            font-size: 13px;
            color: #2f91d0;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.7px;
            margin-bottom: 8px;
        }

        .page-header h1 {
            font-size: 28px;
            color: #10233d;
            margin-bottom: 6px;
        }

        .page-header p {
            color: #718096;
            font-size: 14px;
        }

        /* =========================
           BUTTON
        ========================= */

        .btn {
            height: 42px;
            padding: 0 18px;
            border-radius: 8px;
            border: none;
            text-decoration: none;
            font-size: 14px;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: 0.2s;
            white-space: nowrap;
        }

        .btn-primary {
            background: #3196d3;
            color: #ffffff;
        }

        .btn-primary:hover {
            background: #247fb5;
        }

        .btn-light {
            background: #ffffff;
            color: #4b5d73;
            border: 1px solid #d8e0ea;
        }

        .btn-light:hover {
            background: #f5f7fa;
        }

        /* =========================
           MAIN
        ========================= */

        .main {
            max-width: 1250px;
            margin: 32px auto;
            padding: 0 24px 40px;
        }

        /* =========================
           ALERT
        ========================= */

        .alert {
            padding: 15px 18px;
            border-radius: 10px;
            margin-bottom: 20px;
            font-size: 14px;
            line-height: 1.5;
        }

        .alert-success {
            background: #edfff5;
            border: 1px solid #bcebd0;
            color: #237a4b;
        }

        .alert-warning {
            background: #fff9e8;
            border: 1px solid #f1dfaa;
            color: #8a6518;
        }

        /* =========================
           FILTER
        ========================= */

        .filter-card {
            background: #ffffff;
            border: 1px solid #e3e9f0;
            border-radius: 14px;
            padding: 20px;
            margin-bottom: 22px;
            box-shadow: 0 5px 18px rgba(31, 56, 88, 0.05);
        }

        .filter-title {
            font-size: 15px;
            font-weight: 700;
            color: #273b55;
            margin-bottom: 14px;
        }

        .filter-form {
            display: grid;
            grid-template-columns: 2fr 1fr 1fr auto auto;
            gap: 12px;
            align-items: center;
        }

        .control {
            width: 100%;
            height: 42px;
            border: 1px solid #d8e0ea;
            border-radius: 8px;
            padding: 0 13px;
            background: #ffffff;
            color: #34445a;
            outline: none;
            font-size: 14px;
        }

        .control:focus {
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
        }

        /* =========================
           TABLE
        ========================= */

        .table-card {
            background: #ffffff;
            border: 1px solid #e3e9f0;
            border-radius: 14px;
            overflow: hidden;
            box-shadow: 0 5px 20px rgba(31, 56, 88, 0.06);
        }

        .table-card-header {
            padding: 20px 22px;
            border-bottom: 1px solid #edf1f5;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .table-card-header h2 {
            font-size: 18px;
            color: #172b46;
        }

        .table-card-header span {
            color: #8995a5;
            font-size: 13px;
        }

        .table-wrapper {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead {
            background: #f7f9fc;
        }

        th {
            padding: 14px 18px;
            text-align: left;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 0.4px;
            color: #718096;
            border-bottom: 1px solid #e6ebf1;
            white-space: nowrap;
        }

        td {
            padding: 16px 18px;
            font-size: 14px;
            color: #34445a;
            border-bottom: 1px solid #edf1f5;
            vertical-align: middle;
        }

        tbody tr:hover {
            background: #f9fbfd;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        .id-cell {
            color: #8290a3;
            font-weight: 600;
        }

        .user-name {
            color: #172b46;
            font-weight: 700;
        }

        .email {
            color: #62728a;
        }

        /* =========================
           STATUS
        ========================= */

        .status {
            display: inline-flex;
            align-items: center;
            padding: 6px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
        }

        .status-active {
            background: #e9f9f0;
            color: #248553;
        }

        .status-locked {
            background: #fff0f0;
            color: #c94a4a;
        }

        .status-default {
            background: #eef2f6;
            color: #65758a;
        }

        /* =========================
           ACTION
        ========================= */

        .action-group {
            display: flex;
            align-items: center;
            gap: 7px;
            flex-wrap: wrap;
        }

        .action {
            padding: 7px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 700;
            text-decoration: none;
            transition: 0.2s;
            white-space: nowrap;
        }

        .action-edit {
            background: #edf7ff;
            color: #237db7;
        }

        .action-edit:hover {
            background: #dcefff;
        }

        .action-role {
            background: #f1efff;
            color: #6754bd;
        }

        .action-role:hover {
            background: #e5e1ff;
        }

        .action-lock {
            background: #fff3e8;
            color: #b76b27;
        }

        .action-lock:hover {
            background: #ffe7d1;
        }

        /* =========================
           EMPTY
        ========================= */

        .empty {
            padding: 55px 20px !important;
            text-align: center;
        }

        .empty-title {
            color: #526176;
            font-weight: 700;
            margin-bottom: 6px;
        }

        .empty-text {
            color: #929dab;
            font-size: 13px;
        }

        /* =========================
           PAGINATION
        ========================= */

        .pagination-footer {
            padding: 16px 20px;
            border-top: 1px solid #edf1f5;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            background: #ffffff;
        }

        .pagination-info {
            color: #7b8798;
            font-size: 13px;
        }

        .pagination-info strong {
            color: #34445a;
        }

        .pagination {
            display: flex;
            align-items: center;
            gap: 6px;
            flex-wrap: wrap;
        }

        .page-link {
            min-width: 36px;
            height: 36px;
            padding: 0 10px;
            border: 1px solid #dce3eb;
            border-radius: 7px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            text-decoration: none;
            color: #526176;
            background: #ffffff;
            font-size: 13px;
            font-weight: 700;
            transition: 0.2s;
        }

        .page-link:hover {
            border-color: #3196d3;
            color: #3196d3;
            background: #f4faff;
        }

        .page-link.active {
            background: #3196d3;
            border-color: #3196d3;
            color: #ffffff;
        }

        .page-link.disabled {
            color: #b5bdc8;
            background: #f7f8fa;
            border-color: #e6eaf0;
            pointer-events: none;
            cursor: default;
        }

        /* =========================
           RESPONSIVE
        ========================= */

        @media (max-width: 900px) {
            .filter-form {
                grid-template-columns: 1fr 1fr;
            }

            .filter-form .search-box {
                grid-column: 1 / -1;
            }
        }

        @media (max-width: 650px) {
            .page-header {
                padding: 20px;
            }

            .page-header-inner {
                flex-direction: column;
                align-items: flex-start;
            }

            .page-header .btn {
                width: 100%;
            }

            .main {
                padding: 0 14px 30px;
                margin-top: 20px;
            }

            .filter-form {
                grid-template-columns: 1fr;
            }

            .filter-form .search-box {
                grid-column: auto;
            }

            .filter-form .btn {
                width: 100%;
            }

            .table-card-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 5px;
            }

            .pagination-footer {
                flex-direction: column;
                align-items: flex-start;
            }
        }
    </style>

</head>

<body>

<header class="page-header">

    <div class="page-header-inner">

        <div>

            <div class="breadcrumb">
                Quản lý hệ thống
            </div>

            <h1>Quản lý tài khoản</h1>

            <p>
    Tìm kiếm, theo dõi và quản lý tài khoản người dùng trong hệ thống.
</p>

</div>

<div style="display: flex; gap: 10px; align-items: center;">

    <a
        href="${pageContext.request.contextPath}/"
        class="btn btn-secondary">
         Trang chủ
    </a>

    <a
        href="${pageContext.request.contextPath}/users/create"
        class="btn btn-primary">
         Thêm tài khoản
    </a>

</div>

</div>

</header>


<main class="main">

    <c:if test="${not empty successMessage}">

        <div class="alert alert-success">

            <strong>Thành công!</strong>

            <c:out value="${successMessage}" />

        </div>

    </c:if>


    <c:if test="${not empty warningMessage}">

        <div class="alert alert-warning">

            <strong>Lưu ý!</strong>

            <c:out value="${warningMessage}" />

        </div>

    </c:if>


    <div class="filter-card">

        <div class="filter-title">
            Tìm kiếm và lọc tài khoản
        </div>

        <form
            class="filter-form"
            method="GET"
            action="${pageContext.request.contextPath}/users">

            <input
                type="text"
                name="keyword"
                class="control search-box"
                placeholder="Tìm theo họ tên hoặc email..."
                value="${fn:escapeXml(param.keyword)}">


            <select
                name="role"
                class="control">

                <option value="">
                    Tất cả vai trò
                </option>

                <option
                    value="ADMIN"
                    ${param.role == 'ADMIN' ? 'selected' : ''}>
                    Quản trị viên
                </option>

                <option
                    value="MANAGER"
                    ${param.role == 'MANAGER' ? 'selected' : ''}>
                    Quản lý kinh doanh
                </option>

                <option
                    value="SALES"
                    ${param.role == 'SALES' ? 'selected' : ''}>
                    Nhân viên kinh doanh
                </option>

            </select>


            <select
                name="status"
                class="control">

                <option value="">
                    Tất cả trạng thái
                </option>

                <option
                    value="ACTIVE"
                    ${param.status == 'ACTIVE' ? 'selected' : ''}>
                    Đang hoạt động
                </option>

                <option
                    value="LOCKED"
                    ${param.status == 'LOCKED' ? 'selected' : ''}>
                    Đã khóa
                </option>

            </select>


            <button
                type="submit"
                class="btn btn-primary">
                Tìm kiếm
            </button>


            <a
                href="${pageContext.request.contextPath}/users"
                class="btn btn-light">
                Đặt lại
            </a>

        </form>

    </div>


    <div class="table-card">

        <div class="table-card-header">

            <h2>Danh sách tài khoản</h2>

            <span>
                Quản lý người dùng hệ thống
            </span>

        </div>


        <div class="table-wrapper">

            <table>

                <thead>

                    <tr>
                        <th>ID</th>
                        <th>Họ và tên</th>
                        <th>Email</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>

                </thead>


                <tbody>

                    <c:forEach
                        var="user"
                        items="${users}">

                        <tr>

                            <td class="id-cell">
                                #<c:out value="${user.id}" />
                            </td>


                            <td>

                                <span class="user-name">
                                    <c:out value="${user.fullName}" />
                                </span>

                            </td>


                            <td>

                                <span class="email">
                                    <c:out value="${user.email}" />
                                </span>

                            </td>


                            <td>

                                <c:choose>

                                    <c:when test="${user.status == 'ACTIVE'}">

                                        <span class="status status-active">
                                            Đang hoạt động
                                        </span>

                                    </c:when>

                                    <c:when test="${user.status == 'LOCKED'}">

                                        <span class="status status-locked">
                                            Đã khóa
                                        </span>

                                    </c:when>

                                    <c:otherwise>

                                        <span class="status status-default">
                                            <c:out
                                                value="${user.status}"
                                                default="-" />
                                        </span>

                                    </c:otherwise>

                                </c:choose>

                            </td>


                            <td>

                                <div class="action-group">

                                    <a
                                        href="${pageContext.request.contextPath}/users/edit?id=${user.id}"
                                        class="action action-edit">
                                        Sửa
                                    </a>


                                    <a
                                        href="${pageContext.request.contextPath}/users/role-group?id=${user.id}"
                                        class="action action-role">
                                        Phân quyền
                                    </a>


                                    <a
                                        href="${pageContext.request.contextPath}/users/handover?id=${user.id}"
                                        class="action action-lock">
                                        Bàn giao / Khóa
                                    </a>

                                </div>

                            </td>

                        </tr>

                    </c:forEach>


                    <c:if test="${empty users}">

                        <tr>

                            <td
                                colspan="5"
                                class="empty">

                                <div class="empty-title">
                                    Không tìm thấy tài khoản
                                </div>

                                <div class="empty-text">
                                    Hãy thử thay đổi từ khóa hoặc bộ lọc tìm kiếm.
                                </div>

                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>


        <div class="pagination-footer">

            <div class="pagination-info">

                Tổng

                <strong>
                    <c:out value="${totalItems}" default="0" />
                </strong>

                tài khoản

                &nbsp;•&nbsp;

                <c:out value="${pageSize}" default="20" />

                tài khoản / trang

            </div>


            <c:if test="${totalPages > 1}">

                <div class="pagination">

                    <c:choose>

                        <c:when test="${currentPage > 1}">

                            <c:url
                                var="previousUrl"
                                value="/users">

                                <c:param
                                    name="keyword"
                                    value="${param.keyword}" />

                                <c:param
                                    name="role"
                                    value="${param.role}" />

                                <c:param
                                    name="status"
                                    value="${param.status}" />

                                <c:param
                                    name="page"
                                    value="${currentPage - 1}" />

                            </c:url>

                            <a
                                href="${previousUrl}"
                                class="page-link">
                                Trước
                            </a>

                        </c:when>

                        <c:otherwise>

                            <span class="page-link disabled">
                                Trước
                            </span>

                        </c:otherwise>

                    </c:choose>


                    <c:forEach
                        var="pageNumber"
                        begin="1"
                        end="${totalPages}">

                        <c:url
                            var="pageUrl"
                            value="/users">

                            <c:param
                                name="keyword"
                                value="${param.keyword}" />

                            <c:param
                                name="role"
                                value="${param.role}" />

                            <c:param
                                name="status"
                                value="${param.status}" />

                            <c:param
                                name="page"
                                value="${pageNumber}" />

                        </c:url>

                        <a
                            href="${pageUrl}"
                            class="page-link ${pageNumber == currentPage ? 'active' : ''}">

                            <c:out value="${pageNumber}" />

                        </a>

                    </c:forEach>


                    <c:choose>

                        <c:when test="${currentPage < totalPages}">

                            <c:url
                                var="nextUrl"
                                value="/users">

                                <c:param
                                    name="keyword"
                                    value="${param.keyword}" />

                                <c:param
                                    name="role"
                                    value="${param.role}" />

                                <c:param
                                    name="status"
                                    value="${param.status}" />

                                <c:param
                                    name="page"
                                    value="${currentPage + 1}" />

                            </c:url>

                            <a
                                href="${nextUrl}"
                                class="page-link">
                                Sau
                            </a>

                        </c:when>

                        <c:otherwise>

                            <span class="page-link disabled">
                                Sau
                            </span>

                        </c:otherwise>

                    </c:choose>

                </div>

            </c:if>

        </div>

    </div>

</main>

</body>

</html>