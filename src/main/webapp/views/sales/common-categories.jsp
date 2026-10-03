<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh mục dùng chung - HỆ THỐNG QLKH</title>
    <style>
        body {
            margin: 0; padding: 0; display: flex; justify-content: flex-start; min-height: 100vh; width: 100%;
            background-color: #f4f6f9; font-family: Arial, sans-serif;
        }
        /* Căn giữa và giới hạn khung 1000px đồng bộ với S2-06 */
        .main-content {
            flex: 1; min-width: 0; padding: 30px; width: auto; max-width: none; box-sizing: border-box;
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
            <div style="display:flex; gap:10px; flex-wrap:wrap; margin-bottom:15px;">
                <input
                    type="text"
                    id="searchInput"
                    placeholder="Tìm theo mã, tên, mô tả..."
                    style="flex:1; min-width:220px; padding:9px; border:1px solid #ccc; border-radius:4px;">

                <select
                    id="filterType"
                    style="padding:9px; border:1px solid #ccc; border-radius:4px;">
                    <option value="">Tất cả loại</option>
                </select>

                <select
                    id="filterStatus"
                    style="padding:9px; border:1px solid #ccc; border-radius:4px;">
                    <option value="">Tất cả trạng thái</option>
                    <option value="true">Hoạt động</option>
                    <option value="false">Tạm ngừng</option>
                </select>
            </div>

            <div
                id="messageBox"
                style="display:none; margin-bottom:15px; padding:10px 12px; border-radius:4px;">
            </div>

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
                    <input type="text" id="catCode" maxlength="50" required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                </div>

                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Tên danh mục <span style="color: red;">*</span></label>
                    <input type="text" id="catName" maxlength="255" required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
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
                    <textarea id="catDesc" rows="3" maxlength="500" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;"></textarea>
                </div>

                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px;">Trạng thái</label>
                    <select id="catStatus" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;">
                        <option value="true">Hoạt động</option>
                        <option value="false">Tạm ngừng</option>
                    </select>
                </div>

                <div style="text-align: right;">
                    <button type="button" onclick="closeModal()" style="padding: 8px 15px; border: none; background-color: #95a5a6; color: white; border-radius: 4px; cursor: pointer; margin-right: 10px;">Hủy</button>
                    <button type="submit" style="padding: 8px 15px; border: none; background-color: #3498db; color: white; border-radius: 4px; cursor: pointer;">Lưu thông tin</button>
                </div>
            </form>
        </div>
    </div>

    <!-- S2-07: Kết nối API quản lý danh mục dùng chung -->
<script>
    const API_URL = '${pageContext.request.contextPath}/categories';

    const modal = document.getElementById("categoryModal");
    const form = document.getElementById("categoryForm");
    const tableBody = document.getElementById("tableBody");

    const searchInput = document.getElementById("searchInput");
    const filterType = document.getElementById("filterType");
    const filterStatus = document.getElementById("filterStatus");
    const messageBox = document.getElementById("messageBox");

    let categories = [];

    function escapeHtml(value) {
        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function typeName(type) {
        const names = {
            KENH_BAN_HANG: "Kênh bán hàng",
            LOAI_KHACH_HANG: "Loại khách hàng",
            NHOM_SAN_PHAM: "Nhóm sản phẩm",
            CUSTOMER_TYPE: "Loại khách hàng",
            CUSTOMER_STATUS: "Trạng thái khách hàng"
        };

        return names[type] || type || "";
    }

    function showMessage(message, success = true) {
        messageBox.style.display = "block";
        messageBox.textContent = message;

        if (success) {
            messageBox.style.background = "#d4edda";
            messageBox.style.color = "#155724";
            messageBox.style.border = "1px solid #c3e6cb";
        } else {
            messageBox.style.background = "#f8d7da";
            messageBox.style.color = "#721c24";
            messageBox.style.border = "1px solid #f5c6cb";
        }
    }

    function hideMessage() {
        messageBox.style.display = "none";
        messageBox.textContent = "";
    }

    async function readError(response) {
        try {
            const data = await response.json();
            return data.message || "Có lỗi xảy ra.";
        } catch (e) {
            return "Có lỗi xảy ra khi xử lý yêu cầu.";
        }
    }

    function renderCategories(items) {
        if (!items.length) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7"
                        style="text-align:center; color:#7f8c8d; padding:25px;">
                        Không có danh mục phù hợp.
                    </td>
                </tr>
            `;
            return;
        }

        tableBody.innerHTML = items.map((cat, index) => {
            const active = cat.status === true;

            const statusHtml = active
                ? '<span class="status active">Hoạt động</span>'
                : '<span class="status inactive">Tạm ngừng</span>';

            return `
                <tr>
                    <td>\${index + 1}</td>
                    <td>\${escapeHtml(cat.categoryCode)}</td>
                    <td>\${escapeHtml(cat.categoryName)}</td>
                    <td>\${escapeHtml(typeName(cat.categoryType))}</td>
                    <td>\${escapeHtml(cat.description || "")}</td>
                    <td>\${statusHtml}</td>
                    <td>
                        <button
                            type="button"
                            class="btn-edit"
                            onclick="openModal('edit', \${Number(cat.id)})">
                            Sửa
                        </button>

                        <button
                            type="button"
                            class="btn-delete"
                            onclick="deleteCategory(\${Number(cat.id)})">
                            Xóa
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    function refreshTypeFilter() {
        const current = filterType.value;

        const types = [...new Set(
            categories
                .map(item => item.categoryType)
                .filter(Boolean)
        )].sort();

        filterType.innerHTML =
            '<option value="">Tất cả loại</option>' +
            types.map(type =>
                `<option value="\${escapeHtml(type)}">\${escapeHtml(typeName(type))}</option>`
            ).join("");

        if (types.includes(current)) {
            filterType.value = current;
        }
    }

    function applyFilters() {
        const keyword = searchInput.value.trim().toLowerCase();
        const type = filterType.value;
        const status = filterStatus.value;

        const result = categories.filter(cat => {
            const searchable = [
                cat.categoryCode,
                cat.categoryName,
                cat.categoryType,
                cat.description
            ]
                .map(value => String(value || "").toLowerCase())
                .join(" ");

            const matchKeyword =
                !keyword || searchable.includes(keyword);

            const matchType =
                !type || cat.categoryType === type;

            const matchStatus =
                status === "" ||
                String(cat.status) === status;

            return matchKeyword && matchType && matchStatus;
        });

        renderCategories(result);
    }

    async function loadCategories(clearMessage = true) {
        if (clearMessage) {
            hideMessage();
        }

        tableBody.innerHTML = `
            <tr>
                <td colspan="7"
                    style="text-align:center; color:#7f8c8d; padding:25px;">
                    Đang tải dữ liệu...
                </td>
            </tr>
        `;

        try {
            const response = await fetch(API_URL, {
                method: "GET",
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            const data = await response.json();

            categories = Array.isArray(data) ? data : [];

            refreshTypeFilter();
            applyFilters();

            return true;

        } catch (error) {
            categories = [];

            tableBody.innerHTML = `
                <tr>
                    <td colspan="7"
                        style="text-align:center; color:#c0392b; padding:25px;">
                        Không tải được dữ liệu danh mục.
                    </td>
                </tr>
            `;

            showMessage(error.message, false);
            return false;
        }
    }

    function ensureTypeOption(type) {
        if (!type) {
            return;
        }

        const typeSelect = document.getElementById("catType");

        const exists = Array.from(typeSelect.options)
            .some(option => option.value === type);

        if (!exists) {
            const option = document.createElement("option");
            option.value = type;
            option.textContent = typeName(type);
            typeSelect.appendChild(option);
        }
    }

    async function openModal(mode, id = null) {
        hideMessage();
        form.reset();

        document.getElementById("catId").value = "";
        document.getElementById("catStatus").value = "true";

        if (mode === "create") {
            document.getElementById("modalTitle").innerText =
                "Thêm mới danh mục";

            modal.style.display = "block";
            document.getElementById("catCode").focus();
            return;
        }

        document.getElementById("modalTitle").innerText =
            "Cập nhật danh mục";

        try {
            const response = await fetch(
                API_URL + "/" + encodeURIComponent(id),
                {
                    method: "GET",
                    credentials: "same-origin",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            const cat = await response.json();

            ensureTypeOption(cat.categoryType);

            document.getElementById("catId").value =
                cat.id;

            document.getElementById("catCode").value =
                cat.categoryCode || "";

            document.getElementById("catName").value =
                cat.categoryName || "";

            document.getElementById("catType").value =
                cat.categoryType || "";

            document.getElementById("catDesc").value =
                cat.description || "";

            document.getElementById("catStatus").value =
                String(cat.status);

            modal.style.display = "block";

        } catch (error) {
            showMessage(error.message, false);
        }
    }

    function closeModal() {
        modal.style.display = "none";
        form.reset();
    }

    function validateForm() {
        const code =
            document.getElementById("catCode").value.trim();

        const name =
            document.getElementById("catName").value.trim();

        const type =
            document.getElementById("catType").value.trim();

        const description =
            document.getElementById("catDesc").value.trim();

        if (!code) {
            alert("Vui lòng nhập Mã danh mục.");
            return false;
        }

        if (code.length > 50) {
            alert("Mã danh mục không được vượt quá 50 ký tự.");
            return false;
        }

        if (!name) {
            alert("Vui lòng nhập Tên danh mục.");
            return false;
        }

        if (name.length > 255) {
            alert("Tên danh mục không được vượt quá 255 ký tự.");
            return false;
        }

        if (!type) {
            alert("Vui lòng chọn Loại danh mục.");
            return false;
        }

        if (description.length > 500) {
            alert("Mô tả không được vượt quá 500 ký tự.");
            return false;
        }

        return true;
    }

    form.addEventListener("submit", async function(event) {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        const id =
            document.getElementById("catId").value;

        const params = new URLSearchParams();

        params.append(
            "categoryCode",
            document.getElementById("catCode").value.trim()
        );

        params.append(
            "categoryName",
            document.getElementById("catName").value.trim()
        );

        params.append(
            "categoryType",
            document.getElementById("catType").value
        );

        params.append(
            "description",
            document.getElementById("catDesc").value.trim()
        );

        params.append(
            "status",
            document.getElementById("catStatus").value
        );

        const method = id ? "PUT" : "POST";

        const url = id
            ? API_URL + "/" + encodeURIComponent(id)
            : API_URL;

        try {
            const response = await fetch(url, {
                method: method,
                credentials: "same-origin",
                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded;charset=UTF-8",
                    "Accept": "application/json"
                },
                body: params.toString()
            });

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            closeModal();

            const successMessage = id
                ? "Cập nhật danh mục thành công."
                : "Thêm danh mục thành công.";

            const loaded = await loadCategories(false);

            if (loaded) {
                showMessage(successMessage, true);
            }

        } catch (error) {
            showMessage(error.message, false);
        }
    });

    async function deleteCategory(id) {
        if (!confirm("Bạn có chắc chắn muốn xóa danh mục này?")) {
            return;
        }

        hideMessage();

        try {
            const response = await fetch(
                API_URL + "/" + encodeURIComponent(id),
                {
                    method: "DELETE",
                    credentials: "same-origin",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            if (!response.ok) {
                throw new Error(await readError(response));
            }

            const loaded = await loadCategories(false);

            if (loaded) {
                showMessage("Xóa danh mục thành công.", true);
            }

        } catch (error) {
            showMessage(error.message, false);
        }
    }

    searchInput.addEventListener("input", applyFilters);
    filterType.addEventListener("change", applyFilters);
    filterStatus.addEventListener("change", applyFilters);

    window.addEventListener("click", function(event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    document.addEventListener(
        "DOMContentLoaded",
        () => loadCategories()
    );
</script>
</body>
</html>