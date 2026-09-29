<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Biểu mẫu tài khoản</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">

            <div class="card shadow">

                <div class="card-header bg-primary text-white">
                    <h4 class="mb-0">
                        ${empty user ? 'Thêm tài khoản' : 'Sửa tài khoản'}
                    </h4>
                </div>

                <div class="card-body">

                    <form action="${pageContext.request.contextPath}/users"
                          method="POST">

                        <!-- Khi sửa tài khoản, gửi ID về Backend -->
                        <input type="hidden"
                               name="id"
                               value="${user.id}">

                        <div class="mb-3">
                            <label class="form-label">
                                Họ và tên
                            </label>

                            <input type="text"
                                   class="form-control"
                                   name="fullName"
                                   value="${user.fullName}"
                                   required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">
                                Email
                            </label>

                            <input type="email"
                                   class="form-control"
                                   name="email"
                                   value="${user.email}"
                                   required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">
                                Số điện thoại
                            </label>

                            <input type="text"
                                   class="form-control"
                                   name="phone"
                                   value="${user.phone}">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">
                                ${empty user
                                    ? 'Mật khẩu'
                                    : 'Mật khẩu (để trống nếu không thay đổi)'}
                            </label>

                            <input type="password"
                                   class="form-control"
                                   name="password"
                                   ${empty user ? 'required' : ''}>
                        </div>

                        <div class="d-flex justify-content-between">

                            <a href="${pageContext.request.contextPath}/users"
                               class="btn btn-secondary">
                                Quay lại
                            </a>

                            <button type="submit"
                                    class="btn btn-success">
                                ${empty user
                                    ? 'Thêm tài khoản'
                                    : 'Lưu thay đổi'}
                            </button>

                        </div>

                    </form>

                </div>
            </div>

        </div>
    </div>
</div>

</body>
</html>