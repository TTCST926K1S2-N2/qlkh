<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quan hệ công ty - HỆ THỐNG QLKH</title>
   <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customers.css">
<style>
  .company-relationship-main {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .company-relationship-main .customer-header {
    margin-bottom: 0;
  }

  .company-relationship-main .customer-box {
    margin-bottom: 0;
  }

  .company-relationship-main .form-group {
    display: grid;
    gap: 8px;
    margin-bottom: 14px;
  }

  .company-relationship-main .form-group label {
    font-weight: 600;
    color: #2c3e50;
  }

  .company-relationship-main .form-group select {
    width: 100%;
    max-width: 500px;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    background: #fff;
  }

  .company-relationship-main .customer-box h3 {
    margin-top: 0;
    color: #2c3e50;
  }

  .company-relationship-main .state-message {
    padding: 15px;
    margin-top: 12px;
  }

  .company-relationship-main .state-error {
    color: #b91c1c;
  }

  .company-relationship-main .state-empty {
    color: #64748b;
  }

  .company-relationship-main .total-summary {
    display: none;
  }

  .company-relationship-main .customer-table-wrap {
    width: 100%;
    overflow-x: auto;
  }

  .company-relationship-main #subsidiaryTable {
    width: 100%;
    border-collapse: collapse;
    margin-top: 15px;
  }

  .company-relationship-main #subsidiaryTable th,
  .company-relationship-main #subsidiaryTable td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #eee;
    vertical-align: middle;
  }

  .company-relationship-main #subsidiaryTable th {
    background: #f8f9fa;
    color: #2c3e50;
    font-weight: 600;
  }

  .company-relationship-main #subsidiaryTable td {
    color: #334155;
  }

  .company-relationship-main #subsidiaryTable th:last-child,
  .company-relationship-main #subsidiaryTable td:last-child {
    text-align: right;
  }

  @media (max-width: 768px) {
    .company-relationship-main #subsidiaryTable {
      min-width: 700px;
    }
  }
