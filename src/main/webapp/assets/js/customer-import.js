document.addEventListener('DOMContentLoaded', () => {

  const CONTEXT_PATH = window.APP_CONTEXT_PATH || '';

  const API_ENDPOINTS = {
    DOWNLOAD_TEMPLATE: `${CONTEXT_PATH}/customer-import?action=template`,
    PREVIEW_FILE: `${CONTEXT_PATH}/customer-import?action=preview`,
    EXECUTE_IMPORT: `${CONTEXT_PATH}/customer-import?action=execute`,
    DOWNLOAD_ERRORS: `${CONTEXT_PATH}/customer-import?action=export-errors`
  };

  let currentStep = 1;
  let selectedFile = null;
  let parsedData = [];

  // DOM Elements
  const modal = document.getElementById('importCustomerModal');
  const alertBox = document.getElementById('importAlertBox');
  const dropzone = document.getElementById('dropzone');
  const dropzoneText = document.getElementById('dropzoneText');
  const fileInput = document.getElementById('excelFileInput');
  
  const btnDownloadTemplate = document.getElementById('btnDownloadTemplate');
  const btnPrev = document.getElementById('btnPrevStep');
  const btnNext = document.getElementById('btnNextStep');
  const btnFinish = document.getElementById('btnFinish');
  const btnDownloadErrors = document.getElementById('btnDownloadErrors');

  const totalRowsBadge = document.getElementById('totalRowsBadge');
  const validRowsBadge = document.getElementById('validRowsBadge');
  const duplicateRowsBadge = document.getElementById('duplicateRowsBadge');
  const invalidRowsBadge = document.getElementById('invalidRowsBadge');

  const filterAll = document.getElementById('filterAll');
  const filterValid = document.getElementById('filterValid');
  const filterDuplicate = document.getElementById('filterDuplicate');
  const filterInvalid = document.getElementById('filterInvalid');

  if (modal) {
    modal.addEventListener('hidden.bs.modal', resetImportForm);
  }

  btnDownloadTemplate?.addEventListener('click', async () => {
    try {
      btnDownloadTemplate.disabled = true;
      btnDownloadTemplate.innerText = 'Đang tải tệp...';

      const response = await fetch(API_ENDPOINTS.DOWNLOAD_TEMPLATE, { method: 'GET' });
      if (!response.ok) throw new Error('Không thể tải tệp mẫu từ máy chủ.');

      const blob = await response.blob();
      downloadBlobFile(blob, 'KhachHang_Mau_Import.xlsx');
    } catch (error) {
      showErrorAlert(error.message || 'Lỗi khi tải tệp mẫu');
    } finally {
      btnDownloadTemplate.disabled = false;
      btnDownloadTemplate.innerText = 'Tải tệp mẫu (.xlsx)';
    }
  });

  if (dropzone && fileInput) {
    dropzone.addEventListener('click', () => fileInput.click());

    dropzone.addEventListener('dragover', (e) => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    });

    dropzone.addEventListener('dragleave', () => {
      dropzone.classList.remove('dragover');
    });

    dropzone.addEventListener('drop', (e) => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
      if (e.dataTransfer.files.length > 0) {
        handleFileSelect(e.dataTransfer.files[0]);
      }
    });

    fileInput.addEventListener('change', (e) => {
      if (e.target.files.length > 0) {
        handleFileSelect(e.target.files[0]);
      }
    });
  }

  function handleFileSelect(file) {
    hideErrorAlert();
    const validExtensions = ['.xlsx', '.xls'];
    const fileName = file.name.toLowerCase();
    const isValidType = validExtensions.some(ext => fileName.endsWith(ext));

    if (!isValidType) {
      showErrorAlert('Định dạng tệp không hợp lệ. Vui lòng chọn tệp .xlsx hoặc .xls');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      showErrorAlert('Dung lượng tệp vượt quá 10MB');
      return;
    }

    selectedFile = file;
    dropzoneText.innerHTML = `Tệp đã chọn: <strong>${escapeHtml(file.name)}</strong> (${(file.size / 1024).toFixed(1)} KB)`;
    btnNext.disabled = false;
  }

  btnNext?.addEventListener('click', async () => {
    hideErrorAlert();
    if (currentStep === 1) {
      await uploadAndPreviewFile();
    } else if (currentStep === 2) {
      await executeImport();
    }
  });

  btnPrev?.addEventListener('click', () => {
    hideErrorAlert();
    if (currentStep > 1) {
      goToStep(currentStep - 1);
    }
  });

  btnFinish?.addEventListener('click', () => {
    const bsModal = bootstrap.Modal.getInstance(modal);
    if (bsModal) bsModal.hide();
    window.location.reload();
  });

  function goToStep(step) {
    currentStep = step;

    document.querySelectorAll('.step-content').forEach(el => el.classList.add('d-none'));
    document.getElementById(`step${step}-content`).classList.remove('d-none');

    document.querySelectorAll('#importStepper .nav-link').forEach((tab, index) => {
      const tabStep = index + 1;
      tab.classList.remove('active', 'completed', 'disabled');
      if (tabStep === currentStep) {
        tab.classList.add('active');
      } else if (tabStep < currentStep) {
        tab.classList.add('completed');
      } else {
        tab.classList.add('disabled');
      }
    });

    if (currentStep === 1) {
      btnPrev.classList.add('d-none');
      btnNext.classList.remove('d-none');
      btnNext.innerText = 'Tiếp tục';
      btnNext.disabled = !selectedFile;
      btnFinish.classList.add('d-none');
    } else if (currentStep === 2) {
      btnPrev.classList.remove('d-none');
      btnNext.classList.remove('d-none');
      btnNext.innerText = 'Tiến hành Import';
      const hasImportableRows = parsedData.some(r => r.isValid || r.isDuplicate);
      btnNext.disabled = !hasImportableRows;
      btnFinish.classList.add('d-none');
    } else if (currentStep === 3) {
      btnPrev.classList.add('d-none');
      btnNext.classList.add('d-none');
      btnFinish.classList.remove('d-none');
    }
  }

  async function uploadAndPreviewFile() {
    if (!selectedFile) return;

    btnNext.disabled = true;
    btnNext.innerText = 'Đang đọc dữ liệu...';

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fetch(API_ENDPOINTS.PREVIEW_FILE, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error(`Máy chủ phản hồi lỗi (${response.status})`);
      }

      const result = await response.json();

      if (result.success) {
        parsedData = (result.data || []).map(row => ({
          ...row,
          duplicateAction: row.duplicateAction || 'SKIP'
        }));

        updateSummaryBadges();
        renderPreviewTable('all');
        goToStep(2);
      } else {
        showErrorAlert(result.message || 'Không thể đọc dữ liệu từ tệp');
        btnNext.disabled = false;
        btnNext.innerText = 'Tiếp tục';
      }
    } catch (error) {
      console.error('API Preview Error:', error);
      showErrorAlert('Đã xảy ra lỗi khi tải tệp lên máy chủ: ' + error.message);
      btnNext.disabled = false;
      btnNext.innerText = 'Tiếp tục';
    }
  }

  function updateSummaryBadges() {
    const total = parsedData.length;
    const invalid = parsedData.filter(r => !r.isValid && !r.isDuplicate).length;
    const duplicate = parsedData.filter(r => r.isDuplicate).length;
    const valid = parsedData.filter(r => r.isValid && !r.isDuplicate).length;

    totalRowsBadge.innerText = `Tổng: ${total}`;
    validRowsBadge.innerText = `Hợp lệ: ${valid}`;
    duplicateRowsBadge.innerText = `Trùng lặp: ${duplicate}`;
    invalidRowsBadge.innerText = `Lỗi: ${invalid}`;
  }

  function renderPreviewTable(filterType) {
    const tbody = document.getElementById('previewTableBody');
    if (!tbody) return;

    let rowsToRender = parsedData;
    if (filterType === 'valid') rowsToRender = parsedData.filter(r => r.isValid && !r.isDuplicate);
    if (filterType === 'duplicate') rowsToRender = parsedData.filter(r => r.isDuplicate);
    if (filterType === 'invalid') rowsToRender = parsedData.filter(r => !r.isValid && !r.isDuplicate);

    if (rowsToRender.length === 0) {
      tbody.innerHTML = `<tr><td colspan="9" class="text-center py-3 text-muted">Không có dữ liệu phù hợp</td></tr>`;
      return;
    }

    tbody.innerHTML = rowsToRender.map(row => {
      let rowClass = '';
      let statusBadge = '<span class="badge bg-success">Hợp lệ</span>';

      if (row.isDuplicate) {
        rowClass = 'table-warning';
        statusBadge = '<span class="badge bg-warning text-dark">Trùng lặp</span>';
      } else if (!row.isValid) {
        rowClass = 'table-danger';
        statusBadge = '<span class="badge bg-danger">Lỗi</span>';
      }

      const duplicateActionSelect = row.isDuplicate ? `
        <select class="form-select form-select-sm duplicate-action-select" data-row-index="${row.rowIndex}">
          <option value="SKIP" ${row.duplicateAction === 'SKIP' ? 'selected' : ''}>Bỏ qua</option>
          <option value="UPDATE" ${row.duplicateAction === 'UPDATE' ? 'selected' : ''}>Cập nhật</option>
        </select>
      ` : '-';

      return `
        <tr class="${rowClass}">
          <td>${row.rowIndex || '-'}</td>
          <td>${escapeHtml(row.code || '')}</td>
          <td>${escapeHtml(row.name || '')}</td>
          <td>${escapeHtml(row.taxCode || '')}</td>
          <td>${escapeHtml(row.phone || '')}</td>
          <td>${escapeHtml(row.email || '')}</td>
          <td>${statusBadge}</td>
          <td>${duplicateActionSelect}</td>
          <td class="text-danger fs-7">${row.errors ? escapeHtml(row.errors.join('; ')) : ''}</td>
        </tr>
      `;
    }).join('');

    tbody.querySelectorAll('.duplicate-action-select').forEach(select => {
      select.addEventListener('change', (e) => {
        const index = parseInt(e.target.getAttribute('data-row-index'), 10);
        const targetRow = parsedData.find(r => r.rowIndex === index);
        if (targetRow) {
          targetRow.duplicateAction = e.target.value;
        }
      });
    });
  }

  filterAll?.addEventListener('change', () => renderPreviewTable('all'));
  filterValid?.addEventListener('change', () => renderPreviewTable('valid'));
  filterDuplicate?.addEventListener('change', () => renderPreviewTable('duplicate'));
  filterInvalid?.addEventListener('change', () => renderPreviewTable('invalid'));

  async function executeImport() {
    btnNext.disabled = true;
    btnNext.innerText = 'Đang lưu vào hệ thống...';

    const importableRows = parsedData.filter(r => r.isValid || r.isDuplicate);

    try {
      const response = await fetch(API_ENDPOINTS.EXECUTE_IMPORT, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json; charset=UTF-8' },
        body: JSON.stringify({ rows: importableRows })
      });

      if (!response.ok) {
        throw new Error(`Máy chủ xử lý thất bại (${response.status})`);
      }

      const result = await response.json();

      if (result.success) {
        document.getElementById('importResultSummary').innerText = 
          `Đã xử lý thành công ${result.successCount || importableRows.length} / ${parsedData.length} bản ghi.`;

        const errorReportContainer = document.getElementById('errorReportContainer');
        if (result.hasErrorFile) {
          errorReportContainer.classList.remove('d-none');
        } else {
          errorReportContainer.classList.add('d-none');
        }

        goToStep(3);
      } else {
        showErrorAlert(result.message || 'Xử lý Import không thành công');
        btnNext.disabled = false;
        btnNext.innerText = 'Tiến hành Import';
      }
    } catch (error) {
      console.error('API Execute Error:', error);
      showErrorAlert('Không thể kết nối máy chủ để lưu dữ liệu: ' + error.message);
      btnNext.disabled = false;
      btnNext.innerText = 'Tiến hành Import';
    }
  }

  btnDownloadErrors?.addEventListener('click', async () => {
    try {
      btnDownloadErrors.disabled = true;
      btnDownloadErrors.innerText = 'Đang tải file...';

      const response = await fetch(API_ENDPOINTS.DOWNLOAD_ERRORS, { method: 'GET' });
      if (!response.ok) throw new Error('Không thể tải tệp báo cáo lỗi.');

      const blob = await response.blob();
      downloadBlobFile(blob, 'DanhSach_DongLoi_Import.xlsx');
    } catch (error) {
      alert('Lỗi tải tệp báo cáo lỗi: ' + error.message);
    } finally {
      btnDownloadErrors.disabled = false;
      btnDownloadErrors.innerText = 'Tải về danh sách bản ghi lỗi (.xlsx)';
    }
  });

  function downloadBlobFile(blob, defaultFileName) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = defaultFileName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }

  function showErrorAlert(msg) {
    if (alertBox) {
      alertBox.innerText = msg;
      alertBox.classList.remove('d-none');
    }
  }

  function hideErrorAlert() {
    if (alertBox) {
      alertBox.innerText = '';
      alertBox.classList.add('d-none');
    }
  }

  function resetImportForm() {
    currentStep = 1;
    selectedFile = null;
    parsedData = [];
    hideErrorAlert();
    if (fileInput) fileInput.value = '';
    if (dropzoneText) {
      dropzoneText.innerHTML = 'Kéo thả tệp Excel vào đây hoặc <strong>Bấm để chọn tệp</strong>';
    }
    if (filterAll) filterAll.checked = true;
    goToStep(1);
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
});