
const CURRENT_ROLE =
    (document.body.dataset.userRole || '')
        .toUpperCase();

const CAN_VIEW_COST =
    CURRENT_ROLE === 'MANAGER';

const CAN_MANAGE =
    CURRENT_ROLE === 'MANAGER'
    || CURRENT_ROLE === 'ADMIN';
const CONTEXT_PATH = document.body.dataset.contextPath || '';

const PRODUCT_API = `${CONTEXT_PATH}/api/v1/products/`;
const PRICE_LIST_API = `${CONTEXT_PATH}/api/v1/price-lists/`;

let productList = [];
let priceListData = [];

let currentPriceListId = null;
let currentPriceListItems = [];

let deleteTarget = {
    type: null,
    id: null
};

let productModal;
let priceListModal;
let priceListDetailModal;
let priceListItemModal;
let deleteModal;
let liveToast;


document.addEventListener('DOMContentLoaded', () => {

    productModal = new bootstrap.Modal(
        document.getElementById('productModal')
    );

    priceListModal = new bootstrap.Modal(
        document.getElementById('priceListModal')
    );

    priceListDetailModal = new bootstrap.Modal(
        document.getElementById('priceListDetailModal')
    );

    priceListItemModal = new bootstrap.Modal(
        document.getElementById('priceListItemModal')
    );

    deleteModal = new bootstrap.Modal(
        document.getElementById('deleteModal')
    );

    liveToast = new bootstrap.Toast(
        document.getElementById('liveToast'),
        {
            delay: 3500
        }
    );

    bindEvents();

    loadInitialData();
});

function bindEvents() {

    document
        .getElementById('btnAddProduct')
        .addEventListener('click', openAddProductModal);

    document
        .getElementById('btnAddPriceList')
        .addEventListener('click', openAddPriceListModal);

    document
        .getElementById('filterForm')
        .addEventListener('submit', handleSearch);

    document
        .getElementById('btnResetFilter')
        .addEventListener('click', resetFilter);

    document
        .getElementById('productForm')
        .addEventListener('submit', handleProductSubmit);

    document
        .getElementById('priceListForm')
        .addEventListener('submit', handlePriceListSubmit);

    document
        .getElementById('priceListItemForm')
        .addEventListener('submit', handlePriceListItemSubmit);

    document
        .getElementById('btnAddPriceListItem')
        .addEventListener('click', openAddPriceListItemModal);

    document
        .getElementById('btnConfirmDelete')
        .addEventListener('click', executeDelete);

    document
        .getElementById('productTableBody')
        .addEventListener('click', handleProductTableClick);

    document
        .getElementById('priceListTableBody')
        .addEventListener('click', handlePriceListTableClick);

    document
        .getElementById('priceListItemTableBody')
        .addEventListener('click', handlePriceListItemTableClick);
}

async function loadInitialData() {

    await Promise.all([
        fetchProducts(),
        fetchPriceLists()
    ]);
}

async function fetchProducts() {

    renderProductLoading();

    try {

        const response = await fetch(PRODUCT_API, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(response, 'Không thể tải danh sách sản phẩm.')
            );
        }

        const data = await response.json();

        productList = Array.isArray(data) ? data : [];

        renderProductTable(productList);

    } catch (error) {

        productList = [];

        renderProductError();

        showToast(
            error.message || 'Không thể tải danh sách sản phẩm.',
            'danger'
        );
    }
}

