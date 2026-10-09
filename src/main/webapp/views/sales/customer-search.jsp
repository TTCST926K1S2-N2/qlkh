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
                    <div class="form-group">
                        <label>Từ khóa (Tên/MST):</label>
                        <input type="text" id="keyword" placeholder="Nhập tên hoặc mã số thuế...">
                    </div>
                    <div class="form-group">
                        <label>SĐT người liên hệ:</label>
                        <input type="text" id="phone" placeholder="Nhập số điện thoại...">
                    </div>
                    <div class="form-group">
                        <label>Trạng thái:</label>
                        <select id="status">
                            <option value="">-- Tất cả --</option>
                            <option value="active">Đang giao dịch</option>
                            <option value="inactive">Ngừng giao dịch</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Ngành nghề:</label>
                        <select id="industry">
                            <option value="">-- Tất cả --</option>
                            <option value="it">Công nghệ thông tin</option>
                            <option value="manufacturing">Sản xuất</option>
                            <option value="retail">Bán lẻ</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Quy mô:</label>
                        <select id="scale">
                            <option value="">-- Tất cả --</option>
                            <option value="small">Doanh nghiệp Nhỏ</option>
                            <option value="medium">Doanh nghiệp Vừa</option>
                            <option value="large">Doanh nghiệp Lớn</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Khu vực:</label>
                        <select id="region">
                            <option value="">-- Tất cả --</option>
                            <option value="north">Miền Bắc</option>
                            <option value="central">Miền Trung</option>
                            <option value="south">Miền Nam</option>
                        </select>
                    </div>
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
                Đã xảy ra lỗi khi tải dữ liệu từ máy chủ. Vui lòng thử lại.
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

            <div class="pagination" id="pagination">
                <!-- Sẽ được tạo tự động bằng JS -->
            </div>
        </div>
    </div>

    <script>
        const API_URL = '/qlkh/api/v1/customers/search'; 

        // 1. Hàm gọi API tìm kiếm
        async function handleSearch(event, page = 1) {
            if (event) event.preventDefault();

            // Ẩn các thông báo trước khi tìm kiếm
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('errorState').style.display = 'none';
            document.getElementById('customerTable').style.display = 'table';

            // Thu thập dữ liệu từ TẤT CẢ form input
            const filterParams = {
                keyword: document.getElementById('keyword').value,
                phone: document.getElementById('phone').value,
                status: document.getElementById('status').value,
                industry: document.getElementById('industry').value,
                scale: document.getElementById('scale').value,
                region: document.getElementById('region').value,
                owner: document.getElementById('owner').value,
                page: page
            };

            // Lọc bỏ các tham số rỗng để URL sạch hơn
            const cleanedParams = Object.fromEntries(Object.entries(filterParams).filter(([_, v]) => v != ''));
            const queryString = new URLSearchParams(cleanedParams).toString();

            try {
                const response = await fetch(`${API_URL}?${queryString}`);
                if (!response.ok) throw new Error('Lỗi gọi API');
                
                const result = await response.json();
                const data = result.data || result.items || []; 
                
                if (data.length === 0) {
                    showEmptyState();
                } else {
                    renderTable(data);
                    if (result.totalPages) renderPagination(result.totalPages, page);
                }
            } catch (error) {
                console.error("Lỗi API:", error);
                showErrorState();
            }
        }

        // 2. Hàm vẽ bảng kết quả
        function renderTable(data) {
            const tbody = document.getElementById('tableBody');
            tbody.innerHTML = ''; // Xóa thông báo cũ
            
            data.forEach(item => {
                // Xử lý badge màu trạng thái
                const statusClass = item.status === 'active' ? 'status-active' : 'status-inactive';
                const statusText = item.status === 'active' ? 'Đang giao dịch' : 'Ngừng giao dịch';
                
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${item.customerCode || '---'}</td>
                    <td>${item.name || '---'}</td>
                    <td>${item.taxCode || '---'}</td>
                    <td>${item.contactName || ''} - ${item.phone || ''}</td>
                    <td>${item.industry || '---'}</td>
                    <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                `;
                tbody.appendChild(tr);
            });
        }

        // 3. Hàm vẽ phân trang
        function renderPagination(totalPages, currentPage) {
            const pagination = document.getElementById('pagination');
            pagination.innerHTML = ''; 
            
            for (let i = 1; i <= totalPages; i++) {
                const btn = document.createElement('div');
                btn.className = `page-item ${i === currentPage ? 'active' : ''}`;
                btn.innerText = i;
                btn.onclick = () => handleSearch(null, i); // Click đổi trang gọi lại API
                pagination.appendChild(btn);
            }
        }

        // 4. Lưu bộ lọc
        function saveFilter() {
            const filterParams = {
                keyword: document.getElementById('keyword').value,
                phone: document.getElementById('phone').value,
                status: document.getElementById('status').value,
                industry: document.getElementById('industry').value,
                scale: document.getElementById('scale').value,
                region: document.getElementById('region').value,
                owner: document.getElementById('owner').value
            };
            localStorage.setItem('savedCustomerFilters', JSON.stringify(filterParams));
            alert('Đã lưu bộ lọc thành công! Các thiết lập sẽ được giữ nguyên cho lần sau truy cập.');
        }

        // 5. Xóa bộ lọc và khôi phục giao diện mặc định
        function clearFilter() {
            document.getElementById('filterForm').reset();
            localStorage.removeItem('savedCustomerFilters');
            
            // Đưa bảng về trạng thái chờ
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('errorState').style.display = 'none';
            document.getElementById('customerTable').style.display = 'table';
            document.getElementById('tableBody').innerHTML = '<tr><td colspan="6" style="text-align: center;">Vui lòng nhập điều kiện và bấm Tìm kiếm.</td></tr>';
            document.getElementById('pagination').innerHTML = '';
        }

        // Các hàm phụ trợ hiển thị lỗi/trống
        function showEmptyState() {
            document.getElementById('customerTable').style.display = 'none';
            document.getElementById('emptyState').style.display = 'block'; 
            document.getElementById('pagination').innerHTML = '';
        }

        function showErrorState() {
            document.getElementById('customerTable').style.display = 'none';
            document.getElementById('errorState').style.display = 'block';
            document.getElementById('pagination').innerHTML = '';
        }

        // 6. Tự động load lại dữ liệu lọc đã lưu khi người dùng mở trang
        document.addEventListener('DOMContentLoaded', () => {
            const saved = localStorage.getItem('savedCustomerFilters');
            if (saved) {
                const filters = JSON.parse(saved);
                if (filters.keyword) document.getElementById('keyword').value = filters.keyword;
                if (filters.phone) document.getElementById('phone').value = filters.phone;
                if (filters.status) document.getElementById('status').value = filters.status;
                if (filters.industry) document.getElementById('industry').value = filters.industry;
                if (filters.scale) document.getElementById('scale').value = filters.scale;
                if (filters.region) document.getElementById('region').value = filters.region;
                if (filters.owner) document.getElementById('owner').value = filters.owner;
            }
        });
    </script>
</body>
</html>