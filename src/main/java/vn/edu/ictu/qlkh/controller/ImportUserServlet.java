package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import vn.edu.ictu.qlkh.model.ImportUserResult;
import vn.edu.ictu.qlkh.service.ImportUserService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@WebServlet("/users/import")
@MultipartConfig
public class ImportUserServlet extends HttpServlet {

    private ImportUserService importUserService;

    @Override
    public void init() {
        importUserService = new ImportUserService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "application/json;charset=UTF-8"
        );

        /*
         * S2-01:
         * Chuc nang Import nguoi dung chi danh cho ADMIN.
         *
         * AuthorizationFilter cung bao ve /users/*.
         * Kiem tra lai tai API de tranh goi truc tiep
         * khi cau hinh filter thay doi.
         */
        HttpSession session =
                request.getSession(false);

        Object roleObject =
                session == null
                        ? null
                        : session.getAttribute("userRole");

        String role =
                roleObject == null
                        ? null
                        : roleObject.toString()
                                .trim()
                                .toUpperCase(Locale.ROOT);

        if (!"ADMIN".equals(role)) {
            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            writeMessage(
                    response,
                    false,
                    "Bạn không có quyền sử dụng chức năng Import người dùng."
            );

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
                    "Vui lòng chọn tệp Excel."
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
                    "Tệp không hợp lệ. Chỉ hỗ trợ .xlsx hoặc .xls."
            );

            return;
        }

        try (InputStream inputStream =
                     filePart.getInputStream()) {

            ImportUserResult result =
                    importUserService.importUsers(
                            inputStream
                    );

            writeImportResult(
                    response,
                    result
            );
        }
    }

    private boolean isExcelFile(String fileName) {

        if (fileName == null
                || fileName.isBlank()) {
            return false;
        }

        String normalized =
                fileName.toLowerCase(Locale.ROOT);

        return normalized.endsWith(".xlsx")
                || normalized.endsWith(".xls");
    }

    private void writeImportResult(
            HttpServletResponse response,
            ImportUserResult result)
            throws IOException {

        StringBuilder json =
                new StringBuilder();

        json.append("{")
                .append("\"success\":true,")
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

            ImportUserResult.RowError error =
                    result.getErrors().get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"row\":")
                    .append(error.getRow())
                    .append(",")
                    .append("\"email\":\"")
                    .append(escapeJson(
                            error.getEmail()
                    ))
                    .append("\",")
                    .append("\"message\":\"")
                    .append(escapeJson(
                            error.getMessage()
                    ))
                    .append("\"")
                    .append("}");
        }

        json.append("]}");

        response.getWriter().write(
                json.toString()
        );
    }

    private void writeMessage(
            HttpServletResponse response,
            boolean success,
            String message)
            throws IOException {

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

    private String escapeJson(String value) {

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
}