function renderProductLoading() {

    const tbody = document.getElementById('productTableBody');

    tbody.replaceChildren();

    const row = document.createElement('tr');
    const cell = document.createElement('td');

    cell.colSpan = CAN_VIEW_COST ? 10 : 9;
    cell.className = 'text-center text-muted py-4';
    cell.textContent = 'Đang tải dữ liệu...';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderProductError() {

    const tbody = document.getElementById('productTableBody');

    tbody.replaceChildren();

    const row = document.createElement('tr');
    const cell = document.createElement('td');

    cell.colSpan = CAN_VIEW_COST ? 10 : 9;
    cell.className = 'text-center text-danger py-4';
    cell.textContent = 'Không thể tải dữ liệu sản phẩm / dịch vụ.';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderProductTable(data) {

    const tbody = document.getElementById('productTableBody');

    tbody.replaceChildren();

    if (!data || data.length === 0) {

        const row = document.createElement('tr');
        const cell = document.createElement('td');

        cell.colSpan = CAN_VIEW_COST ? 10 : 9;
        cell.className = 'text-center text-muted py-4';
        cell.textContent = 'Chưa có dữ liệu phù hợp.';

        row.appendChild(cell);
        tbody.appendChild(row);

        return;
    }

    data.forEach((item, index) => {

        const row = document.createElement('tr');

        addTextCell(row, index + 1);
        addTextCell(row, item.code, 'fw-semibold');
        addTextCell(row, item.name);

        const typeCell = document.createElement('td');

        const typeBadge = document.createElement('span');

        typeBadge.className =
            item.type === 'PRODUCT'
                ? 'badge badge-product'
                : 'badge badge-service';

        typeBadge.textContent =
            item.type === 'PRODUCT'
                ? 'Sản phẩm'
                : 'Dịch vụ';

        typeCell.appendChild(typeBadge);
        row.appendChild(typeCell);

        addTextCell(row, item.unit);

        addTextCell(
            row,
            formatCurrency(item.basePrice),
            'fw-semibold text-primary'
        );

        addTextCell(
            row,
            formatCurrency(item.floorPrice)
        );

        if (CAN_VIEW_COST) {

            addTextCell(
                row,
                item.costPrice == null
                    ? '-'
                    : formatCurrency(item.costPrice),
                'manager-only-cost'
            );
        }

        const statusCell = document.createElement('td');

        const statusBadge = document.createElement('span');

        statusBadge.className =
            item.status === 'ACTIVE'
                ? 'badge bg-success-subtle text-success'
                : 'badge bg-secondary-subtle text-secondary';

        statusBadge.textContent =
            item.status === 'ACTIVE'
                ? 'Đang kinh doanh'
                : 'Ngừng kinh doanh';

        statusCell.appendChild(statusBadge);
        row.appendChild(statusCell);

        const actionCell = document.createElement('td');

        actionCell.className = 'text-center';

        const editButton = document.createElement('button');

        editButton.type = 'button';
        editButton.className =
            'btn btn-sm btn-outline-primary me-1';
        editButton.dataset.action = 'edit';
        editButton.dataset.id = item.id;
        editButton.textContent = 'Sửa';

        const deleteButton = document.createElement('button');

        deleteButton.type = 'button';
        deleteButton.className =
            'btn btn-sm btn-outline-danger';
        deleteButton.dataset.action = 'delete';
        deleteButton.dataset.id = item.id;
        deleteButton.textContent = 'Ngừng';

        actionCell.appendChild(editButton);

        if (item.status === 'ACTIVE') {
            actionCell.appendChild(deleteButton);
        }

        row.appendChild(actionCell);

        tbody.appendChild(row);
    });
}

function handleProductTableClick(event) {

    const button = event.target.closest('button');

    if (!button) {
        return;
    }

    const id = Number(button.dataset.id);
    const action = button.dataset.action;

    if (!id) {
        return;
    }

    if (action === 'edit') {
        openEditProductModal(id);
    }

    if (action === 'delete') {
        openProductDeleteModal(id);
    }
}

function openAddProductModal() {

    const form = document.getElementById('productForm');

    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('productId').value = '';

    document.getElementById('status').value = 'ACTIVE';

    document.getElementById('productModalTitle').textContent =
        'Thêm mới Sản phẩm / Dịch vụ';

    productModal.show();
}

function openEditProductModal(id) {

    const item = productList.find(
        product => Number(product.id) === Number(id)
    );

    if (!item) {
        showToast('Không tìm thấy sản phẩm.', 'danger');
        return;
    }

    const form = document.getElementById('productForm');

    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('productId').value = item.id;
    document.getElementById('productCode').value = item.code || '';
    document.getElementById('productType').value = item.type || '';
    document.getElementById('productName').value = item.name || '';
    document.getElementById('unit').value = item.unit || '';
    document.getElementById('basePrice').value =
        item.basePrice ?? '';
    document.getElementById('floorPrice').value =
        item.floorPrice ?? '';

    const costPriceInput =
        document.getElementById('costPrice');

    if (costPriceInput && CAN_VIEW_COST) {
        costPriceInput.value =
            item.costPrice ?? '';
    }
    document.getElementById('status').value =
        item.status || 'ACTIVE';
    document.getElementById('description').value =
        item.description || '';

    document.getElementById('productModalTitle').textContent =
        'Chỉnh sửa Sản phẩm / Dịch vụ';

    productModal.show();
}

async function handleProductSubmit(event) {

    event.preventDefault();

    const form = event.target;

    if (!form.checkValidity()) {

        form.classList.add('was-validated');

        return;
    }

    const basePrice = Number(
        document.getElementById('basePrice').value
    );

    const floorPrice = Number(
        document.getElementById('floorPrice').value
    );

    if (floorPrice > basePrice) {

        showToast(
            'Giá sàn không được lớn hơn giá niêm yết.',
            'danger'
        );

        return;
    }

    const id =
        document.getElementById('productId').value.trim();

    const productData = {
        code: document.getElementById('productCode').value.trim(),
        name: document.getElementById('productName').value.trim(),
        type: document.getElementById('productType').value,
        unit: document.getElementById('unit').value.trim(),
        basePrice: basePrice,
        floorPrice: floorPrice,

        ...(CAN_VIEW_COST
            && document.getElementById('costPrice')
            && document.getElementById('costPrice').value !== ''
            ? {
                costPrice: Number(
                    document.getElementById('costPrice').value
                )
            }
            : {}),
        status: document.getElementById('status').value,
        description:
            document.getElementById('description').value.trim()
    };

    const saveButton =
        document.getElementById('btnSaveProduct');

    setButtonLoading(saveButton, true, 'Đang lưu...');

    try {

        let response;

        if (id) {

            response = await fetch(
                `${PRODUCT_API}${encodeURIComponent(id)}`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(productData)
                }
            );

        } else {

            response = await fetch(
                PRODUCT_API,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(productData)
                }
            );
        }

        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response,
                    'Không thể lưu sản phẩm.'
                )
            );
        }

        const savedProduct =
            await parseJsonSafely(response);

        if (savedProduct && savedProduct.id) {

            if (id) {

                const index = productList.findIndex(
                    product =>
                        Number(product.id) === Number(id)
                );

                if (index !== -1) {
                    productList[index] = savedProduct;
                }

            } else {

                productList.unshift(savedProduct);
            }

        } else {

            await fetchProducts();
        }

        productModal.hide();

        renderProductTable(productList);

        showToast(
            id
                ? 'Cập nhật sản phẩm thành công.'
                : 'Thêm sản phẩm thành công.',
            'success'
        );

    } catch (error) {

        showToast(
            error.message || 'Có lỗi xảy ra khi lưu sản phẩm.',
            'danger'
        );

    } finally {

        setButtonLoading(saveButton, false, 'Lưu');
    }
}

