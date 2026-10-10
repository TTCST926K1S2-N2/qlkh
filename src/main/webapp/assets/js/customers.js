(() => {
  "use strict";
  const $ = (id) => document.getElementById(id);
  const api = window.customerContextPath + "/api/v1/customers";
  const form = $("customerForm"),
    dialog = $("customerDialog"),
    rows = $("customerRows"),
    msg = $("customerMessage");
  let customers = [],
    editingId = null,
    readOnly = false;
  const labels = {
    POTENTIAL: "Tiềm năng",
    IN_PROGRESS: "Đang giao dịch",
    CUSTOMER: "Khách hàng",
    INACTIVE: "Ngừng hợp tác",
  };
  function formError(text) {
    const error = $("customerFormError");
    error.textContent = text || "";
    error.hidden = !text;
  }

  function message(t, error = false) {
    msg.textContent = t;
    msg.className = error ? "customer-error" : "customer-success";
  }
  async function request(url, options = {}) {
    const res = await fetch(url, {
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
      },
      ...options,
    });
    const loginUrl = window.customerContextPath + "/login";
    const responseUrl = new URL(res.url, location.href);

    if (
      res.status === 401 ||
      (res.redirected && responseUrl.pathname === loginUrl)
    ) {
      location.replace(loginUrl);
      throw Error("Phiên đăng nhập đã hết hạn");
    }
    let data;
    try {
      data = await res.json();
    } catch {
      if (res.status === 403) {
        throw Error("Bạn không có quyền thực hiện thao tác này.");
      }
      throw Error("Phản hồi máy chủ không hợp lệ");
    }
    if (!res.ok)
      throw Error(data.message || "Yêu cầu thất bại (" + res.status + ")");
    return data;
  }
  function cell(tr, value) {
    const td = document.createElement("td");
    td.textContent = value == null ? "" : String(value);
    tr.appendChild(td);
    return td;
  }
  function render() {
    rows.replaceChildren();
    const q = $("customerSearch").value.trim().toLocaleLowerCase("vi");
    const status = $("customerStatus").value;
    const filtered = customers.filter(
      (c) =>
        (!status || c.status === status) &&
        (!q ||
          [c.companyName, c.taxCode, c.industry, c.address].some((v) =>
            String(v || "")
              .toLocaleLowerCase("vi")
              .includes(q),
          )),
    );
    if (!filtered.length) {
      const tr = document.createElement("tr");
      const td = cell(tr, "Không có khách hàng phù hợp");
      td.colSpan = 7;
      rows.appendChild(tr);
      return;
    }
    filtered.forEach((c, i) => {
      const tr = document.createElement("tr");
      cell(tr, i + 1);
      cell(tr, c.companyName);
      cell(tr, c.taxCode);
      cell(tr, c.industry);
      cell(tr, c.ownerId);
      cell(tr, labels[c.status] || c.status);
      const actions = cell(tr, "");
      [
        ["Chi tiết", "view"],
        ["Sửa", "edit"],
      ].forEach(([label, mode]) => {
        const b = document.createElement("button");
        b.type = "button";
        b.textContent = label;
        b.style.marginRight = "6px";
        b.addEventListener("click", () => openCustomer(c.id, mode));
        actions.appendChild(b);
      });
      rows.appendChild(tr);
    });
  }
  async function load() {
    try {
      message("Đang tải dữ liệu...");
      const data = await request(api);
      if (!Array.isArray(data)) throw Error("Danh sách API không hợp lệ");
      customers = data;
      render();
      message("Đã tải " + data.length + " khách hàng");
    } catch (e) {
      customers = [];
      render();
      message(e.message, true);
    }
  }
  function setFields(c) {
    for (const name of [
      "companyName",
      "taxCode",
      "industry",
      "companySize",
      "website",
      "address",
      "ownerId",
      "status",
    ]) {
      form.elements[name].value =
        c && c[name] != null
          ? String(c[name])
          : name === "status"
            ? "POTENTIAL"
            : "";
    }
  }
  function show(c, mode) {
    formError("");
    editingId = c ? c.id : null;
    readOnly = mode === "view";
    form.reset();
    setFields(c);
    $("customerOwnerLabel").hidden = !readOnly;
    $("customerOwnerHint").hidden = readOnly;
    $("customerOwnerHint").textContent = editingId
      ? "Giữ nguyên người phụ trách hiện tại."
      : "Tự động giao cho tài khoản đang đăng nhập.";
    $("customerFormTitle").textContent = readOnly
      ? "Chi tiết khách hàng"
      : editingId
        ? "Sửa khách hàng"
        : "Thêm khách hàng";
    for (const element of form.querySelectorAll("input,select"))
      element.disabled = readOnly;
    $("customerSave").hidden = readOnly;
    dialog.showModal();
  }
  async function openCustomer(id, mode) {
    try {
      const c = await request(api + "/" + encodeURIComponent(id));
      show(c, mode);
    } catch (e) {
      message(e.message, true);
    }
  }
  function close() {
    dialog.close();
  }
  $("addCustomer").addEventListener("click", () => show(null, "create"));
  $("customerCancel").addEventListener("click", close);
  $("customerSearch").addEventListener("input", render);
  $("customerStatus").addEventListener("change", render);
  form.elements.taxCode.addEventListener("input", () => formError(""));
  form.addEventListener("submit", async (e) => {
    formError("");
    e.preventDefault();
    if (readOnly) return;
    const payload = {};
    for (const name of [
      "companyName",
      "taxCode",
      "industry",
      "companySize",
      "website",
      "address",
      "status",
    ]) {
      const v = form.elements[name].value.trim();
      payload[name] = v || null;
    }
    if (!payload.companyName) {
      message("Tên doanh nghiệp không được để trống", true);
      return;
    }
    payload.status = payload.status || "POTENTIAL";
    const btn = $("customerSave");
    btn.disabled = true;
    try {
      const edit = editingId !== null;
      await request(edit ? api + "/" + encodeURIComponent(editingId) : api, {
        method: edit ? "PUT" : "POST",
        body: JSON.stringify(payload),
      });
      close();
      await load();
      message(edit ? "Cập nhật thành công" : "Thêm khách hàng thành công");
    } catch (err) {
      formError(err.message || "Không thể lưu khách hàng");
    } finally {
      btn.disabled = false;
    }
  });
  load();
})();
