<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Import Lead từ Excel</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            background: #f5f7fb;
            color: #243247;
            font-family: Arial, sans-serif;
        }
        main {
            max-width: 1100px;
            margin: 36px auto;
            padding: 0 20px;
        }
        .panel {
            background: white;
            padding: 24px;
            border-radius: 12px;
            margin-bottom: 20px;
            box-shadow: 0 2px 12px #0000000b;
        }
        h1 { margin-top: 0; }
        .muted { color: #64748b; }
        .actions {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 12px;
            margin-top: 18px;
        }
        button, .download {
            padding: 11px 17px;
            border: 0;
            border-radius: 7px;
            background: #2563eb;
            color: white;
            font-size: 14px;
            cursor: pointer;
            text-decoration: none;
        }
        button:disabled {
            opacity: .5;
            cursor: not-allowed;
        }
        .download { background: #475569; }
        input[type=file] { max-width: 100%; }
        #message {
            margin-top: 16px;
            white-space: pre-wrap;
        }
        .error { color: #b91c1c; }
        .success { color: #15803d; }
        .table-wrap { overflow-x: auto; }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        th, td {
            padding: 11px;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
            vertical-align: top;
        }
        th { background: #f8fafc; }
        .bad { color: #b91c1c; }
        .good { color: #15803d; }
        [hidden] { display: none !important; }
    </style>
</head>
<body>
<main>
    <section class="panel">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap">
    <h1>Import Lead từ Excel</h1>
    <a class="download"
       href="${pageContext.request.contextPath}/customers">
        Quay lại quản lý khách hàng
    </a>
</div>
        <p class="muted">
            Tải file mẫu, điền thông tin Lead, sau đó xem trước
            và xác nhận import. Nguồn Lead nhập vào là EXCEL.
        </p>

        <div class="actions">
            <a class="download"
               href="${pageContext.request.contextPath}/api/v1/leads/import/template">
                Tải file Excel mẫu
            </a>
        </div>

        <form id="importForm">
            <p><label for="excelFile">Chọn tệp Excel (.xlsx hoặc .xls)</label></p>
            <input id="excelFile" name="file" type="file"
                   accept=".xlsx,.xls" required>

            <div class="actions">
                <button id="previewButton" type="submit">
                    Xem trước dữ liệu
                </button>
                <button id="importButton" type="button" disabled>
                    Xác nhận import
                </button>
            </div>
        </form>

        <div id="message" role="status" aria-live="polite"></div>
    </section>

    <section id="resultPanel" class="panel" hidden>
        <h2 id="resultTitle">Kết quả xem trước</h2>
        <p id="summary"></p>

        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>Dòng Excel</th>
                    <th>Doanh nghiệp</th>
                    <th>Người liên hệ</th>
                    <th>Email</th>
                    <th>Số điện thoại</th>
                    <th>Trạng thái / Lỗi</th>
                </tr>
                </thead>
                <tbody id="resultRows"></tbody>
            </table>
        </div>
    </section>
</main>

<script>
(function () {
    "use strict";

    const api = "${pageContext.request.contextPath}/api/v1/leads/import";
    const form = document.getElementById("importForm");
    const fileInput = document.getElementById("excelFile");
    const previewButton = document.getElementById("previewButton");
    const importButton = document.getElementById("importButton");
    const message = document.getElementById("message");
    const resultPanel = document.getElementById("resultPanel");
    const resultTitle = document.getElementById("resultTitle");
    const summary = document.getElementById("summary");
    const resultRows = document.getElementById("resultRows");

    let previewedFile = null;
    let busy = false;

    function notify(text, error) {
        message.textContent = text || "";
        message.className = error ? "error" : "success";
    }

    function resetPreview() {
        previewedFile = null;
        importButton.disabled = true;
        resultPanel.hidden = true;
        resultRows.replaceChildren();
        notify("", false);
    }

    function setBusy(value) {
        busy = value;
        previewButton.disabled = value;
        fileInput.disabled = value;
        importButton.disabled = value || !previewedFile;
    }

    function cell(tr, value, className) {
        const td = document.createElement("td");
        td.textContent = value == null ? "" : String(value);
        if (className) td.className = className;
        tr.appendChild(td);
    }

    function showResult(data, preview) {
        resultPanel.hidden = false;
        resultTitle.textContent = preview
            ? "Kết quả xem trước"
            : "Kết quả import";

        summary.textContent =
            "Tổng số dòng: " + (data.totalRows ?? 0) +
            " | Hợp lệ / Thành công: " + (data.successCount ?? 0) +
            " | Lỗi: " + (data.failedCount ?? 0);

        resultRows.replaceChildren();

        for (const row of (data.rows || [])) {
            const tr = document.createElement("tr");
            cell(tr, row.row);
            cell(tr, row.companyName);
            cell(tr, row.contactName);
            cell(tr, row.email);
            cell(tr, row.phone);
            cell(
                tr,
                row.valid
                    ? (preview ? "Hợp lệ" : "Đã import")
                    : (row.message || "Không hợp lệ"),
                row.valid ? "good" : "bad"
            );
            resultRows.appendChild(tr);
        }
    }

    async function submitFile(preview) {
        const file = fileInput.files[0];

        if (!file) {
            notify("Vui lòng chọn file Excel.", true);
            return;
        }

        if (!/\.(xlsx|xls)$/i.test(file.name)) {
            notify("Chỉ chấp nhận file .xlsx hoặc .xls.", true);
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            notify("File vượt quá giới hạn 10 MB.", true);
            return;
        }

        if (!preview && file !== previewedFile) {
            notify("File đã thay đổi. Vui lòng xem trước lại.", true);
            return;
        }

        setBusy(true);
        notify(preview ? "Đang xem trước..." : "Đang import...", false);

        try {
            const body = new FormData();
            body.append("file", file);

            const url = preview ? api + "?preview=true" : api;

            const response = await fetch(url, {
                method: "POST",
                body: body,
                credentials: "same-origin"
            });

            const contentType = response.headers.get("content-type") || "";

            if (!contentType.includes("application/json")) {
                throw new Error(
                    "Máy chủ không trả về JSON. Hãy kiểm tra đăng nhập."
                );
            }

            const data = await response.json();

            if (!response.ok || data.success !== true) {
                throw new Error(data.message || "Không thể xử lý file Excel.");
            }

            showResult(data, preview);

            if (preview) {
                previewedFile = Number(data.successCount) > 0
                    ? file
                    : null;

                notify(
                    previewedFile
                        ? "Đã xem trước. Kiểm tra kết quả trước khi xác nhận."
                        : "Không có dòng hợp lệ để import.",
                    !previewedFile
                );
            } else {
                previewedFile = null;
                notify(
                    "Đã xử lý import. Thành công: " +
                    (data.successCount ?? 0) +
                    ", lỗi: " + (data.failedCount ?? 0) + ".",
                    false
                );
            }
        } catch (error) {
            if (preview) {
                previewedFile = null;
                resultPanel.hidden = true;
            }
            notify(error.message || "Có lỗi xảy ra.", true);
        } finally {
            setBusy(false);
        }
    }

    fileInput.addEventListener("change", resetPreview);

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        if (!busy) submitFile(true);
    });

    importButton.addEventListener("click", function () {
        if (busy || !previewedFile) return;

        if (!window.confirm(
            "Xác nhận ghi các Lead hợp lệ vào cơ sở dữ liệu?"
        )) {
            return;
        }

        submitFile(false);
    });
})();
</script>
</body>
</html>