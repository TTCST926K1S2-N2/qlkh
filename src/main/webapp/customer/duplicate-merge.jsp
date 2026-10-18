<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cảnh báo và Gộp khách hàng trùng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-4">
    <div class="alert alert-warning shadow-sm" role="alert">
        <h4 class="alert-heading">⚠️ Cảnh báo: Phát hiện khách hàng trùng lặp!</h4>
        <p class="mb-0">Hệ thống tìm thấy các hồ sơ có thông tin tương đồng (Số điện thoại hoặc Email trùng nhau). Vui lòng đối chiếu và chọn hồ sơ chính trước khi gộp.</p>
    </div>

    <% String msg = request.getParameter("message");
       if ("merge_success".equals(msg)) { %>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Gộp khách hàng thành công! Dữ liệu đã được đồng bộ sang hồ sơ chính.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/customers/merge" method="POST">
        <div class="row">
            <!-- Hồ sơ 1 -->
            <div class="col-md-6">
                <div class="card shadow-sm mb-3 border-primary">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <span>Hồ sơ số 1</span>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="primaryId" id="primary1" value="101" checked>
                            <label class="form-check-label text-white fw-bold" for="primary1">Chọn làm hồ sơ chính</label>
                        </div>
                    </div>
                    <div class="card-body">
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item"><strong>ID:</strong> 101</li>
                            <li class="list-group-item"><strong>Họ tên:</strong> Nguyễn Văn An</li>
                            <li class="list-group-item"><strong>Email:</strong> an.nguyen@ictu.edu.vn</li>
                            <li class="list-group-item"><strong>Số điện thoại:</strong> 0912345678</li>
                            <li class="list-group-item"><strong>Nhóm:</strong> Miền Bắc</li>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- Hồ sơ 2 -->
            <div class="col-md-6">
                <div class="card shadow-sm mb-3 border-secondary">
                    <div class="card-header bg-secondary text-white d-flex justify-content-between align-items-center">
                        <span>Hồ sơ số 2 (Trùng)</span>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="primaryId" id="primary2" value="102">
                            <input type="hidden" name="duplicateId" value="102">
                            <label class="form-check-label text-white fw-bold" for="primary2">Chọn làm hồ sơ chính</label>
                        </div>
                    </div>
                    <div class="card-body">
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item"><strong>ID:</strong> 102</li>
                            <li class="list-group-item"><strong>Họ tên:</strong> Nguyễn Văn A</li>
                            <li class="list-group-item"><strong>Email:</strong> an.nguyen@ictu.edu.vn</li>
                            <li class="list-group-item"><strong>Số điện thoại:</strong> 0912345678</li>
                            <li class="list-group-item"><strong>Nhóm:</strong> Miền Nam</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <div class="card shadow-sm mt-3">
            <div class="card-body bg-white">
                <h5 class="text-danger">Thông tin dự kiến sau khi gộp:</h5>
                <ul>
                    <li>Hồ sơ được chọn sẽ giữ lại toàn bộ thông tin định danh và lịch sử giao dịch liên quan.</li>
                    <li>Hồ sơ trùng lặp sẽ bị vô hiệu hóa và chuyển toàn bộ liên kết dữ liệu sang hồ sơ chính.</li>
                </ul>
                <div class="d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/customers" class="btn btn-secondary">Hủy bỏ</a>
                    <button type="submit" class="btn btn-danger" onclick="return confirm('Bạn có chắc chắn muốn gộp các hồ sơ này không? Thao tác không thể hoàn tác!');">Xác nhận và Gộp</button>
                </div>
            </div>
        </div>
    </form>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
