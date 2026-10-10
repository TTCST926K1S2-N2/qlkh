<div class="modal fade" id="importCustomerModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-xl modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">Import Khách hàng từ Excel</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      
      <div class="modal-body">
        <ul class="nav nav-pills nav-justified mb-4" id="importStepper">
          <li class="nav-item"><span class="nav-link active" id="step1-tab">1. Chọn File</span></li>
          <li class="nav-item"><span class="nav-link disabled" id="step2-tab">2. Xem Trước & Kiểm Tra</span></li>
          <li class="nav-item"><span class="nav-link disabled" id="step3-tab">3. Kết Quả</span></li>
        </ul>

        <div id="step1-content" class="step-content">
          <div class="alert alert-info d-flex justify-content-between align-items-center">
            <span>Sử dụng tệp mẫu tiêu chuẩn để đảm bảo định dạng dữ liệu chính xác.</span>
            <a href="/api/v1/customers/import/template" class="btn btn-sm btn-outline-primary" id="btnDownloadTemplate">
              Tải file mẫu (.xlsx)
            </a>
          </div>
          
          <div class="dropzone-box text-center p-5 border border-2 border-dashed rounded" id="dropzone" style="cursor: pointer;">
            <p class="mb-1">Kéo thả file Excel vào đây hoặc <strong>Bấm để chọn file</strong></p>
            <small class="text-muted">Hỗ trợ định dạng .xlsx, .xls (Tối đa 10MB)</small>
            <input type="file" id="excelFileInput" accept=".xlsx, .xls" class="d-none">
          </div>
        </div>

        <div id="step2-content" class="step-content d-none">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <span class="badge bg-primary fs-6 me-2" id="totalRowsBadge">Tổng: 0</span>
              <span class="badge bg-success fs-6 me-2" id="validRowsBadge">Hợp lệ: 0</span>
              <span class="badge bg-danger fs-6" id="invalidRowsBadge">Lỗi: 0</span>
            </div>
            <div class="btn-group" role="group">
              <input type="radio" class="btn-check" name="filterRows" id="filterAll" checked>
              <label class="btn btn-outline-secondary btn-sm" for="filterAll">Tất cả</label>
              <input type="radio" class="btn-check" name="filterRows" id="filterValid">
              <label class="btn btn-outline-success btn-sm" for="filterValid">Hợp lệ</label>
              <input type="radio" class="btn-check" name="filterRows" id="filterInvalid">
              <label class="btn btn-outline-danger btn-sm" for="filterInvalid">Lỗi</label>
            </div>
          </div>

          <div class="table-responsive style-scroll" style="max-height: 400px;">
            <table class="table table-bordered table-hover align-middle" id="previewTable">
              <thead class="table-light sticky-top">
                <tr>
                  <th>Dòng</th>
                  <th>Mã KH</th>
                  <th>Tên Khách Hàng</th>
                  <th>Mã Số Thuế</th>
                  <th>Số Điện Thoại</th>
                  <th>Email</th>
                  <th>Trạng Thái</th>
                  <th>Chi Tiết Lỗi</th>
                </tr>
              </thead>
              <tbody id="previewTableBody">
                <!-- Data Render via JS -->
              </tbody>
            </table>
          </div>
        </div>

        <div id="step3-content" class="step-content d-none text-center py-4">
          <h4 class="text-success mb-2">Xử lý hoàn tất!</h4>
          <p class="text-muted" id="importResultSummary">Đã nhập thành công 0/0 bản ghi.</p>
          <div id="errorReportDownload" class="mt-3 d-none">
            <button class="btn btn-outline-danger" id="btnDownloadErrors">
              Tải danh sách dòng lỗi (.xlsx)
            </button>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" id="btnPrevStep" class="d-none">Quay lại</button>
        <button type="button" class="btn btn-primary" id="btnNextStep" disabled>Tiếp tục</button>
        <button type="button" class="btn btn-success d-none" id="btnFinish">Hoàn tất</button>
      </div>
    </div>
  </div>
</div>