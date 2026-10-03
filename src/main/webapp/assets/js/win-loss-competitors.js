(() => {

    'use strict';

    const body =
        document.body;

    const contextPath =
        body.dataset.contextPath || '';

    const REASON_API =
        contextPath + '/api/v1/win-loss-reasons';

    const COMPETITOR_API =
        contextPath + '/api/v1/competitors';


    let activeTab = 'won';

    let reasons = [];

    let competitors = [];

    let deleteTarget = null;


    const wonCount =
        document.getElementById('wonCount');

    const lostCount =
        document.getElementById('lostCount');

    const competitorCount =
        document.getElementById(
            'competitorCount'
        );

    const dataBody =
        document.getElementById('dataBody');

    const loadingState =
        document.getElementById(
            'loadingState'
        );

    const emptyState =
        document.getElementById(
            'emptyState'
        );

    const tableWrapper =
        document.getElementById(
            'tableWrapper'
        );

    const searchInput =
        document.getElementById(
            'searchInput'
        );

    const statusFilter =
        document.getElementById(
            'statusFilter'
        );

    const btnAdd =
        document.getElementById('btnAdd');

    const btnReload =
        document.getElementById(
            'btnReload'
        );


    const editModal =
        document.getElementById(
            'editModal'
        );

    const modalTitle =
        document.getElementById(
            'modalTitle'
        );

    const editForm =
        document.getElementById(
            'editForm'
        );

    const entityId =
        document.getElementById(
            'entityId'
        );

    const entityCode =
        document.getElementById(
            'entityCode'
        );

    const entityName =
        document.getElementById(
            'entityName'
        );

    const entityStatus =
        document.getElementById(
            'entityStatus'
        );

    const entityDescription =
        document.getElementById(
            'entityDescription'
        );

    const reasonTypeField =
        document.getElementById(
            'reasonTypeField'
        );

    const reasonType =
        document.getElementById(
            'reasonType'
        );

    const btnCloseModal =
        document.getElementById(
            'btnCloseModal'
        );

    const btnCancel =
        document.getElementById(
            'btnCancel'
        );

    const btnSave =
        document.getElementById(
            'btnSave'
        );


    const deleteModal =
        document.getElementById(
            'deleteModal'
        );

    const deleteName =
        document.getElementById(
            'deleteName'
        );

    const btnCancelDelete =
        document.getElementById(
            'btnCancelDelete'
        );

    const btnConfirmDelete =
        document.getElementById(
            'btnConfirmDelete'
        );

    const toast =
        document.getElementById(
            'wlToast'
        );


    function escapeHtml(value) {

        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }


    function showToast(
        message,
        type = 'success'
    ) {

        if (!toast) {
            return;
        }

        toast.textContent =
            message;

        toast.classList.remove(
            'show',
            'success',
            'error'
        );

        toast.classList.add(
            type === 'error'
                ? 'error'
                : 'success'
        );

        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        window.clearTimeout(
            showToast.timer
        );

        showToast.timer =
            window.setTimeout(
                () => {
                    toast.classList.remove(
                        'show'
                    );
                },
                2800
            );
    }


    async function readJson(
        response
    ) {

        const text =
            await response.text();

        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch {
            return {
                message: text
            };
        }
    }


    async function apiRequest(
        url,
        options = {}
    ) {

        const response =
            await fetch(
                url,
                {
                    credentials:
                        'same-origin',

                    ...options
                }
            );

        const data =
            await readJson(
                response
            );

        if (!response.ok) {

            const message =
                data &&
                data.message
                    ? data.message
                    : 'Có lỗi xảy ra khi xử lý yêu cầu.';

            throw new Error(message);
        }

        return data;
    }


    function setLoading(
        loading
    ) {

        if (loadingState) {
            loadingState.hidden =
                !loading;
        }

        if (tableWrapper) {
            tableWrapper.hidden =
                loading;
        }

        if (emptyState && loading) {
            emptyState.hidden =
                true;
        }
    }


    function getActiveItems() {

        if (activeTab === 'competitors') {
            return competitors;
        }

        const type =
            activeTab === 'won'
                ? 'WON'
                : 'LOST';

        return reasons.filter(
            item =>
                item.type === type
        );
    }


    function getTabLabel() {

        if (activeTab === 'won') {
            return 'lý do thắng';
        }

        if (activeTab === 'lost') {
            return 'lý do thua';
        }

        return 'đối thủ';
    }


    function renderSummary() {

        if (wonCount) {
            wonCount.textContent =
                reasons.filter(
                    item =>
                        item.type === 'WON'
                ).length;
        }

        if (lostCount) {
            lostCount.textContent =
                reasons.filter(
                    item =>
                        item.type === 'LOST'
                ).length;
        }

        if (competitorCount) {
            competitorCount.textContent =
                competitors.length;
        }
    }


    function renderTable() {

        const keyword =
            (
                searchInput.value || ''
            )
            .trim()
            .toLowerCase();

        const status =
            statusFilter.value;

        let items =
            getActiveItems();

        items =
            items.filter(item => {

                const matchesKeyword =
                    !keyword
                    ||
                    String(
                        item.code || ''
                    )
                    .toLowerCase()
                    .includes(keyword)
                    ||
                    String(
                        item.name || ''
                    )
                    .toLowerCase()
                    .includes(keyword);

                const matchesStatus =
                    !status
                    ||
                    item.status === status;

                return (
                    matchesKeyword
                    &&
                    matchesStatus
                );
            });


        dataBody.innerHTML = '';

        if (items.length === 0) {

            emptyState.hidden =
                false;

            tableWrapper.hidden =
                true;

            return;
        }

        emptyState.hidden =
            true;

        tableWrapper.hidden =
            false;


        for (const item of items) {

            const row =
                document.createElement(
                    'tr'
                );

            const active =
                item.status === 'ACTIVE';

            row.innerHTML = `
                <td>
                    <strong>
                        ${escapeHtml(item.code)}
                    </strong>
                </td>

                <td>
                    ${escapeHtml(item.name)}
                </td>

                <td>
                    ${escapeHtml(
                        item.description || '-'
                    )}
                </td>

                <td>
                    <span class="${
                        active
                            ? 'wl-status wl-status-active'
                            : 'wl-status wl-status-inactive'
                    }">
                        ${
                            active
                                ? 'Đang hoạt động'
                                : 'Ngừng hoạt động'
                        }
                    </span>
                </td>

                <td class="wl-actions">

                    <button
                        type="button"
                        class="wl-action-button"
                        data-action="edit"
                        data-id="${item.id}">

                        Sửa

                    </button>

                    <button
                        type="button"
                        class="wl-action-button wl-action-danger"
                        data-action="delete"
                        data-id="${item.id}">

                        Xóa

                    </button>

                </td>
            `;

            dataBody.appendChild(row);
        }
    }


    async function loadData(
        showSuccess = false
    ) {

        setLoading(true);

        try {

            const [
                reasonData,
                competitorData
            ] =
            await Promise.all([
                apiRequest(
                    REASON_API
                ),

                apiRequest(
                    COMPETITOR_API
                )
            ]);

            reasons =
                Array.isArray(reasonData)
                    ? reasonData
                    : [];

            competitors =
                Array.isArray(
                    competitorData
                )
                    ? competitorData
                    : [];

            renderSummary();

            renderTable();

            if (showSuccess) {

                showToast(
                    'Đã tải lại dữ liệu.'
                );
            }

        } catch (error) {

            dataBody.innerHTML = '';

            tableWrapper.hidden =
                true;

            emptyState.hidden =
                false;

            emptyState.textContent =
                error.message;

            showToast(
                error.message,
                'error'
            );

        } finally {

            loadingState.hidden =
                true;
        }
    }


    function updateTabButtons() {

        document
            .querySelectorAll(
                '.wl-tab'
            )
            .forEach(button => {

                button.classList.toggle(
                    'active',
                    button.dataset.tab
                        === activeTab
                );
            });
    }


    function switchTab(
        tab
    ) {

        activeTab = tab;

        searchInput.value = '';

        statusFilter.value = '';

        updateTabButtons();

        renderTable();
    }


    function findItem(
        id
    ) {

        const numericId =
            Number(id);

        if (activeTab === 'competitors') {

            return competitors.find(
                item =>
                    Number(item.id)
                    === numericId
            );
        }

        return reasons.find(
            item =>
                Number(item.id)
                === numericId
        );
    }


    function openCreateModal() {

        editForm.reset();

        entityId.value = '';

        entityStatus.value =
            'ACTIVE';

        if (
            activeTab === 'competitors'
        ) {

            modalTitle.textContent =
                'Thêm đối thủ cạnh tranh';

            reasonTypeField.hidden =
                true;

        } else {

            const won =
                activeTab === 'won';

            modalTitle.textContent =
                won
                    ? 'Thêm lý do thắng'
                    : 'Thêm lý do thua';

            reasonTypeField.hidden =
                false;

            reasonTypeText.value =
                won
                    ? 'Lý do thắng'
                    : 'Lý do thua';
        }

        editModal.hidden =
            false;

        entityCode.focus();
    }


    function openEditModal(
        item
    ) {

        if (!item) {
            return;
        }

        entityId.value =
            item.id;

        entityCode.value =
            item.code || '';

        entityName.value =
            item.name || '';

        entityStatus.value =
            item.status || 'ACTIVE';

        entityDescription.value =
            item.description || '';

        if (
            activeTab === 'competitors'
        ) {

            modalTitle.textContent =
                'Cập nhật đối thủ cạnh tranh';

            reasonTypeField.hidden =
                true;

        } else {

            const won =
                item.type === 'WON';

            modalTitle.textContent =
                won
                    ? 'Cập nhật lý do thắng'
                    : 'Cập nhật lý do thua';

            reasonTypeField.hidden =
                false;

            reasonTypeText.value =
                won
                    ? 'Lý do thắng'
                    : 'Lý do thua';
        }

        editModal.hidden =
            false;

        entityName.focus();
    }


    function closeEditModal() {

        editModal.hidden =
            true;

        editForm.reset();

        entityId.value = '';
    }


    function validateForm() {

        const code =
            entityCode.value
                .trim()
                .toUpperCase();

        const name =
            entityName.value
                .trim();

        const status =
            entityStatus.value;

        const description =
            entityDescription.value
                .trim();


        if (!name) {

            throw new Error(
                'Tên không được để trống.'
            );
        }

        if (!code) {

            throw new Error(
                'Mã không được để trống.'
            );
        }

        if (
            !/^[A-Z][A-Z0-9_]*$/
                .test(code)
        ) {

            throw new Error(
                'Mã phải bắt đầu bằng chữ và chỉ gồm chữ in hoa, số hoặc dấu gạch dưới.'
            );
        }

        if (
            status !== 'ACTIVE'
            &&
            status !== 'INACTIVE'
        ) {

            throw new Error(
                'Trạng thái không hợp lệ.'
            );
        }

        return {
            code,
            name,
            status,
            description
        };
    }


    async function submitForm(
        event
    ) {

        event.preventDefault();

        try {

            const data =
                validateForm();

            const id =
                entityId.value;

            const editing =
                Boolean(id);

            let url;

            if (
                activeTab ===
                'competitors'
            ) {

                url =
                    editing
                        ? COMPETITOR_API
                            + '/'
                            + id
                        : COMPETITOR_API;

            } else {

                data.type =
                    activeTab === 'won'
                        ? 'WON'
                        : 'LOST';

                url =
                    editing
                        ? REASON_API
                            + '/'
                            + id
                        : REASON_API;
            }


            btnSave.disabled =
                true;

            const params =
                new URLSearchParams();

            Object.entries(data)
                .forEach(
                    ([key, value]) => {

                        params.set(
                            key,
                            value ?? ''
                        );
                    }
                );


            await apiRequest(
                url,
                {
                    method:
                        editing
                            ? 'PUT'
                            : 'POST',

                    headers: {
                        'Content-Type':
                            'application/x-www-form-urlencoded;charset=UTF-8'
                    },

                    body: params
                }
            );


            closeEditModal();

            await loadData();

            showToast(
                editing
                    ? 'Cập nhật thành công.'
                    : 'Thêm mới thành công.'
            );

        } catch (error) {

            showToast(
                error.message,
                'error'
            );

        } finally {

            btnSave.disabled =
                false;
        }
    }


    function openDeleteModal(
        item
    ) {

        if (!item) {
            return;
        }

        deleteTarget = {
            id: item.id,
            name: item.name,
            type: activeTab
        };

        deleteName.textContent =
            item.name;

        deleteModal.hidden =
            false;
    }


    function closeDeleteModal() {

        deleteModal.hidden =
            true;

        deleteTarget = null;
    }


    async function confirmDelete() {

        if (!deleteTarget) {
            return;
        }

        try {

            btnConfirmDelete.disabled =
                true;

            const url =
                deleteTarget.type
                    === 'competitors'
                    ? COMPETITOR_API
                        + '/'
                        + deleteTarget.id
                    : REASON_API
                        + '/'
                        + deleteTarget.id;


            await apiRequest(
                url,
                {
                    method:
                        'DELETE'
                }
            );

            closeDeleteModal();

            await loadData();

            showToast(
                'Xóa thành công.'
            );

        } catch (error) {

            showToast(
                error.message,
                'error'
            );

        } finally {

            btnConfirmDelete.disabled =
                false;
        }
    }


    document
        .querySelectorAll(
            '.wl-tab'
        )
        .forEach(button => {

            button.addEventListener(
                'click',
                () => {

                    switchTab(
                        button.dataset.tab
                    );
                }
            );
        });


    dataBody.addEventListener(
        'click',
        event => {

            const button =
                event.target.closest(
                    'button[data-action]'
                );

            if (!button) {
                return;
            }

            const item =
                findItem(
                    button.dataset.id
                );

            if (
                button.dataset.action
                === 'edit'
            ) {

                openEditModal(item);

                return;
            }

            if (
                button.dataset.action
                === 'delete'
            ) {

                openDeleteModal(item);
            }
        }
    );


    searchInput.addEventListener(
        'input',
        renderTable
    );

    statusFilter.addEventListener(
        'change',
        renderTable
    );

    btnAdd.addEventListener(
        'click',
        openCreateModal
    );

    btnReload.addEventListener(
        'click',
        () => loadData(true)
    );

    btnCloseModal.addEventListener(
        'click',
        closeEditModal
    );

    btnCancel.addEventListener(
        'click',
        closeEditModal
    );

    editForm.addEventListener(
        'submit',
        submitForm
    );

    btnCancelDelete.addEventListener(
        'click',
        closeDeleteModal
    );

    btnConfirmDelete.addEventListener(
        'click',
        confirmDelete
    );


    editModal.addEventListener(
        'click',
        event => {

            if (
                event.target
                === editModal
            ) {

                closeEditModal();
            }
        }
    );


    deleteModal.addEventListener(
        'click',
        event => {

            if (
                event.target
                === deleteModal
            ) {

                closeDeleteModal();
            }
        }
    );


    document.addEventListener(
        'keydown',
        event => {

            if (
                event.key !== 'Escape'
            ) {
                return;
            }

            if (!editModal.hidden) {
                closeEditModal();
            }

            if (!deleteModal.hidden) {
                closeDeleteModal();
            }
        }
    );


    loadData();

})();