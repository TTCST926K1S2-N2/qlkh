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
  </head>
  <body>
    <jsp:include page="/components/sidebar.jsp"/>
    <main class="customer-main">
      <header class="customer-header">
        <h2>Quản lý khách hàng doanh nghiệp</h2>
        <button id="addCustomer" type="button">Thêm khách hàng</button>
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
    </main>
    <script>
      window.customerContextPath = "${pageContext.request.contextPath}";
    </script>
    <script
      src="${pageContext.request.contextPath}/assets/js/customers.js"
      defer
    ></script>
  </body>
</html>