function openProductDeleteModal(id) {

    const item = productList.find(
        product => Number(product.id) === Number(id)
    );

    if (!item) {
        return;
    }

    deleteTarget = {
        type: 'product',
        id: Number(id)
    };

    document.getElementById('deleteTargetName').textContent =
        item.name || item.code || '';

    document.getElementById('btnConfirmDelete')
        .textContent = 'Ngừng kinh doanh';

    deleteModal.show();
}

async function fetchPriceLists() {

    renderPriceListLoading();

    try {

        const response = await fetch(
            PRICE_LIST_API,
            {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            }
        );

        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response,
                    'Không thể tải danh sách bảng giá.'
                )
            );
        }

        const data = await response.json();

        priceListData = Array.isArray(data) ? data : [];

        renderPriceListTable(priceListData);

    } catch (error) {

        priceListData = [];

        renderPriceListError();

        showToast(
            error.message || 'Không thể tải danh sách bảng giá.',
            'danger'
        );
    }
}

function renderPriceListLoading() {

    const tbody =
        document.getElementById('priceListTableBody');

    tbody.replaceChildren();

    const row = document.createElement('tr');
    const cell = document.createElement('td');

    cell.colSpan = 5;
    cell.className = 'text-center text-muted py-4';
    cell.textContent = 'Đang tải dữ liệu...';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderPriceListError() {

    const tbody =
        document.getElementById('priceListTableBody');

    tbody.replaceChildren();

    const row = document.createElement('tr');
    const cell = document.createElement('td');

    cell.colSpan = 5;
    cell.className = 'text-center text-danger py-4';
    cell.textContent =
        'Không thể tải danh sách bảng giá.';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderPriceListTable(data) {

    const tbody =
        document.getElementById('priceListTableBody');

    tbody.replaceChildren();

    if (!data || data.length === 0) {

        const row = document.createElement('tr');
        const cell = document.createElement('td');

        cell.colSpan = 5;
        cell.className = 'text-center text-muted py-4';
        cell.textContent = 'Chưa có bảng giá.';

        row.appendChild(cell);
        tbody.appendChild(row);

        return;
    }

    data.forEach(item => {

        const row = document.createElement('tr');

        addTextCell(row, item.code, 'fw-semibold');
        addTextCell(row, item.name);

        const dateText =
            formatDateRange(
                item.startDate,
                item.endDate
            );

        addTextCell(row, dateText);

        const statusCell = document.createElement('td');

        const badge = document.createElement('span');

        badge.className =
            item.status === 'ACTIVE'
                ? 'badge bg-success-subtle text-success'
                : 'badge bg-secondary-subtle text-secondary';

        badge.textContent =
            item.status === 'ACTIVE'
                ? 'Đang áp dụng'
                : 'Ngừng áp dụng';

        statusCell.appendChild(badge);

        row.appendChild(statusCell);

        const actionCell = document.createElement('td');

        actionCell.className = 'text-center';

        const detailButton = createActionButton(
            'Xem chi tiết',
            'outline-primary',
            'detail',
            item.id
        );

        const editButton = createActionButton(
            'Sửa',
            'outline-secondary',
            'edit',
            item.id
        );

        const deleteButton = createActionButton(
            'Xóa',
            'outline-danger',
            'delete',
            item.id
        );

        actionCell.appendChild(detailButton);
        actionCell.appendChild(editButton);
        actionCell.appendChild(deleteButton);

        row.appendChild(actionCell);

        tbody.appendChild(row);
    });
}

function handlePriceListTableClick(event) {

    const button = event.target.closest('button');

    if (!button) {
        return;
    }

    const id = Number(button.dataset.id);
    const action = button.dataset.action;

    if (!id) {
        return;
    }

    if (action === 'detail') {
        openPriceListDetail(id);
    }

    if (action === 'edit') {
        openEditPriceListModal(id);
    }

    if (action === 'delete') {
        openPriceListDeleteModal(id);
    }
}

function openAddPriceListModal() {

    const form =
        document.getElementById('priceListForm');

    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('priceListId').value = '';

    document.getElementById('priceListStatus').value =
        'ACTIVE';

    document.getElementById('priceListModalTitle').textContent =
        'Thêm bảng giá';

    priceListModal.show();
}

function openEditPriceListModal(id) {

    const item = priceListData.find(
        priceList =>
            Number(priceList.id) === Number(id)
    );

    if (!item) {
        showToast('Không tìm thấy bảng giá.', 'danger');
        return;
    }

    const form =
        document.getElementById('priceListForm');

    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('priceListId').value =
        item.id;

    document.getElementById('priceListCode').value =
        item.code || '';

    document.getElementById('priceListName').value =
        item.name || '';

    document.getElementById('priceListStartDate').value =
        normalizeDateInput(item.startDate);

    document.getElementById('priceListEndDate').value =
        normalizeDateInput(item.endDate);

    document.getElementById('priceListStatus').value =
        item.status || 'ACTIVE';

    document.getElementById('priceListDescription').value =
        item.description || '';

    document.getElementById('priceListModalTitle').textContent =
        'Chỉnh sửa bảng giá';

    priceListModal.show();
}

async function handlePriceListSubmit(event) {

    event.preventDefault();

    const form = event.target;

    if (!form.checkValidity()) {

        form.classList.add('was-validated');

        return;
    }

    const startDate =
        document.getElementById('priceListStartDate').value;

    const endDate =
        document.getElementById('priceListEndDate').value;

    if (endDate && endDate < startDate) {

        showToast(
            'Ngày kết thúc không được trước ngày bắt đầu.',
            'danger'
        );

        return;
    }

    const id =
        document.getElementById('priceListId').value.trim();

    const data = {
        code:
            document.getElementById('priceListCode').value.trim(),

        name:
            document.getElementById('priceListName').value.trim(),

        startDate: startDate,

        endDate: endDate || null,

        status:
            document.getElementById('priceListStatus').value,

        description:
            document.getElementById('priceListDescription')
                .value
                .trim()
    };

    const button =
        document.getElementById('btnSavePriceList');

    setButtonLoading(button, true, 'Đang lưu...');

    try {

        let response;

        if (id) {

            response = await fetch(
                `${PRICE_LIST_API}${encodeURIComponent(id)}`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(data)
                }
            );

        } else {

            response = await fetch(
                PRICE_LIST_API,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(data)
                }
            );
        }

        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response,
                    'Không thể lưu bảng giá.'
                )
            );
        }

        const saved =
            await parseJsonSafely(response);

        if (saved && saved.id) {

            if (id) {

                const index = priceListData.findIndex(
                    item =>
                        Number(item.id) === Number(id)
                );

                if (index !== -1) {
                    priceListData[index] = saved;
                }

            } else {

                priceListData.unshift(saved);
            }

        } else {

            await fetchPriceLists();
        }

        priceListModal.hide();

        renderPriceListTable(priceListData);

        showToast(
            id
                ? 'Cập nhật bảng giá thành công.'
                : 'Thêm bảng giá thành công.',
            'success'
        );

    } catch (error) {

        showToast(
            error.message || 'Không thể lưu bảng giá.',
            'danger'
        );

    } finally {

        setButtonLoading(button, false, 'Lưu');
    }
}

