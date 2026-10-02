
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
       /* CSS cho Cấu trúc cây (Tree View) */
        .tree-container { padding: 10px; background: #fff; border-radius: 8px; width: 100%; box-sizing: border-box; }
        .tree, .tree ul { list-style-type: none; margin: 0; padding: 0; }
        .tree ul { padding-left: 30px; border-left: 1px dashed #bdc3c7; margin-left: 10px; margin-top: 5px; }
        .tree li { margin: 10px 0; }
        .tree-node { display: flex; align-items: center; padding: 10px 15px; background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; transition: background 0.2s; }
        .tree-node:hover { background-color: #f1f2f6; }
        .caret { cursor: pointer; user-select: none; width: 20px; height: 20px; display: inline-block; text-align: center; margin-right: 10px; font-weight: bold; color: #2c3e50; }
        .caret::before { content: "\25B6"; display: inline-block; transition: transform 0.2s; }
        .caret-down::before { transform: rotate(90deg); }
        .caret.empty::before { content: "\25CF"; font-size: 10px; color: #95a5a6; cursor: default; } /* Nút không có con */
        .node-title { flex-grow: 1; font-weight: 500; color: #2c3e50; font-size: 15px; }
        .node-actions { display: flex; gap: 5px; margin-left: 15px; }
        .btn-add-emp { background-color: #8e44ad; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 13px; }
        .btn-add-emp:hover { background-color: #9b59b6; }
        .nested { display: none; }
        .active-tree { display: block; }
        
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
            <!-- Container trống: JS sẽ lấy dữ liệu từ API và tự động vẽ cây vào đây -->
            <div id="treeContainer" class="tree-container">
                <div style="text-align: center; padding: 20px; color: #7f8c8d;">
                    Đang tải cấu trúc cây từ Backend...
                </div>
            </div>
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
                            <!-- Dữ liệu Đơn vị cha sẽ được đổ từ API của Backend -->
                        </select>
                    </div>
                    
                    <!-- Bổ sung trường Trưởng nhóm (Yêu cầu số 4) -->
                    <div class="form-group">
                        <label>Trưởng nhóm <span style="color:red">*</span></label>
                        <select id="leaderId" name="leaderId" required>
                            <option value="">-- Chọn trưởng nhóm --</option>
                            <!-- Dữ liệu Trưởng nhóm sẽ được đổ từ API -->
                        </select>
                    </div>

                    <!-- Bổ sung trường Khu vực địa lý (Yêu cầu số 6) -->
                    <div class="form-group">
                        <label>Khu vực địa lý <span style="color:red">*</span></label>
                        <select id="regionId" name="regionId" required>
                            <option value="">-- Chọn khu vực --</option>
                            <!-- Dữ liệu Khu vực sẽ được đổ từ API -->
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

    <!-- Script xử lý giao diện S2-06 -->
<script>
    const modal = document.getElementById("salesOrgModal");
    const btnAdd = document.getElementById("btnAdd");
    const spanClose = document.querySelector(".close-btn");
    const btnCancel = document.querySelector(".btn-cancel");
    const form = document.getElementById("salesOrgForm");
    const treeContainer = document.getElementById("treeContainer");

    const orgCodeInput = document.getElementById("orgCode");
    const orgNameInput = document.getElementById("orgName");
    const parentSelect = document.getElementById("parentId");
    const leaderSelect = document.getElementById("leaderId");
    const regionSelect = document.getElementById("regionId");

    function showBackendUnavailable() {
        treeContainer.innerHTML = `
            <div style="
                padding: 32px 20px;
                text-align: center;
                color: #6c757d;
                border: 1px dashed #ced4da;
                border-radius: 6px;
                background: #f8f9fa;">
                <strong>Chưa có dữ liệu cơ cấu tổ chức</strong>
                <div style="margin-top:8px">
                    Chức năng đang chờ API quản lý cơ cấu tổ chức từ Backend.
                </div>
            </div>
        `;
    }

    function openCreateModal() {
        form.reset();

        document.getElementById("modalTitle").innerText =
            "Thêm mới đơn vị";

        modal.style.display = "block";
        orgCodeInput.focus();
    }

    function closeModal() {
        modal.style.display = "none";
        form.reset();
    }

    btnAdd.addEventListener("click", openCreateModal);
    spanClose.addEventListener("click", closeModal);
    btnCancel.addEventListener("click", closeModal);

    window.addEventListener("click", function(event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    function validateForm() {
        const orgCode = orgCodeInput.value.trim();
        const orgName = orgNameInput.value.trim();

        if (!orgCode) {
            alert("Vui lòng nhập Mã đơn vị.");
            orgCodeInput.focus();
            return false;
        }

        if (orgCode.length > 20) {
            alert("Mã đơn vị không được vượt quá 20 ký tự.");
            orgCodeInput.focus();
            return false;
        }

        if (!orgName) {
            alert("Vui lòng nhập Tên đơn vị.");
            orgNameInput.focus();
            return false;
        }

        if (!leaderSelect.value) {
            alert("Vui lòng chọn Trưởng nhóm.");
            leaderSelect.focus();
            return false;
        }

        if (!regionSelect.value) {
            alert("Vui lòng chọn Khu vực địa lý.");
            regionSelect.focus();
            return false;
        }

        return true;
    }

    form.addEventListener("submit", function(event) {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        alert(
            "Chưa thể lưu dữ liệu vì API Backend S2-06 " +
            "chưa được cung cấp."
        );
    });

    /*
     * Không sử dụng dữ liệu hard-code hoặc mock.
     * Các danh sách này sẽ được tải từ Backend
     * khi API S2-06 được cung cấp.
     */
    parentSelect.disabled = true;
    leaderSelect.disabled = true;
    regionSelect.disabled = true;

    showBackendUnavailable();
</script>
</body>
</html>