<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Khách hàng cần chăm sóc - HỆ THỐNG QLKH</title>
    <style>
        body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background-color: #f0f3f6; font-family: 'Segoe UI', Arial, sans-serif; 
        }
        .main-content { padding: 40px; width: 100%; max-width: 1200px; box-sizing: border-box; }
        
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; 
            padding-bottom: 12px; margin-bottom: 20px; border-bottom: 2px solid #e1e8ed;
        }
        .page-header h2 { margin: 0; color: #1a2a3a; font-size: 24px; font-weight: 700; }
        
        .content-box { 
            background: #fff; padding: 25px; border-radius: 8px; 
            box-shadow: 0 4px 15px rgba(0,0,0,0.05); margin-bottom: 25px;
            border: 1px solid #e1e8ed; 
        }

        /* Cấu hình N ngày */
        .config-section {
            display: flex; align-items: center; gap: 15px; padding: 15px;
            background-color: #f8fafd; border-radius: 6px; border-left: 4px solid #f39c12;
            margin-bottom: 20px;
        }
        .config-section label { font-weight: 600; color: #2c3e50; font-size: 14px; }
        .config-section input {
            padding: 8px 12px; border: 1px solid #bdc3c7; border-radius: 4px; 
            width: 80px; font-size: 14px; outline: none; text-align: center;
        }
        .config-section input:focus { border-color: #f39c12; }
        
        .btn { padding: 9px 16px; border-radius: 4px; border: none; font-weight: 600; cursor: pointer; transition: 0.2s; font-size: 13px;}
        .btn-warning { background-color: #f39c12; color: white; }
        .btn-warning:hover { background-color: #e67e22; }
        .btn-success { background-color: #27ae60; color: white; }
        .btn-success:hover { background-color: #2ecc71; }

        /* Bảng dữ liệu */
        table { width: 100%; border-collapse: collapse; overflow: hidden; border-radius: 6px; }
        th, td { padding: 12px 15px; text-align: left; vertical-align: middle; }
        th { background-color: #1f3a60; color: #ffffff; font-weight: 600; font-size: 13px; text-transform: uppercase; }
        tr { border-bottom: 1px solid #eef2f5; }
        tbody tr:nth-child(even) { background-color: #fcfcfc; }
        tbody tr:hover { background-color: #f1f5f9; }
        
        td { color: #3c4a57; font-size: 14px; }
        .contract-value { font-weight: 700; color: #e74c3c; }
        .customer-link { color: #2980b9; font-weight: 600; text-decoration: none; }
        .customer-link:hover { text-decoration: underline; }
        
        /* Cảnh báo */
        .alert-badge {
            display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;
            background-color: #fdf2f2; color: #e74c3c; border: 1px solid #fadbd8;
        }
        .warning-text { color: #e67e22; font-size: 13px; font-weight: 600; }

        /* Trạng thái */
        .state-message { text-align: center; padding: 30px; font-size: 14px; border-radius: 8px; font-weight: 500; display: none; margin-bottom: 20px;}
        .state-empty { background-color: #fef9e7; color: #b7950b; border: 1px solid #f9e79f; }
        .state-error { background-color: #fdf2f2; color: #ec7063; border: 1px solid #fadbd8; }
    </style>
</head>
<body>
    <div class="main-content">
        <div class="page-header">
            <h2>Khách hàng cần chăm sóc định kỳ</h2>
        </div>

        <div class="content-box">
            <!-- Cấu hình N ngày -->
            <div class="config-section">
                <label for="nDays">Hiển thị khách hàng chưa tương tác trong:</label>
                <input type="number" id="nDays" value="30" min="1">
                <label>ngày</label>
                <button class="btn btn-warning" onclick="saveConfig()">Lưu cấu hình</button>
            </div>

            <!-- Các thông báo trạng thái -->
            <div id="emptyState" class="state-message state-empty">
                Tuyệt vời! Không có khách hàng nào đang bị bỏ quên quá hạn.
            </div>
            
            <div id="errorState" class="state-message state-error">
                Đã xảy ra lỗi khi tải dữ liệu từ hệ thống. Vui lòng thử lại sau.
            </div>

            <!-- Bảng dữ liệu -->
            <table id="careTable">
                <thead>
                    <tr>
                        <th>Tên Khách hàng</th>
                        <th>Giá trị hợp đồng</th>
                        <th>Thời gian bỏ quên</th>
                        <th>Tiêu chí cảnh báo khác</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody id="tableBody">
                    <!-- Dữ liệu render bằng JS -->
                </tbody>
            </table>
        </div>
    </div>

    <script>
    // 1. Khai báo API
    const GET_CUSTOMERS_API = '/api/v1/customer-care';
    const UPDATE_CONTACT_API = '/api/v1/customer-care/interact'; 

    // 2. Hàm tải danh sách khách hàng cần chăm sóc
    async function fetchCustomers() {
        // Ẩn toàn bộ để reset trạng thái trước khi fetch
        document.getElementById('careTable').style.display = 'none';
        document.getElementById('emptyState').style.display = 'none';
        document.getElementById('errorState').style.display = 'none';

        // Lấy cấu hình N ngày (khớp với id HTML)
        const nDays = localStorage.getItem('careDaysConfig') || 30;
        
        try {
            // Đã bổ sung tham số sắp xếp hợp đồng giảm dần: sort=contractValue,desc
            const response = await fetch(`${GET_CUSTOMERS_API}?days=${nDays}&sort=contractValue,desc`);
            if (!response.ok) throw new Error('Lỗi tải dữ liệu');
            
            const result = await response.json();
            const data = result.data || result.items || [];
            
            // Xử lý logic hiển thị
            if (data.length === 0) {
                document.getElementById('emptyState').style.display = 'block';
            } else {
                document.getElementById('careTable').style.display = 'table';
                renderTable(data);
            }
        } catch (error) {
            console.error('Lỗi API:', error);
            document.getElementById('errorState').style.display = 'block';
        }
    }

    // 3. Hàm lưu cấu hình N ngày
    function saveConfig() {
        const nDaysInput = document.getElementById('nDays').value;
        if (!nDaysInput || isNaN(nDaysInput) || nDaysInput <= 0) {
            alert("Vui lòng nhập số ngày hợp lệ!");
            return;
        }
        
        // Lưu xuống localStorage
        localStorage.setItem('careDaysConfig', nDaysInput);
        alert(`Đã lưu cấu hình cảnh báo: ${nDaysInput} ngày.`);
        
        // Load lại danh sách với số ngày mới
        fetchCustomers();
    }

    // 4. Hàm xử lý nút "Đã liên hệ"
    async function markAsContacted(customerId) {
        if (!confirm('Xác nhận đã liên hệ với khách hàng này?')) return;

        try {
            const response = await fetch(`${UPDATE_CONTACT_API}/${customerId}`, {
                method: 'POST', 
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: 'contacted' }) 
            });

            if (!response.ok) throw new Error('Lỗi cập nhật');
            
            alert('Cập nhật trạng thái thành công!');
            // Reload lại để ẩn khách hàng vừa chăm sóc khỏi danh sách
            fetchCustomers(); 
            
        } catch (error) {
            console.error('Lỗi:', error);
            alert('Có lỗi xảy ra khi cập nhật trạng thái.');
        }
    }

    // 5. Hàm vẽ bảng
    function renderTable(data) {
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        
        data.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <a href="/customers/detail?id=${item.id}" class="customer-link">${item.name || '---'}</a>
                </td>
                <td class="contract-value">${item.contractValue || '---'}</td>
                <td class="warning-text">${item.daysWithoutInteraction || 0} ngày</td>
                <td><span class="alert-badge">${item.warningCriteria || 'Quá hạn tương tác'}</span></td>
                <td><button class="btn btn-success" onclick="markAsContacted('${item.id}')">Đã liên hệ</button></td>
            `;
            tbody.appendChild(tr);
        });
    }

    // 6. Khởi chạy khi load trang
    document.addEventListener('DOMContentLoaded', () => {
        // Khôi phục giá trị input trên giao diện
        const savedDays = localStorage.getItem('careDaysConfig');
        if (savedDays && document.getElementById('nDays')) {
            document.getElementById('nDays').value = savedDays;
        }
        // Gọi API lần đầu tiên
        fetchCustomers();
    });
    </script>
</body>
</html>