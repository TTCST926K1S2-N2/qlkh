package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.edu.ictu.qlkh.model.ImportLeadResult;
import vn.edu.ictu.qlkh.service.ImportLeadService;
import vn.edu.ictu.qlkh.service.PermissionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@WebServlet("/api/v1/leads/import/*")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 11 * 1024 * 1024
)
public class ImportLeadServlet extends HttpServlet {

    private ImportLeadService importLeadService;
    private PermissionService permissionService;

    public ImportLeadServlet() {
    }

    ImportLeadServlet(
            ImportLeadService importLeadService,
            PermissionService permissionService) {

        this.importLeadService =
                importLeadService;

        this.permissionService =
                permissionService;
    }

    @Override
    public void init() {

        if (importLeadService == null) {
            importLeadService =
                    new ImportLeadService();
        }

        if (permissionService == null) {
            permissionService =
                    new PermissionService();
        }
    }

    /*
     * GET /api/v1/leads/import/template
     *
     * Tai file Excel mau.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        SessionUser user =
                getSessionUser(request);

        if (user == null) {
            writeUnauthorized(response);
            return;
        }

        if (permissionService
                .resolveDataScope(user.role()) == null) {

            writeForbidden(response);
            return;
        }

        String pathInfo =
                request.getPathInfo();

        if (!"/template".equals(pathInfo)) {

            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
            );

            writeMessage(
                    response,
                    false,
                    "Khong tim thay tai nguyen"
            );

            return;
        }

        writeTemplate(response);
    }

    /*
     * POST /api/v1/leads/import?preview=true
     *     -> chi Preview.
     *
     * POST /api/v1/leads/import
     *     -> Import that.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        SessionUser user =
                getSessionUser(request);

        if (user == null) {
            writeUnauthorized(response);
            return;
        }

        if (permissionService
                .resolveDataScope(user.role()) == null) {

            writeForbidden(response);
            return;
        }

        Part filePart =
                request.getPart("file");

        if (filePart == null ||
                filePart.getSize() == 0) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            writeMessage(
                    response,
                    false,
                    "Vui long chon tep Excel"
            );

            return;
        }

        String fileName =
                filePart.getSubmittedFileName();

        if (!isExcelFile(fileName)) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            writeMessage(
                    response,
                    false,
                    "Tep khong hop le. Chi ho tro .xlsx hoac .xls"
            );

            return;
        }

        boolean preview =
                "true".equalsIgnoreCase(
                        request.getParameter("preview")
                );

        try (InputStream inputStream =
                     filePart.getInputStream()) {

            ImportLeadResult result =
                    preview
                            ? importLeadService
                                    .previewLeads(
                                            inputStream,
                                            user.id(),
                                            user.role()
                                    )
                            : importLeadService
                                    .importLeads(
                                            inputStream,
                                            user.id(),
                                            user.role()
                                    );

            writeImportResult(
                    response,
                    result,
                    preview
            );

        } catch (IllegalArgumentException exception) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            writeMessage(
                    response,
                    false,
                    safeMessage(exception)
            );

        } catch (SecurityException exception) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            writeMessage(
                    response,
                    false,
                    safeMessage(exception)
            );
        }
    }

    /*
     * Doc tai khoan dang dang nhap tu Session.
     */
    private SessionUser getSessionUser(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userIdObject =
                session.getAttribute("userId");

        Object roleObject =
                session.getAttribute("userRole");

        if (!(userIdObject instanceof Number)
                || roleObject == null) {

            return null;
        }

        long userId =
                ((Number) userIdObject)
                        .longValue();

        String role =
                roleObject.toString()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (userId <= 0 ||
                role.isBlank()) {

            return null;
        }

        return new SessionUser(
                userId,
                role
        );
    }

    /*
     * Chi chap nhan Excel xlsx/xls.
     */
    private boolean isExcelFile(
            String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            return false;
        }

        String normalized =
                fileName.toLowerCase(
                        Locale.ROOT
                );

