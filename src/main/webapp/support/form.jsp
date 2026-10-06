<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Ghi nhận yêu cầu hỗ trợ</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-4" style="max-width: 700px;">
    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white">
            <h4 class="mb-0">📝 Ghi nhận yêu cầu hỗ trợ khách hàng</h4>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/supports/create" method="POST">
                <div class="mb-3">
                    <label for="title" class="form-label fw-bold">Tiêu đề yêu cầu <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="title" name="title" required placeholder="Nhập tiêu đề tóm tắt sự cố/yêu cầu">
                </div>

                <div class="mb-3">
                    <label for="customerId" class="form-label fw-bold">Liên kết khách hàng <span class="text-danger">*</span></label>
                    <select class="form-select" id="customerId" name="customerId" required>
                        <option value="">-- Chọn khách hàng liên quan --</option>
                        <option value="1">Nguyễn Văn An (0912345678)</option>
                        <option value="2">Công ty Cổ phần Công nghệ ABC</option>
                    </select>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label for="priority" class="form-label fw-bold">Mức độ ưu tiên</label>
                        <select class="form-select" id="priority" name="priority">
                            <option value="Low">Thấp</option>
                            <option value="Medium" selected>Trung bình</option>
                            <option value="High">Cao</option>
                            <option value="Urgent">Khẩn cấp</option>
                        </select>
                    </div>

                    <div class="col-md-6 mb-3">
                        <label for="assignee" class="form-label fw-bold">Người xử lý</label>
                        <select class="form-select" id="assignee" name="assignee">
                            <option value="Tran Van Quan Tri">Trần Văn Quản Trị</option>
                            <option value="Le Thi Ho Tro">Lê Thị Hỗ Trợ</option>
                        </select>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="description" class="form-label fw-bold">Mô tả chi tiết nội dung hỗ trợ</label>
                    <textarea class="form-control" id="description" name="description" rows="4" placeholder="Nhập thông tin chi tiết vấn đề..."></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/supports" class="btn btn-secondary">Hủy bỏ</a>
                    <button type="submit" class="btn btn-primary">Lưu yêu cầu hỗ trợ</button>
                </div>
            </form>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>
