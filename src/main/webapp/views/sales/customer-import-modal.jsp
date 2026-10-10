
<script>
  window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>

<div class="modal fade" id="importCustomerModal" tabindex="-1" aria-labelledby="importCustomerModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl modal-dialog-centered">
    <div class="modal-content">
      
      <div class="modal-header">
        <h5 class="modal-title" id="importCustomerModalLabel">Import Khách Hàng Bằng Excel</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
      </div>
      
      <div class="modal-body">
        
        <ul class="nav nav-pills nav-justified mb-4" id="importStepper">
          <li class="nav-item">
            <span class="nav-link active" id="step1-tab">1. Chọn Tệp Mẫu</span>
          </li>
          <li class="nav-item">
            <span class="nav-link disabled" id="step2-tab">2. Xem Trước & Kiểm Tra</span>
          </li>
          <li class="nav-item">
            <span class="nav-link disabled" id="step3-tab">3. Hoàn Tất</span>
          </li>
        </ul>
->
        <div id="importAlertBox" class="alert alert-danger d-none mb-3" role="alert"></div>

        <div id="step1-content" class="step-content">
          <div class="alert alert-info d-flex justify-content-between align-items-center mb-3">
            <span>Vui lòng tải tệp Excel mẫu để chuẩn hóa cấu trúc dữ liệu trước khi tải lên.</span>
            <button type="button" class="btn btn-sm btn-outline-primary" id="btnDownloadTemplate">
              Tải tệp mẫu (.xlsx)
            </button>
          </div>
          
          <div class="dropzone-box text-center p-5 border border-2 border-dashed rounded" id="dropzone" style="cursor: pointer;">
            <p class="mb-1 text-dark fs-5" id="dropzoneText">Kéo thả tệp Excel vào đây hoặc <strong>Bấm để chọn tệp</strong></p>
            <small class="text-muted">Hỗ trợ định dạng .xlsx, .xls (Tối đa 10MB)</small>
            <input type="file" id="excelFileInput" accept=".xlsx, .xls" class="d-none">
          </div>
        </div>

        <div id="step2-content" class="step-content d-none">
          <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
            <div>
              <span class="badge bg-primary fs-6 me-2" id="totalRowsBadge">Tổng: 0</span>
              <span class="badge bg-success fs-6 me-2" id="validRowsBadge">Hợp lệ: 0</span>
              <span class="badge bg-warning text-dark fs-6 me-2" id="duplicateRowsBadge">Trùng lặp: 0</span>
              <span class="badge bg-danger fs-6" id="invalidRowsBadge">Lỗi: 0</span>
            </div>
            
            <div class="btn-group" role="group" aria-label="Bộ lọc xem trước">
              <input type="radio" class="btn-check" name="filterRows" id="filterAll" checked>
              <label class="btn btn-outline-secondary btn-sm" for="filterAll">Tất cả</label>
              
              <input type="radio" class="btn-check" name="filterRows" id="filterValid">
              <label class="btn btn-outline-success btn-sm" for="filterValid">Chỉ hợp lệ</label>
              
              <input type="radio" class="btn-check" name="filterRows" id="filterDuplicate">
              <label class="btn btn-outline-warning btn-sm text-dark" for="filterDuplicate">Chỉ bị trùng</label>
              
              <input type="radio" class="btn-check" name="filterRows" id="filterInvalid">
              <label class="btn btn-outline-danger btn-sm" for="filterInvalid">Chỉ bị lỗi</label>
            </div>
          </div>

          <div class="table-responsive style-scroll" style="max-height: 380px;">
            <table class="table table-bordered table-hover align-middle mb-0" id="previewTable">
              <thead class="table-light sticky-top">
                <tr>
                  <th style="width: 50px;">STT</th>
                  <th>Mã KH</th>
                  <th>Tên Khách Hàng</th>
                  <th>Mã Số Thuế</th>
                  <th>Số Điện Thoại</th>
                  <th>Email</th>
                  <th style="width: 100px;">Trạng Thái</th>
                  <th style="width: 130px;">Xử Lý Trùng</th>
                  <th>Chi Tiết Lỗi</th>
                </tr>
              </thead>
              <tbody id="previewTableBody">
  
              </tbody>
            </table>
          </div>
        </div>

        <div id="step3-content" class="step-content d-none text-center py-4">
          <h4 class="text-success mb-2">Đã xử lý xong dữ liệu!</h4>
          <p class="text-muted fs-6" id="importResultSummary">Đã nhập thành công 0/0 bản ghi.</p>
          
          <div id="errorReportContainer" class="mt-3 d-none">
            <button type="button" class="btn btn-outline-danger" id="btnDownloadErrors">
              Tải về danh sách bản ghi lỗi (.xlsx)
            </button>
          </div>
        </div>

      </div>

      <!-- Modal Footer -->
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary d-none" id="btnPrevStep">Quay lại</button>
        <button type="button" class="btn btn-primary" id="btnNextStep" disabled>Tiếp tục</button>
        <button type="button" class="btn btn-success d-none" id="btnFinish">Hoàn tất</button>
      </div>

    </div>
  </div>
</div>