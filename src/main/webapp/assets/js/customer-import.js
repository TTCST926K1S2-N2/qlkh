document.addEventListener("DOMContentLoaded", () => {
  const $ = id => document.getElementById(id);
  const dialog = $("customerImportDialog");
  if (!dialog) return;

  const context = window.customerContextPath || "";
  const endpoint = `${context}/api/v1/customers/import`;

  const fileInput = $("customerImportFile");
  const message = $("customerImportMessage");
  const previewBox = $("customerImportPreview");
  const summary = $("customerImportSummary");
  const errorsBody = $("customerImportErrors");
  const executeButton = $("executeCustomerImport");
  const previewButton = $("previewCustomerImport");

  let previewedFile = null;

  function setMessage(value) {
    message.textContent = value;
  }

  function resetPreview() {
    previewedFile = null;
    executeButton.disabled = true;
    previewBox.hidden = true;
    errorsBody.replaceChildren();
  }

  function renderResult(result) {
    previewBox.hidden = false;

    summary.textContent =
      `Tổng: ${result.totalRows || 0} | ` +
      `Hợp lệ: ${result.successCount || 0} | ` +
      `Lỗi: ${result.failedCount || 0}`;

    errorsBody.replaceChildren();

    for (const error of result.errors || []) {
      const tr = document.createElement("tr");

      for (const value of [
        error.row,
        error.taxCode,
        error.message
      ]) {
        const td = document.createElement("td");
        td.textContent = value ?? "";
        tr.appendChild(td);
      }

      errorsBody.appendChild(tr);
    }
  }

  async function upload(preview) {
    const file = fileInput.files[0];

    if (!file) {
      setMessage("Vui lòng chọn tệp Excel.");
      return;
    }

    if (!/\.(xlsx|xls)$/i.test(file.name)) {
      setMessage("Chỉ hỗ trợ tệp .xlsx hoặc .xls.");
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      setMessage("Tệp vượt quá dung lượng 10MB.");
      return;
    }

    previewButton.disabled = true;
    executeButton.disabled = true;

    setMessage(preview ? "Đang kiểm tra dữ liệu..." : "Đang nhập dữ liệu...");

    try {
      const formData = new FormData();
      formData.append("file", file);

      const url = preview ? `${endpoint}?preview=true` : endpoint;

      const response = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        body: formData
      });

      const result = await response.json();

      if (!response.ok || !result.success) {
        throw new Error(result.message || "Không thể xử lý Excel.");
      }

      renderResult(result);

      if (preview) {
        previewedFile = file;

        executeButton.disabled = (result.successCount || 0) === 0;

        setMessage("Đã kiểm tra xong. Chỉ các dòng hợp lệ được nhập.");
      } else {
        previewedFile = null;
        executeButton.disabled = true;
        setMessage("Đã hoàn tất nhập khách hàng. Tải lại trang để xem danh sách mới.");
      }

    } catch (error) {
      resetPreview();
      setMessage(error.message || "Có lỗi xảy ra.");
    } finally {
      previewButton.disabled = false;
    }
  }

  $("openCustomerImport").addEventListener("click", () => {
    resetPreview();
    fileInput.value = "";
    setMessage("");
    dialog.showModal();
  });

  $("closeCustomerImport").addEventListener("click", () => {
    dialog.close();
  });

  fileInput.addEventListener("change", resetPreview);

  $("downloadCustomerTemplate").addEventListener("click", () => {
    window.location.href = `${endpoint}/template`;
  });

  previewButton.addEventListener("click", () => upload(true));

  executeButton.addEventListener("click", () => {
    if (!previewedFile || previewedFile !== fileInput.files[0]) {
      setMessage("Vui lòng kiểm tra lại tệp trước khi nhập.");
      return;
    }

    upload(false);
  });
});