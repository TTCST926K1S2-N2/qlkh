<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tìm kiếm và Lọc Khách hàng - HỆ THỐNG QLKH</title>
    <style>
        body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background-color: #f0f3f6; font-family: 'Segoe UI', Arial, sans-serif; 
        }
        .main-content { padding: 40px; width: 100%; max-width: 1200px; box-sizing: border-box; }
        
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; 
            padding-bottom: 12px; margin-bottom: 30px; 
        }
        .page-header h2 { margin: 0; color: #1a2a3a; font-size: 26px; font-weight: 700; }
        
        .content-box { 
            background: #fff; padding: 30px; border-radius: 10px; 
            box-shadow: 0 4px 15px rgba(0,0,0,0.06); margin-bottom: 25px;
            border: 1px solid #e1e8ed; 
        }

        /* Form Lọc & Tìm kiếm */
        .filter-section {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }
        .form-group { display: flex; flex-direction: column; }
        .form-group label { font-weight: 600; margin-bottom: 5px; color: #2c3e50; font-size: 14px; }
        .form-group input, .form-group select { 
            padding: 10px; border: 1px solid #bdc3c7; border-radius: 4px; font-size: 14px; outline: none;
        }
        .form-group input:focus, .form-group select:focus { border-color: #1f3a60; }
        
        /* Cụm Nút bấm */
        .action-buttons {
            display: flex; gap: 10px; justify-content: flex-end; align-items: center;
        }
        .btn { padding: 10px 20px; border-radius: 5px; border: none; font-weight: 600; cursor: pointer; transition: opacity 0.2s; }
        .btn:hover { opacity: 0.9; }
        .btn-primary { background-color: #1f3a60; color: white; }
        .btn-secondary { background-color: #e0e6ed; color: #1a2a3a; }
        .btn-danger { background-color: #e74c3c; color: white; }

        /* Bảng dữ liệu */
        table { width: 100%; border-collapse: collapse; margin-top: 20px; overflow: hidden; border-radius: 6px; }
        th, td { padding: 12px 15px; text-align: left; }
        th { background-color: #1f3a60; color: #ffffff; font-weight: 600; font-size: 13px; text-transform: uppercase; border: none; }
        tr { border-bottom: 1px solid #eef2f5; }
        tbody tr:nth-child(even) { background-color: #f8fafd; }
        tbody tr:hover { background-color: #f1f5f9; }
        td { color: #3c4a57; font-size: 14px; }
        
        .status-badge {
            display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; text-align: center;
        }
        .status-active { background-color: #27ae60; color: white; }
        .status-inactive { background-color: #95a5a6; color: white; }

        /* Phân trang */
        .pagination { display: flex; justify-content: center; gap: 5px; margin-top: 20px; }
        .page-item { padding: 8px 12px; border: 1px solid #ddd; background: #fff; cursor: pointer; border-radius: 4px; }
        .page-item.active { background: #1f3a60; color: #fff; border-color: #1f3a60; }
        
        /* Trạng thái trống/lỗi */
        .state-message { text-align: center; padding: 30px; font-size: 15px; border-radius: 8px; font-weight: 500; display: none; }
        .state-empty { background-color: #fef9e7; color: #b7950b; border: 1px solid #f9e79f; }
        .state-error { background-color: #fdf2f2; color: #ec7063; border: 1px solid #fadbd8; }
    </style>
</head>
<body>
    <div class="main-content">
        <div class="page-header">
            <h2>Tìm kiếm và Lọc Khách hàng</h2>
        </div>

        <div class="content-box">
            <!-- Khu vực Bộ lọc -->
            <form id="filterForm" onsubmit="handleSearch(event)">
                <div class="filter-section">
                    <!-- Yêu cầu: Tìm theo tên, mã số thuế -->
                    <div class="form-group">
                        <label>Từ khóa (Tên/MST):</label>
                        <input type="text" id="keyword" placeholder="Nhập tên hoặc mã số thuế...">
                    </div>
                    <!-- Yêu cầu: Tìm theo SĐT người liên hệ -->
                    <div class="form-group">
                        <label>SĐT người liên hệ:</label>
                        <input type="text" id="phone" placeholder="Nhập số điện thoại...">
                    </div>
                    <!-- Yêu cầu: Lọc theo trạng thái -->
                    <div class="form-group">
                        <label>Trạng thái:</label>
                        <select id="status">
                            <option value="">-- Tất cả --</option>
                            <option value="active">Đang giao dịch</option>
                            <option value="inactive">Ngừng giao dịch</option>
                        </select>
                    </div>
                    <!-- Yêu cầu: Lọc theo ngành nghề -->
                    <div class="form-group">
                        <label>Ngành nghề:</label>
                        <select id="industry">
                            <option value="">-- Tất cả --</option>
                            <option value="it">Công nghệ thông tin</option>
                            <option value="manufacturing">Sản xuất</option>
                            <option value="retail">Bán lẻ</option>
                        </select>
                    </div>
                    <!-- Yêu cầu: Lọc theo quy mô -->
                    <div class="form-group">
                        <label>Quy mô:</label>
                        <select id="scale">
                            <option value="">-- Tất cả --</option>
                            <option value="small">Doanh nghiệp Nhỏ</option>
                            <option value="medium">Doanh nghiệp Vừa</option>
                            <option value="large">Doanh nghiệp Lớn</option>
                        </select>
                    </div>
                     <!-- Yêu cầu: Lọc theo khu vực -->
                    <div class="form-group">
                        <label>Khu vực:</label>
                        <select id="region">
                            <option value="">-- Tất cả --</option>
                            <option value="north">Miền Bắc</option>
                            <option value="central">Miền Trung</option>
                            <option value="south">Miền Nam</option>
                        </select>
                    </div>
                     <!-- Yêu cầu: Lọc theo người sở hữu -->
                     <div class="form-group">
                        <label>Người phụ trách:</label>
                        <select id="owner">
                            <option value="">-- Tất cả --</option>
                            <option value="1">Nguyễn Văn A</option>
                            <option value="2">Nguyễn Văn B</option>
                        </select>
                    </div>
                </div>

                <div class="action-buttons">
                    <button type="button" class="btn btn-secondary" onclick="saveFilter()">Lưu bộ lọc</button>
                    <!-- Yêu cầu: Nút xóa bộ lọc -->
                    <button type="button" class="btn btn-danger" onclick="clearFilter()">Xóa bộ lọc</button>
                    <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                </div>
            </form>
        </div>

        <div class="content-box">
            <h3>Kết quả tìm kiếm</h3>
            
            <div id="emptyState" class="state-message state-empty">
                Không tìm thấy khách hàng nào phù hợp với điều kiện lọc.
            </div>
            <div id="errorState" class="state-message state-error">
                Đã xảy ra lỗi khi tải dữ liệu. Vui lòng thử lại.
            </div>

            <table id="customerTable">
                <thead>
                    <tr>
                        <th>Mã KH</th>
                        <th>Tên Khách hàng</th>
                        <th>Mã số thuế</th>
                        <th>Người liên hệ (SĐT)</th>
                        <th>Ngành nghề</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody id="tableBody">
                    <tr><td colspan="6" style="text-align: center;">Vui lòng nhập điều kiện và bấm Tìm kiếm.</td></tr>
                </tbody>
            </table>

            <!-- Yêu cầu: Phân trang -->
            <div class="pagination" id="pagination">
                <!-- Sẽ được tạo bằng JS -->
            </div>
        </div>
    </div>

    <script>
        const API_URL = '/qlkh/api/v1/customers/search';
        
        // Cấu trúc Dữ liệu Mock để Test UI
        const mockData = [
            { id: "KH001", name: "Công ty Cổ phần Alpha", taxCode: "0101234567", contactPhone: "0901234567", industry: "Công nghệ thông tin", status: "active" },
            { id: "KH002", name: "Công ty TNHH Beta", taxCode: "0307654321", contactPhone: "0987654321", industry: "Sản xuất", status: "inactive" }
        ];

        function handleSearch(event) {
            event.preventDefault();
            
            // Thu thập dữ liệu form để đồng bộ API
            const filterParams = {
                keyword: document.getElementById('keyword').value,
                phone: document.getElementById('phone').value,
                status: document.getElementById('status').value,
                industry: document.getElementById('industry').value,
                scale: document.getElementById('scale').value,
                region: document.getElementById('region').value,
                owner: document.getElementById('owner').value
            };
            
            console.log("Payload gửi đi:", filterParams);
            fetchData(filterParams);
        }

        function fetchData(params) {
            const tableBody = document.getElementById("tableBody");
            const emptyState = document.getElementById("emptyState");
            const errorState = document.getElementById("errorState");
            const customerTable = document.getElementById("customerTable");
            const pagination = document.getElementById("pagination");

            // Reset view
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Đang tìm kiếm...</td></tr>';
            emptyState.style.display = 'none';
            errorState.style.display = 'none';
            customerTable.style.display = 'table';
            pagination.innerHTML = '';

            // --- GIẢ LẬP GỌI API ---
            setTimeout(() => {
                // Để test trạng thái rỗng, đổi isTestEmpty = true
                const isTestEmpty = false; 
                // Để test trạng thái lỗi, đổi isTestError = true
                const isTestError = false;

                if (isTestError) {
                    customerTable.style.display = 'none';
                    errorState.style.display = 'block';
                    return;
                }

                if (isTestEmpty || mockData.length === 0) {
                    customerTable.style.display = 'none';
                    emptyState.style.display = 'block';
                    return;
                }

                renderTable(mockData);
                renderPagination(1, 5); // Giả lập đang ở trang 1, có tổng cộng 5 trang
            }, 500);
        }

        function renderTable(data) {
            const tableBody = document.getElementById("tableBody");
            tableBody.innerHTML = "";
            
            data.forEach(item => {
                const statusHtml = item.status === 'active' 
                    ? '<span class="status-badge status-active">Đang giao dịch</span>' 
                    : '<span class="status-badge status-inactive">Ngừng giao dịch</span>';

                const row = `<tr>
                    <td>${item.id}</td>
                    <td><strong>${item.name}</strong></td>
                    <td>${item.taxCode}</td>
                    <td>${item.contactPhone}</td>
                    <td>${item.industry}</td>
                    <td>${statusHtml}</td>
                </tr>`;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        }

        function renderPagination(currentPage, totalPages) {
            const pagination = document.getElementById("pagination");
            let html = '';
            for (let i = 1; i <= totalPages; i++) {
                html += `<div class="page-item ${i === currentPage ? 'active' : ''}">${i}</div>`;
            }
            pagination.innerHTML = html;
        }

        function clearFilter() {
            document.getElementById('filterForm').reset();
            document.getElementById('tableBody').innerHTML = '<tr><td colspan="6" style="text-align: center;">Vui lòng nhập điều kiện và bấm Tìm kiếm.</td></tr>';
            document.getElementById('pagination').innerHTML = '';
        }

        function saveFilter() {
            alert("Đã lưu bộ lọc hiện tại (Sẽ triển khai logic lưu vào LocalStorage hoặc Backend sau).");
        }
    </script>
</body>
</html>