<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Theo dõi Lead theo Chiến dịch & Nguồn - QLKH</title>
    <style>
        :root {
            --primary: #4f46e5;
            --primary-hover: #4338ca;
            --secondary: #ec4899;
            --bg-gradient: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
            --text-main: #0f172a;
            --text-muted: #475569;
            --border: #e2e8f0;
            --card-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
            --card-shadow-hover: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
        }

        body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background: var(--bg-gradient); 
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif; 
            min-height: 100vh;
            color: var(--text-main);
        }
        .main-content { padding: 40px; width: 100%; max-width: 1140px; box-sizing: border-box; }
        
        /* Tiêu đề trang */
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; 
            margin-bottom: 32px; 
        }
        .page-header h2 { 
            margin: 0; font-size: 32px; font-weight: 800; letter-spacing: -0.025em;
            background: linear-gradient(90deg, var(--primary), var(--secondary));
            -webkit-background-clip: text; -webkit-text-fill-color: transparent;
            display: flex; align-items: center; gap: 12px;
        }
        .page-header h2::before {
            content: "🎯"; font-size: 28px;
        }
        
        /* Bộ lọc Filter Box */
        .filter-box {
            background: #ffffff; padding: 24px; border-radius: 16px;
            box-shadow: var(--card-shadow);
            display: flex; gap: 20px; align-items: flex-end; margin-bottom: 24px;
            border: 1px solid rgba(226, 232, 240, 0.8);
            position: relative;
        }
        .filter-box::before {
            content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%;
            background: var(--primary); border-top-left-radius: 16px; border-bottom-left-radius: 16px;
        }
        .filter-group { flex: 1; display: flex; flex-direction: column; gap: 8px; }
        .filter-group label { font-weight: 600; font-size: 13.5px; color: var(--text-muted); display: flex; align-items: center; gap: 6px; }
        .filter-group select {
            padding: 12px 16px; border: 1.5px solid var(--border); border-radius: 10px;
            font-size: 14px; font-weight: 500; outline: none; background: #f8fafc; cursor: pointer;
            transition: all 0.2s ease-in-out;
            color: var(--text-main);
            appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke-width='1.5' stroke='%23475569'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M19.5 8.25l-7.5 7.5-7.5-7.5' /%3E%3C/svg%3E");
            background-repeat: no-repeat; background-position: right 16px center; background-size: 16px;
        }
        .filter-group select:focus { 
            border-color: var(--primary); background: #ffffff; 
            box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
        }

        .btn-filter {
            padding: 0 28px; background: var(--primary); color: white; border: none;
            border-radius: 10px; font-weight: 600; font-size: 14.5px; cursor: pointer; height: 46px; 
            transition: all 0.2s ease-in-out;
            display: inline-flex; align-items: center; gap: 8px;
            box-shadow: 0 4px 10px rgba(79, 70, 229, 0.2);
        }
        .btn-filter:hover { 
            background: var(--primary-hover); 
            transform: translateY(-1px);
            box-shadow: 0 6px 12px rgba(79, 70, 229, 0.3);
        }
        .btn-filter:active { transform: translateY(0); }

        /* Khối thống kê KPI */
        .kpi-cards {
            display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 32px;
        }
        .kpi-card {
            background: #ffffff; padding: 24px; border-radius: 16px; border: 1px solid rgba(226, 232, 240, 0.8);
            box-shadow: var(--card-shadow); display: flex; flex-direction: column; gap: 8px;
            position: relative; overflow: hidden; transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .kpi-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--card-shadow-hover);
        }
        .kpi-title { font-size: 12.5px; color: #64748b; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; display: flex; align-items: center; gap: 8px; }
        .kpi-value { font-size: 32px; font-weight: 800; color: var(--primary); letter-spacing: -0.025em; margin-top: 4px; }
        
        /* Chỉnh style KPI 1 */
        .kpi-card.leads { border-left: 4px solid #6366f1; }
        .kpi-card.leads .kpi-icon { color: #6366f1; background: #e0e7ff; }
        /* KPI 2 */
        .kpi-card.opps { border-left: 4px solid #06b6d4; }
        .kpi-card.opps .kpi-icon { color: #06b6d4; background: #ecfeff; }
        .kpi-card.opps .kpi-value { color: #0891b2; }
        /* KPI 3 */
        .kpi-card.revenue { border-left: 4px solid #10b981; }
        .kpi-card.revenue .kpi-icon { color: #10b981; background: #ecfdf5; }
        .kpi-card.revenue .kpi-value { color: #059669; }

        .kpi-header { display: flex; justify-content: space-between; align-items: center; }
        .kpi-icon {
            width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px;
        }

        /* Bảng dữ liệu & Khối nội dung */
        .content-box { 
            background: #ffffff; padding: 32px; border-radius: 20px; 
            box-shadow: var(--card-shadow);
            border: 1px solid rgba(226, 232, 240, 0.8);
        }
        .content-title {
            font-size: 18px; font-weight: 750; margin: 0 0 20px 0; color: var(--text-main);
            display: flex; align-items: center; gap: 8px;
        }
        
        table { width: 100%; border-collapse: separate; border-spacing: 0 10px; margin-top: 8px; }
        th { 
            padding: 8px 16px 12px 16px; text-align: left; color: #64748b; 
            font-weight: 700; font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; border: none;
        }
        td { padding: 18px 16px; background: #ffffff; border-top: 1.5px solid #f1f5f9; border-bottom: 1.5px solid #f1f5f9; color: var(--text-muted); font-size: 14.5px; transition: all 0.2s ease; }
        td:first-child { border-left: 1.5px solid #f1f5f9; border-top-left-radius: 12px; border-bottom-left-radius: 12px; }
        td:last-child { border-right: 1.5px solid #f1f5f9; border-top-right-radius: 12px; border-bottom-right-radius: 12px; }
        
        tbody tr { position: relative; }
        tbody tr::after {
            content: ''; position: absolute; left: 0; top: 10px; bottom: 10px; width: 3px; background: transparent; border-radius: 4px; transition: all 0.2s ease;
        }
        tbody tr:hover td { background-color: #f8fafc; border-color: #e2e8f0; color: var(--text-main); }
        tbody tr:hover::after { background: var(--primary); }
        
        /* Trạng thái */
        .status-pill {
            display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 9999px; font-size: 12.5px; font-weight: 600;
        }
        .status-pill.default { background: #f1f5f9; color: #475569; }
        .status-pill.care { background: #eff6ff; color: #1d4ed8; }
        .status-pill.win { background: #ecfdf5; color: #047857; }

        .badge-campaign { background: #f0fdf4; color: #166534; padding: 6px 12px; border-radius: 8px; font-weight: 600; font-size: 12px; border: 1px solid #dcfce7; }
        .badge-source { background: #fff5f5; color: #c53030; padding: 6px 12px; border-radius: 8px; font-weight: 600; font-size: 12px; border: 1px solid #fed7d7; }

        /* Trạng thái tin nhắn */
        .state-message { text-align: center; padding: 48px; font-size: 15.5px; border-radius: 16px; font-weight: 600; margin-top: 24px; box-shadow: inset 0 2px 4px rgba(0,0,0,0.02); }
        .state-empty { background: #fffbeb; color: #b45309; border: 1px dashed #fef3c7; display: none; }
        .state-error { background: #fef2f2; color: #b91c1c; border: 1px dashed #fee2e2; display: none; }
    </style>
</head>
<body>

    <div class="main-content">
        <div class="page-header">
            <h2>Theo dõi Lead theo Chiến dịch & Nguồn</h2>
        </div>
        
        <!-- Bộ lọc chiến dịch và nguồn -->
        <div class="filter-box">
            <div class="filter-group">
                <label for="campaignFilter">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.34 15.84c-.688-.06-1.386-.09-2.09-.09H7.5a4.5 4.5 0 1 1 0-9h.75c.704 0 1.402-.03 2.09-.09m0 9.18c.253.962.584 1.892.985 2.783.247.55.5 1.1.75 1.65.14.308.38.541.68.65a2.247 2.247 0 0 0 2.525-.83c.29-.414.242-.965-.11-1.326l-.423-.432a12.447 12.447 0 0 0-3.41-2.502m0-9.18c.253-.962.584-1.892.985-2.783a2.247 2.247 0 0 1 3.525-.33c.29.414.242.965-.11 1.326l-.41.418a12.448 12.448 0 0 1-2.99 1.379m0 9.18a38.411 38.411 0 0 0 3.32-.45c1.472-.257 2.8-.828 3.932-1.637m-7.252-7.09a38.416 38.416 0 0 1 3.32.45c1.472.257 2.8.828 3.932 1.637m0 0a8.55 8.55 0 0 1 1.171 1.637m-1.171-1.637a8.553 8.553 0 0 0 1.171-1.637"/></svg>
                    Chiến dịch (Campaign):
                </label>
                <select id="campaignFilter">
                    <option value="">-- Tất cả chiến dịch --</option>
                    <option value="Summer2026">Chiến dịch Hè 2026</option>
                    <option value="BlackFriday">Black Friday</option>
                </select>
            </div>
            <div class="filter-group">
                <label for="sourceFilter">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 21a9.004 9.004 0 0 0 8.716-6.747M12 21a9.004 9.004 0 0 1-8.716-6.747M12 21c2.485 0 4.5-4.03 4.5-9S14.485 3 12 3m0 18c-2.485 0-4.5-4.03-4.5-9S9.515 3 12 3m0 0a8.997 8.997 0 0 1 7.843 4.582M12 3a8.997 8.997 0 0 0-7.843 4.582m15.686 0A11.953 11.953 0 0 1 12 10.5c-2.998 0-5.74-1.1-7.843-2.918m15.686 0A8.959 8.959 0 0 1 21 12c0 .778-.099 1.533-.284 2.253m0 0A17.919 17.919 0 0 1 12 16.5c-3.162 0-6.133-.815-8.716-2.247m0 0A9.015 9.015 0 0 1 3 12c0-.778.099-1.533.284-2.253"/></svg>
                    Nguồn (Source):
                </label>
                <select id="sourceFilter">
                    <option value="">-- Tất cả nguồn --</option>
                    <option value="Facebook">Facebook Ads</option>
                    <option value="Google">Google Search</option>
                    <option value="Website">Trực tiếp Website</option>
                </select>
            </div>
            <button class="btn-filter" onclick="fetchLeads()">
                <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v1.044a2.25 2.25 0 0 1-.659 1.591l-5.432 5.432a2.25 2.25 0 0 0-.659 1.591v2.927a2.25 2.25 0 0 1-1.24 2.013L10.75 21v-6.568a2.25 2.25 0 0 0-.659-1.591L4.659 7.41A2.25 2.25 0 0 1 4 5.818V4.774c0-.54.384-1.006.917-1.096A48.32 48.32 0 0 1 12 3z"/></svg>
                Lọc dữ liệu
            </button>
        </div>

        <div class="kpi-cards" id="kpiSection" style="display: none;">
            <div class="kpi-card leads">
                <div class="kpi-header">
                    <span class="kpi-title">Tổng số Lead</span>
                    <div class="kpi-icon">👥</div>
                </div>
                <span class="kpi-value" id="kpiLeads">0</span>
            </div>
            <div class="kpi-card opps">
                <div class="kpi-header">
                    <span class="kpi-title">Số Cơ Hội (Opp)</span>
                    <div class="kpi-icon">🌟</div>
                </div>
                <span class="kpi-value" id="kpiOpps">0</span>
            </div>
            <div class="kpi-card revenue">
                <div class="kpi-header">
                    <span class="kpi-title">Giá trị đã chốt</span>
                    <div class="kpi-icon">💰</div>
                </div>
                <span class="kpi-value" id="kpiRevenue">0 VNĐ</span>
            </div>
        </div>

        <div class="content-box">
            <h3 class="content-title">
                <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8.25 6.75h12M8.25 12h12m-12 5.25h12M3.75 6.75h.007v.008H3.75V6.75zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0zM3.75 12h.007v.008H3.75V12zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0zm-.375 5.25h.007v.008H3.75v-.008zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0z"/></svg>
                Danh sách Lead đang quản lý
            </h3>
            
            <div id="emptyState" class="state-message state-empty">
                Không tìm thấy lead nào phù hợp với bộ lọc chiến dịch và nguồn này.
            </div>
            <div id="errorState" class="state-message state-error">
                Đã xảy ra lỗi khi tải dữ liệu từ hệ thống. Vui lòng thử lại sau!
            </div>

            <table id="leadTable">
                <thead>
                    <tr>
                        <th>Họ và Tên</th>
                        <th>Số điện thoại</th>
                        <th>Chiến dịch</th>
                        <th>Nguồn</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody id="tableBody">
                    <tr><td colspan="5" style="text-align: center; color: #64748b;">Đang tải dữ liệu...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        // Yêu cầu 2: Cập nhật đường dẫn API kèm context path /qlkh
        const API_URL = '/qlkh/api/v1/leads/tracking';

        function fetchLeads() {
            const campaign = document.getElementById('campaignFilter').value;
            const source = document.getElementById('sourceFilter').value;

            document.getElementById('leadTable').style.display = 'table';
            document.getElementById('kpiSection').style.display = 'none';
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('errorState').style.display = 'none';
            
            // Hàm khởi tạo không chứa dữ liệu API, chỉ là text tĩnh nên có thể dùng innerHTML
            document.getElementById('tableBody').innerHTML = '<tr><td colspan="5" style="text-align: center; color: #64748b;">Đang tải dữ liệu...</td></tr>';

            let queryUrl = `${API_URL}?1=1`;
            if (campaign) queryUrl += `&campaign=${campaign}`;
            if (source) queryUrl += `&source=${source}`;

            fetch(queryUrl)
                .then(res => {
                    if (!res.ok) throw new Error('Lỗi tải dữ liệu');
                    return res.json();
                })
                .then(data => {
                    renderKPI(data.summary);
                    renderTable(data.items);
                })
                .catch(err => {
                    console.error(err);
                    document.getElementById('leadTable').style.display = 'none';
                    document.getElementById('errorState').style.display = 'block';
                });
        }

        function renderKPI(summary) {
            if(!summary) return;
            document.getElementById('kpiLeads').innerText = summary.totalLeads || 0;
            document.getElementById('kpiOpps').innerText = summary.totalOpportunities || 0;
            document.getElementById('kpiRevenue').innerText = new Intl.NumberFormat('vi-VN').format(summary.totalRevenue || 0) + ' VNĐ';
            document.getElementById('kpiSection').style.display = 'grid';
        }

        // Yêu cầu 3: Sửa lại hàm renderTable thay innerHTML bằng DOM Element an toàn (XSS Protection)
        function renderTable(leads) {
            const tbody = document.getElementById('tableBody');
            tbody.innerHTML = ''; // Làm sạch bảng

            if (!leads || leads.length === 0) {
                document.getElementById('leadTable').style.display = 'none';
                document.getElementById('emptyState').style.display = 'block';
                return;
            }

            leads.forEach(lead => {
                const tr = document.createElement('tr');
                
                let statusClass = 'default';
                if(lead.status === 'Đang chăm sóc') statusClass = 'care';
                if(lead.status === 'Chốt Deal (Win)') statusClass = 'win';

                // Tạo Cột Tên
                const tdName = document.createElement('td');
                const strongName = document.createElement('strong');
                strongName.style.color = 'var(--text-main)';
                strongName.style.fontWeight = '700';
                strongName.textContent = lead.name || '---'; // Dùng textContent an toàn
                tdName.appendChild(strongName);

                // Tạo Cột Số điện thoại
                const tdPhone = document.createElement('td');
                tdPhone.style.fontWeight = '500';
                tdPhone.textContent = lead.phone || '---';

                // Tạo Cột Chiến dịch
                const tdCampaign = document.createElement('td');
                const spanCampaign = document.createElement('span');
                spanCampaign.className = 'badge-campaign';
                spanCampaign.textContent = lead.campaign || '---';
                tdCampaign.appendChild(spanCampaign);

                // Tạo Cột Nguồn
                const tdSource = document.createElement('td');
                const spanSource = document.createElement('span');
                spanSource.className = 'badge-source';
                spanSource.textContent = lead.source || '---';
                tdSource.appendChild(spanSource);

                // Tạo Cột Trạng thái
                const tdStatus = document.createElement('td');
                const spanStatus = document.createElement('span');
                spanStatus.className = `status-pill ${statusClass}`;
                spanStatus.textContent = lead.status || 'Mới';
                tdStatus.appendChild(spanStatus);

                // Gắn các cột vào hàng (tr)
                tr.appendChild(tdName);
                tr.appendChild(tdPhone);
                tr.appendChild(tdCampaign);
                tr.appendChild(tdSource);
                tr.appendChild(tdStatus);

                // Gắn hàng vào bảng (tbody)
                tbody.appendChild(tr);
            });
        }

        document.addEventListener('DOMContentLoaded', () => {
            fetchLeads();
        });
    </script>
</body>
</html>