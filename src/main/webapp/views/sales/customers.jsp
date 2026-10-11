<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>Quản lý khách hàng</title>
    <link
      rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/customers.css"
    />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer-import.css" />

<style>
.s3-care-button,
#openAdvancedCustomerSearch {
    background: #2563eb !important;
    color: #ffffff !important;
    border: 1px solid #2563eb !important;
    border-radius: 4px !important;
    padding: 8px 12px !important;
    font-weight: 600 !important;
    cursor: pointer !important;
}

.s3-care-button:hover,
#openAdvancedCustomerSearch:hover {
    background: #1d4ed8 !important;
    border-color: #1d4ed8 !important;
}
</style>
<style id="s308-toolbar-fix">
#openAdvancedCustomerSearch,
.s3-care-button {
    display: inline-block !important;
    background-color: #2563eb !important;
    color: #ffffff !important;
    border: 1px solid #2563eb !important;
    border-radius: 4px !important;
    padding: 8px 12px !important;
    font-size: 12px !important;
    font-weight: 600 !important;
    text-decoration: none !important;
    cursor: pointer !important;
}

#openAdvancedCustomerSearch:hover,
.s3-care-button:hover {
    background-color: #1d4ed8 !important;
}
</style>
</head>
  <body>
    <jsp:include page="/components/sidebar.jsp"/>
    <main class="customer-main">
      <header class="customer-header">
        <h2>Quản lý khách hàng doanh nghiệp</h2>
        <div class="s304-header-actions">
          <button id="s304FindDuplicates" type="button" hidden>Phát hiện trùng lặp</button>
          <button id="openCustomerImport" type="button">Nhập Excel</button>
          <button id="openAdvancedCustomerSearch"
        type="button"
        onclick="window.location.href='${pageContext.request.contextPath}/views/sales/customer-search.jsp'">
    Tìm kiếm nâng cao
</button>

<button type="button"
        class="s3-care-button"
        onclick="window.location.href='${pageContext.request.contextPath}/views/sales/customer-care.jsp'">
    Chăm sóc khách hàng
</button>
<a href="${pageContext.request.contextPath}/customer-care-list"
   style="display:inline-block;background:#2563eb;color:#fff;padding:8px 12px;border-radius:5px;text-decoration:none;margin-left:6px;">
    Khách hàng cần chăm sóc
</a>
<button id="addCustomer" type="button">Thêm khách hàng</button>
        </div>
      </header>
      <section class="customer-box">
        <div class="customer-filters">
          <input
            id="customerSearch"
            placeholder="Tìm tên doanh nghiệp, mã số thuế..."
          /><select id="customerStatus">
            <option value="">Tất cả trạng thái</option>
            <option value="POTENTIAL">Tiềm năng</option>
            <option value="IN_PROGRESS">Đang giao dịch</option>
            <option value="CUSTOMER">Khách hàng</option>
            <option value="INACTIVE">Ngừng hợp tác</option>
          </select>
        </div>
        <p id="customerMessage" role="status" aria-live="polite"></p>
        <div class="customer-table-wrap">
          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>Doanh nghiệp</th>
                <th>Mã số thuế</th>
                <th>Ngành nghề</th>
                <th>Người phụ trách</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody id="customerRows"></tbody>
          </table>
        </div>
      </section>
            <!-- S3-04: Integrated duplicate detection and merge -->
      <section id="s304Panel" class="customer-box s304-panel" hidden>
        <div class="s304-panel-title">
          <h3>Khách hàng nghi ngờ trùng lặp</h3>
          <button id="s304ClosePanel" type="button">Đóng</button>
        </div>
        <p id="s304Message" role="status" aria-live="polite"></p>
        <div class="customer-table-wrap">
          <table>
            <thead><tr><th>STT</th><th>Khách hàng 1</th><th>Khách hàng 2</th><th>Điểm</th><th>Lý do</th><th>Thao tác</th></tr></thead>
            <tbody id="s304Rows"></tbody>
          </table>
        </div>
      </section>
      <dialog id="s304Dialog" class="s304-dialog">
        <h3>So sánh và gộp khách hàng</h3>
        <p>Chọn hồ sơ cần giữ lại. Liên hệ, cơ hội và tệp đính kèm sẽ được chuyển từ hồ sơ nguồn sang hồ sơ đích.</p>
        <div class="s304-compare">
          <div class="s304-customer-card"><label><input type="radio" name="s304Target" id="s304TargetFirst" value="first" checked /> Giữ khách hàng 1</label><div id="s304First"></div></div>
          <div class="s304-customer-card"><label><input type="radio" name="s304Target" id="s304TargetSecond" value="second" /> Giữ khách hàng 2</label><div id="s304Second"></div></div>
        </div>
        <p class="s304-warning">Khách hàng nguồn sẽ được đánh dấu đã gộp và ẩn khỏi danh sách. Thao tác không thể hoàn tác trên giao diện.</p>
        <p id="s304DialogMessage" role="alert" aria-live="polite"></p>
        <div class="customer-actions">
          <button id="s304Cancel" type="button">Hủy</button>
          <button id="s304Confirm" type="button">Xác nhận gộp</button>
        </div>
      </dialog>
