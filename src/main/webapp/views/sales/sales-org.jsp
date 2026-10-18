<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         language="java" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0">

    <title>Cơ cấu tổ chức kinh doanh</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f4f7fb;
            font-family: Arial, sans-serif;
            color: #1f2937;
        }

        .sales-org-layout {
            position: absolute;
            top: 0;
            left: 270px;
            width: calc(100% - 270px);
            min-height: 100vh;
            margin: 0;
            padding: 28px 32px;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 20px;
            margin-bottom: 22px;
        }

        .page-title {
            margin: 0;
            font-size: 25px;
            font-weight: 700;
        }

        .page-subtitle {
            margin: 7px 0 0;
            color: #64748b;
            font-size: 14px;
        }

        .btn {
            border: 1px solid transparent;
            border-radius: 6px;
            padding: 9px 14px;
            cursor: pointer;
            font-size: 14px;
        }

        .btn-primary {
            color: #fff;
            background: #0d6efd;
        }

        .btn-primary:hover {
            background: #0b5ed7;
        }

        .btn-outline-primary {
            color: #0d6efd;
            background: #fff;
            border-color: #0d6efd;
        }

        .btn-outline-danger {
            color: #dc3545;
            background: #fff;
            border-color: #dc3545;
        }

        .btn-secondary {
            color: #fff;
            background: #6c757d;
        }

        .btn-sm {
            padding: 6px 10px;
            font-size: 13px;
        }

        .card {
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,.04);
        }

        .toolbar {
            padding: 16px 18px;
            margin-bottom: 18px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
        }

        .summary {
            color: #64748b;
            font-size: 14px;
        }

        .tree-card {
            padding: 18px;
        }

        .tree-empty,
        .tree-loading,
        .tree-error {
            padding: 35px 20px;
            text-align: center;
            color: #64748b;
        }

        .tree-error {
            color: #b42318;
        }

        .org-tree,
        .org-tree ul {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .org-tree ul {
            margin-left: 32px;
            padding-left: 18px;
            border-left: 1px dashed #cbd5e1;
        }

        .org-tree li {
            margin: 10px 0;
        }

        .org-node {
            display: grid;
            grid-template-columns:
                minmax(220px, 1.5fr)
                minmax(150px, 1fr)
                minmax(150px, 1fr)
                120px
                auto;
            gap: 14px;
            align-items: center;

            padding: 13px 14px;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            background: #fff;
        }

        .org-node:hover {
            background: #f8fafc;
        }

        .org-main {
            min-width: 0;
        }

        .org-name {
            font-weight: 700;
            color: #0f172a;
            word-break: break-word;
        }

        .org-code {
            margin-top: 4px;
            color: #64748b;
            font-size: 12px;
        }

        .org-info-label {
            display: block;
            margin-bottom: 3px;
            color: #94a3b8;
            font-size: 11px;
            text-transform: uppercase;
        }

        .org-info-value {
            font-size: 13px;
            color: #334155;
        }

        .status-badge {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
        }

        .status-active {
            color: #067647;
            background: #d1fadf;
        }

        .status-inactive {
            color: #475467;
            background: #eaecf0;
        }

        .node-actions {
            display: flex;
            gap: 6px;
            justify-content: flex-end;
            flex-wrap: wrap;
        }

        .modal-backdrop {
            display: none;
            position: fixed;
            inset: 0;
            z-index: 2000;
            background: rgba(15, 23, 42, .55);
            padding: 30px 15px;
            overflow-y: auto;
        }

        .modal-backdrop.show {
            display: block;
        }

        .modal-dialog {
            width: min(620px, 100%);
            margin: 30px auto;
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0,0,0,.2);
            overflow: hidden;
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 18px 20px;
            border-bottom: 1px solid #e5e7eb;
        }

        .modal-header h3 {
            margin: 0;
            font-size: 19px;
        }

        .modal-close {
            border: 0;
            background: transparent;
            cursor: pointer;
            font-size: 25px;
            color: #667085;
        }

        .modal-body {
            padding: 20px;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .form-group {
            margin-bottom: 16px;
        }

        .form-group label {
            display: block;
            margin-bottom: 6px;
            font-weight: 600;
            font-size: 13px;
        }

        .required {
            color: #dc3545;
        }

        .form-control {
            width: 100%;
            height: 40px;
            padding: 8px 10px;
            border: 1px solid #d0d5dd;
            border-radius: 6px;
            background: #fff;
            color: #101828;
        }

        .form-control:focus {
            outline: none;
            border-color: #84adff;
            box-shadow: 0 0 0 3px rgba(13,110,253,.1);
        }

        .modal-footer {
            display: flex;
            justify-content: flex-end;
            gap: 8px;
            padding: 16px 20px;
            border-top: 1px solid #e5e7eb;
        }

        .alert {
            display: none;
            margin-bottom: 16px;
            padding: 10px 12px;
            border-radius: 6px;
            font-size: 13px;
        }

        .alert.show {
            display: block;
        }

        .alert-danger {
            color: #b42318;
            background: #fef3f2;
            border: 1px solid #fecdca;
        }

        .toast-message {
            position: fixed;
            right: 22px;
            bottom: 22px;
            z-index: 3000;
            min-width: 280px;
            max-width: 420px;
            padding: 13px 16px;
            border-radius: 8px;
            color: #fff;
            box-shadow: 0 10px 30px rgba(0,0,0,.2);
            display: none;
        }

        .toast-message.show {
            display: block;
        }

        .toast-success {
            background: #198754;
        }

        .toast-danger {
            background: #dc3545;
        }

        @media (max-width: 1000px) {
            .sales-org-layout {
                left: 270px;
                width: calc(100% - 270px);
                padding: 20px;
            }

            .org-node {
                grid-template-columns: 1fr;
            }

            .node-actions {
                justify-content: flex-start;
            }
        }

                @media (max-width: 768px) {
            .sales-org-layout {
                position: static;
                top: auto;
                left: auto;
                width: 100%;
                min-height: auto;
                margin: 0;
                padding: 20px;
            }
        }
@media (max-width: 650px) {
            .form-row {
                grid-template-columns: 1fr;
            }

            .page-header {
                flex-direction: column;
            }
        }
    </style>
</head>

<body
    data-context-path="${pageContext.request.contextPath}"
    data-user-role="${sessionScope.userRole}">

    <jsp:include page="/components/sidebar.jsp"/>

    <main class="sales-org-layout">

        <div class="page-header">

            <div>
                <h1 class="page-title">
                    Cơ cấu tổ chức kinh doanh
                </h1>

                <p class="page-subtitle">
                    Quản lý cây nhóm kinh doanh,
                    trưởng nhóm và khu vực phụ trách
                </p>
            </div>

            <button
                type="button"
                id="btnAddGroup"
                class="btn btn-primary">

                + Thêm nhóm kinh doanh
            </button>

        </div>


        <div class="card toolbar">

            <div class="summary">
                Tổng số nhóm:
                <strong id="totalGroups">0</strong>
            </div>

            <button
                type="button"
                id="btnReload"
                class="btn btn-outline-primary">

                Tải lại
            </button>

        </div>


        <section class="card tree-card">

            <div id="treeContainer">

                <div class="tree-loading">
                    Đang tải cơ cấu tổ chức...
                </div>

            </div>

        </section>

    </main>


    <div
        id="salesOrgModal"
        class="modal-backdrop">

        <div class="modal-dialog">

            <div class="modal-header">

                <h3 id="modalTitle">
                    Thêm nhóm kinh doanh
                </h3>

                <button
                    type="button"
                    id="btnCloseModal"
                    class="modal-close">
                    &times;
                </button>

            </div>

            <form id="salesOrgForm">

                <div class="modal-body">

                    <div
                        id="formError"
                        class="alert alert-danger">
                    </div>

                    <input
                        type="hidden"
                        id="groupId">


                    <div class="form-row">

                        <div class="form-group">

                            <label for="orgCode">
                                Mã nhóm
                                <span class="required">*</span>
                            </label>

                            <input
                                type="text"
                                id="orgCode"
                                class="form-control"
                                maxlength="50"
                                required>

                        </div>


                        <div class="form-group">

                            <label for="status">
                                Trạng thái
                            </label>

                            <select
                                id="status"
                                class="form-control">

                                <option value="ACTIVE">
                                    Đang hoạt động
                                </option>

                                <option value="INACTIVE">
                                    Ngừng hoạt động
                                </option>

                            </select>

                        </div>

                    </div>


                    <div class="form-group">

                        <label for="orgName">
                            Tên nhóm
                            <span class="required">*</span>
                        </label>

                        <input
                            type="text"
                            id="orgName"
                            class="form-control"
                            maxlength="150"
                            required>

                    </div>


                    <div class="form-group">

                        <label for="parentId">
                            Nhóm cha
                        </label>

                        <select
                            id="parentId"
                            class="form-control">

                            <option value="">
                                -- Không có, là nhóm gốc --
                            </option>

                        </select>

                    </div>


                    <div class="form-row">

                        <div class="form-group">

                            <label for="leaderId">
                                Trưởng nhóm
                                <span class="required">*</span>
                            </label>

                            <select
                                id="leaderId"
                                class="form-control"
                                required>

                                <option value="">
                                    -- Chọn trưởng nhóm --
                                </option>

                            </select>

                        </div>


                        <div class="form-group">

                            <label for="regionId">
                                Khu vực địa lý
                                <span class="required">*</span>
                            </label>

                            <select
                                id="regionId"
                                class="form-control"
                                required>

                                <option value="">
                                    -- Chọn khu vực --
                                </option>

                            </select>

                        </div>

                    </div>

                </div>


                <div class="modal-footer">

                    <button
                        type="button"
                        id="btnCancel"
                        class="btn btn-secondary">
                        Hủy
                    </button>

                    <button
                        type="submit"
                        id="btnSave"
                        class="btn btn-primary">
                        Lưu thông tin
                    </button>

                </div>

            </form>

        </div>

    </div>


    <div
        id="toast"
        class="toast-message">
    </div>


<script>
(() => {

    const CONTEXT_PATH =
        document.body.dataset.contextPath || '';

    const API =
        CONTEXT_PATH + '/api/v1/sales-orgs';

    let organizations = [];
    let treeData = [];
    let managers = [];
    let regions = [];

    const treeContainer =
        document.getElementById('treeContainer');

    const totalGroups =
        document.getElementById('totalGroups');

    const modal =
        document.getElementById('salesOrgModal');

    const form =
        document.getElementById('salesOrgForm');

    const formError =
        document.getElementById('formError');

    const groupIdInput =
        document.getElementById('groupId');

    const orgCodeInput =
        document.getElementById('orgCode');

    const orgNameInput =
        document.getElementById('orgName');

    const parentSelect =
        document.getElementById('parentId');

    const leaderSelect =
        document.getElementById('leaderId');

    const regionSelect =
        document.getElementById('regionId');

    const statusSelect =
        document.getElementById('status');

    const btnSave =
        document.getElementById('btnSave');


    async function apiRequest(
        url,
        options = {}
    ) {

        const response =
            await fetch(url, {
                ...options,
                headers: {
                    ...(options.body
                        ? {
                            'Content-Type':
                                'application/json'
                        }
                        : {}),
                    ...(options.headers || {})
                }
            });

        if (response.status === 204) {
            return null;
        }

        const text =
            await response.text();

        let data = null;

        if (text) {
            try {
                data = JSON.parse(text);
            }
            catch {
                data = {
                    message: text
                };
            }
        }

        if (!response.ok) {

            throw new Error(
                data?.message
                || 'HTTP ' + response.status
            );
        }

        return data;
    }


    async function loadAll() {

        setTreeLoading();

        try {

            const [
                allData,
                tree,
                managerData,
                regionData
            ] = await Promise.all([

                apiRequest(API + '/'),

                apiRequest(API + '/tree'),

                apiRequest(API + '/managers'),

                apiRequest(API + '/regions')
            ]);

            organizations =
                Array.isArray(allData)
                    ? allData
                    : [];

            treeData =
                Array.isArray(tree)
                    ? tree
                    : [];

            managers =
                Array.isArray(managerData)
                    ? managerData
                    : [];

            regions =
                Array.isArray(regionData)
                    ? regionData
                    : [];

            totalGroups.textContent =
                organizations.length;

            populateManagerOptions();
            populateRegionOptions();

            renderTree();

        }
        catch (error) {

            treeContainer.innerHTML = '';

            const errorBox =
                document.createElement('div');

            errorBox.className =
                'tree-error';

            errorBox.textContent =
                error.message
                || 'Không tải được cơ cấu tổ chức.';

            treeContainer.appendChild(
                errorBox
            );
        }
    }


    function setTreeLoading() {

        treeContainer.innerHTML =
            '<div class="tree-loading">' +
            'Đang tải cơ cấu tổ chức...' +
            '</div>';
    }


    function renderTree() {

        treeContainer.innerHTML = '';

        if (treeData.length === 0) {

            const empty =
                document.createElement('div');

            empty.className =
                'tree-empty';

            empty.textContent =
                'Chưa có nhóm kinh doanh.';

            treeContainer.appendChild(
                empty
            );

            return;
        }

        const rootList =
            document.createElement('ul');

        rootList.className =
            'org-tree';

        treeData.forEach(item => {

            rootList.appendChild(
                createTreeItem(item)
            );
        });

        treeContainer.appendChild(
            rootList
        );
    }


    function createTreeItem(item) {

        const li =
            document.createElement('li');

        const node =
            document.createElement('div');

        node.className =
            'org-node';


        const main =
            document.createElement('div');

        main.className =
            'org-main';

        const name =
            document.createElement('div');

        name.className =
            'org-name';

        name.textContent =
            item.orgName || '-';

        const code =
            document.createElement('div');

        code.className =
            'org-code';

        code.textContent =
            'Mã: ' + (item.orgCode || '-');

        main.appendChild(name);
        main.appendChild(code);


        const leader =
            createInfoBlock(
                'Trưởng nhóm',
                item.leaderName || '-'
            );


        const region =
            createInfoBlock(
                'Khu vực',
                item.regionName || '-'
            );


        const statusWrap =
            document.createElement('div');

        const badge =
            document.createElement('span');

        const active =
            item.status === 'ACTIVE';

        badge.className =
            'status-badge '
            + (
                active
                ? 'status-active'
                : 'status-inactive'
            );

        badge.textContent =
            active
                ? 'Đang hoạt động'
                : 'Ngừng hoạt động';

        statusWrap.appendChild(badge);


        const actions =
            document.createElement('div');

        actions.className =
            'node-actions';


        const editButton =
            document.createElement('button');

        editButton.type = 'button';

        editButton.className =
            'btn btn-sm btn-outline-primary';

        editButton.textContent =
            'Sửa';

        editButton.addEventListener(
            'click',
            () => openEditModal(item.id)
        );

        actions.appendChild(
            editButton
        );


        const membersButton =
            document.createElement('button');

        membersButton.type = 'button';
        membersButton.className =
            'btn btn-sm btn-outline-primary';
        membersButton.textContent = 'Xem thành viên';

        membersButton.addEventListener(
            'click',
            () => openMembersModal(item)
        );

        actions.appendChild(membersButton);

        if (active) {

            const stopButton =
                document.createElement('button');

            stopButton.type =
                'button';

            stopButton.className =
                'btn btn-sm btn-outline-danger';

            stopButton.textContent =
                'Ngừng';

            stopButton.addEventListener(
                'click',
                () => deactivateGroup(item)
            );

            actions.appendChild(
                stopButton
            );
        }


        node.appendChild(main);
        node.appendChild(leader);
        node.appendChild(region);
        node.appendChild(statusWrap);
        node.appendChild(actions);

        li.appendChild(node);


        if (
            Array.isArray(item.children)
            && item.children.length > 0
        ) {

            const children =
                document.createElement('ul');

            item.children.forEach(child => {

                children.appendChild(
                    createTreeItem(child)
                );
            });

            li.appendChild(children);
        }

        return li;
    }


    function createInfoBlock(
        label,
        value
    ) {

        const block =
            document.createElement('div');

        const labelEl =
            document.createElement('span');

        labelEl.className =
            'org-info-label';

        labelEl.textContent =
            label;

        const valueEl =
            document.createElement('span');

        valueEl.className =
            'org-info-value';

        valueEl.textContent =
            value;

        block.appendChild(labelEl);
        block.appendChild(valueEl);

        return block;
    }


    function populateManagerOptions(
        selectedId = ''
    ) {

        leaderSelect.innerHTML = '';

        const first =
            new Option(
                '-- Chọn trưởng nhóm --',
                ''
            );

        leaderSelect.add(first);

        managers.forEach(manager => {

            const option =
                new Option(
                    manager.fullName + ' (' + manager.email + ')',
                    manager.id
                );

            if (
                String(manager.id)
                === String(selectedId)
            ) {
                option.selected = true;
            }

            leaderSelect.add(option);
        });
    }


    function populateRegionOptions(
        selectedId = ''
    ) {

        regionSelect.innerHTML = '';

        regionSelect.add(
            new Option(
                '-- Chọn khu vực --',
                ''
            )
        );

        regions.forEach(region => {

            const option =
                new Option(
                    region.name,
                    region.id
                );

            if (
                String(region.id)
                === String(selectedId)
            ) {
                option.selected = true;
            }

            regionSelect.add(option);
        });
    }


    function populateParentOptions(
        selectedId = '',
        excludedId = ''
    ) {

        parentSelect.innerHTML = '';

        parentSelect.add(
            new Option(
                '-- Không có, là nhóm gốc --',
                ''
            )
        );

        organizations.forEach(item => {

            if (
                String(item.id)
                === String(excludedId)
            ) {
                return;
            }

            if (item.status !== 'ACTIVE') {
                return;
            }

            const option =
                new Option(
                    item.orgCode + ' - ' + item.orgName,
                    item.id
                );

            if (
                String(item.id)
                === String(selectedId)
            ) {
                option.selected = true;
            }

            parentSelect.add(option);
        });
    }


    function openCreateModal() {

        form.reset();

        groupIdInput.value = '';

        document.getElementById(
            'modalTitle'
        ).textContent =
            'Thêm nhóm kinh doanh';

        statusSelect.value =
            'ACTIVE';

        populateParentOptions();
        populateManagerOptions();
        populateRegionOptions();

        hideFormError();

        modal.classList.add('show');

        orgCodeInput.focus();
    }


    async function openMembersModal(item) {
        const overlay = document.createElement('div');
        overlay.style.cssText =
            'position:fixed;inset:0;background:rgba(0,0,0,.5);' +
            'display:flex;align-items:center;justify-content:center;' +
            'z-index:9999;padding:20px';

        const panel = document.createElement('div');
        panel.style.cssText =
            'background:white;border-radius:12px;padding:24px;' +
            'width:min(850px,100%);max-height:85vh;overflow:auto';

        const title = document.createElement('h3');
        title.textContent =
            'Thành viên - ' + (item.orgName || '');

        const close = document.createElement('button');
        close.type = 'button';
        close.className = 'btn btn-secondary';
        close.textContent = 'Đóng';

        const content = document.createElement('div');
        content.textContent = 'Đang tải thành viên...';

        function dismiss() {
            overlay.remove();
            document.removeEventListener('keydown', onKey);
        }

        function onKey(e) {
            if (e.key === 'Escape') dismiss();
        }

        close.addEventListener('click', dismiss);
        overlay.addEventListener('click', e => {
            if (e.target === overlay) dismiss();
        });
        document.addEventListener('keydown', onKey);

        panel.style.boxShadow =
            '0 16px 48px rgba(0,0,0,.18)';

        title.style.cssText =
            'margin:0;font-size:20px;color:#17324d';

        close.style.cssText =
            'margin-left:auto;display:block;margin-top:20px';

        content.style.cssText =
            'margin-top:20px;overflow-x:auto';
        panel.append(title, content, close);
        overlay.appendChild(panel);
        document.body.appendChild(overlay);

        try {
            const members = await apiRequest(
                API + '/' + item.id + '/members'
            );

            if (!overlay.isConnected) return;

            if (!Array.isArray(members)) {
                throw new Error('Dữ liệu không hợp lệ.');
            }

            content.replaceChildren();

            if (members.length === 0) {
                content.textContent =
                    'Nhóm chưa có thành viên.';
                return;
            }

            const table = document.createElement('table');
            table.className = 'table table-bordered';
            table.style.cssText =
                'width:100%;border-collapse:collapse;' +
                'text-align:left';

            const thead = document.createElement('thead');
            const trHead = document.createElement('tr');

            ['Họ tên', 'Email', 'Vai trò', 'Trạng thái']
                .forEach(label => {
                    const th = document.createElement('th');
                    th.textContent = label;
                    th.style.cssText =
                        'padding:12px;border-bottom:2px solid #ddd;' +
                        'background:#f5f7fa';
                    trHead.appendChild(th);
                });

            thead.appendChild(trHead);

            const tbody = document.createElement('tbody');

            members.forEach(member => {
                const tr = document.createElement('tr');

                [
                    member.fullName,
                    member.email,
                    member.role,
                    member.status
                ].forEach(value => {
                    const td = document.createElement('td');
                    td.textContent = value ?? '-';
                    td.style.cssText =
                        'padding:12px;border-bottom:1px solid #eee';
                    tr.appendChild(td);
                });

                tbody.appendChild(tr);
            });

            table.append(thead, tbody);
            content.appendChild(table);

        } catch (error) {
            if (overlay.isConnected) {
                content.textContent =
                    error.message || 'Không tải được thành viên.';
            }
        }
    }

    function openEditModal(id) {

        const item =
            organizations.find(
                x => Number(x.id)
                    === Number(id)
            );

        if (!item) {
            showToast(
                'Không tìm thấy nhóm kinh doanh.',
                'danger'
            );
            return;
        }

        form.reset();

        groupIdInput.value =
            item.id;

        orgCodeInput.value =
            item.orgCode || '';

        orgNameInput.value =
            item.orgName || '';

        statusSelect.value =
            item.status || 'ACTIVE';

        populateParentOptions(
            item.parentId ?? '',
            item.id
        );

        populateManagerOptions(
            item.leaderId ?? ''
        );

        populateRegionOptions(
            item.regionId ?? ''
        );

        document.getElementById(
            'modalTitle'
        ).textContent =
            'Chỉnh sửa nhóm kinh doanh';

        hideFormError();

        modal.classList.add('show');
    }


    function closeModal() {

        modal.classList.remove('show');

        form.reset();

        hideFormError();
    }


    async function handleSubmit(event) {

        event.preventDefault();

        const id =
            groupIdInput.value.trim();

        const orgCode =
            orgCodeInput.value.trim();

        const orgName =
            orgNameInput.value.trim();

        const leaderId =
            leaderSelect.value;

        const regionId =
            regionSelect.value;


        if (!orgCode) {
            showFormError(
                'Vui lòng nhập mã nhóm.'
            );
            return;
        }

        if (!orgName) {
            showFormError(
                'Vui lòng nhập tên nhóm.'
            );
            return;
        }

        if (!leaderId) {
            showFormError(
                'Vui lòng chọn trưởng nhóm.'
            );
            return;
        }

        if (!regionId) {
            showFormError(
                'Vui lòng chọn khu vực.'
            );
            return;
        }


        const body = {

            orgCode,

            orgName,

            parentId:
                parentSelect.value
                    ? Number(parentSelect.value)
                    : null,

            leaderId:
                Number(leaderId),

            regionId:
                Number(regionId),

            status:
                statusSelect.value
        };


        setSaving(true);

        try {

            if (id) {

                await apiRequest(
                    API + '/' + id,
                    {
                        method: 'PUT',
                        body:
                            JSON.stringify(body)
                    }
                );

                showToast(
                    'Cập nhật nhóm kinh doanh thành công.',
                    'success'
                );

            }
            else {

                await apiRequest(
                    API + '/',
                    {
                        method: 'POST',
                        body:
                            JSON.stringify(body)
                    }
                );

                showToast(
                    'Thêm nhóm kinh doanh thành công.',
                    'success'
                );
            }


            closeModal();

            await loadAll();

        }
        catch (error) {

            showFormError(
                error.message
                || 'Không lưu được nhóm kinh doanh.'
            );

        }
        finally {

            setSaving(false);
        }
    }


    async function deactivateGroup(item) {

        const confirmed =
            window.confirm(
                'Ngừng hoạt động nhóm "' + item.orgName + '"?'
            );

        if (!confirmed) {
            return;
        }

        try {

            await apiRequest(
                API + '/' + item.id,
                {
                    method: 'DELETE'
                }
            );

            showToast(
                'Ngừng hoạt động nhóm thành công.',
                'success'
            );

            await loadAll();

        }
        catch (error) {

            showToast(
                error.message
                || 'Không ngừng được nhóm kinh doanh.',
                'danger'
            );
        }
    }


    function showFormError(message) {

        formError.textContent =
            message;

        formError.classList.add(
            'show'
        );
    }


    function hideFormError() {

        formError.textContent = '';

        formError.classList.remove(
            'show'
        );
    }


    function setSaving(saving) {

        btnSave.disabled =
            saving;

        btnSave.textContent =
            saving
                ? 'Đang lưu...'
                : 'Lưu thông tin';
    }


    let toastTimer = null;

    function showToast(
        message,
        type
    ) {

        const toast =
            document.getElementById(
                'toast'
            );

        toast.textContent =
            message;

        toast.className =
            'toast-message show '
            + (
                type === 'success'
                    ? 'toast-success'
                    : 'toast-danger'
            );

        clearTimeout(toastTimer);

        toastTimer =
            setTimeout(
                () => {
                    toast.className =
                        'toast-message';
                },
                3500
            );
    }


    document.getElementById(
        'btnAddGroup'
    ).addEventListener(
        'click',
        openCreateModal
    );


    document.getElementById(
        'btnReload'
    ).addEventListener(
        'click',
        loadAll
    );


    document.getElementById(
        'btnCloseModal'
    ).addEventListener(
        'click',
        closeModal
    );


    document.getElementById(
        'btnCancel'
    ).addEventListener(
        'click',
        closeModal
    );


    modal.addEventListener(
        'click',
        event => {

            if (event.target === modal) {
                closeModal();
            }
        }
    );


    form.addEventListener(
        'submit',
        handleSubmit
    );


    loadAll();

})();
</script>

</body>
</html>