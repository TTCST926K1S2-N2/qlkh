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

import vn.edu.ictu.qlkh.model.ImportCustomerResult;
import vn.edu.ictu.qlkh.service.ImportCustomerService;
import vn.edu.ictu.qlkh.service.PermissionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@WebServlet("/api/v1/customers/import/*")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 11 * 1024 * 1024
)
public class ImportCustomerServlet extends HttpServlet {

    private ImportCustomerService importCustomerService;
    private PermissionService permissionService;

    public ImportCustomerServlet() {
    }

    ImportCustomerServlet(
            ImportCustomerService importCustomerService,
            PermissionService permissionService) {

        this.importCustomerService =
                importCustomerService;

        this.permissionService =
                permissionService;
    }

    @Override
    public void init() {

        if (importCustomerService == null) {
            importCustomerService =
                    new ImportCustomerService();
        }

        if (permissionService == null) {
            permissionService =
                    new PermissionService();
        }
    }

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

        if (filePart == null
                || filePart.getSize() == 0) {

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

            ImportCustomerResult result =
                    preview
                            ? importCustomerService
                                    .previewCustomers(
                                            inputStream,
                                            user.id(),
                                            user.role()
                                    )
                            : importCustomerService
                                    .importCustomers(
                                            inputStream,
                                            user.id(),
                                            user.role()
                                    );

            writeImportResult(
                    response,
                    result,
                    preview
            );
        }
    }

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
                ((Number) userIdObject).longValue();

        String role =
                roleObject.toString()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (userId <= 0
                || role.isBlank()) {
            return null;
        }

        return new SessionUser(
                userId,
                role
        );
    }

    private boolean isExcelFile(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {
            return false;
        }

        String normalized =
                fileName.toLowerCase(Locale.ROOT);

        return normalized.endsWith(".xlsx")
                || normalized.endsWith(".xls");
    }

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
                "attachment; filename=customer-import-template.xlsx"
        );

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet("Customers");

            Row header =
                    sheet.createRow(0);

            String[] columns = {
                    "Ten doanh nghiep",
                    "Ma so thue",
                    "Nganh nghe",
                    "Quy mo",
                    "Website",
                    "Dia chi",
                    "Nguoi phu trach ID",
                    "Trang thai"
            };

            for (int i = 0;
                 i < columns.length;
                 i++) {

                header.createCell(i)
                        .setCellValue(columns[i]);

                sheet.autoSizeColumn(i);
            }

            Row example =
                    sheet.createRow(1);

            example.createCell(0)
                    .setCellValue("Cong ty ABC");

            example.createCell(1)
                    .setCellValue("0101234567");

            example.createCell(2)
                    .setCellValue("Cong nghe thong tin");

            example.createCell(3)
                    .setCellValue("50-100");

            example.createCell(4)
                    .setCellValue("https://example.vn");

            example.createCell(5)
                    .setCellValue("Ha Noi");

            example.createCell(6)
                    .setCellValue("");

            example.createCell(7)
                    .setCellValue("POTENTIAL");

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

    private void writeImportResult(
            HttpServletResponse response,
            ImportCustomerResult result,
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
                .append("\"errors\":[");

        for (int i = 0;
             i < result.getErrors().size();
             i++) {

            ImportCustomerResult.RowError error =
                    result.getErrors().get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"row\":")
                    .append(error.getRow())
                    .append(",")
                    .append("\"taxCode\":\"")
                    .append(
                            escapeJson(
                                    error.getTaxCode())
                    )
                    .append("\",")
                    .append("\"message\":\"")
                    .append(
                            escapeJson(
                                    error.getMessage())
                    )
                    .append("\"}");
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
                "Ban khong co quyen Import khach hang"
        );
    }

    private void writeMessage(
            HttpServletResponse response,
            boolean success,
            String message)
            throws IOException {

        prepareJson(response);

        response.getWriter().write(
                "{"
                        + "\"success\":"
                        + success
                        + ","
                        + "\"message\":\""
                        + escapeJson(message)
                        + "\""
                        + "}"
        );
    }

    private void prepareJson(
            HttpServletResponse response) {

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setContentType(
                "application/json;charset=UTF-8"
        );
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

    private record SessionUser(
            long id,
            String role) {
    }
}