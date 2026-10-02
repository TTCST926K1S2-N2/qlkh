<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh mục dùng chung - HỆ THỐNG QLKH</title>
    <style>
        body { 
            margin: 0; padding: 0; display: flex; justify-content: center; 
            background-color: #f4f6f9; font-family: Arial, sans-serif; 
        }
        /* Căn giữa và giới hạn khung 1000px đồng bộ với S2-06 */
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
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; }
        th { background-color: #f8f9fa; color: #2c3e50; font-weight: 600; font-size: 14px; }
        td { font-size: 14px; color: #333; }
        
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
    <!-- Nhúng Menu Sidebar của hệ thống (Giải quyết Yêu cầu 8) -->
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <div class="page-header">
            <h2>Quản lý danh mục dùng chung</h2>
            <button id="btnAdd" class="btn-add" onclick="openModal('create')">+ Thêm danh mục mới</button>
        </div>
        
        <div class="content-box">
            <table>
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>MÃ DANH MỤC</th>
                        <th>TÊN DANH MỤC</th>
                        <th>LOẠI DANH MỤC</th>
                        <th>MÔ TẢ</th>
                        <th>TRẠNG THÁI</th> <!-- Bổ sung cột trạng thái (Yêu cầu 6) -->
                        <th>THAO TÁC</th>
                    </tr>
                </thead>
                <!-- Đã xóa thẻ tbody lồng nhau (Yêu cầu 7) và xóa dữ liệu hard-code (Yêu cầu 1) -->
                <tbody id="tableBody">
                    <tr>
                        <td colspan="7" style="text-align: center; color: #7f8c8d;">Đang tải dữ liệu từ Backend...</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Hộp thoại Modal Thêm/Sửa Danh mục (Đã nâng cấp để dùng chung - Yêu cầu 9) -->
    <div id="categoryModal" class="modal" style="display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.4);">
        <div class="modal-content" style="background-color: #fff; margin: 5% auto; padding: 20px; border-radius: 8px; width: 500px; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 20px;">
                <h3 id="modalTitle" style="margin: 0; color: #2c3e50;">Thêm mới danh mục</h3>
                <span class="close" onclick="closeModal()" style="color: #aaa; font-size: 28px; font-weight: bold; cursor: pointer;">&times;</span>
            </div>
            
            <form id="categoryForm">
                <!-- Thẻ ẩn lưu ID để phân biệt Thêm hay Sửa -->
                <input type="hidden" id="catId" name="catId">

                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Mã danh mục <span style="color: red;">*</span></label>
                    <input type="text" id="catCode" required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                </div>
                
                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Tên danh mục <span style="color: red;">*</span></label>
                    <input type="text" id="catName" required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                </div>
                
                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Loại danh mục <span style="color: red;">*</span></label>
                    <!-- Chuẩn hóa Value Type theo chuẩn Backend (Yêu cầu 5) -->
                    <select id="catType" required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                        <option value="">-- Chọn loại danh mục --</option>
                        <option value="KENH_BAN_HANG">Kênh bán hàng</option>
                        <option value="LOAI_KHACH_HANG">Loại khách hàng</option>
                        <option value="NHOM_SAN_PHAM">Nhóm sản phẩm</option>
                    </select>
                </div>

                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Mô tả</label>
                    <textarea id="catDesc" rows="3" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;"></textarea>
                </div>
                
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Trạng thái</label>
                    <select id="catStatus" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                        <option value="1">Hoạt động</option>
                        <option value="0">Tạm ngừng</option>
                    </select>
                </div>
                
                <div style="text-align: right;">
                    <button type="button" onclick="closeModal()" style="padding: 8px 15px; border: none; background-color: #95a5a6; color: white; border-radius: 4px; cursor: pointer; margin-right: 10px;">Hủy</button>
                    <button type="submit" style="padding: 8px 15px; border: none; background-color: #3498db; color: white; border-radius: 4px; cursor: pointer;">Lưu thông tin</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Script chuẩn bị khung Call API (Giải quyết Yêu cầu 2, 3, 4, 10, 11) -->
    <script>
        const modal = document.getElementById("categoryModal");
        const form = document.getElementById("categoryForm");
        const tableBody = document.getElementById("tableBody");

        // Dữ liệu giả lập tạm thời chờ API Backend
        let mockCategories = [
            { id: 1, code: "KB_ONLINE", name: "Kênh Online", type: "KENH_BAN_HANG", typeName: "Kênh bán hàng", desc: "Bán qua Web, App", status: 1 },
            { id: 2, code: "SP_AODAI", name: "Áo dài truyền thống", type: "NHOM_SAN_PHAM", typeName: "Nhóm sản phẩm", desc: "Áo dài nam nữ", status: 1 }
        ];

        // 1. Hàm lấy danh sách và vẽ bảng (GET)
        function loadCategories() {
            // TODO: Thay thế bằng fetch('/qlkh/api/v1/categories') khi có BE
            tableBody.innerHTML = "";
            mockCategories.forEach((cat, index) => {
                const statusHtml = cat.status == 1 ? '<span class="status active">Hoạt động</span>' : '<span class="status inactive">Tạm ngừng</span>';
                const row = `<tr>
                    <td>${index + 1}</td>
                    <td>${cat.code}</td>
                    <td>${cat.name}</td>
                    <td>${cat.typeName}</td>
                    <td>${cat.desc}</td>
                    <td>${statusHtml}</td>
                    <td>
                        <button class="btn-edit" onclick="openModal('edit', ${cat.id})">Sửa</button>
                        <button class="btn-delete" onclick="deleteCategory(${cat.id})">Xóa</button>
                    </td>
                </tr>`;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        }

        // 2. Hàm mở Modal cho Thêm hoặc Sửa
        function openModal(mode, id = null) {
            form.reset();
            if (mode === 'create') {
                document.getElementById("modalTitle").innerText = "Thêm mới danh mục";
                document.getElementById("catId").value = "";
            } else if (mode === 'edit') {
                document.getElementById("modalTitle").innerText = "Cập nhật danh mục";
                // Tìm dữ liệu và đổ lên form (TODO: Gọi API GET /qlkh/api/v1/categories/{id})
                const cat = mockCategories.find(c => c.id === id);
                if(cat) {
                    document.getElementById("catId").value = cat.id;
                    document.getElementById("catCode").value = cat.code;
                    document.getElementById("catName").value = cat.name;
                    document.getElementById("catType").value = cat.type;
                    document.getElementById("catDesc").value = cat.desc;
                    document.getElementById("catStatus").value = cat.status;
                }
            }
            modal.style.display = "block";
        }

        // Đóng Modal
        function closeModal() { modal.style.display = "none"; }
        window.onclick = function(event) { if (event.target == modal) closeModal(); }

        // 3. Xử lý Lưu thông tin (POST/PUT)
        form.onsubmit = function(event) {
            event.preventDefault();
            const id = document.getElementById("catId").value;
            const payload = {
                code: document.getElementById("catCode").value.trim(),
                name: document.getElementById("catName").value.trim(),
                type: document.getElementById("catType").value,
                desc: document.getElementById("catDesc").value.trim(),
                status: document.getElementById("catStatus").value
            };

            if(id) {
                // Update (TODO: fetch PUT/PATCH '/qlkh/api/v1/categories/' + id)
                console.log("Đang gọi API CẬP NHẬT (PUT):", payload);
                alert("Đã gửi Request Cập nhật lên BE. Chờ phản hồi thành công...");
            } else {
                // Create (TODO: fetch POST '/qlkh/api/v1/categories')
                console.log("Đang gọi API THÊM MỚI (POST):", payload);
                alert("Đã gửi Request Thêm mới lên BE. Chờ phản hồi thành công...");
            }
            
            closeModal();
            // TODO: Gọi lại hàm loadCategories() bên trong block .then() của fetch
        };

        // 4. Xử lý Xóa (DELETE)
        function deleteCategory(id) {
            if (confirm("Bạn có chắc chắn muốn xóa danh mục này?")) {
                // TODO: fetch DELETE '/qlkh/api/v1/categories/' + id
                console.log("Đang gọi API XÓA (DELETE) ID:", id);
                alert("Đã gửi Request Xóa lên BE. Chờ phản hồi...");
                // TODO: Gọi lại hàm loadCategories() khi xóa thành công
            }
        }

        // Tự động tải dữ liệu khi vừa mở trang
        document.addEventListener("DOMContentLoaded", () => {
            loadCategories();
        });
    </script>
</body>
</html>