<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Lead - Tạo mới & Nhập Excel</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme-base.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">

    <div class="container py-4">
        <h3 class="mb-4">Quản lý Lead</h3>

        <!-- KHU VỰC 1: TẠO LEAD THỦ CÔNG -->
        <div class="card p-4 mb-4 shadow-sm">
            <h5 class="card-title text-primary mb-3">Tạo Lead thủ công</h5>
            <form id="createLeadForm" onsubmit="handleManualCreate(event)">
                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label font-weight-bold">Tên doanh nghiệp</label>
                        <input type="text" class="form-control" id="companyName" placeholder="Nhập tên công ty">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label font-weight-bold">Họ tên người liên hệ <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="contactName" required placeholder="Nhập họ tên">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label font-weight-bold">Nguồn lead <span class="text-danger">*</span></label>
                        <select class="form-select" id="leadSource" required>
                            <option value="">-- Chọn nguồn lead --</option>
                            <option value="WEBSITE">Website</option>
                            <option value="EVENT">Sự kiện / Hội thảo</option>
                            <option value="REFERRAL">Giới thiệu</option>
                            <option value="COLD_CALL">Gọi điện trực tiếp</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label font-weight-bold">Email liên hệ <span class="text-danger">*</span></label>
                        <input type="email" class="form-control" id="contactEmail" required placeholder="example@domain.com">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label font-weight-bold">Số điện thoại <span class="text-danger">*</span></label>
                        <input type="tel" class="form-control" id="contactPhone" required placeholder="0901234567">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label font-weight-bold">Vai trò mua hàng (Kế thừa S3-02)</label>
                        <select class="form-select" id="buyingRole">
                            <option value="">-- Chọn vai trò quyết định --</option>
                            <option value="DECISION_MAKER">Người quyết định</option>
                            <option value="INFLUENCER">Người ảnh hưởng</option>
                            <option value="EVALUATOR">Người đánh giá</option>
                            <option value="USER">Người sử dụng</option>
                        </select>
                    </div>
                </div>
                <div class="mt-3">
                    <button type="submit" class="btn btn-primary">Lưu thông tin Lead</button>
                </div>
            </form>
        </div>

        <!-- KHU VỰC 2: NHẬP LEAD TỪ EXCEL -->
        <div class="card p-4 shadow-sm">
            <h5 class="card-title text-primary mb-3">Nhập danh sách Lead từ Excel</h5>
            
            <!-- Thao tác chọn file và tải template -->
            <div class="d-flex align-items-center gap-3 my-2">
                <button type="button" class="btn btn-outline-primary" onclick="downloadTemplate()">
                    Tải tệp Excel mẫu
                </button>

                <input type="file" id="excelFileInput" accept=".xlsx, .xls" style="display: none;" onchange="onFileSelected(this)" />
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('excelFileInput').click()">
                    Chọn tệp Excel
                </button>
                <span id="selectedFileName" class="text-muted fw-bold">Chưa chọn tệp</span>
            </div>

            <!-- Nút bấm xử lý API -->
            <div class="mt-3">
                <button type="button" class="btn btn-info text-white" onclick="previewExcel()">
                    Xem trước (Preview)
                </button>
                <button type="button" id="btnImportSubmit" class="btn btn-success" onclick="importExcel()" disabled>
                    Xác nhận Import
                </button>
            </div>

            <!-- Khung thống kê số bản ghi -->
            <div id="importStats" class="alert alert-info mt-3 d-none">
                <strong>Tổng số dòng:</strong> <span id="totalRows">0</span> | 
                <strong class="text-success">Hợp lệ:</strong> <span id="successCount">0</span> | 
                <strong class="text-danger">Không hợp lệ / Lỗi:</strong> <span id="failedCount">0</span>
            </div>

            <!-- Bảng Preview dữ liệu -->
            <div class="table-responsive mt-3">
                <table class="table table-bordered table-hover">
                    <thead class="table-light">
                        <tr>
                            <th>STT Dòng</th>
                            <th>Tên doanh nghiệp</th>
                            <th>Họ tên liên hệ</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th>Trạng thái & Báo lỗi</th>
                        </tr>
                    </thead>
                    <tbody id="previewTableBody">
                        <tr>
                            <td colspan="6" class="text-center text-muted">Chưa có dữ liệu xem trước</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>
        const CONTEXT_PATH = '${pageContext.request.contextPath}';

        // 1. Tải file mẫu
        function downloadTemplate() {
            window.location.href = CONTEXT_PATH + '/api/v1/leads/import/template';
        }

        // 2. Hiển thị tên file đã chọn
        function onFileSelected(input) {
            const fileNameSpan = document.getElementById('selectedFileName');
            if (input.files && input.files.length > 0) {
                fileNameSpan.innerText = input.files[0].name;
            } else {
                fileNameSpan.innerText = 'Chưa chọn tệp';
            }
        }

        // 3. Xử lý Preview dữ liệu
        function previewExcel() {
            const fileInput = document.getElementById('excelFileInput');
            const file = fileInput.files[0];

            if (!validateFile(file)) return;

            const formData = new FormData();
            formData.append('file', file);

            fetch(CONTEXT_PATH + '/api/v1/leads/import?preview=true', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    renderPreviewData(data);
                } else {
                    alert('Lỗi Preview: ' + (data.message || 'Không thể xem trước tệp'));
                }
            })
            .catch(error => {
                console.error('API Error:', error);
                alert('Có lỗi xảy ra khi kết nối máy chủ.');
            });
        }

        // 4. Xử lý Import chính thức
        function importExcel() {
            const fileInput = document.getElementById('excelFileInput');
            const file = fileInput.files[0];

            if (!validateFile(file)) return;

            const formData = new FormData();
            formData.append('file', file);

            fetch(CONTEXT_PATH + '/api/v1/leads/import', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(`Import thành công ${data.successCount}/${data.totalRows} bản ghi!`);
                    location.reload();
                } else {
                    alert('Lỗi Import: ' + (data.message || 'Lưu dữ liệu thất bại'));
                }
            })
            .catch(error => {
                console.error('API Error:', error);
                alert('Không thể lưu dữ liệu Import.');
            });
        }

        // 5. Kiểm tra file trước khi gửi
        function validateFile(file) {
            if (!file) {
                alert('Vui lòng chọn tệp Excel trước!');
                return false;
            }

            const fileName = file.name.toLowerCase();
            if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
                alert('Định dạng tệp không hợp lệ! Chỉ hỗ trợ .xlsx hoặc .xls');
                return false;
            }

            if (file.size > 10 * 1024 * 1024) {
                alert('Dung lượng tệp vượt quá 10MB!');
                return false;
            }

            return true;
        }

        // 6. Hiển thị dữ liệu Preview và tô màu báo lỗi
        function renderPreviewData(data) {
            document.getElementById('importStats').classList.remove('d-none');
            document.getElementById('totalRows').innerText = data.totalRows;
            document.getElementById('successCount').innerText = data.successCount;
            document.getElementById('failedCount').innerText = data.failedCount;

            const tbody = document.getElementById('previewTableBody');
            tbody.innerHTML = '';

            if (!data.rows || data.rows.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Tệp không có dữ liệu</td></tr>';
                document.getElementById('btnImportSubmit').disabled = true;
                return;
            }

            data.rows.forEach(row => {
                const tr = document.createElement('tr');

                // Highlight bản ghi lỗi (màu đỏ) hoặc trùng lặp (màu vàng)
                if (!row.valid) {
                    tr.classList.add('table-danger');
                } else if (row.duplicate) {
                    tr.classList.add('table-warning');
                }

                tr.innerHTML = `
                    <td>${row.row}</td>
                    <td>${escapeHtml(row.companyName)}</td>
                    <td>${escapeHtml(row.contactName)}</td>
                    <td>${escapeHtml(row.email)}</td>
                    <td>${escapeHtml(row.phone)}</td>
                    <td>
                        ${!row.valid 
                            ? `<span class="badge bg-danger">${escapeHtml(row.message || 'Không hợp lệ')}</span>`
                            : (row.duplicate 
                                ? '<span class="badge bg-warning text-dark">Bản ghi trùng lặp</span>' 
                                : '<span class="badge bg-success">Hợp lệ</span>')
                        }
                    </td>
                `;
                tbody.appendChild(tr);
            });

            document.getElementById('btnImportSubmit').disabled = (data.successCount === 0);
        }

        // 7. Xử lý lưu thủ công
        function handleManualCreate(event) {
            event.preventDefault();
            alert('Đã lưu thông tin Lead thủ công!');
        }

        function escapeHtml(text) {
            if (!text) return '';
            return text.replace(/&/g, "&amp;")
                       .replace(/</g, "&lt;")
                       .replace(/>/g, "&gt;")
                       .replace(/"/g, "&quot;")
                       .replace(/'/g, "&#039;");
        }
    </script>
</body>
</html>