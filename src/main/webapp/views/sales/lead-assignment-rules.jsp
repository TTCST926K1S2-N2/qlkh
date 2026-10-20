<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
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
        .state-error { text-align: center; padding: 48px; color: #b91c1c; background: #fef2f2; border-radius: 12px; border: 1px dashed #fecaca; display: none; font-weight: 600;}
        /* S406-135 layout */
        body {
            justify-content: flex-start;
            align-items: stretch;
            width: 100%;
            overflow-x: hidden;
        }

        .main-content {
            flex: 1 1 0;
            min-width: 0;
            max-width: none;
            width: auto;
            padding: 32px;
            margin: 0;
        }

        .page-header {
            gap: 20px;
            flex-wrap: wrap;
        }

        .page-header h2 {
            font-size: clamp(24px, 2vw, 32px);
        }

        .content-box {
            width: 100%;
            box-sizing: border-box;
            overflow-x: auto;
        }

        #rulesTable {
            width: 100%;
            min-width: 850px;
        }

        #rulesTable th,
        #rulesTable td {
            padding-left: 12px;
            padding-right: 12px;
        }

        .modal-content {
            box-sizing: border-box;
            max-width: min(600px, calc(100vw - 32px));
            max-height: calc(100vh - 32px);
            overflow-y: auto;
        }

        @media (max-width: 900px) {
            .main-content {
                padding: 20px;
            }

            .content-box {
                padding: 20px;
            }
        }

        @media (max-width: 600px) {
            .main-content {
                padding: 16px;
            }

            .page-header {
                align-items: flex-start;
            }

            .form-grid {
                grid-template-columns: 1fr;
            }

            .form-group.full-width {
                grid-column: auto;
            }

            .modal-content {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <div class="page-header">
            <h2>Cấu hình Quy tắc Phân bổ Lead</h2>
            <button class="btn btn-primary" onclick="openModal()">
                <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" /></svg>
                Thêm quy tắc mới
            </button>
        </div>

        <div class="content-box">
            <!-- Thêm state báo lỗi API -->
            <div id="errorState" class="state-error">Đã xảy ra lỗi khi kết nối với hệ thống. Vui lòng tải lại trang!</div>
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
                    <tr><td colspan="6" style="text-align: center; color: var(--text-muted);">Đang tải dữ liệu...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Modal Thêm/Sửa Quy tắc -->
    <div class="modal-overlay" id="ruleModal">
        <div class="modal-content" id="modalCard">
            <div class="modal-header" id="modalTitle">️ Thêm quy tắc phân bổ mới</div>
            <div class="form-grid">
                <!-- Thêm trường ẩn lưu ID khi sửa -->
                <input type="hidden" id="ruleId">

                <div class="form-group full-width">
                    <label> Tên quy tắc <span style="color:red">*</span></label>
                    <input type="text" id="ruleName" class="form-control" placeholder="VD: Phân bổ khách hàng miền Bắc">
                    <div class="error-msg" id="nameError">Vui lòng nhập tên quy tắc.</div>
                </div>
                <div class="form-group">
                    <label> Tiêu chí điều kiện</label>
                    <select id="ruleConditionType" class="form-control">
                        <option value="REGION">Khu vực</option>
                        <option value="SOURCE">Nguồn Lead</option>
                        <option value="INDUSTRY">Ngành nghề</option>
                    </select>
                </div>
                <div class="form-group">
                    <label> Giá trị điều kiện</label>
                    <input type="text" id="ruleConditionValue" class="form-control" placeholder="VD: Miền Bắc, Bất động sản...">
                </div>
                <div class="form-group full-width">
                    <label>ID nhân viên nhận Lead</label>
                    <input type="number" id="assignToUserId" class="form-control" min="1" step="1" placeholder="Nhập ID nhân viên">
                </div>
                <div class="form-group full-width">
                    <label> Phương thức phân bổ</label>
                    <select id="ruleMethod" class="form-control">
                        <option value="ROUND_ROBIN" disabled>Xoay vòng đều trong nhóm (Round-Robin)</option>
                        <option value="DIRECT" selected>Chỉ định trực tiếp cho nhân viên</option>
                    </select>
                </div>
                <div class="form-group">
                    <label> Mức độ ưu tiên (Số nhỏ ưu tiên cao)</label>
                    <input type="number" id="rulePriority" class="form-control" value="1" min="1">
                </div>
                <div class="form-group">
                    <label> Trạng thái</label>
                    <select id="ruleStatus" class="form-control">
                        <option value="Hoạt động">Hoạt động</option>
                        <option value="Vô hiệu hóa">Vô hiệu hóa</option>
                    </select>
                </div>
            </div>

            <!-- Thêm thông báo lỗi lưu modal -->
            <div id="saveErrorMsg" class="error-msg" style="text-align: center; margin-bottom: 15px; font-size: 14px;"></div>

            <div class="modal-actions">
                <button class="btn btn-outline" onclick="closeModal()">Hủy</button>
                <button class="btn btn-primary" id="btnSaveRule" onclick="saveRule()">Lưu quy tắc</button>
            </div>
        </div>
    </div>

    <script>
        // Sửa 1: Cập nhật đúng đường dẫn API (Thêm context path /qlkh)
        const API_URL = "<%= request.getContextPath() %>/api/assignment-rules";

        async function readApiJson(response) {
            const type = response.headers.get('content-type') || '';

            if (!type.toLowerCase().includes('application/json')) {
                if (response.redirected || response.url.includes('/login')) {
                    throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                }
                throw new Error('API phân bổ Lead chưa sẵn sàng hoặc máy chủ trả về HTML.');
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Yêu cầu API thất bại.');
            }

            return data;
        }
        function fetchRules() {
            document.getElementById('rulesTable').style.display = 'table';
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('errorState').style.display = 'none';

            // Render text tĩnh báo đang tải
            const tbody = document.getElementById('tableBody');
            tbody.innerHTML = '';
            const loadingTr = document.createElement('tr');
            const loadingTd = document.createElement('td');
            loadingTd.colSpan = 6;
            loadingTd.style.textAlign = 'center';
            loadingTd.style.color = 'var(--text-muted)';
            loadingTd.textContent = 'Đang tải dữ liệu...';
            loadingTr.appendChild(loadingTd);
            tbody.appendChild(loadingTr);

            fetch(API_URL)
                .then(res => readApiJson(res))
                .then(data => {
                    if (!data || !Array.isArray(data.rules)) {
                        throw new Error('Backend chưa trả danh sách quy tắc.');
                    }
                    renderTable(data.rules);
                })
                .catch(err => {
                    console.error("Lỗi API:", err);
                    document.getElementById('rulesTable').style.display = 'none';
                    document.getElementById('errorState').textContent = err.message || 'Không thể tải dữ liệu.';
                    document.getElementById('errorState').style.display = 'block';
                });
        }

        // Sửa 3: Xử lý hiển thị an toàn chống XSS
        function renderTable(rules) {
            const tbody = document.getElementById('tableBody');
            tbody.innerHTML = ''; // Làm sạch bảng

            // Sửa 4: Xử lý ngoại lệ trường hợp không có dữ liệu
            if(!rules || rules.length === 0) {
                document.getElementById('rulesTable').style.display = 'none';
                document.getElementById('emptyState').style.display = 'block';
                return;
            }

            document.getElementById('rulesTable').style.display = 'table';
            document.getElementById('emptyState').style.display = 'none';

            // Sắp xếp theo ưu tiên
            rules.sort((a, b) => a.priority - b.priority).forEach(rule => {
                const tr = document.createElement('tr');

                // Cột Ưu tiên
                const tdPriority = document.createElement('td');
                tdPriority.style.textAlign = 'center';
                const spanPriority = document.createElement('span');
                spanPriority.className = 'priority-badge';
                spanPriority.textContent = `#${rule.priority || 0}`;
                tdPriority.appendChild(spanPriority);

                // Cột Tên
                const tdName = document.createElement('td');
                const strongName = document.createElement('strong');
                strongName.style.color = 'var(--text-main)';
                strongName.style.fontWeight = '700';
                strongName.textContent = rule.name || '---';
                tdName.appendChild(strongName);

                // Cột Điều kiện
                const tdCondition = document.createElement('td');
                const strongCond = document.createElement('strong');
                strongCond.textContent = `${rule.conditionType || '---'}: `;
                tdCondition.appendChild(strongCond);
                tdCondition.appendChild(document.createTextNode(rule.conditionValue || '---'));

                // Cột Phương thức
                const tdMethod = document.createElement('td');
                tdMethod.textContent = rule.assignmentMethod === 'ROUND_ROBIN' ? 'Xoay vòng' : 'Chỉ định trực tiếp';

                // Cột Trạng thái
                const tdStatus = document.createElement('td');
                const spanStatus = document.createElement('span');
                const statusBadge = rule.active === true ? 'badge-active' : 'badge-inactive';
                spanStatus.className = `badge ${statusBadge}`;
                spanStatus.textContent = rule.active === true ? 'Hoạt động' : 'Vô hiệu hóa';
                tdStatus.appendChild(spanStatus);

                // Cột Thao tác
                const tdAction = document.createElement('td');
                tdAction.style.textAlign = 'right';
                const spanAction = document.createElement('span');
                spanAction.className = 'action-link';
                spanAction.textContent = 'Sửa';
                // Gắn sự kiện click an toàn
                spanAction.onclick = () => editRule(rule);
                tdAction.appendChild(spanAction);

                const deleteAction = document.createElement('span');
                deleteAction.className = 'action-link';
                deleteAction.textContent = 'Xóa';
                deleteAction.style.marginLeft = '12px';
                deleteAction.style.color = '#dc2626';
                deleteAction.onclick = () => deleteRule(rule);
                tdAction.appendChild(deleteAction);

                // Gắn các cột vào hàng
                tr.appendChild(tdPriority);
                tr.appendChild(tdName);
                tr.appendChild(tdCondition);
                tr.appendChild(tdMethod);
                tr.appendChild(tdStatus);
                tr.appendChild(tdAction);

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
            document.getElementById('saveErrorMsg').style.display = 'none';
        }

        function closeModal() {
            const overlay = document.getElementById('ruleModal');
            const card = document.getElementById('modalCard');
            card.style.transform = 'scale(0.95)';
            setTimeout(() => {
                overlay.style.display = 'none';
                resetForm();
            }, 150);
        }

        function resetForm() {
            document.getElementById('ruleId').value = '';
            document.getElementById('assignToUserId').value = '';
            document.getElementById('ruleName').value = '';
            document.getElementById('ruleConditionType').value = 'REGION';
            document.getElementById('ruleConditionValue').value = '';
            document.getElementById('ruleMethod').value = 'DIRECT';
            document.getElementById('rulePriority').value = '1';
            document.getElementById('ruleStatus').value = 'Hoạt động';
            document.getElementById('modalTitle').innerText = '️ Thêm quy tắc phân bổ mới';
        }

        // Sửa 2: Đảm bảo cấu trúc JSON khi lưu (thêm/sửa)
        function saveRule() {
            const name = document.getElementById('ruleName').value.trim();
            const conditionType = document.getElementById('ruleConditionType').value;
            const conditionValue = document.getElementById('ruleConditionValue').value.trim();
            const assignToUserId = document.getElementById('assignToUserId').value.trim();
            const ruleId = document.getElementById('ruleId').value;
            const errorDiv = document.getElementById('saveErrorMsg');

            errorDiv.style.display = 'none';
            document.getElementById('nameError').style.display = 'none';

            if (!name) {
                document.getElementById('nameError').style.display = 'block';
                return;
            }

            if (!conditionValue || !/^[1-9]\d*$/.test(assignToUserId)) {
                errorDiv.textContent = 'Vui lòng nhập giá trị điều kiện và ID nhân viên hợp lệ.';
                errorDiv.style.display = 'block';
                return;
            }

            const btnSave = document.getElementById('btnSaveRule');
            btnSave.disabled = true;
            btnSave.textContent = 'Đang xử lý...';

            const params = new URLSearchParams({
                name,
                conditionType,
                conditionValue,
                assignToUserId,
                priority: document.getElementById('rulePriority').value || '1',
                active: document.getElementById('ruleStatus').value === 'Hoạt động' ? 'true' : 'false',
                assignmentMethod: document.getElementById('ruleMethod').value || 'DIRECT'
            });

            if (ruleId) {
                params.set('id', ruleId);
            }

            fetch(API_URL, {
                method: ruleId ? 'PUT' : 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'Accept': 'application/json'
                },
                credentials: 'same-origin',
                body: params.toString()
            })
            .then(async response => {
                const data = await readApiJson(response);
                if (!response.ok || data.status !== 'success') {
                    throw new Error(data.message || 'Không thể lưu quy tắc.');
                }
                return data;
            })
            .then(() => {
                alert(ruleId ? 'Cập nhật quy tắc thành công!' : 'Tạo quy tắc thành công!');
                closeModal();
                fetchRules();
            })
            .catch(error => {
                errorDiv.textContent = error.message || 'Lỗi hệ thống.';
                errorDiv.style.display = 'block';
            })
            .finally(() => {
                btnSave.disabled = false;
                btnSave.textContent = 'Lưu quy tắc';
            });
        }
        function editRule(rule) {
            document.getElementById('modalTitle').innerText = 'Chỉnh sửa quy tắc';
            document.getElementById('ruleId').value = rule.id;
            document.getElementById('assignToUserId').value = rule.assignToUserId || '';
            document.getElementById('ruleName').value = rule.name || '';
            document.getElementById('ruleConditionType').value = rule.conditionType || 'REGION';
            document.getElementById('ruleConditionValue').value = rule.conditionValue || '';
            document.getElementById('ruleMethod').value = rule.assignmentMethod || 'DIRECT';
            document.getElementById('rulePriority').value = rule.priority || 1;
            document.getElementById('ruleStatus').value = rule.active === true ? 'Hoạt động' : 'Vô hiệu hóa';

            openModal();
        }


        async function deleteRule(rule) {
            if (!confirm('Bạn có chắc muốn xóa quy tắc "' + rule.name + '"?')) {
                return;
            }

            try {
                const params = new URLSearchParams({
                    id: String(rule.id)
                });

                const response = await fetch(API_URL, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                        'Accept': 'application/json'
                    },
                    credentials: 'same-origin',
                    body: params.toString()
                });

                const data = await readApiJson(response);

                if (data.status !== 'success') {
                    throw new Error(data.message || 'Không thể xóa quy tắc.');
                }

                alert('Xóa quy tắc thành công!');
                fetchRules();

            } catch (error) {
                alert(error.message || 'Lỗi khi xóa quy tắc.');
            }
        }
        document.addEventListener('DOMContentLoaded', fetchRules);
    </script>
</body>
</html>