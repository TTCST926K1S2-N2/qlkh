
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cơ cấu tổ chức kinh doanh - HỆ THỐNG QLKH</title>
    <style>
    body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background-color: #f4f6f9; font-family: Arial, sans-serif; 
        }
        /* Giới hạn khung nội dung chính đúng 1000px */
        .main-content { 
            padding: 30px; width: 100%; max-width: 1000px; box-sizing: border-box;
        }
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; 
            border-bottom: 2px solid #ecf0f1; padding-bottom: 15px; margin-bottom: 25px; 
        }
        .page-header h2 { margin: 0; color: #2c3e50; }
        .btn-add { background-color: #2ecc71; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; border: none; cursor: pointer; }
        .btn-add:hover { background-color: #27ae60; }
        .content-box { 
            background: #fff; padding: 20px; border-radius: 8px; 
            box-shadow: 0 2px 5px rgba(0,0,0,0.05); 
        }
        /* CSS cho Bảng danh sách */
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; }
        th { background-color: #f8f9fa; color: #2c3e50; font-weight: 600; font-size: 14px; }
        td { font-size: 14px; color: #333; }
        tr:hover { background-color: #f1f2f6; }
        
        /* CSS cho Nút Thao tác & Trạng thái */
        .btn-edit { background-color: #f39c12; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px; }
        .btn-delete { background-color: #e74c3c; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px; margin-left: 5px;}
        .btn-edit:hover { background-color: #d68910; }
        .btn-delete:hover { background-color: #c0392b; }
        
        .status { padding: 5px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; }
        .status.active { background-color: #d4edda; color: #155724; }
        .status.inactive { background-color: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <!-- Nhúng Menu Sidebar của Sprint 1 -->
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <div class="page-header">
            <h2>Quản lý cơ cấu tổ chức kinh doanh</h2>
            <button id="btnAdd" class="btn-add">+ Thêm đơn vị mới</button>
        </div>
        
        <div class="content-box">
            <table>
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>Mã đơn vị</th>
                        <th>Tên đơn vị</th>
                        <th>Đơn vị cha</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Dữ liệu mẫu (Tĩnh) để test UI -->
                    <tr>
                        <td>1</td>
                        <td>KV_MB</td>
                        <td>Khu vực Miền Bắc</td>
                        <td>-</td>
                        <td><span class="status active">Hoạt động</span></td>
                        <td>
                            <button class="btn-edit">Sửa</button>
                            <button class="btn-delete">Xóa</button>
                        </td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td>CN_HN</td>
                        <td>Chi nhánh Hà Nội</td>
                        <td>Khu vực Miền Bắc</td>
                        <td><span class="status active">Hoạt động</span></td>
                        <td>
                            <button class="btn-edit">Sửa</button>
                            <button class="btn-delete">Xóa</button>
                        </td>
                    </tr>
                    <tr>
                        <td>3</td>
                        <td>KV_MN</td>
                        <td>Khu vực Miền Nam</td>
                        <td>-</td>
                        <td><span class="status inactive">Tạm ngừng</span></td>
                        <td>
                            <button class="btn-edit">Sửa</button>
                            <button class="btn-delete">Xóa</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <!-- Khu vực Modal Form Thêm/Sửa -->
    <div id="salesOrgModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">Thêm mới đơn vị</h3>
                <span class="close-btn">&times;</span>
            </div>
            <div class="modal-body">
                <form id="salesOrgForm">
                    <div class="form-group">
                        <label>Mã đơn vị <span style="color:red">*</span></label>
                        <input type="text" id="orgCode" name="orgCode" required placeholder="Ví dụ: KV_MB">
                    </div>
                    <div class="form-group">
                        <label>Tên đơn vị <span style="color:red">*</span></label>
                        <input type="text" id="orgName" name="orgName" required placeholder="Ví dụ: Khu vực Miền Bắc">
                    </div>
                    <div class="form-group">
                        <label>Đơn vị cha</label>
                        <select id="parentId" name="parentId">
                            <option value="">-- Không có (Đơn vị gốc) --</option>
                            <option value="1">Khu vực Miền Bắc</option>
                            <option value="3">Khu vực Miền Nam</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Trạng thái</label>
                        <select id="status" name="status">
                            <option value="1">Hoạt động</option>
                            <option value="0">Tạm ngừng</option>
                        </select>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn-cancel">Hủy</button>
                        <button type="submit" class="btn-save">Lưu thông tin</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Bổ sung CSS cho Modal -->
    <style>
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.5); }
        .modal-content { background-color: #fff; margin: 5% auto; width: 450px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); }
        .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-bottom: 1px solid #eee; }
        .modal-header h3 { margin: 0; font-size: 18px; color: #2c3e50; }
        .close-btn { font-size: 24px; cursor: pointer; color: #888; }
        .close-btn:hover { color: #e74c3c; }
        .modal-body { padding: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: 500; font-size: 14px; color: #333; }
        .form-group input, .form-group select { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .form-actions { text-align: right; margin-top: 25px; }
        .btn-cancel { background-color: #95a5a6; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; margin-right: 10px; }
        .btn-save { background-color: #3498db; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; font-weight: bold; }
        .btn-cancel:hover { background-color: #7f8c8d; }
        .btn-save:hover { background-color: #2980b9; }
    </style>

    <!-- Script xử lý Đóng/Mở Modal -->
    <script>
        const modal = document.getElementById("salesOrgModal");
        const btnAdd = document.getElementById("btnAdd");
        const spanClose = document.getElementsByClassName("close-btn")[0];
        const btnCancel = document.querySelector(".btn-cancel");

        // Mở modal khi bấm Thêm mới
        btnAdd.onclick = function() {
            document.getElementById("modalTitle").innerText = "Thêm mới đơn vị";
            document.getElementById("salesOrgForm").reset();
            modal.style.display = "block";
        }

        // Tắt modal khi bấm nút X hoặc Hủy
        spanClose.onclick = function() { modal.style.display = "none"; }
        btnCancel.onclick = function() { modal.style.display = "none"; }

        // Tắt modal khi click ra ngoài vùng xám
        window.onclick = function(event) {
            if (event.target == modal) {
                modal.style.display = "none";
            }
        }
        // Lấy đối tượng form
    const form = document.getElementById("salesOrgForm");
    
    // Xử lý sự kiện khi bấm nút "Lưu thông tin" (submit form)
    form.onsubmit = function(event) {
        // 1. Ngăn chặn hành vi tự động tải lại trang (reload) mặc định của HTML
        event.preventDefault();

        // 2. Lấy giá trị người dùng nhập và xóa khoảng trắng thừa ở 2 đầu (trim)
        const orgCode = document.getElementById("orgCode").value.trim();
        const orgName = document.getElementById("orgName").value.trim();

        // 3. Thực hiện Validate (Kiểm tra dữ liệu)
        if (orgCode === "") {
            alert("Lỗi: Vui lòng nhập Mã đơn vị!");
            document.getElementById("orgCode").focus(); // Trỏ con trỏ chuột lại ô bị lỗi
            return false;
        }

        // Validate độ dài của Mã đơn vị (Giả sử tối đa 20 ký tự)
        if (orgCode.length > 20) {
            alert("Lỗi: Mã đơn vị không được vượt quá 20 ký tự!");
            document.getElementById("orgCode").focus();
            return false;
        }

        if (orgName === "") {
            alert("Lỗi: Vui lòng nhập Tên đơn vị!");
            document.getElementById("orgName").focus();
            return false;
        }

        // 4. Nếu vượt qua toàn bộ các bước kiểm tra -> Thành công
        // (Đến bước ghép Backend, chúng ta sẽ viết code gọi API AJAX ở đúng vị trí này)
        alert("Dữ liệu hợp lệ! Đang chuẩn bị gọi API lưu thông tin:\n- Mã: " + orgCode + "\n- Tên: " + orgName);
        
        // Mô phỏng việc lưu thành công: đóng Modal và làm sạch form
        modal.style.display = "none";
        form.reset();
    };
    // Bắt sự kiện click cho các nút Xóa trên bảng
        const deleteButtons = document.querySelectorAll('.btn-delete');
        deleteButtons.forEach(button => {
            button.onclick = function() {
                const confirmDelete = confirm("Bạn có chắc chắn muốn xóa danh mục này không? Thao tác này không thể hoàn tác.");
                if (confirmDelete) {
                    alert("Đã xác nhận! Sẽ gọi API để xóa danh mục khỏi cơ sở dữ liệu.");
                }
            };
        });
    </script>
</body>
</html>