function openPriceListDeleteModal(id) {

    const item = priceListData.find(
        priceList =>
            Number(priceList.id) === Number(id)
    );

    if (!item) {
        return;
    }

    deleteTarget = {
        type: 'priceList',
        id: Number(id)
    };

    document.getElementById('deleteTargetName').textContent =
        item.name || item.code || '';

    deleteModal.show();
}

async function openPriceListDetail(id) {

    const item = priceListData.find(
        priceList =>
            Number(priceList.id) === Number(id)
    );

    if (!item) {
        showToast('Không tìm thấy bảng giá.', 'danger');
        return;
    }

    currentPriceListId = Number(id);

    document.getElementById('priceListDetailTitle')
        .textContent = item.name || 'Chi tiết bảng giá';

    document.getElementById('priceListDetailSubtitle')
        .textContent =
            `${item.code || ''} | ${formatDateRange(
                item.startDate,
                item.endDate
            )}`;

    priceListDetailModal.show();

    await fetchPriceListItems(currentPriceListId);
}

async function fetchPriceListItems(priceListId) {

    const tbody =
        document.getElementById('priceListItemTableBody');

    renderItemLoading();

    try {

        const response = await fetch(
            `${PRICE_LIST_API}${encodeURIComponent(priceListId)}/items`,
            {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            }
        );

        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response,
                    'Không thể tải chi tiết bảng giá.'
                )
            );
        }

        const data = await response.json();

        currentPriceListItems =
            Array.isArray(data) ? data : [];

        renderPriceListItems(
            currentPriceListItems
        );

    } catch (error) {

        currentPriceListItems = [];

        tbody.replaceChildren();

        const row = document.createElement('tr');
        const cell = document.createElement('td');

        cell.colSpan = 8;
        cell.className = 'text-center text-danger py-4';
        cell.textContent =
            'Không thể tải chi tiết bảng giá.';

        row.appendChild(cell);
        tbody.appendChild(row);

        showToast(
            error.message || 'Không thể tải chi tiết bảng giá.',
            'danger'
        );
    }
}

