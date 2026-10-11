<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chăm sóc khách hàng</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/customer-care.css">
</head>
<body>
<main class="care-container"
      data-context="${pageContext.request.contextPath}">

    <header class="care-header">
        <a class="care-back"
           href="${pageContext.request.contextPath}/customers">
            Quay lại Quản lý khách hàng
        </a>
        <h1>Chăm sóc khách hàng</h1>
        <p>Quản lý yêu cầu hỗ trợ và chăm sóc sau bán</p>
    </header>

    <section class="care-panel">
        <h2>Chọn khách hàng</h2>
        <div class="care-row">
            <label for="customerId">Mã khách hàng</label>
            <input id="customerId" type="number" min="1"
                   placeholder="Nhập mã khách hàng">
            <button id="loadCustomer" type="button">
                Xem yêu cầu
            </button>
        </div>
        <p id="riskWarning" class="risk-warning" hidden></p>
    </section>

    <section class="care-panel">
        <h2>Tạo yêu cầu hỗ trợ</h2>

        <form id="createCareForm">
            <label for="careTitle">Tiêu đề</label>
            <input id="careTitle" maxlength="255" required>

            <label for="careDescription">Nội dung hỗ trợ</label>
            <textarea id="careDescription" rows="4"
                      maxlength="10000" required></textarea>

            <label for="carePriority">Mức độ ưu tiên</label>
            <select id="carePriority">
                <option value="LOW">Thấp</option>
                <option value="MEDIUM" selected>Trung bình</option>
                <option value="HIGH">Cao</option>
                <option value="URGENT">Khẩn cấp</option>
            </select>

            <label for="careAssignee">Mã nhân viên phụ trách</label>
            <input id="careAssignee" type="number" min="1"
                   placeholder="Để trống nếu chưa phân công">

            <button type="submit">Tạo yêu cầu</button>
        </form>
    </section>

    <section class="care-panel">
        <h2>Danh sách yêu cầu hỗ trợ</h2>

        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>Mã</th>
                    <th>Tiêu đề</th>
                    <th>Ưu tiên</th>
                    <th>Trạng thái</th>
                    <th>Người phụ trách</th>
                    <th>Thao tác</th>
                </tr>
                </thead>
                <tbody id="careTableBody">
                <tr>
                    <td colspan="6">Chọn khách hàng để xem dữ liệu.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>

    <section class="care-panel" id="editCarePanel" hidden>
        <h2>Cập nhật yêu cầu</h2>

        <form id="editCareForm">
            <input type="hidden" id="editCareId">

            <label for="editCareStatus">Trạng thái</label>
            <select id="editCareStatus">
                <option value="OPEN">Mới</option>
                <option value="IN_PROGRESS">Đang xử lý</option>
                <option value="RESOLVED">Đã giải quyết</option>
                <option value="CLOSED">Đã đóng</option>
            </select>

            <label for="editCarePriority">Mức độ ưu tiên</label>
            <select id="editCarePriority">
                <option value="LOW">Thấp</option>
                <option value="MEDIUM">Trung bình</option>
                <option value="HIGH">Cao</option>
                <option value="URGENT">Khẩn cấp</option>
            </select>

            <label for="editCareAssignee">Mã nhân viên phụ trách</label>
            <input id="editCareAssignee" type="number" min="1">

            <label for="editCareNote">Ghi chú xử lý</label>
            <textarea id="editCareNote" rows="4"
                      maxlength="10000"></textarea>

            <button type="submit">Lưu thay đổi</button>
            <button type="button" id="cancelEdit"
                    class="secondary-button">Hủy</button>
        </form>
    </section>

    <p id="careMessage" role="status" aria-live="polite"></p>
</main>

<script defer
        src="${pageContext.request.contextPath}/assets/js/customer-care.js">
</script>
</body>
</html>