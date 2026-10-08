<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quan hệ công ty - HỆ THỐNG QLKH</title>
   <style>
        body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background-color: #f0f3f6; font-family: 'Segoe UI', Arial, sans-serif; 
        }
        .main-content { padding: 40px; width: 100%; max-width: 1000px; box-sizing: border-box; }
        
        /* Tiêu đề chính trang */
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; 
            border-bottom: 3px solid #34495e; padding-bottom: 12px; margin-bottom: 30px; 
        }
        .page-header h2 { margin: 0; color: #1a2a3a; font-size: 26px; font-weight: 700; }
        
        /* Card / Hộp chứa */
        .content-box { 
            background: #fff; padding: 30px; border-radius: 10px; 
            box-shadow: 0 4px 15px rgba(0,0,0,0.06); margin-bottom: 25px;
            border: 1px solid #e1e8ed; transition: all 0.3s ease;
        }
        .content-box:hover {
            box-shadow: 0 6px 20px rgba(0,0,0,0.1);
        }
        .content-box h3 {
            margin-top: 0; margin-bottom: 20px; color: #1f3a60; font-size: 18px; font-weight: 700;
        }
        
        /* Form Chọn công ty mẹ */
        .form-group { margin-bottom: 5px; }
        .form-group label { display: block; font-weight: 700; margin-bottom: 10px; color: #2c3e50; font-size: 15px; }
        .form-group select { 
            width: 100%; max-width: 450px; padding: 12px 15px; 
            border: 2px solid #bdc3c7; border-radius: 6px; 
            font-size: 14px; color: #34495e; background-color: #fdfdfd;
            transition: border-color 0.2s; outline: none;
        }
        .form-group select:focus {
            border-color: #1f3a60;
            background-color: #fff;
        }
        
        /* Bảng danh sách - Nâng cấp đậm đà */
        table { width: 100%; border-collapse: collapse; margin-top: 20px; overflow: hidden; border-radius: 6px; }
        th, td { padding: 14px 18px; text-align: left; }
        
        /* Header bảng màu Navy đậm */
        th { 
            background-color: #1f3a60; color: #ffffff; 
            font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px;
            border: none;
        }
        
        /* Các dòng dữ liệu xen kẽ & hover */
        tr { border-bottom: 1px solid #eef2f5; }
        tbody tr:nth-child(even) { background-color: #f8fafd; }
        tbody tr:hover { background-color: #f1f5f9; }
        
        td { color: #3c4a57; font-size: 14px; }
        td strong { color: #1a2a3a; font-weight: 600; }
        
        /* Tag Trạng thái "Đang hoạt động" dạng Badge nổi bật */
        .status-badge {
            display: inline-block; padding: 5px 12px; border-radius: 20px;
            font-size: 12px; font-weight: 600; text-align: center;
            background-color: #27ae60; color: #ffffff;
            box-shadow: 0 2px 5px rgba(39, 174, 96, 0.2);
        }
        
        /* Khung Tổng hợp đặc biệt - Đậm và thu hút hơn */
        .total-summary { 
            margin-top: 25px; padding: 18px 24px; 
            background: linear-gradient(135deg, #1f3a60 0%, #112540 100%); 
            color: #ffffff; border-radius: 8px;
            font-size: 16px; font-weight: 500; text-align: right;
            box-shadow: 0 4px 12px rgba(31, 58, 96, 0.15);
            display: flex; justify-content: flex-end; align-items: center; gap: 8px;
        }
        .total-summary span { 
            color: #ff4757; font-size: 22px; font-weight: 800; 
            text-shadow: 0 1px 2px rgba(0,0,0,0.2);
        }
        
        /* Các thông báo trạng thái rỗng / lỗi */
        .state-message { text-align: center; padding: 40px 20px; font-size: 15px; border-radius: 8px; font-weight: 500; }
        .state-empty { background-color: #fef9e7; color: #b7950b; border: 1.5px solid #f9e79f; display: none; }
        .state-error { background-color: #fdf2f2; color: #ec7063; border: 1.5px solid #fadbd8; display: none; }
    </style>
</head>
<body>
    <!-- Nhúng Menu Sidebar -->
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <div class="page-header">
            <h2>Hồ sơ quan hệ công ty mẹ - con</h2>
        </div>
        
        <!-- YÊU CẦU 1: Bổ sung trường lựa chọn công ty mẹ trong hồ sơ -->
        <div class="content-box">
            <div class="form-group">
                <label>Trực thuộc Công ty Mẹ (nếu có):</label>
                <select id="parentCompanySelect" onchange="updateParentCompany()">
                    <option value="">-- Không trực thuộc (Là công ty độc lập/mẹ) --</option>
                    <option value="1">Tập đoàn Vingroup</option>
                    <option value="2">Tập đoàn FPT</option>
                    <option value="3">Công ty Cổ phần Sữa Việt Nam (Vinamilk)</option>
                </select>
            </div>
        </div>

        <!-- YÊU CẦU 2 & 3: Hiển thị quan hệ và danh sách công ty con -->
        <div class="content-box">
            <h3 style="margin-top: 0; color: #2c3e50;">Danh sách Công ty con trực thuộc</h3>
            
            <!-- YÊU CẦU 6: Hiển thị trạng thái không có dữ liệu và lỗi -->
            <div id="emptyState" class="state-message state-empty">
                Công ty này hiện không có công ty con nào trực thuộc.
            </div>
            <div id="errorState" class="state-message state-error">
                Đã xảy ra lỗi khi tải dữ liệu từ máy chủ. Vui lòng thử lại sau!
            </div>

            <!-- Bảng danh sách công ty con -->
            <table id="subsidiaryTable">
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>TÊN CÔNG TY CON</th>
                        <th>MÃ SỐ THUẾ</th>
                        <th>TRẠNG THÁI</th>
                        <th style="text-align: right;">GIÁ TRỊ HỢP ĐỒNG (VNĐ)</th>
                    </tr>
                </thead>
                <tbody id="tableBody">
                    <tr><td colspan="5" style="text-align: center;">Đang tải dữ liệu...</td></tr>
                </tbody>
            </table>

            <!-- YÊU CẦU 4: Hiển thị giá trị hợp đồng tổng hợp -->
            <div id="totalSummaryBox" class="total-summary" style="display: none;">
                Tổng giá trị hợp đồng từ các công ty con: <span id="totalContractValue">0</span> VNĐ
            </div>
        </div>
    </div>

    <script>
        const API_URL = '/qlkh/api/v1/companies/subsidiaries'; // Thay ID công ty hiện tại vào cuối API
        const tableBody = document.getElementById("tableBody");
        const subsidiaryTable = document.getElementById("subsidiaryTable");
        const emptyState = document.getElementById("emptyState");
        const errorState = document.getElementById("errorState");
        const totalSummaryBox = document.getElementById("totalSummaryBox");
        const totalContractValueLabel = document.getElementById("totalContractValue");

        // Format tiền tệ VNĐ
        const formatCurrency = (amount) => {
            return new Intl.NumberFormat('vi-VN').format(amount);
        };

        // YÊU CẦU 5: Kết nối API quan hệ công ty
        function fetchSubsidiaries() {
            // Reset các trạng thái UI
            tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Đang tải dữ liệu...</td></tr>';
            emptyState.style.display = 'none';
            errorState.style.display = 'none';
            subsidiaryTable.style.display = 'table';
            totalSummaryBox.style.display = 'none';

            fetch(API_URL)
                .then(response => {
                    if (!response.ok) throw new Error("Lỗi API");
                    return response.json();
                })
                .then(data => {
                    renderSubsidiaries(data);
                })
                .catch(error => {
                    // Xử lý Lỗi (Yêu cầu 6)
                    subsidiaryTable.style.display = 'none';
                    errorState.style.display = 'block';
                    console.error("Lỗi khi gọi API: ", error);
                });
        }

        // Render dữ liệu ra bảng
        function renderSubsidiaries(data) {
            // Xử lý Không có dữ liệu (Yêu cầu 6)
            if (!data || data.length === 0) {
                subsidiaryTable.style.display = 'none';
                emptyState.style.display = 'block';
                return;
            }

            tableBody.innerHTML = "";
            let totalContractValue = 0;

            data.forEach((company, index) => {
                const contractValue = company.contractValue || 0;
                totalContractValue += contractValue; // Cộng dồn tổng giá trị (Yêu cầu 4)

                const row = `<tr>
                    <td>${index + 1}</td>
                    <td><strong>${company.name}</strong></td>
                    <td>${company.taxCode}</td>
                    <td><span style="color: green;">Đang hoạt động</span></td>
                    <td style="text-align: right;">${formatCurrency(contractValue)}</td>
                </tr>`;
                tableBody.insertAdjacentHTML('beforeend', row);
            });

            // Hiển thị tổng giá trị hợp đồng (Yêu cầu 4)
            totalContractValueLabel.innerText = formatCurrency(totalContractValue);
            totalSummaryBox.style.display = 'block';
        }

        // Hàm cập nhật Công ty mẹ (Yêu cầu 1)
        function updateParentCompany() {
            const parentId = document.getElementById("parentCompanySelect").value;
            // TODO: Bắn API PUT/PATCH để cập nhật quan hệ công ty mẹ cho hồ sơ này
            console.log("Đã chọn công ty mẹ ID:", parentId);
        }

        // -------- MOCK API ĐỂ TEST UI (XÓA KHI CÓ BACKEND THẬT) --------
        // Chặn hàm fetch thật và trả về dữ liệu giả lập để test giao diện
        window.fetch = function() {
            return new Promise((resolve, reject) => {
                setTimeout(() => {
                    // Đổi isError hoặc isEmpty thành true để test UI báo lỗi / trống
                    const isError = false; 
                    const isEmpty = false; 
                    
                    if (isError) {
                        reject(new Error("500 Internal Server Error"));
                    } else if (isEmpty) {
                        resolve({ ok: true, json: () => Promise.resolve([]) });
                    } else {
                        resolve({
                            ok: true,
                            json: () => Promise.resolve([
                                { id: 101, name: "Công ty Cổ phần Phần mềm FPT", taxCode: "0101778163", contractValue: 1500000000 },
                                { id: 102, name: "Công ty TNHH Hệ thống Thông tin FPT", taxCode: "0104128565", contractValue: 3200000000 }
                            ])
                        });
                    }
                }, 800);
            });
        };
        // -------------------------------------------------------------

        // Tự động load dữ liệu khi mở trang
        document.addEventListener("DOMContentLoaded", () => {
            fetchSubsidiaries();
        });
    </script>
</body>
</html>