function renderItemLoading() {

    const tbody =
        document.getElementById('priceListItemTableBody');

    tbody.replaceChildren();

    const row = document.createElement('tr');
    const cell = document.createElement('td');

    cell.colSpan = 8;
    cell.className = 'text-center text-muted py-4';
    cell.textContent = 'Đang tải dữ liệu...';

    row.appendChild(cell);
    tbody.appendChild(row);
}

function renderPriceListItems(data) {

    const tbody =
        document.getElementById('priceListItemTableBody');

    tbody.replaceChildren();

    if (!data || data.length === 0) {

        const row = document.createElement('tr');
        const cell = document.createElement('td');

        cell.colSpan = 8;
        cell.className = 'text-center text-muted py-4';
        cell.textContent =
            'Bảng giá chưa có sản phẩm / dịch vụ.';

        row.appendChild(cell);
        tbody.appendChild(row);

        return;
    }

    data.forEach((item, index) => {

        const row = document.createElement('tr');

        addTextCell(row, index + 1);
        addTextCell(row, item.productCode);
        addTextCell(row, item.productName);

        const typeCell = document.createElement('td');

        const badge = document.createElement('span');

        badge.className =
            item.productType === 'PRODUCT'
                ? 'badge badge-product'
                : 'badge badge-service';

        badge.textContent =
            item.productType === 'PRODUCT'
                ? 'Sản phẩm'
                : 'Dịch vụ';

        typeCell.appendChild(badge);
        row.appendChild(typeCell);

        addTextCell(row, item.unit);
        addTextCell(
            row,
            formatCurrency(item.listPrice)
        );
        addTextCell(
            row,
            formatCurrency(item.floorPrice)
        );

        const actionCell = document.createElement('td');

        actionCell.className = 'text-center';

        const editButton = createActionButton(
            'Sửa',
            'outline-primary',
            'edit',
            item.id
        );

        const deleteButton = createActionButton(
            'Xóa',
            'outline-danger',
            'delete',
            item.id
        );

        actionCell.appendChild(editButton);
        actionCell.appendChild(deleteButton);

        row.appendChild(actionCell);

        tbody.appendChild(row);
    });
}

