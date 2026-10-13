<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cấu hình Quy tắc Phân bổ Lead - QLKH</title>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --secondary: #ec4899;
            --success: #10b981;
            --danger: #ef4444;
            --bg-gradient: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
            --text-main: #0f172a;
            --text-muted: #475569;
            --border: #e2e8f0;
            --card-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01);
            --card-shadow-hover: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 10px 10px -6px rgba(0, 0, 0, 0.03);
        }

        body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background: var(--bg-gradient); 
            font-family: 'Segoe UI', system-ui, -apple-system, BlinkMacSystemFont, sans-serif; 
            min-height: 100vh; color: var(--text-main);
        }
        .main-content { padding: 40px; width: 100%; max-width: 1200px; box-sizing: border-box; }
        
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; 
        }
        .page-header h2 { 
            margin: 0; font-size: 32px; font-weight: 850; letter-spacing: -0.025em;
            background: linear-gradient(90deg, var(--primary), var(--secondary));
            -webkit-background-clip: text; -webkit-text-fill-color: transparent;
            display: flex; align-items: center; gap: 12px;
            text-shadow: 0px 2px 10px rgba(79, 70, 229, 0.1);
        }
        .page-header h2::before {
            content: "⚙️"; font-size: 28px;
        }

        /* Buttons nâng cấp */
        .btn {
            padding: 12px 24px; border-radius: 10px; font-weight: 600; font-size: 14.5px;
            cursor: pointer; transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); border: none; 
            display: inline-flex; align-items: center; gap: 8px;
        }
        .btn-primary { 
            background: linear-gradient(95deg, var(--primary), #6366f1); 
            color: white; 
            box-shadow: 0 4px 14px rgba(79, 70, 229, 0.3); 
        }
        .btn-primary:hover { 
            background: linear-gradient(95deg, var(--primary-hover), var(--primary));
            transform: translateY(-2px); 
            box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4); 
        }
        .btn-primary:active { transform: translateY(0); }
        
        .btn-outline { background: white; border: 1.5px solid var(--border); color: var(--text-main); }
        .btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; transform: translateY(-1px); }

        /* Khối danh sách Table Box */
        .content-box { 
            background: #ffffff; padding: 32px; border-radius: 20px; 
            box-shadow: var(--card-shadow); border: 1px solid rgba(226, 232, 240, 0.8);
            position: relative; overflow: hidden;
            transition: all 0.3s ease;
        }
        .content-box::before {
            content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 4px;
            background: linear-gradient(90deg, var(--primary), var(--secondary));
        }
        
        table { width: 100%; border-collapse: separate; border-spacing: 0 10px; margin-top: 8px; }
        th { 
            padding: 12px 20px; text-align: left; color: #64748b; 
            font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; border: none;
        }
        td { 
            padding: 18px 20px; background: #fff; 
            border-top: 1.5px solid #f1f5f9; border-bottom: 1.5px solid #f1f5f9; 
            font-size: 14.5px; transition: all 0.25s ease;
            color: var(--text-muted);
        }
        td:first-child { 
            border-left: 1.5px solid #f1f5f9; border-top-left-radius: 12px; border-bottom-left-radius: 12px; 
            font-weight: 700; color: var(--text-main);
        }
        td:last-child { border-right: 1.5px solid #f1f5f9; border-radius: 0 12px 12px 0; }
        
        /* Hiệu ứng 3D nhẹ khi di chuột qua hàng */
        tbody tr { position: relative; transition: all 0.25s ease; }
        tbody tr:hover td { 
            background-color: #f8fafc; 
            border-color: #cbd5e1;
            color: var(--text-main);
            transform: translateY(-1px);
        }
        tbody tr:hover td:first-child {
            border-left: 4px solid var(--primary);
        }
        tbody tr:hover {
            box-shadow: 0 4px 10px rgba(0,0,0,0.03);
        }

        .priority-badge {
            background: #e0e7ff; color: #3730a3; padding: 4px 10px; border-radius: 8px; font-weight: 700; font-size: 13px;
            border: 1px solid #c7d2fe; display: inline-block; min-width: 24px; text-align: center;
        }

        /* Badge Trạng thái mượt mà */
        .badge { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 9999px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;}
        .badge-active { background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; }
        .badge-active::before { content: ''; display: block; width: 6px; height: 6px; border-radius: 50%; background: #16a34a; }
        .badge-inactive { background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }
        .badge-inactive::before { content: ''; display: block; width: 6px; height: 6px; border-radius: 50%; background: #64748b; }

        /* Action Links style lại */
        .action-link { 
            color: var(--primary); text-decoration: none; font-weight: 700; margin-right: 12px; cursor: pointer;
            padding: 6px 12px; border-radius: 6px; background: #f5f3ff; transition: all 0.2s;
            border: 1px solid #e0e7ff;
        }
        .action-link:hover { 
            background: var(--primary); color: white; border-color: var(--primary);
            box-shadow: 0 2px 6px rgba(79,70,229,0.2);
        }

        /* Modal Overlay & Card hiện đại */
        .modal-overlay {
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(15, 23, 42, 0.4); backdrop-filter: blur(8px);
            display: none; justify-content: center; align-items: center; z-index: 1000;
            transition: all 0.3s ease;
        }
        .modal-content {
            background: white; padding: 36px; border-radius: 20px; width: 100%; max-width: 600px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
            border: 1px solid rgba(255, 255, 255, 0.8);
            transform: scale(0.95); transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        .modal-header { 
            font-size: 22px; font-weight: 800; margin-bottom: 24px; color: var(--text-main);
            border-bottom: 2px solid var(--border); padding-bottom: 12px;
            display: flex; align-items: center; gap: 8px;
        }
        
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 28px; }
        .form-group { display: flex; flex-direction: column; gap: 8px; }
        .form-group.full-width { grid-column: span 2; }
        .form-group label { font-size: 13.5px; font-weight: 600; color: var(--text-muted); display: flex; align-items: center; gap: 6px;}
        .form-control {
            padding: 12px 16px; border: 1.5px solid var(--border); border-radius: 10px;
            font-size: 14.5px; outline: none; transition: 0.2s; background: #f8fafc;
            color: var(--text-main);
        }
        .form-control:focus { 
            border-color: var(--primary); background: white;
            box-shadow: 0 0 0 4px rgba(79,70,229,0.1); 
        }
        
        .error-msg { color: var(--danger); font-size: 12px; margin-top: 4px; display: none; font-weight: 500;}
        .modal-actions { display: flex; justify-content: flex-end; gap: 12px; border-top: 1.5px solid var(--border); padding-top: 20px;}

        /* States */
        .state-empty { text-align: center; padding: 48px; color: #b45309; background: #fffbeb; border-radius: 12px; border: 1px dashed #fcd34d; display: none; font-weight: 600;}
    </style>
</head>
<body>

    <div class="main-content">
        <div class="page-header">
            <h2>Cấu hình Quy tắc Phân bổ Lead</h2>
            <button class="btn btn-primary" onclick="openModal()">
                <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" /></svg>
                Thêm quy tắc mới
            </button>
        </div>

        <div class="content-box">
            <div id="emptyState" class="state-empty">Chưa có quy tắc phân bổ nào được thiết lập.</div>
            
            <table id="rulesTable">
                <thead>
                    <tr>
                        <th style="width: 100px; text-align: center;">Ưu tiên</th>
                        <th>Tên quy tắc</th>
                        <th>Điều kiện (Khu vực / Ngành)</th>
                        <th>Phương thức phân bổ</th>
                        <th>Trạng thái</th>
                        <th style="text-align: right; width: 100px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody id="tableBody">
                    <!-- Dữ liệu render tại đây -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- Modal Thêm/Sửa Quy tắc -->
    <div class="modal-overlay" id="ruleModal">
        <div class="modal-content" id="modalCard">
            <div class="modal-header" id="modalTitle">✍️ Thêm quy tắc phân bổ mới</div>
            <div class="form-grid">
                <div class="form-group full-width">
                    <label>📝 Tên quy tắc <span style="color:red">*</span></label>
                    <input type="text" id="ruleName" class="form-control" placeholder="VD: Phân bổ khách hàng miền Bắc">
                    <div class="error-msg" id="nameError">Vui lòng nhập tên quy tắc.</div>
                </div>
                <div class="form-group">
                    <label>🔍 Tiêu chí điều kiện</label>
                    <select id="ruleConditionType" class="form-control">
                        <option value="Khu vực">Khu vực</option>
                        <option value="Ngành nghề">Ngành nghề</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>📍 Giá trị điều kiện</label>
                    <input type="text" id="ruleConditionValue" class="form-control" placeholder="VD: Miền Bắc, Bất động sản...">
                </div>
                <div class="form-group full-width">
                    <label>🔄 Phương thức phân bổ</label>
                    <select id="ruleMethod" class="form-control">
                        <option value="Xoay vòng (Round-Robin)">Xoay vòng đều trong nhóm (Round-Robin)</option>
                        <option value="Chỉ định trực tiếp">Chỉ định trực tiếp cho nhân viên</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>🔢 Mức độ ưu tiên (Số nhỏ ưu tiên cao)</label>
                    <input type="number" id="rulePriority" class="form-control" value="1" min="1">
                </div>
                <div class="form-group">
                    <label>🚦 Trạng thái</label>
                    <select id="ruleStatus" class="form-control">
                        <option value="Hoạt động">Hoạt động</option>
                        <option value="Vô hiệu hóa">Vô hiệu hóa</option>
                    </select>
                </div>
            </div>
            <div class="modal-actions">
                <button class="btn btn-outline" onclick="closeModal()">Hủy</button>
                <button class="btn btn-primary" onclick="saveRule()">Lưu quy tắc</button>
            </div>
        </div>
    </div>

    <script>
        const API_URL = '/api/v1/lead-assignment-rules';
        
        function fetchRules() {
            document.getElementById('tableBody').innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">Đang tải dữ liệu...</td></tr>';
            
            fetch(API_URL)
                .then(res => res.json())
                .then(data => renderTable(data))
                .catch(err => console.error("Lỗi API:", err));
        }

        function renderTable(rules) {
            const tbody = document.getElementById('tableBody');
            tbody.innerHTML = '';
            
            if(!rules || rules.length === 0) {
                document.getElementById('rulesTable').style.display = 'none';
                document.getElementById('emptyState').style.display = 'block';
                return;
            }
            
            document.getElementById('rulesTable').style.display = 'table';
            document.getElementById('emptyState').style.display = 'none';

            rules.sort((a, b) => a.priority - b.priority).forEach(rule => {
                const statusBadge = rule.status === 'Hoạt động' ? 'badge-active' : 'badge-inactive';
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="text-align: center;"><span class="priority-badge">#${rule.priority}</span></td>
                    <td><strong style="color: var(--text-main); font-weight: 700;">${rule.name}</strong></td>
                    <td><strong>${rule.conditionType}:</strong> ${rule.conditionValue}</td>
                    <td>${rule.method}</td>
                    <td><span class="badge ${statusBadge}">${rule.status}</span></td>
                    <td style="text-align: right;">
                        <span class="action-link" onclick="editRule(${rule.id})">Sửa</span>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }

        function openModal() {
            const overlay = document.getElementById('ruleModal');
            const card = document.getElementById('modalCard');
            overlay.style.display = 'flex';
            setTimeout(() => {
                card.style.transform = 'scale(1)';
            }, 50);
            document.getElementById('nameError').style.display = 'none';
        }
        
        function closeModal() {
            const overlay = document.getElementById('ruleModal');
            const card = document.getElementById('modalCard');
            card.style.transform = 'scale(0.95)';
            setTimeout(() => {
                overlay.style.display = 'none';
            }, 150);
        }

        function saveRule() {
            const nameInput = document.getElementById('ruleName').value.trim();
            if(!nameInput) {
                document.getElementById('nameError').style.display = 'block';
                return;
            }
            alert("Đã gửi dữ liệu lưu quy tắc thành công!");
            closeModal();
            fetchRules();
        }

        function editRule(id) {
            document.getElementById('modalTitle').innerText = 'Chỉnh sửa quy tắc';
            openModal();
        }

        document.addEventListener('DOMContentLoaded', fetchRules);
    </script>
</body>
</html>