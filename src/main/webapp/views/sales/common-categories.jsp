<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh mục dùng chung - HỆ THỐNG QLKH</title>
    <style>
        body { margin: 0; padding: 0; display: flex; background-color: #f4f6f9; font-family: Arial, sans-serif; }
        .main-content { flex-grow: 1; padding: 30px; width: 100%; }
        .page-header { 
            display: flex; justify-content: space-between; align-items: center; 
            border-bottom: 2px solid #ecf0f1; padding-bottom: 15px; 
            margin: 0 auto 25px auto; max-width: 1000px; 
        }
        .page-header h2 { margin: 0; color: #2c3e50; }
        .btn-add { background-color: #2ecc71; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; border: none; cursor: pointer; }
        .btn-add:hover { background-color: #27ae60; }
        .content-box { 
            background: #fff; padding: 20px; border-radius: 8px; 
            box-shadow: 0 2px 5px rgba(0,0,0,0.05); 
            margin: 0 auto; max-width: 1000px; 
        }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; }
        th { background-color: #f8f9fa; color: #2c3e50; font-weight: 600; font-size: 14px; }
        td { font-size: 14px; color: #333; }
    </style>
</head>
<body>
    <div class="main-content">
        <div class="page-header">
            <h2>Quản lý danh mục dùng chung</h2>
            <button id="btnAdd" class="btn-add">+ Thêm danh mục mới</button>
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
                        <th>THAO TÁC</th>
                    </tr>
                </thead>
                <tbody>
                    <tbody>
                    <tr>
                        <td>1</td>
                        <td>KB_ONLINE</td>
                        <td>Kênh Online</td>
                        <td>Kênh bán hàng</td>
                        <td>Bán hàng qua Website, App, Facebook...</td>
                        <td>
                            <button class="btn-edit" style="background-color: #f39c12; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">Sửa</button>
                            <button class="btn-delete" style="background-color: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">Xóa</button>
                        </td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td>SP_AODAI</td>
                        <td>Áo dài truyền thống</td>
                        <td>Nhóm sản phẩm</td>
                        <td>Các loại áo dài nam, nữ, cách tân</td>
                        <td>
                            <button class="btn-edit" style="background-color: #f39c12; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">Sửa</button>
                            <button class="btn-delete" style="background-color: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">Xóa</button>
                        </td>
                    </tr>
                </tbody>
                    <!-- Dữ liệu mẫu sẽ điền vào đây -->
                </tbody>
            </table>
        </div>
    </div>
    <!-- Hộp thoại Modal Thêm/Sửa Danh mục -->
    <div id="categoryModal" class="modal" style="display: none; position: fixed; z-index: 1; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.4);">
        <div class="modal-content" style="background-color: #fff; margin: 5% auto; padding: 20px; border-radius: 8px; width: 500px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 20px;">
                <h3 style="margin: 0; color: #2c3e50;">Thêm mới danh mục</h3>
                <span class="close" style="color: #aaa; font-size: 28px; font-weight: bold; cursor: pointer;">&times;</span>
            </div>
            
            <form id="categoryForm">
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
                    <select id="catType" required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                        <option value="">-- Chọn loại danh mục --</option>
                        <option value="kenh_ban">Kênh bán hàng</option>
                        <option value="loai_khach">Loại khách hàng</option>
                        <option value="nhom_sp">Nhóm sản phẩm</option>
                    </select>
                </div>

                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Mô tả</label>
                    <textarea id="catDesc" rows="3" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;"></textarea>
                </div>
                
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Trạng thái</label>
                    <select id="catStatus" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                        <option value="active">Hoạt động</option>
                        <option value="inactive">Tạm ngừng</option>
                    </select>
                </div>
                
                <div style="text-align: right;">
                    <button type="button" id="btnCancel" style="padding: 8px 15px; border: none; background-color: #95a5a6; color: white; border-radius: 4px; cursor: pointer; margin-right: 10px;">Hủy</button>
                    <button type="submit" style="padding: 8px 15px; border: none; background-color: #3498db; color: white; border-radius: 4px; cursor: pointer;">Lưu thông tin</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Script điều khiển Modal và Validate -->
    <script>
        const modal = document.getElementById("categoryModal");
        const btnAdd = document.getElementById("btnAdd");
        const spanClose = document.getElementsByClassName("close")[0];
        const btnCancel = document.getElementById("btnCancel");
        const form = document.getElementById("categoryForm");

        // Bật/tắt form
        btnAdd.onclick = function() { modal.style.display = "block"; }
        spanClose.onclick = function() { modal.style.display = "none"; }
        btnCancel.onclick = function() { modal.style.display = "none"; }
        window.onclick = function(event) {
            if (event.target == modal) { modal.style.display = "none"; }
        }

        // Validate và Submit form
        form.onsubmit = function(event) {
            event.preventDefault(); // Chặn reload trang
            
            const catCode = document.getElementById("catCode").value.trim();
            const catName = document.getElementById("catName").value.trim();
            const catType = document.getElementById("catType").value;

            // Bắt lỗi rỗng loại danh mục (Mã và Tên đã được HTML5 bắt qua thuộc tính required)
            if (catType === "") {
                alert("Vui lòng chọn Loại danh mục!");
                document.getElementById("catType").focus();
                return false;
            }

            alert("Dữ liệu hợp lệ! Đang chuẩn bị gọi API lưu:\n- Mã: " + catCode + "\n- Tên: " + catName + "\n- Loại: " + catType);
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