function handlePriceListItemTableClick(event) {

    const button = event.target.closest('button');

    if (!button) {
        return;
    }

    const id = Number(button.dataset.id);
    const action = button.dataset.action;

    if (!id) {
        return;
    }

    if (action === 'edit') {
        openEditPriceListItemModal(id);
    }

    if (action === 'delete') {
        openPriceListItemDeleteModal(id);
    }
}

function openAddPriceListItemModal() {

    if (!currentPriceListId) {
        showToast('Chưa chọn bảng giá.', 'danger');
        return;
    }

    const form =
        document.getElementById('priceListItemForm');

    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('priceListItemId').value = '';

    document.getElementById('priceListItemModalTitle')
        .textContent =
            'Thêm sản phẩm vào bảng giá';

    populateProductSelect();

    priceListItemModal.show();
}

function openEditPriceListItemModal(id) {

    const item = currentPriceListItems.find(
        currentItem =>
            Number(currentItem.id) === Number(id)
    );

    if (!item) {
        showToast(
            'Không tìm thấy sản phẩm trong bảng giá.',
            'danger'
        );
        return;
    }

    const form =
        document.getElementById('priceListItemForm');

    form.reset();
    form.classList.remove('was-validated');

    document.getElementById('priceListItemId').value =
        item.id;

    populateProductSelect(item.productId);

    document.getElementById('priceListItemProductId').value =
        item.productId;

    document.getElementById('priceListItemListPrice').value =
        item.listPrice ?? '';

    document.getElementById('priceListItemFloorPrice').value =
        item.floorPrice ?? '';

    document.getElementById('priceListItemModalTitle')
        .textContent =
            'Chỉnh sửa sản phẩm trong bảng giá';

    priceListItemModal.show();
}

function populateProductSelect(selectedProductId = null) {

    const select =
        document.getElementById('priceListItemProductId');

    select.replaceChildren();

    const defaultOption =
        document.createElement('option');

    defaultOption.value = '';
    defaultOption.textContent =
        '-- Chọn sản phẩm / dịch vụ --';

    select.appendChild(defaultOption);

    const existingProductIds =
        new Set(
            currentPriceListItems.map(
                item => Number(item.productId)
            )
        );

    productList.forEach(product => {

        const productId = Number(product.id);

        if (
            existingProductIds.has(productId) &&
            productId !== Number(selectedProductId)
        ) {
            return;
        }

        const option =
            document.createElement('option');

        option.value = productId;

        option.textContent =
            `${product.code} - ${product.name}`;

        select.appendChild(option);
    });
}