</style>
</head>
<body>
    <!-- Nhúng Menu Sidebar -->
    <jsp:include page="/components/sidebar.jsp" />

    <main class="customer-main company-relationship-main">
        <section class="customer-box">
            <div class="form-group">
                <label for="currentCompanySelect">Công ty cần quản lý:</label>
                <select id="currentCompanySelect">
                    <option value="">-- Chọn công ty --</option>
                </select>
            </div>
            <p id="saveMessage" role="status" aria-live="polite"></p>
        </section>

        <header class="customer-header">
            <h2>Quan hệ công ty mẹ - con</h2>
        </header>

        <!-- YÊU CẦU 1: Bổ sung trường lựa chọn công ty mẹ trong hồ sơ -->
        <section class="customer-box">
            <div class="form-group">
                <label>Trực thuộc Công ty Mẹ (nếu có):</label>
                <select id="parentCompanySelect">
                    <option value="">-- Không trực thuộc (Là công ty độc lập/mẹ) --</option>
                </select>
            </div>
            <div class="customer-actions">
                <button id="saveRelationshipBtn"
                        type="button"
                        disabled
                        style="background:#2ecc71;color:white;">
                    Lưu quan hệ
                </button>
            </div>
        </section>

        <!-- YÊU CẦU 2 & 3: Hiển thị quan hệ và danh sách công ty con -->
        <section class="customer-box">
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
        </section>
    </main>

    <script>
    const API_URL = '${pageContext.request.contextPath}/api/v1/customer-relationships/';

    const currentSelect = document.getElementById('currentCompanySelect');
    const parentSelect = document.getElementById('parentCompanySelect');
    const tableBody = document.getElementById('tableBody');
    const subsidiaryTable = document.getElementById('subsidiaryTable');
    const emptyState = document.getElementById('emptyState');
    const errorState = document.getElementById('errorState');
    const totalSummaryBox = document.getElementById('totalSummaryBox');
    const saveMessage = document.getElementById('saveMessage');
    const saveRelationshipBtn = document.getElementById('saveRelationshipBtn');

    let companies = [];
    let isSaving = false;

    function showMessage(message, isError = false) {
        saveMessage.textContent = message;
        saveMessage.style.color = isError ? '#c0392b' : '#207245';
    }

    function addOption(select, value, label) {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = label;
        select.appendChild(option);
    }

    async function loadCompanies(selectedId = '') {
        const response = await fetch(API_URL, {
            credentials: 'same-origin',
            cache: 'no-store'
        });

        if (!response.ok) {
            throw new Error('Không thể tải danh sách công ty (HTTP ' + response.status + ')');
        }

        const data = await response.json();

        if (!Array.isArray(data)) {
            throw new Error('Dữ liệu API không hợp lệ');
        }

        companies = data;

        currentSelect.replaceChildren();
        addOption(currentSelect, '', '-- Chọn công ty --');

        companies.forEach(company => {
            addOption(currentSelect, company.id, company.companyName);
        });

        if (selectedId && companies.some(c => String(c.id) === String(selectedId))) {
            currentSelect.value = String(selectedId);
        }

        renderCurrentCompany();
    }

    function renderCurrentCompany() {
        const currentId = Number(currentSelect.value);

        saveRelationshipBtn.disabled = true;
        parentSelect.replaceChildren();
        addOption(parentSelect, '', '-- Không thuộc công ty mẹ nào --');

        emptyState.style.display = 'none';
        errorState.style.display = 'none';
        totalSummaryBox.style.display = 'none';
        tableBody.replaceChildren();

        if (!currentId) {
            parentSelect.disabled = true;
            subsidiaryTable.style.display = 'none';
            showMessage('Vui lòng chọn công ty cần quản lý.');
            return;
        }

        const current = companies.find(c => Number(c.id) === currentId);

        if (!current) {
            showMessage('Không tìm thấy công ty.', true);
            return;
        }

        parentSelect.disabled = false;
        showMessage('Đã tải thông tin quan hệ công ty.');

        companies
            .filter(c => Number(c.id) !== currentId)
            .forEach(company => {
                addOption(parentSelect, company.id, company.companyName);
            });

        parentSelect.value = current.parentId == null
            ? ''
            : String(current.parentId);

        const children = companies.filter(
            c => Number(c.parentId) === currentId
        );

        if (children.length === 0) {
            subsidiaryTable.style.display = 'none';
            emptyState.style.display = 'block';
            showMessage('Đã tải thông tin công ty. Hiện chưa có công ty con.');
            return;
        }

        subsidiaryTable.style.display = 'table';

        children.forEach((company, index) => {
            const row = document.createElement('tr');

            const values = [
                String(index + 1),
                company.companyName,
                'Chưa có dữ liệu',
                'Chưa có dữ liệu',
                'Chưa có dữ liệu hợp đồng'
            ];

            values.forEach(value => {
                const cell = document.createElement('td');
                cell.textContent = value;
                row.appendChild(cell);
            });

            tableBody.appendChild(row);
        });
    }

    async function updateParentCompany() {
        const childId = currentSelect.value;
        const parentId = parentSelect.value;

        if (!childId) {
            showMessage('Vui lòng chọn công ty cần quản lý.', true);
            return;
        }

        parentSelect.disabled = true;
        currentSelect.disabled = true;

        try {
            const url = API_URL
                + encodeURIComponent(childId)
                + '?parentId='
                + encodeURIComponent(parentId);

            const response = await fetch(url, {
                method: 'PUT',
                credentials: 'same-origin'
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Không thể cập nhật quan hệ công ty');
            }

            await loadCompanies(childId);
            showMessage('Đã cập nhật quan hệ công ty thành công.');
        } catch (error) {
            renderCurrentCompany();
            showMessage(error.message, true);
        } finally {
            currentSelect.disabled = false;
            parentSelect.disabled = !currentSelect.value;
        }
    }

    parentSelect.addEventListener('change', () => {
        const current = companies.find(
            c => String(c.id) === String(currentSelect.value)
        );

        const originalParentId = current && current.parentId != null
            ? String(current.parentId)
            : '';

        saveRelationshipBtn.disabled =
            !current || parentSelect.value === originalParentId;
    });

    saveRelationshipBtn.addEventListener('click', async () => {
        if (isSaving || saveRelationshipBtn.disabled) {
            return;
        }

        if (!confirm('Bạn có chắc chắn muốn lưu quan hệ công ty này?')) {
            return;
        }

        isSaving = true;
        saveRelationshipBtn.disabled = true;

        try {
            await updateParentCompany();
        } finally {
            isSaving = false;
        }
    });
    currentSelect.addEventListener('change', renderCurrentCompany);

    document.addEventListener('DOMContentLoaded', async () => {
        try {
            await loadCompanies();
        } catch (error) {
            currentSelect.disabled = true;
            parentSelect.disabled = true;
            subsidiaryTable.style.display = 'none';
            errorState.style.display = 'block';
            showMessage(error.message, true);
        }
    });
</script>
</body>
</html>