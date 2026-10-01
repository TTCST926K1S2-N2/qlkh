/**
 * FE Script cho Sprint 2 (S2-05: Quản lý sản phẩm, dịch vụ và bảng giá)
 * Người thực hiện: Ma Thị Hiện
 */

const API_BASE_URL = '/api/v1/products';

let productList = [];
let deleteTargetId = null;

let productModal, deleteModal, liveToast;

document.addEventListener('DOMContentLoaded', () => {

    productModal = new bootstrap.Modal(
        document.getElementById('productModal')
    );

    deleteModal = new bootstrap.Modal(
        document.getElementById('deleteModal')
    );

    liveToast = new bootstrap.Toast(
        document.getElementById('liveToast')
    );

    fetchProducts();
    fetchPriceLists();

    // Event listener cho Form submit
    document
        .getElementById('productForm')
        .addEventListener('submit', handleFormSubmit);

    document
        .getElementById('btnConfirmDelete')
        .addEventListener('click', executeDelete);
});


// =====================================================
// 1. KẾT NỐI API & HIỂN THỊ DANH SÁCH
// =====================================================

async function fetchProducts() {

    try {

        // Khi kết nối Backend thật, sử dụng:
        // const res = await fetch(API_BASE_URL);
        // productList = await res.json();

        // Dữ liệu giả lập để test giao diện
        productList = [

            {
                id: 1,
                code: 'SP001',
                name: 'Phần mềm Quản lý Khách hàng',
                type: 'PRODUCT',
                unit: 'Gói',
                basePrice: 5000000,
                status: 'ACTIVE'
            },

            {
                id: 2,
                code: 'DV001',
                name: 'Dịch vụ Bảo trì Hệ thống',
                type: 'SERVICE',
                unit: 'Tháng',
                basePrice: 1200000,
                status: 'ACTIVE'
            },

            {
                id: 3,
                code: 'SP002',
                name: 'Thẻ từ nhân viên',
                type: 'PRODUCT',
                unit: 'Cái',
                basePrice: 50000,
                status: 'INACTIVE'
            }

        ];

        renderProductTable(productList);

    } catch (error) {

        showToast(
            'Lỗi khi tải danh sách sản phẩm/dịch vụ',
            'danger'
        );
    }
}


function renderProductTable(data) {

    const tbody = document.getElementById('productTableBody');

    tbody.innerHTML = '';

    if (!data || data.length === 0) {

        tbody.innerHTML = `
            <tr>
                <td colspan="8"
                    class="text-center text-muted py-4">

                    Không tìm thấy dữ liệu phù hợp

                </td>
            </tr>
        `;

        return;
    }


    data.forEach((item, index) => {

        // Không sử dụng icon
        const typeBadge =
            item.type === 'PRODUCT'
                ? `<span class="badge badge-product">Sản phẩm</span>`
                : `<span class="badge badge-service">Dịch vụ</span>`;


        const statusBadge =
            item.status === 'ACTIVE'
                ? `<span class="badge bg-success-subtle text-success">
                        Đang kinh doanh
                   </span>`
                : `<span class="badge bg-secondary-subtle text-secondary">
                        Ngừng kinh doanh
                   </span>`;


        const row = `

            <tr>

                <td>
                    ${index + 1}
                </td>

                <td class="fw-bold">
                    ${item.code}
                </td>

                <td>
                    ${item.name}
                </td>

                <td>
                    ${typeBadge}
                </td>

                <td>
                    ${item.unit}
                </td>

                <td class="fw-bold text-primary">
                    ${formatCurrency(item.basePrice)}
                </td>

                <td>
                    ${statusBadge}
                </td>

                <td class="text-center">

                    <button
                        class="btn btn-sm btn-outline-primary me-1"
                        onclick="openEditProductModal(${item.id})">

                        Sửa

                    </button>

                    <button
                        class="btn btn-sm btn-outline-danger"
                        onclick="openDeleteModal(${item.id}, '${item.name}')">

                        Xóa

                    </button>

                </td>

            </tr>

        `;

        tbody.innerHTML += row;
    });
}


// =====================================================
// 2. GIẢ LẬP BẢNG GIÁ
// =====================================================

function fetchPriceLists() {

    const priceLists = [

        {
            code: 'BG2026_STANDARD',
            name: 'Bảng giá Niêm yết 2026',
            startDate: '01/01/2026',
            status: 'ACTIVE'
        },

        {
            code: 'BG2026_VIP',
            name: 'Bảng giá Ưu đãi Đối tác VIP',
            startDate: '15/02/2026',
            status: 'ACTIVE'
        }

    ];


    const tbody = document.getElementById(
        'priceListTableBody'
    );

    tbody.innerHTML = '';


    priceLists.forEach(item => {

        tbody.innerHTML += `

            <tr>

                <td class="fw-bold">
                    ${item.code}
                </td>

                <td>
                    ${item.name}
                </td>

                <td>
                    ${item.startDate}
                </td>

                <td>

                    <span class="badge bg-success-subtle text-success">
                        Đang áp dụng
                    </span>

                </td>

                <td class="text-center">

                    <button
                        class="btn btn-sm btn-outline-secondary">

                        Xem chi tiết

                    </button>

                </td>

            </tr>

        `;
    });
}


// =====================================================
// 3. TÌM KIẾM VÀ LỌC
// =====================================================

