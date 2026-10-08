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
        .customer-link { color: #2980b9; font-weight: 600; text-decoration: none; }
        .customer-link:hover { text-decoration: underline; }
        .contract-value { font-weight: 700; color: #e74c3c; }
        
        /* Cảnh báo */
        .alert-badge {
            display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;
            background-color: #fdf2f2; color: #e74c3c; border: 1px solid #fadbd8;
        }
        .warning-text { color: #e67e22; font-size: 12px; font-weight: 500; }

        /* Trạng thái */
        .state-message { text-align: center; padding: 30px; font-size: 14px; border-radius: 8px; font-weight: 500; display: none; }
        .state-empty { background-color: #fef9e7; color: #b7950b; border: 1px solid #f9e79f; }
    </style>
</head>
<body>
    <div class="main-content">
        <div class="page-header">
            <h2>Khách hàng cần chăm sóc định kỳ</h2>
        </div>

        <!-- Cấu hình N ngày (Hiển thị nếu có quyền) -->
        <div class="content-box">
            <div class="config-section">
                <label for="nDays">Hiển thị khách hàng chưa tương tác trong:</label>
                <input type="number" id="nDays" value="30" min="1">
                <label>ngày</label>
                <button class="btn btn-warning" onclick="saveConfig()">Lưu cấu hình</button>
            </div>

            <div id="emptyState" class="state-message state-empty">
                Tuyệt vời! Không có khách hàng nào đang bị bỏ quên quá hạn.
            </div>

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
        // Mock API Data: Đã được sắp xếp theo Giá trị hợp đồng giảm dần từ Backend
        let mockData = [
            { id: "KH001", name: "Công ty Cổ phần Alpha", contractValue: 1500000000, daysInactive: 45, extraWarning: "Sắp đến hạn gia hạn hợp đồng" },
            { id: "KH002", name: "Công ty TNHH Beta", contractValue: 850000000, daysInactive: 32, extraWarning: "Chưa phản hồi báo giá mới" },
            { id: "KH003", name: "Tập đoàn Gamma", contractValue: 420000000, daysInactive: 60, extraWarning: "Khách VIP rủi ro rời bỏ" }
        ];

        function formatCurrency(value) {
            return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
        }

        function renderTable() {
            const tableBody = document.getElementById("tableBody");
            const careTable = document.getElementById("careTable");
            const emptyState = document.getElementById("emptyState");
            
            tableBody.innerHTML = "";

            if (mockData.length === 0) {
                careTable.style.display = 'none';
                emptyState.style.display = 'block';
                return;
            }

            careTable.style.display = 'table';
            emptyState.style.display = 'none';

            mockData.forEach(item => {
                const row = `<tr id="row-${item.id}">
                    <td><a href="/qlkh/customers/profile?id=${item.id}" class="customer-link">${item.name}</a></td>
                    <td class="contract-value">${formatCurrency(item.contractValue)}</td>
                    <td><span class="alert-badge">${item.daysInactive} ngày</span></td>
                    <td class="warning-text">${item.extraWarning || '---'}</td>
                    <td>
                        <button class="btn btn-success" onclick="markAsContacted('${item.id}')">Đã liên hệ</button>
                    </td>
                </tr>`;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        }

        function saveConfig() {
            const days = document.getElementById('nDays').value;
            if (days < 1) {
                alert("Số ngày cấu hình phải lớn hơn 0");
                return;
            }
            alert(`Đã lưu cấu hình: Cảnh báo khách hàng chưa tương tác quá ${days} ngày.`);
            // Sẽ gọi API fetch lại danh sách dựa trên N ngày mới tại đây
        }

        function markAsContacted(customerId) {
            if(confirm("Xác nhận bạn đã liên hệ và chăm sóc khách hàng này?")) {
                // Xóa khách hàng khỏi mảng mockData (mô phỏng việc đã xử lý)
                mockData = mockData.filter(item => item.id !== customerId);
                renderTable();
                
                // Sau này sẽ gọi API cập nhật trạng thái tương tác mới nhất tại đây
                console.log(`Đã cập nhật tương tác cho KH: ${customerId}`);
            }
        }

        // Tự động load dữ liệu khi mở trang
        document.addEventListener("DOMContentLoaded", renderTable);
    </script>
</body>
</html>