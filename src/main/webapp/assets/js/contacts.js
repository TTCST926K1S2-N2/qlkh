"use strict";

(() => {
    const base = window.contactContextPath || "";
    const api = `${base}/api/v1`;
    const $ = (id) => document.getElementById(id);

    const filter = $("customerFilter");
    const search = $("contactSearch");
    const rows = $("contactRows");
    const message = $("contactMessage");
    const dialog = $("contactDialog");
    const form = $("contactForm");
    const formError = $("contactFormError");
    const saveButton = $("saveContact");

    const roles = {
        DECISION_MAKER: "Người quyết định",
        INFLUENCER: "Người ảnh hưởng",
        END_USER: "Người dùng cuối",
        BLOCKER: "Người cản trở"
    };

    let customers = [];
    let contacts = [];
    let editingId = null;
    let loadingSequence = 0;
    let busy = false;

    function notify(text, error = false) {
        message.textContent = text;
        message.style.color = error ? "#b91c1c" : "#2563eb";
    }

    function showError(text) {
        formError.textContent = text;
        formError.hidden = !text;
    }

    async function request(url, options = {}) {
        let response;

        try {
            response = await fetch(url, {
                credentials: "same-origin",
                headers: {
                    Accept: "application/json",
                    ...(options.body ? {
                        "Content-Type": "application/json"
                    } : {})
                },
                ...options
            });
        } catch {
            throw new Error("Không thể kết nối máy chủ.");
        }

        if (response.redirected ||
            response.status === 401 ||
            response.status === 403) {
            if (response.redirected || response.status === 401) {
                window.location.assign(`${base}/login`);
                throw new Error("Phiên đăng nhập đã hết hạn.");
            }

            throw new Error("Bạn không có quyền thực hiện thao tác này.");
        }

        const contentType = response.headers.get("content-type") || "";
        let data = null;

        if (contentType.includes("application/json")) {
            try {
                data = await response.json();
            } catch {
                throw new Error("Phản hồi máy chủ không hợp lệ.");
            }
        } else {
            throw new Error("Máy chủ không trả về dữ liệu JSON.");
        }

        if (!response.ok) {
            throw new Error(data?.message || "Thao tác thất bại.");
        }

        return data;
    }

    function addOption(select, value, label) {
        const option = document.createElement("option");
        option.value = String(value);
        option.textContent = label;
        select.append(option);
    }

    function populateCustomers() {
        const previous = filter.value;
        filter.replaceChildren();
        addOption(filter, "", "Chọn khách hàng");

        const destination = form.elements.namedItem("customerId");
        destination.replaceChildren();
        addOption(destination, "", "Chọn khách hàng");

        for (const customer of customers) {
            addOption(filter, customer.id, customer.companyName);
            addOption(destination, customer.id, customer.companyName);
        }

        if (customers.some((c) => String(c.id) === previous)) {
            filter.value = previous;
        } else if (customers.length) {
            filter.value = String(customers[0].id);
        }
    }

    function cell(tr, value) {
        const td = document.createElement("td");
        td.textContent = value == null || value === ""
            ? "—"
            : String(value);
        tr.append(td);
        return td;
    }

    function action(td, title, handler) {
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = title;
        button.addEventListener("click", handler);
        td.append(button);
    }

    function render() {
        rows.replaceChildren();

        const query = search.value.trim().toLocaleLowerCase("vi");

        const visible = contacts.filter((contact) => {
            const text = [
                contact.fullName,
                contact.jobTitle,
                contact.email,
                contact.phone,
                roles[contact.decisionRole]
            ].join(" ").toLocaleLowerCase("vi");

            return text.includes(query);
        });

        if (!visible.length) {
            const tr = document.createElement("tr");
            const td = cell(
                tr,
                contacts.length
                    ? "Không tìm thấy người liên hệ phù hợp."
                    : "Chưa có người liên hệ."
            );
            td.colSpan = 8;
            rows.append(tr);
            return;
        }

        visible.forEach((contact, index) => {
            const tr = document.createElement("tr");

            cell(tr, index + 1);
            cell(tr, contact.fullName);
            cell(tr, contact.jobTitle);
            cell(tr, contact.email);
            cell(tr, contact.phone);
            cell(tr, roles[contact.decisionRole] || "Chưa xác định");
            cell(tr, contact.isPrimary ? "Chính" : "—");

            const actions = cell(tr, "");
            actions.textContent = "";

            action(actions, "Sửa", () => openForm(contact));
            action(actions, "Xóa", () => removeContact(contact));

            rows.append(tr);
        });
    }

    async function loadContacts() {
        const sequence = ++loadingSequence;
        const customerId = filter.value;

        contacts = [];
        rows.replaceChildren();

        if (!customerId) {
            notify("Chưa có khách hàng để hiển thị.");
            render();
            return;
        }

        notify("Đang tải người liên hệ...");

        try {
            const data = await request(
                `${api}/contacts?customerId=${encodeURIComponent(customerId)}`
            );

            if (sequence !== loadingSequence) return;

            if (!Array.isArray(data)) {
                throw new Error("Danh sách liên hệ không hợp lệ.");
            }

            contacts = data;
            render();
            notify(`Đã tải ${contacts.length} người liên hệ.`);
            return true;
        } catch (error) {
            if (sequence !== loadingSequence) return;
            rows.replaceChildren();
            notify(error.message, true);
            return false;
        }
    }

    function openForm(contact = null) {
        if (busy || !customers.length) return;

        form.reset();
        showError("");
        editingId = contact ? contact.id : null;

        $("contactFormTitle").textContent = contact
            ? "Cập nhật người liên hệ"
            : "Thêm người liên hệ";

        const fields = [
            "fullName",
            "jobTitle",
            "email",
            "phone",
            "decisionRole"
        ];

        form.elements.namedItem("customerId").value =
            String(contact?.customerId ?? filter.value);

        for (const field of fields) {
            form.elements.namedItem(field).value =
                contact?.[field] ?? "";
        }

        form.elements.namedItem("isPrimary").checked =
            contact
                ? Boolean(contact.isPrimary)
                : !contacts.some((c) => c.isPrimary);

        dialog.showModal();
    }

    function payload() {
        const get = (name) =>
            form.elements.namedItem(name).value.trim();

        return {
            customerId: Number(get("customerId")),
            fullName: get("fullName"),
            jobTitle: get("jobTitle") || null,
            email: get("email") || null,
            phone: get("phone") || null,
            decisionRole: get("decisionRole") || null,
            isPrimary: form.elements.namedItem("isPrimary").checked
        };
    }

    async function saveContact(event) {
        event.preventDefault();
        if (busy || !form.reportValidity()) return;

        const data = payload();

        if (!Number.isSafeInteger(data.customerId) ||
            data.customerId <= 0 ||
            !data.fullName) {
            showError("Vui lòng chọn khách hàng và nhập họ tên.");
            return;
        }

        const old = contacts.find((c) => c.id === editingId);
        const moved = old &&
            Number(old.customerId) !== data.customerId;

        if (old?.isPrimary &&
            (moved || !data.isPrimary)) {
            showError(
                "Không thể bỏ hoặc chuyển đầu mối chính. " +
                "Hãy chỉ định người liên hệ khác làm đầu mối chính trước."
            );
            return;
        }

        if (moved && !window.confirm(
            "Chuyển người liên hệ sang khách hàng khác?"
        )) {
            return;
        }

        busy = true;
        saveButton.disabled = true;
        showError("");

        try {
            const url = editingId
                ? `${api}/contacts/${editingId}`
                : `${api}/contacts`;

            await request(url, {
                method: editingId ? "PUT" : "POST",
                body: JSON.stringify(data)
            });

            dialog.close();

            const wasEditing = editingId !== null;
            editingId = null;

            const refreshed = await loadContacts();
            if (!refreshed) return;

            if (moved) {
                notify("Đã chuyển người liên hệ sang khách hàng mới.");
            } else {
                notify(wasEditing
                    ? "Đã cập nhật người liên hệ."
                    : "Đã thêm người liên hệ.");
            }
        } catch (error) {
            showError(error.message);
        } finally {
            busy = false;
            saveButton.disabled = false;
        }
    }

    async function removeContact(contact) {
        if (busy) return;

        if (contact.isPrimary) {
            notify(
                "Không thể xóa đầu mối chính. " +
                "Hãy chỉ định người liên hệ khác làm đầu mối chính trước.",
                true
            );
            return;
        }

        if (!window.confirm(
            `Xóa người liên hệ "${contact.fullName}"?`
        )) {
            return;
        }

        busy = true;

        try {
            await request(`${api}/contacts/${contact.id}`, {
                method: "DELETE"
            });

            const refreshed = await loadContacts();
            if (refreshed) {
                notify("Đã xóa người liên hệ.");
            }
        } catch (error) {
            notify(error.message, true);
        } finally {
            busy = false;
        }
    }

    async function initialize() {
        $("addContact").disabled = true;
        notify("Đang tải danh sách khách hàng...");

        try {
            const data = await request(`${api}/customers`);

            if (!Array.isArray(data)) {
                throw new Error("Danh sách khách hàng không hợp lệ.");
            }

            customers = data;
            populateCustomers();
            $("addContact").disabled = !customers.length;

            const selected = new URLSearchParams(
                window.location.search
            ).get("customerId");

            if (selected &&
                customers.some((c) => String(c.id) === selected)) {
                filter.value = selected;
            }

            await loadContacts();
        } catch (error) {
            notify(error.message, true);
        }
    }

    filter.addEventListener("change", loadContacts);
    search.addEventListener("input", render);

    $("addContact").addEventListener("click", () => openForm());

    $("cancelContact").addEventListener("click", () => {
        if (!busy) dialog.close();
    });

    dialog.addEventListener("cancel", (event) => {
        if (busy) event.preventDefault();
    });

    form.addEventListener("submit", saveContact);

    initialize();
})();