function handleSearch() {

    const keyword =
        document
            .getElementById('searchKeyword')
            .value
            .trim()
            .toLowerCase();


    const type =
        document.getElementById('filterType').value;


    const status =
        document.getElementById('filterStatus').value;


    const filtered = productList.filter(item => {

        const matchesKeyword =
            item.name
                .toLowerCase()
                .includes(keyword)
            ||
            item.code
                .toLowerCase()
                .includes(keyword);


        const matchesType =
            type === '' || item.type === type;


        const matchesStatus =
            status === '' || item.status === status;


        return (
            matchesKeyword &&
            matchesType &&
            matchesStatus
        );
    });


    renderProductTable(filtered);
}


function resetFilter() {

    document
        .getElementById('filterForm')
        .reset();

    renderProductTable(productList);
}


// =====================================================
// 4. FORM MODAL & VALIDATION
// =====================================================

function openAddProductModal() {

    document
        .getElementById('productForm')
        .reset();


    document
        .getElementById('productForm')
        .classList
        .remove('was-validated');


    document
        .getElementById('productId')
        .value = '';


    document
        .getElementById('productModalTitle')
        .textContent =
        'Thêm mới Sản phẩm / Dịch vụ';


    productModal.show();
}


function openEditProductModal(id) {

    const item =
        productList.find(
            p => p.id === id
        );


    if (!item) {
        return;
    }


    document
        .getElementById('productForm')
        .classList
        .remove('was-validated');


    document
        .getElementById('productId')
        .value = item.id;


    document
        .getElementById('productCode')
        .value = item.code;


    document
        .getElementById('productType')
        .value = item.type;


    document
        .getElementById('productName')
        .value = item.name;


    document
        .getElementById('unit')
        .value = item.unit;


    document
        .getElementById('basePrice')
        .value = item.basePrice;


    document
        .getElementById('status')
        .value = item.status;


    document
        .getElementById('description')
        .value =
        item.description || '';


    document
        .getElementById('productModalTitle')
        .textContent =
        'Chỉnh sửa Sản phẩm / Dịch vụ';


    productModal.show();
}


async function handleFormSubmit(e) {

    e.preventDefault();

    const form = e.target;


    if (!form.checkValidity()) {

        e.stopPropagation();

        form.classList.add('was-validated');

        return;
    }


    const id =
        document
            .getElementById('productId')
            .value;


    const productData = {

        code:
            document
                .getElementById('productCode')
                .value
                .trim(),

        type:
            document
                .getElementById('productType')
                .value,

        name:
            document
                .getElementById('productName')
                .value
                .trim(),

        unit:
            document
                .getElementById('unit')
                .value
                .trim(),

        basePrice:
            parseFloat(
                document
                    .getElementById('basePrice')
                    .value
            ),

        status:
            document
                .getElementById('status')
                .value,

        description:
            document
                .getElementById('description')
                .value
                .trim()
    };


    try {

        if (id) {

            // Edit Mode

            // Khi kết nối Backend thật:
            // await fetch(`${API_BASE_URL}/${id}`, {
            //     method: 'PUT',
            //     headers: {
            //         'Content-Type': 'application/json'
            //     },
            //     body: JSON.stringify(productData)
            // });


            const index =
                productList.findIndex(
                    p => p.id === parseInt(id)
                );


            if (index !== -1) {

                productList[index] = {
                    id: parseInt(id),
                    ...productData
                };
            }


            showToast(
                'Cập nhật thông tin thành công!',
                'success'
            );

        } else {

            // Add Mode

            // Khi kết nối Backend thật:
            // await fetch(API_BASE_URL, {
            //     method: 'POST',
            //     headers: {
            //         'Content-Type': 'application/json'
            //     },
            //     body: JSON.stringify(productData)
            // });


            const newProduct = {
                id: Date.now(),
                ...productData
            };


            productList.unshift(newProduct);


            showToast(
                'Thêm mới thành công!',
                'success'
            );
        }


        productModal.hide();

        renderProductTable(productList);

    } catch (err) {

        showToast(
            'Có lỗi xảy ra khi lưu thông tin!',
            'danger'
        );
    }
}


// =====================================================
// 5. CHỨC NĂNG XÓA
// =====================================================

function openDeleteModal(id, name) {

    deleteTargetId = id;

    document
        .getElementById('deleteTargetName')
        .textContent = name;

    deleteModal.show();
}


async function executeDelete() {

    if (!deleteTargetId) {
        return;
    }


    try {

        // Khi kết nối Backend thật:
        // await fetch(`${API_BASE_URL}/${deleteTargetId}`, {
        //     method: 'DELETE'
        // });


        productList =
            productList.filter(
                p => p.id !== deleteTargetId
            );


        showToast(
            'Xóa thành công!',
            'success'
        );


        deleteModal.hide();

        renderProductTable(productList);


        deleteTargetId = null;

    } catch (error) {

        showToast(
            'Xóa thất bại. Vui lòng thử lại!',
            'danger'
        );
    }
}


// =====================================================
// 6. HÀM BỔ TRỢ
// =====================================================

function showToast(message, type = 'success') {

    const toastEl =
        document.getElementById('liveToast');


    const msgEl =
        document.getElementById('toastMessage');


    toastEl.className =
        `toast align-items-center text-white border-0 bg-${type}`;


    msgEl.textContent = message;

    liveToast.show();
}


function formatCurrency(amount) {

    return new Intl.NumberFormat(
        'vi-VN',
        {
            style: 'currency',
            currency: 'VND'
        }
    ).format(amount);
}


// =====================================================
// 7. THÊM BẢNG GIÁ
// =====================================================

function openAddPriceListModal() {

    showToast(
        'Chức năng tạo mới Bảng giá đang phát triển!',
        'primary'
    );
}