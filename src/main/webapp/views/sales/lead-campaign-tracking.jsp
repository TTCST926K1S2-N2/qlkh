<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
            content: ""; font-size: 28px;
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
            <a href="${pageContext.request.contextPath}/home"
               class="btn-filter"
               style="display:inline-block;text-decoration:none;margin:0 0 16px 0;">
                Về trang chủ
            </a>
        </div>

        <section id="campaignManagement" class="content-box" style="margin-bottom:24px;">
            <h3 class="content-title">Quản lý chiến dịch Marketing</h3>

            <form id="campaignCreateForm" style="display:none; margin-bottom:24px;">
                <div style="display:flex;flex-wrap:wrap;gap:12px;">
                    <input name="name" placeholder="Tên chiến dịch" maxlength="255" required>
                    <input name="budget" type="number" min="0" step="0.01"
                           placeholder="Ngân sách (VNĐ)" required>
                    <input name="startDate" type="date" required>
                    <input name="endDate" type="date" required>
                    <input name="channel" placeholder="Kênh Marketing"
                           maxlength="100" required>
                    <button type="submit" class="btn-filter">Tạo chiến dịch</button>
                </div>
            </form>

            <p id="campaignMessage" role="status"></p>

            <div style="overflow-x:auto;">
                <table style="width:100%;">
                    <thead>
                        <tr>
                            <th>Chiến dịch</th>
                            <th>Kênh</th>
                            <th>Ngân sách</th>
                            <th>Bắt đầu</th>
                            <th>Kết thúc</th>
                            <th>Lead</th>
                            <th>Cơ hội</th>
                            <th>Giá trị thắng dự kiến</th>
                        </tr>
                    </thead>
                    <tbody id="campaignTableBody"></tbody>
                </table>
            </div>
        </section>
        <section id="campaignLinkSection" class="content-box"
                 style="display:none;margin-bottom:24px;">
            <h3 class="content-title">Liên kết Lead / Cơ hội với chiến dịch</h3>

            <form id="campaignLinkForm">
                <div style="display:flex;flex-wrap:wrap;gap:12px;">
                    <select name="type" required>
                        <option value="lead">Lead</option>
                        <option value="opportunity">Cơ hội kinh doanh</option>
                    </select>

                    <input name="recordId" type="number" min="1"
                           placeholder="ID Lead hoặc cơ hội" required>

                    <select name="campaignId" id="linkCampaignSelect">
                        <option value="">-- Bỏ liên kết chiến dịch --</option>
                    </select>

                    <button type="submit" class="btn-filter">
                        Lưu liên kết
                    </button>
                </div>
            </form>

            <p id="campaignLinkMessage" role="status"></p>
        </section>
        <!-- Bộ lọc chiến dịch và nguồn -->
        <div class="filter-box">
            <div class="filter-group">
                <label for="campaignFilter">

                    Chiến dịch (Campaign):
                </label>
                <select id="campaignFilter">
                    <option value="">-- Tất cả chiến dịch --</option>
                </select>
            </div>
            <div class="filter-group">
                <label for="sourceFilter">

                    Nguồn (Source):
                </label>
                <select id="sourceFilter">
                    <option value="">-- Tất cả nguồn --</option>
                </select>
            </div>
            <button class="btn-filter" onclick="fetchLeads()">

                Lọc dữ liệu
            </button>
        </div>

        <div class="kpi-cards" id="kpiSection" style="display: none;">
            <div class="kpi-card leads">
                <div class="kpi-header">
                    <span class="kpi-title">Tổng số Lead</span>

                </div>
                <span class="kpi-value" id="kpiLeads">0</span>
            </div>
            <div class="kpi-card opps">
                <div class="kpi-header">
                    <span class="kpi-title">Số Cơ Hội (Opp)</span>

                </div>
                <span class="kpi-value" id="kpiOpps">0</span>
            </div>
            <div class="kpi-card revenue">
                <div class="kpi-header">
                    <span class="kpi-title">Giá trị đã chốt</span>

                </div>
                <span class="kpi-value" id="kpiRevenue">0 VNĐ</span>
            </div>
        </div>

        <div class="content-box">
            <h3 class="content-title">

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
        const CAMPAIGN_ROLE = '${sessionScope.userRole}';

        document.addEventListener('DOMContentLoaded', () => {
            const role = CAMPAIGN_ROLE.trim().toUpperCase();

            if (role === 'ADMIN' || role === 'MANAGER') {
                document.getElementById('campaignCreateForm').style.display = 'block';
            }
        });
        const CAMPAIGN_API = '${pageContext.request.contextPath}/api/v1/campaigns';

        const campaignMoney = value =>
            new Intl.NumberFormat('vi-VN').format(value || 0) + ' VNĐ';

        function campaignCell(row, value) {
            const cell = document.createElement('td');
            cell.textContent = value == null ? '---' : String(value);
            row.appendChild(cell);
        }

        async function loadCampaigns() {
            const message = document.getElementById('campaignMessage');
            const tbody = document.getElementById('campaignTableBody');
            tbody.replaceChildren();

            try {
                const response = await fetch(CAMPAIGN_API, {
                    credentials: 'same-origin'
                });

                if (!response.ok) {
                    throw new Error('Không tải được danh sách chiến dịch');
                }

                const campaigns = await response.json();

                window.campaignData = campaigns;

                window.campaignNames = Object.fromEntries(
                    campaigns.map(c => [String(c.id), c.name])
                );

                campaigns.forEach(c => {
                    const row = document.createElement('tr');
                    campaignCell(row, c.name);
                    campaignCell(row, c.channel);
                    campaignCell(row, campaignMoney(c.budget));
                    campaignCell(row, c.startDate);
                    campaignCell(row, c.endDate);
                    campaignCell(row, c.totalLeads);
                    campaignCell(row, c.totalOpportunities);
                    campaignCell(row, campaignMoney(c.wonValue));
                    tbody.appendChild(row);
                });

                message.textContent = campaigns.length
                    ? ''
                    : 'Chưa có chiến dịch nào.';

                const filter = document.getElementById('campaignFilter');
                const selected = filter.value;
                filter.replaceChildren(new Option('-- Tất cả chiến dịch --', ''));

                campaigns.forEach(c => {
                    filter.add(new Option(c.name, String(c.id)));
                });

                const linkSelect = document.getElementById('linkCampaignSelect');
                const previousLink = linkSelect.value;

                linkSelect.replaceChildren(
                    new Option('-- Bỏ liên kết chiến dịch --', '')
                );

                campaigns.forEach(c => {
                    linkSelect.add(new Option(c.name, String(c.id)));
                });

                linkSelect.value = previousLink;
                filter.value = selected;

            } catch (error) {
                message.textContent = error.message;
            }
        }

        async function createCampaign(event) {
            event.preventDefault();

            const form = event.currentTarget;
            const message = document.getElementById('campaignMessage');
            const body = new URLSearchParams(new FormData(form));

            try {
                const response = await fetch(CAMPAIGN_API, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: body.toString()
                });

                if (!response.ok) {
                    throw new Error('Không thể tạo chiến dịch. Kiểm tra quyền và dữ liệu.');
                }

                form.reset();
                await loadCampaigns();
                await fetchLeads();
                message.textContent = 'Tạo chiến dịch thành công.';

            } catch (error) {
                message.textContent = error.message;
            }
        }

        document.addEventListener('DOMContentLoaded', () => {
            document.getElementById('campaignCreateForm')
                .addEventListener('submit', createCampaign);

            loadCampaigns();
        });
        async function saveCampaignLink(event) {
            event.preventDefault();

            const form = event.currentTarget;
            const message = document.getElementById('campaignLinkMessage');

            try {
                const response = await fetch(
                    '${pageContext.request.contextPath}/api/v1/campaigns/link',
                    {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: {
                            'Content-Type':
                                'application/x-www-form-urlencoded;charset=UTF-8'
                        },
                        body: new URLSearchParams(new FormData(form)).toString()
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        'Không thể cập nhật liên kết. Kiểm tra ID và quyền truy cập.'
                    );
                }

                message.textContent = 'Cập nhật liên kết thành công.';
                await loadCampaigns();
                fetchLeads();

            } catch (error) {
                message.textContent = error.message;
            }
        }

        document.addEventListener('DOMContentLoaded', () => {
            if (CAMPAIGN_ROLE.trim().toUpperCase() === 'ADMIN') {
                document.getElementById('campaignLinkSection').style.display =
                    'block';
            }

            document.getElementById('campaignLinkForm')
                .addEventListener('submit', saveCampaignLink);
        });
        // Yêu cầu 2: Cập nhật đường dẫn API kèm context path /qlkh
        const API_URL = '${pageContext.request.contextPath}/api/v1/leads/tracking';

        function fetchLeads() {
            const campaign = document.getElementById('campaignFilter').value;
            const source = document.getElementById('sourceFilter').value;

            document.getElementById('leadTable').style.display = 'table';
            document.getElementById('kpiSection').style.display = 'none';
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('errorState').style.display = 'none';

            // Hàm khởi tạo không chứa dữ liệu API, chỉ là text tĩnh nên có thể dùng innerHTML
            document.getElementById('tableBody').innerHTML = '<tr><td colspan="5" style="text-align: center; color: #64748b;">Đang tải dữ liệu...</td></tr>';

            let queryUrl = API_URL + "?1=1";
            if (campaign) queryUrl += "&campaign=" + encodeURIComponent(campaign);
            if (source) queryUrl += "&source=" + encodeURIComponent(source);

            fetch(queryUrl)
                .then(res => {
                    if (!res.ok) throw new Error('Lỗi tải dữ liệu');
                    return res.json();
                })
                .then(data => {
                    updateFilters(data.campaigns, data.sources);
                    renderKPI(data.summary);
                    renderTable(data.items);
                })
                .catch(err => {
                    console.error(err);
                    document.getElementById('leadTable').style.display = 'none';
                    document.getElementById('errorState').style.display = 'block';
                });
        }

        function updateFilters(campaigns, sources) {
            const campaignSelect = document.getElementById('campaignFilter');
            const sourceSelect = document.getElementById('sourceFilter');

            const selectedCampaign = campaignSelect.value;
            const selectedSource = sourceSelect.value;

            campaignSelect.replaceChildren(campaignSelect.options[0]);
            sourceSelect.replaceChildren(sourceSelect.options[0]);

            const campaignOptions = window.campaignData ||
                (campaigns || []).map(value => ({
                    id: value,
                    name: 'Chiến dịch #' + value
                }));

            campaignOptions.forEach(c => {
                campaignSelect.add(new Option(c.name, String(c.id)));
            });

            if (window.campaignNames) {
                Array.from(campaignSelect.options).forEach(option => {
                    if (window.campaignNames[option.value]) {
                        option.textContent = window.campaignNames[option.value];
                    }
                });
            }

            (sources || []).forEach(value => {
                const option = new Option(value, value);
                sourceSelect.add(option);
            });

            campaignSelect.value = selectedCampaign;
            sourceSelect.value = selectedSource;
        }
        function renderKPI(summary) {
            if(!summary) return;
            document.getElementById('kpiLeads').innerText = summary.totalLeads || 0;
            document.getElementById('kpiOpps').innerText = summary.totalOpportunities ?? 'Chưa có dữ liệu';
            document.getElementById('kpiRevenue').innerText = summary.totalRevenue == null ? 'Chưa có dữ liệu' : new Intl.NumberFormat('vi-VN').format(summary.totalRevenue) + ' VNĐ';
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