async function handlePriceListItemSubmit(event) {

    event.preventDefault();

    const form = event.target;

    if (!form.checkValidity()) {

        form.classList.add('was-validated');

        return;
    }

    const listPrice = Number(
        document.getElementById('priceListItemListPrice').value
    );

    const floorPrice = Number(
        document.getElementById('priceListItemFloorPrice').value
    );

    if (floorPrice > listPrice) {

        showToast(
            'Giá sàn không được lớn hơn giá bán.',
            'danger'
        );

        return;
    }

    const itemId =
        document.getElementById('priceListItemId')
            .value
            .trim();

    const productId =
        Number(
            document.getElementById('priceListItemProductId')
                .value
        );

    const data = {
        productId: productId,
        listPrice: listPrice,
        floorPrice: floorPrice
    };

    const button =
        document.getElementById('btnSavePriceListItem');

    setButtonLoading(button, true, 'Đang lưu...');

    try {

        let response;

        if (itemId) {

            response = await fetch(
                `${PRICE_LIST_API}${currentPriceListId}/items/${encodeURIComponent(itemId)}`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(data)
                }
            );

        } else {

            response = await fetch(
                `${PRICE_LIST_API}${currentPriceListId}/items`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(data)
                }
            );
        }

        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response,
                    'Không thể lưu sản phẩm vào bảng giá.'
                )
            );
        }

        const saved =
            await parseJsonSafely(response);

        if (saved && saved.id) {

            if (itemId) {

                const index =
                    currentPriceListItems.findIndex(
                        item =>
                            Number(item.id) === Number(itemId)
                    );

                if (index !== -1) {
                    currentPriceListItems[index] = saved;
                }

            } else {

                currentPriceListItems.push(saved);
            }

        }

        priceListItemModal.hide();

        await fetchPriceListItems(currentPriceListId);

        showToast(
            itemId
                ? 'Cập nhật sản phẩm trong bảng giá thành công.'
                : 'Thêm sản phẩm vào bảng giá thành công.',
            'success'
        );

    } catch (error) {

        showToast(
            error.message ||
            'Không thể lưu sản phẩm vào bảng giá.',
            'danger'
        );

    } finally {

        setButtonLoading(
            button,
            false,
            'Lưu'
        );
    }
}

function openPriceListItemDeleteModal(id) {

    const item = currentPriceListItems.find(
        currentItem =>
            Number(currentItem.id) === Number(id)
    );

    if (!item) {
        return;
    }

    deleteTarget = {
        type: 'priceListItem',
        id: Number(id)
    };

    document.getElementById('deleteTargetName')
        .textContent =
            item.productName ||
            item.productCode ||
            '';

    deleteModal.show();
}

async function executeDelete() {

    if (!deleteTarget.type || !deleteTarget.id) {
        return;
    }

    const button =
        document.getElementById('btnConfirmDelete');

    setButtonLoading(button, true, 'Đang xử lý...');

    try {

        let url;

        if (deleteTarget.type === 'product') {

            url =
                `${PRODUCT_API}${encodeURIComponent(
                    deleteTarget.id
                )}`;

        } else if (deleteTarget.type === 'priceList') {

            url =
                `${PRICE_LIST_API}${encodeURIComponent(
                    deleteTarget.id
                )}`;

        } else if (deleteTarget.type === 'priceListItem') {

            url =
                `${PRICE_LIST_API}${encodeURIComponent(
                    currentPriceListId
                )}/items/${encodeURIComponent(
                    deleteTarget.id
                )}`;
        }

        const response = await fetch(
            url,
            {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json'
                }
            }
        );

        if (!response.ok) {

            throw new Error(
                await getErrorMessage(
                    response,
                    'Xóa dữ liệu thất bại.'
                )
            );
        }

        if (deleteTarget.type === 'product') {

            await fetchProducts();

            showToast(
                'Ngừng kinh doanh sản phẩm thành công.',
                'success'
            );

        } else if (deleteTarget.type === 'priceList') {

            priceListData =
                priceListData.filter(
                    item =>
                        Number(item.id) !==
                        Number(deleteTarget.id)
                );

            renderPriceListTable(priceListData);

            showToast(
                'Xóa bảng giá thành công.',
                'success'
            );

        } else if (deleteTarget.type === 'priceListItem') {

            currentPriceListItems =
                currentPriceListItems.filter(
                    item =>
                        Number(item.id) !==
                        Number(deleteTarget.id)
                );

            renderPriceListItems(
                currentPriceListItems
            );

            showToast(
                'Xóa sản phẩm khỏi bảng giá thành công.',
                'success'
            );
        }

        deleteModal.hide();

        deleteTarget = {
            type: null,
            id: null
        };

    } catch (error) {

        showToast(
            error.message ||
            'Xóa dữ liệu thất bại.',
            'danger'
        );

    } finally {

        setButtonLoading(
            button,
            false,
            'Xác nhận'
        );
    }
}