<dialog id="customerDialog">
        <form id="customerForm">
          <p id="customerFormError" class="customer-error" role="alert" aria-live="assertive" hidden></p>
          <h3 id="customerFormTitle">Thông tin khách hàng</h3>
          <label
            >Tên doanh nghiệp<input
              name="companyName"
              maxlength="255"
              required /></label
          ><label>Mã số thuế<input name="taxCode" maxlength="50" /></label
          ><label>Ngành nghề<input name="industry" maxlength="150" /></label
          ><label>Quy mô<input name="companySize" maxlength="100" /></label
          ><label>Website<input name="website" maxlength="500" /></label
          ><label>Địa chỉ<input name="address" maxlength="500" /></label
          ><label id="customerOwnerLabel" hidden>
              Người phụ trách (ID)
              <input name="ownerId" readonly />
            </label>
            <p id="customerOwnerHint">Người phụ trách được hệ thống tự động xác định.</p><label
            >Trạng thái<select name="status">
              <option value="POTENTIAL">Tiềm năng</option>
              <option value="IN_PROGRESS">Đang giao dịch</option>
              <option value="CUSTOMER">Khách hàng</option>
              <option value="INACTIVE">Ngừng hợp tác</option>
            </select></label
          >
          <div class="customer-actions">
            <button type="button" id="customerCancel">Đóng</button
            ><button type="submit" id="customerSave">Lưu</button>
          </div>
        </form>
      </dialog>

      <dialog id="customerImportDialog">
        <h3>Nhập khách hàng từ Excel</h3>

        <p>Tải tệp mẫu, điền dữ liệu và chọn tệp Excel để kiểm tra.</p>

        <div class="customer-import-actions">
          <button type="button" id="downloadCustomerTemplate">Tải tệp mẫu</button>
          <input type="file" id="customerImportFile" accept=".xlsx,.xls">
        </div>

        <p id="customerImportMessage" role="status"></p>

        <div id="customerImportPreview" hidden>
          <h4>Kết quả kiểm tra</h4>
          <p id="customerImportSummary"></p>

          <div class="customer-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Dòng Excel</th>
                  <th>Mã số thuế</th>
                  <th>Chi tiết lỗi</th>
                </tr>
              </thead>
              <tbody id="customerImportErrors"></tbody>
            </table>
          </div>
        </div>

        <div class="customer-import-actions">
          <button type="button" id="previewCustomerImport">Kiểm tra Excel</button>
          <button type="button" id="executeCustomerImport" disabled>Xác nhận nhập</button>
          <button type="button" id="closeCustomerImport">Đóng</button>
        </div>
      </dialog>
    </main>
    <script>
      window.customerContextPath = "${pageContext.request.contextPath}";
      window.s304CanMerge = ${sessionScope.userRole eq "ADMIN" or sessionScope.userRole eq "MANAGER"};
    </script>
    <script
      src="${pageContext.request.contextPath}/assets/js/customers.js"
      defer
    ></script>
    <script src="${pageContext.request.contextPath}/assets/js/customer-import.js" defer></script>
  </body>
</html>