        return normalized.endsWith(".xlsx")
                || normalized.endsWith(".xls");
    }

    /*
     * Tao file Excel mau cho Lead Import.
     *
     * Thu tu cot phai trung voi
     * ImportLeadService.
     */
    private void writeTemplate(
            HttpServletResponse response)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=lead-import-template.xlsx"
        );

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet("Leads");

            Row header =
                    sheet.createRow(0);

            String[] columns = {
                    "Ten doanh nghiep",
                    "Ho ten lien he",
                    "Email",
                    "So dien thoai",
                    "Chuc danh",
                    "Ma so thue",
                    "Nganh nghe",
                    "Quy mo",
                    "Website",
                    "Dia chi"
            };

            for (int i = 0;
                 i < columns.length;
                 i++) {

                header.createCell(i)
                        .setCellValue(columns[i]);
            }

            /*
             * Mot dong vi du de nguoi dung
             * biet cach nhap du lieu.
             */
            Row example =
                    sheet.createRow(1);

            example.createCell(0)
                    .setCellValue("Cong ty ABC");

            example.createCell(1)
                    .setCellValue("Nguyen Van A");

            example.createCell(2)
                    .setCellValue("a@example.com");

            example.createCell(3)
                    .setCellValue("0901234567");

            example.createCell(4)
                    .setCellValue("Truong phong");

            example.createCell(5)
                    .setCellValue("0101234567");

            example.createCell(6)
                    .setCellValue("Cong nghe thong tin");

            example.createCell(7)
                    .setCellValue("50-100");

            example.createCell(8)
                    .setCellValue("https://example.vn");

            example.createCell(9)
                    .setCellValue("Ha Noi");

            for (int i = 0;
                 i < columns.length;
                 i++) {

                sheet.autoSizeColumn(i);
            }

            workbook.write(
                    response.getOutputStream()
            );
        }
    }

    /*
     * Chuyen ImportLeadResult thanh JSON
     * de Frontend hien Preview va loi.
     */
    private void writeImportResult(
            HttpServletResponse response,
            ImportLeadResult result,
            boolean preview)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        prepareJson(response);

        StringBuilder json =
                new StringBuilder();

        json.append("{")
                .append("\"success\":true,")
                .append("\"preview\":")
                .append(preview)
                .append(",")
                .append("\"totalRows\":")
                .append(result.getTotalRows())
                .append(",")
                .append("\"successCount\":")
                .append(result.getSuccessCount())
                .append(",")
                .append("\"failedCount\":")
                .append(result.getFailedCount())
                .append(",")
                .append("\"rows\":[");

        /*
         * Danh sach tat ca cac dong Preview.
         */
        for (int i = 0;
             i < result.getRows().size();
             i++) {

            ImportLeadResult.PreviewRow row =
                    result.getRows().get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"row\":")
                    .append(row.getRow())
                    .append(",")
                    .append("\"companyName\":")
                    .append(jsonString(
                            row.getCompanyName()))
                    .append(",")
                    .append("\"contactName\":")
                    .append(jsonString(
                            row.getContactName()))
                    .append(",")
                    .append("\"email\":")
                    .append(jsonString(
                            row.getEmail()))
                    .append(",")
                    .append("\"phone\":")
                    .append(jsonString(
                            row.getPhone()))
                    .append(",")
                    .append("\"valid\":")
                    .append(row.isValid())
                    .append(",")
                    .append("\"duplicate\":")
                    .append(row.isDuplicate())
                    .append(",")
                    .append("\"message\":")
                    .append(jsonString(
                            row.getMessage()))
                    .append("}");
        }

        json.append("],")
                .append("\"errors\":[");

        /*
         * Danh sach rieng cac dong bi loi.
         */
        for (int i = 0;
             i < result.getErrors().size();
             i++) {

            ImportLeadResult.RowError error =
                    result.getErrors().get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"row\":")
                    .append(error.getRow())
                    .append(",")
                    .append("\"email\":")
                    .append(jsonString(
                            error.getEmail()))
                    .append(",")
                    .append("\"phone\":")
                    .append(jsonString(
                            error.getPhone()))
                    .append(",")
                    .append("\"message\":")
                    .append(jsonString(
                            error.getMessage()))
                    .append("}");
        }

        json.append("]}");

        response.getWriter()
                .write(json.toString());
    }

    private void writeUnauthorized(
            HttpServletResponse response)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        writeMessage(
                response,
                false,
                "Chua dang nhap"
        );
    }

    private void writeForbidden(
            HttpServletResponse response)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        writeMessage(
                response,
                false,
                "Ban khong co quyen Import Lead"
        );
    }

    private void writeMessage(
            HttpServletResponse response,
            boolean success,
            String message)
            throws IOException {

        prepareJson(response);

        String json =
                "{"
                + "\"success\":"
                + success
                + ","
                + "\"message\":"
                + jsonString(message)
                + "}";

        response.getWriter()
                .write(json);
    }

    private void prepareJson(
            HttpServletResponse response) {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );
    }

    /*
     * Chuyen String Java thanh String JSON.
     * Null se thanh JSON null.
     */
    private String jsonString(
            String value) {

        if (value == null) {
            return "null";
        }

        return "\""
                + escapeJson(value)
                + "\"";
    }

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String safeMessage(
            Exception exception) {

        if (exception == null ||
                exception.getMessage() == null ||
                exception.getMessage().isBlank()) {

            return "Khong the xu ly Import Lead";
        }

        return exception.getMessage();
    }

    private record SessionUser(
            long id,
            String role) {
    }
}