<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>
        ${empty user.id ? 'Thêm tài khoản' : 'Sửa tài khoản'}
    </title>

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

        .page-header {
            background: #ffffff;
            border-bottom: 1px solid #e4eaf2;
            padding: 22px 40px;
        }

        .page-header-inner {
            max-width: 1100px;
            margin: 0 auto;
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

        .main {
            max-width: 760px;
            margin: 38px auto;
            padding: 0 20px;
        }

        .card {
            background: #ffffff;
            border: 1px solid #e4eaf2;
            border-radius: 16px;
            box-shadow: 0 8px 30px rgba(31, 56, 88, 0.08);
            overflow: hidden;
        }

        .card-header {
            padding: 24px 28px;
            border-bottom: 1px solid #edf1f5;
        }

        .card-header h2 {
            font-size: 21px;
            color: #10233d;
            margin-bottom: 6px;
        }

        .card-header p {
            color: #7a8798;
            font-size: 14px;
        }

        .card-body {
            padding: 28px;
        }

        .alert {
            padding: 14px 16px;
            border-radius: 10px;
            margin-bottom: 22px;
            font-size: 14px;
            line-height: 1.5;
        }

        .alert-danger {
            background: #fff2f2;
            color: #c53030;
            border: 1px solid #fed7d7;
        }

        .alert-info {
            background: #edf7ff;
            color: #285f8f;
            border: 1px solid #cfe8fb;
        }

        .form-group {
            margin-bottom: 22px;
        }

        .form-label {
            display: block;
            font-size: 14px;
            font-weight: 700;
            color: #34445a;
            margin-bottom: 8px;
        }

        .required {
            color: #e05252;
        }

        .form-control {
            width: 100%;
            height: 46px;
            padding: 0 14px;
            border: 1px solid #d8e0ea;
            border-radius: 9px;
            outline: none;
            background: #ffffff;
            color: #17243a;
            font-size: 14px;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .form-control:focus {
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.12);
        }

        .form-control::placeholder {
            color: #a0aabc;
        }

        .form-help {
            display: block;
            color: #8a96a6;
            font-size: 12px;
            margin-top: 7px;
        }

        .actions {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 12px;
            margin-top: 30px;
            padding-top: 22px;
            border-top: 1px solid #edf1f5;
        }

        .btn {
            min-width: 120px;
            height: 42px;
            border-radius: 8px;
            padding: 0 18px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 700;
            text-decoration: none;
            cursor: pointer;
            border: none;
            transition: 0.2s;
        }

        .btn-secondary {
            color: #4a5568;
            background: #ffffff;
            border: 1px solid #d7dee8;
        }

        .btn-secondary:hover {
            background: #f5f7fa;
        }

        .btn-primary {
            color: #ffffff;
            background: #3196d3;
            border: 1px solid #3196d3;
        }

        .btn-primary:hover {
            background: #247fb5;
            border-color: #247fb5;
        }

        @media (max-width: 600px) {
            .page-header {
                padding: 20px;
            }

            .main {
                margin: 22px auto;
            }

            .card-header,
            .card-body {
                padding: 20px;
            }

            .actions {
                flex-direction: column-reverse;
            }

            .btn {
                width: 100%;
            }
        }
    </style>
</head>

<body>

<header class="page-header">
    <div class="page-header-inner">

        <div class="breadcrumb">
            Quản lý tài khoản
        </div>

        <h1>
            ${empty user.id ? 'Thêm tài khoản mới' : 'Sửa tài khoản'}
        </h1>

        <p>
            ${empty user.id
                ? 'Tạo tài khoản cho người dùng mới trong hệ thống.'
                : 'Cập nhật thông tin tài khoản người dùng.'}
        </p>

    </div>
</header>

<main class="main">

    <div class="card">

        <div class="card-header">
            <h2>
                ${empty user.id ? 'Thông tin tài khoản' : 'Thông tin cần chỉnh sửa'}
            </h2>

            <p>
                Vui lòng nhập đầy đủ các thông tin bên dưới.
            </p>
        </div>

        <div class="card-body">

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">
                    <strong>Không thể lưu tài khoản.</strong><br>
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <form
                action="${pageContext.request.contextPath}/users"
                method="POST"
                autocomplete="off">

                <input
                    type="hidden"
                    name="id"
                    value="${user.id}">

                <div class="form-group">

                    <label for="fullName" class="form-label">
                        Họ và tên
                        <span class="required">*</span>
                    </label>

                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        class="form-control"
                        value="<c:out value='${user.fullName}'/>"
                        placeholder="Nhập họ và tên người dùng"
                        maxlength="255"
                        required>

                </div>

                <div class="form-group">

                    <label for="email" class="form-label">
                        Email
                        <span class="required">*</span>
                    </label>

                    <input
                        type="email"
                        id="email"
                        name="email"
                        class="form-control"
                        value="<c:out value='${user.email}'/>"
                        placeholder="example@gmail.com"
                        maxlength="255"
                        autocomplete="off"
                        required>

                    <span class="form-help">
                        Email được sử dụng để đăng nhập và nhận thông báo từ hệ thống.
                    </span>

                </div>

                <c:if test="${empty user.id}">
                    <div class="alert alert-info">
                        <strong>Tài khoản mới</strong><br>
                        Hệ thống sẽ tự tạo mật khẩu tạm và gửi đến email của
                        người dùng sau khi tài khoản được tạo thành công.
                    </div>
                </c:if>

                <div class="actions">

                    <a
                        href="${pageContext.request.contextPath}/users"
                        class="btn btn-secondary">
                        Quay lại
                    </a>

                    <button
                        type="submit"
                        class="btn btn-primary">

                        ${empty user.id
                            ? 'Tạo tài khoản'
                            : 'Lưu thay đổi'}

                    </button>

                </div>

            </form>

        </div>
    </div>

</main>

</body>
</html>