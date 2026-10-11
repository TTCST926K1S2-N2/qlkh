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
        ["Customer 360°", "360"],
        ["Sửa", "edit"],
      ].forEach(([label, mode]) => {
        const b = document.createElement("button");
        b.type = "button";
        b.textContent = label;
        b.style.marginRight = "6px";
        b.addEventListener("click", () => {
          if (mode === "360") {
            window.location.href =
              window.customerContextPath +
              "/customer-360?id=" +
              encodeURIComponent(c.id);
          } else {
            openCustomer(c.id, mode);
          }
        });
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

/* S3-04 DUPLICATE UI - reuse existing customers page */
(() => {
  "use strict";
  const $ = (id) => document.getElementById(id);
  const root = window.customerContextPath || "";
  const btn = $("s304FindDuplicates");
  if (!btn) return;
  const panel = $("s304Panel");
  const dialog = $("s304Dialog");
  const rows = $("s304Rows");
  let selected = null;
  const roleAllowed = window.s304CanMerge === true;
  btn.hidden = !roleAllowed;
  if (!roleAllowed) return;
  const endpoint = root + "/api/v1/customers";
  const showMessage = (id, text, error = false) => {
    const el = $(id);
    el.textContent = text;
    el.className = error ? "customer-error" : "customer-success";
  };
  const field = (container, label, value) => {
    const line = document.createElement("p");
    const strong = document.createElement("strong");
    strong.textContent = label + ": ";
    line.append(strong, document.createTextNode(value == null || value === "" ? "—" : String(value)));
    container.appendChild(line);
  };
  function displayCustomer(id, customer) {
    const el = $(id);
    el.replaceChildren();
    field(el, "ID", customer.id);
    field(el, "Doanh nghiệp", customer.companyName);
    field(el, "Mã số thuế", customer.taxCode);
    field(el, "Website", customer.website);
    field(el, "Ngành nghề", customer.industry);
    field(el, "Quy mô", customer.companySize);
    field(el, "Địa chỉ", customer.address);
    field(el, "Người phụ trách", customer.ownerId);
    field(el, "Trạng thái", customer.status);
  }
  function openCompare(pair) {
    selected = pair;
    $("s304TargetFirst").checked = true;
    displayCustomer("s304First", pair.first);
    displayCustomer("s304Second", pair.second);
    showMessage("s304DialogMessage", "");
    dialog.showModal();
  }
  async function call(url, options = {}) {
    const response = await fetch(url, {
      credentials: "same-origin",
      headers: { Accept: "application/json", ...(options.body ? { "Content-Type": "application/json" } : {}) },
      ...options,
    });
    if (response.status === 401) {
      window.location.href = root + "/login";
      throw Error("Phiên đăng nhập đã hết hạn");
    }
    const data = await response.json().catch(() => null);
    if (!response.ok || !data || data.success === false) {
      throw Error((data && data.message) || (response.status === 403 ? "Bạn không có quyền thực hiện thao tác này" : "Yêu cầu thất bại (" + response.status + ")"));
    }
    return data;
  }
  async function loadPairs() {
    panel.hidden = false;
    btn.disabled = true;
    showMessage("s304Message", "Đang tìm khách hàng trùng lặp...");
    rows.replaceChildren();
    try {
      const data = await call(endpoint + "/duplicates");
      const pairs = Array.isArray(data.data) ? data.data : [];
      showMessage("s304Message", "Tìm thấy " + pairs.length + " cặp khách hàng nghi ngờ trùng lặp.");
      for (const [index, pair] of pairs.entries()) {
        const tr = document.createElement("tr");
        for (const value of [index + 1, pair.first.companyName, pair.second.companyName, pair.score, (pair.reasons || []).join(", ")]) {
          const td = document.createElement("td");
          td.textContent = String(value ?? "");
          tr.appendChild(td);
        }
        const action = document.createElement("td");
        const b = document.createElement("button");
        b.type = "button";
        b.textContent = "So sánh & gộp";
        b.addEventListener("click", () => openCompare(pair));
        action.appendChild(b);
        tr.appendChild(action);
        rows.appendChild(tr);
      }
    } catch (e) {
      showMessage("s304Message", e.message, true);
    } finally {
      btn.disabled = false;
    }
  }
  btn.addEventListener("click", loadPairs);
  $("s304ClosePanel").addEventListener("click", () => { panel.hidden = true; });
  $("s304Cancel").addEventListener("click", () => dialog.close());
  $("s304Confirm").addEventListener("click", async () => {
    if (!selected) return;
    const keepFirst = $("s304TargetFirst").checked;
    const source = keepFirst ? selected.second : selected.first;
    const target = keepFirst ? selected.first : selected.second;
    if (!window.confirm("Gộp khách hàng #" + source.id + " vào #" + target.id + "? Thao tác không thể hoàn tác trên giao diện.")) return;
    const confirmButton = $("s304Confirm");
    confirmButton.disabled = true;
    try {
      const result = await call(endpoint + "/merge", {
        method: "POST",
        body: JSON.stringify({ sourceId: source.id, targetId: target.id }),
      });
      dialog.close();
      await loadPairs();
      showMessage("s304Message", "Gộp thành công: " + result.contacts + " liên hệ, " + result.opportunities + " cơ hội, " + result.attachments + " tệp đính kèm. Hãy tải lại trang để cập nhật danh sách khách hàng.");
    } catch (e) {
      showMessage("s304DialogMessage", e.message, true);
    } finally {
      confirmButton.disabled = false;
    }
  });
})();