function handleSearch(event) {

    event.preventDefault();

    const keyword =
        document.getElementById('searchKeyword')
            .value
            .trim()
            .toLowerCase();

    const type =
        document.getElementById('filterType').value;

    const status =
        document.getElementById('filterStatus').value;

    const filtered =
        productList.filter(item => {

            const code =
                String(item.code || '').toLowerCase();

            const name =
                String(item.name || '').toLowerCase();

            const matchesKeyword =
                !keyword ||
                code.includes(keyword) ||
                name.includes(keyword);

            const matchesType =
                !type ||
                item.type === type;

            const matchesStatus =
                !status ||
                item.status === status;

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

function addTextCell(row, value, className = '') {

    const cell = document.createElement('td');

    if (className) {
        cell.className = className;
    }

    cell.textContent =
        value === null ||
        value === undefined ||
        value === ''
            ? '-'
            : String(value);

    row.appendChild(cell);

    return cell;
}

function createActionButton(
    text,
    style,
    action,
    id
) {

    const button =
        document.createElement('button');

    button.type = 'button';

    button.className =
        `btn btn-sm btn-${style} me-1`;

    button.dataset.action = action;
    button.dataset.id = id;

    button.textContent = text;

    return button;
}

function formatCurrency(amount) {

    if (
        amount === null ||
        amount === undefined ||
        amount === ''
    ) {
        return '-';
    }

    return new Intl.NumberFormat(
        'vi-VN',
        {
            style: 'currency',
            currency: 'VND'
        }
    ).format(Number(amount));
}

function normalizeDateInput(value) {

    if (!value) {
        return '';
    }

    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        return value;
    }

    const match =
        String(value).match(
            /^(\d{2})\/(\d{2})\/(\d{4})$/
        );

    if (match) {

        return `${match[3]}-${match[2]}-${match[1]}`;
    }

    return String(value).substring(0, 10);
}

function formatDate(date) {

    if (!date) {
        return '-';
    }

    const normalized =
        normalizeDateInput(date);

    const parts =
        normalized.split('-');

    if (parts.length !== 3) {
        return String(date);
    }

    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function formatDateRange(startDate, endDate) {

    const start =
        formatDate(startDate);

    const end =
        endDate
            ? formatDate(endDate)
            : 'Không giới hạn';

    return `${start} - ${end}`;
}

function setButtonLoading(
    button,
    loading,
    loadingText
) {

    if (!button) {
        return;
    }

    if (loading) {

        button.dataset.originalText =
            button.textContent;

        button.disabled = true;
        button.textContent = loadingText;

    } else {

        button.disabled = false;

        button.textContent =
            button.dataset.originalText ||
            loadingText;
    }
}

async function parseJsonSafely(response) {

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();

    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return null;
    }
}

async function getErrorMessage(
    response,
    defaultMessage
) {

    try {

        const text =
            await response.text();

        if (!text) {
            return defaultMessage;
        }

        try {

            const data =
                JSON.parse(text);

            return (
                data.message ||
                data.error ||
                defaultMessage
            );

        } catch (error) {

            return text || defaultMessage;
        }

    } catch (error) {

        return defaultMessage;
    }
}

function showToast(
    message,
    type = 'success'
) {

    const toastEl =
        document.getElementById('liveToast');

    const messageEl =
        document.getElementById('toastMessage');

    const titleEl =
        document.getElementById('toastTitle');

    toastEl.classList.remove(
        'toast-success',
        'toast-danger',
        'toast-warning',
        'toast-primary'
    );

    toastEl.classList.add(
        `toast-${type}`
    );

    titleEl.textContent =
        type === 'success'
            ? 'Thành công'
            : type === 'danger'
                ? 'Lỗi'
                : 'Thông báo';

    messageEl.textContent =
        message || 'Đã xảy ra lỗi.';

    liveToast.show();
}