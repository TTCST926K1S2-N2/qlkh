document.addEventListener('DOMContentLoaded', () => {
  let currentStep = 1;
  let selectedFile = null;
  let parsedData = [];

  const modal = document.getElementById('importCustomerModal');
  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('excelFileInput');
  
  const btnPrev = document.getElementById('btnPrevStep');
  const btnNext = document.getElementById('btnNextStep');
  const btnFinish = document.getElementById('btnFinish');
  const btnDownloadErrors = document.getElementById('btnDownloadErrors');

  const totalRowsBadge = document.getElementById('totalRowsBadge');
  const validRowsBadge = document.getElementById('validRowsBadge');
  const invalidRowsBadge = document.getElementById('invalidRowsBadge');

  const filterAll = document.getElementById('filterAll');
  const filterValid = document.getElementById('filterValid');
  const filterInvalid = document.getElementById('filterInvalid');

  if (modal) {
    modal.addEventListener('hidden.bs.modal', resetImportForm);
  }

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
    const validExtensions = ['.xlsx', '.xls'];
    const fileName = file.name.toLowerCase();
    const isValidType = validExtensions.some(ext => fileName.endsWith(ext));

    if (!isValidType) {
      alert('Định dạng tệp không hợp lệ. Vui lòng chọn tệp .xlsx hoặc .xls');
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      alert('Dung lượng tệp vượt quá 10MB');
      return;
    }

    selectedFile = file;
    dropzone.querySelector('p').innerHTML = `Tệp đã chọn: <strong>${file.name}</strong> (${(file.size / 1024).toFixed(1)} KB)`;
    btnNext.disabled = false;
  }

  btnNext?.addEventListener('click', async () => {
    if (currentStep === 1) {
      await uploadAndPreviewFile();
    } else if (currentStep === 2) {
      await executeImport();
    }
  });

  btnPrev?.addEventListener('click', () => {
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
      const hasValidRows = parsedData.some(r => r.isValid);
      btnNext.disabled = !hasValidRows;
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
    btnNext.innerText = 'Đang xử lý...';

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fetch('/api/v1/customers/import/preview', {
        method: 'POST',
        body: formData
      });
      const result = await response.json();

      if (result.success) {
        parsedData = result.data || [];
        updateSummaryBadges();
        renderPreviewTable('all');
        goToStep(2);
      } else {
        alert(result.message || 'Không thể đọc dữ liệu từ tệp');
        btnNext.disabled = false;
        btnNext.innerText = 'Tiếp tục';
      }
    } catch (error) {
      console.error('Error previewing file:', error);
      alert('Đã xảy ra lỗi khi kết nối tới máy chủ');
      btnNext.disabled = false;
      btnNext.innerText = 'Tiếp tục';
    }
  }

  function updateSummaryBadges() {
    const total = parsedData.length;
    const valid = parsedData.filter(r => r.isValid).length;
    const invalid = total - valid;

    totalRowsBadge.innerText = `Tổng: ${total}`;
    validRowsBadge.innerText = `Hợp lệ: ${valid}`;
    invalidRowsBadge.innerText = `Lỗi: ${invalid}`;
  }

  function renderPreviewTable(filterType) {
    const tbody = document.getElementById('previewTableBody');
    if (!tbody) return;

    let rowsToRender = parsedData;
    if (filterType === 'valid') rowsToRender = parsedData.filter(r => r.isValid);
    if (filterType === 'invalid') rowsToRender = parsedData.filter(r => !r.isValid);

    if (rowsToRender.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" class="text-center py-3 text-muted">Không có dữ liệu phù hợp</td></tr>`;
      return;
    }

    tbody.innerHTML = rowsToRender.map(row => `
      <tr class="${row.isValid ? '' : 'table-danger'}">
        <td>${row.rowIndex || '-'}</td>
        <td>${escapeHtml(row.code || '')}</td>
        <td>${escapeHtml(row.name || '')}</td>
        <td>${escapeHtml(row.taxCode || '')}</td>
        <td>${escapeHtml(row.phone || '')}</td>
        <td>${escapeHtml(row.email || '')}</td>
        <td>
          <span class="badge ${row.isValid ? 'bg-success' : 'bg-danger'}">
            ${row.isValid ? 'Hợp lệ' : 'Lỗi'}
          </span>
        </td>
        <td class="text-danger fs-7">${row.errors ? escapeHtml(row.errors.join('; ')) : ''}</td>
      </tr>
    `).join('');
  }

  filterAll?.addEventListener('change', () => renderPreviewTable('all'));
  filterValid?.addEventListener('change', () => renderPreviewTable('valid'));
  filterInvalid?.addEventListener('change', () => renderPreviewTable('invalid'));

  async function executeImport() {
    btnNext.disabled = true;
    btnNext.innerText = 'Đang lưu...';

    const validRows = parsedData.filter(r => r.isValid);

    try {
      const response = await fetch('/api/v1/customers/import/execute', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rows: validRows })
      });
      const result = await response.json();

      if (result.success) {
        document.getElementById('importResultSummary').innerText = 
          `Đã nhập thành công ${result.successCount || validRows.length} / ${parsedData.length} bản ghi.`;

        const errorReportContainer = document.getElementById('errorReportDownload');
        if (result.hasErrorFile) {
          errorReportContainer.classList.remove('d-none');
        } else {
          errorReportContainer.classList.add('d-none');
        }

        goToStep(3);
      } else {
        alert(result.message || 'Lỗi trong quá trình xử lý Import');
        btnNext.disabled = false;
        btnNext.innerText = 'Tiến hành Import';
      }
    } catch (error) {
      console.error('Error executing import:', error);
      alert('Lỗi kết nối hệ thống');
      btnNext.disabled = false;
      btnNext.innerText = 'Tiến hành Import';
    }
  }

  btnDownloadErrors?.addEventListener('click', () => {
    window.location.href = '/api/v1/customers/import/export-errors';
  });

  function resetImportForm() {
    currentStep = 1;
    selectedFile = null;
    parsedData = [];
    if (fileInput) fileInput.value = '';
    if (dropzone) {
      dropzone.querySelector('p').innerHTML = 'Kéo thả file Excel vào đây hoặc <strong>Bấm để chọn file</strong>';
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