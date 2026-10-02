package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import vn.edu.ictu.qlkh.dao.AuditLogDAO;
import vn.edu.ictu.qlkh.model.AuditLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(
        name = "AuditLogServlet",
        value = "/api/audit-logs"
)
public class AuditLogServlet
        extends HttpServlet {

    private final AuditLogDAO auditLogDAO =
            new AuditLogDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "application/json;charset=UTF-8"
        );
        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        String keyword =
                request.getParameter("keyword");

        String username =
                request.getParameter("username");

        String action =
                request.getParameter("action");

        String targetObject =
                request.getParameter(
                        "targetObject"
                );

        String startDate =
                request.getParameter(
                        "startDate"
                );

        String endDate =
                request.getParameter(
                        "endDate"
                );

        try {

            validateDateRange(
                    startDate,
                    endDate
            );

            List<AuditLog> logs =
                    auditLogDAO.getAuditLogs(
                            keyword,
                            username,
                            action,
                            targetObject,
                            startDate,
                            endDate
                    );

            writeLogs(
                    response,
                    logs
            );

        } catch (DateTimeException e) {

            response.setStatus(
                    HttpServletResponse
                            .SC_BAD_REQUEST
            );

            writeError(
                    response,
                    "Ngày lọc không hợp lệ. "
                            + "Định dạng yêu cầu: yyyy-MM-dd."
            );

        } catch (IllegalArgumentException e) {

            response.setStatus(
                    HttpServletResponse
                            .SC_BAD_REQUEST
            );

            writeError(
                    response,
                    e.getMessage()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse
                            .SC_INTERNAL_SERVER_ERROR
            );

            writeError(
                    response,
                    "Không thể tải nhật ký thay đổi."
            );
        }
    }

    private void validateDateRange(
            String startDate,
            String endDate) {

        LocalDate start = null;
        LocalDate end = null;

        if (startDate != null
                && !startDate.isBlank()) {

            start =
                    LocalDate.parse(
                            startDate.trim()
                    );
        }

        if (endDate != null
                && !endDate.isBlank()) {

            end =
                    LocalDate.parse(
                            endDate.trim()
                    );
        }

        if (start != null
                && end != null
                && start.isAfter(end)) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được sau ngày kết thúc."
            );
        }
    }

    private void writeLogs(
            HttpServletResponse response,
            List<AuditLog> logs)
            throws IOException {

        PrintWriter out =
                response.getWriter();

        out.print("[");

        for (int i = 0;
             i < logs.size();
             i++) {

            if (i > 0) {
                out.print(",");
            }

            AuditLog log =
                    logs.get(i);

            out.print("{");

            out.print("\"id\":");
            out.print(log.getId());

            out.print(",\"executor\":");
            writeJsonString(
                    out,
                    log.getUsername()
            );

            out.print(",\"executorRole\":null");

            out.print(",\"action\":");
            writeJsonString(
                    out,
                    log.getActionType()
            );

            out.print(",\"targetObject\":");
            writeJsonString(
                    out,
                    log.getTargetObject()
            );

            out.print(",\"oldValue\":");
            writeJsonString(
                    out,
                    log.getOldValue()
            );

            out.print(",\"newValue\":");
            writeJsonString(
                    out,
                    log.getNewValue()
            );

            out.print(",\"details\":");
            writeJsonString(
                    out,
                    log.getDetails()
            );

            out.print(",\"timestamp\":");

            writeJsonString(
                    out,
                    log.getActionTime() == null
                            ? null
                            : log.getActionTime()
                                 .toLocalDateTime()
                                 .toString()
            );

            out.print("}");
        }

        out.print("]");
    }

    private void writeError(
            HttpServletResponse response,
            String message)
            throws IOException {

        PrintWriter out =
                response.getWriter();

        out.print("{\"error\":");

        writeJsonString(
                out,
                message
        );

        out.print("}");
    }

    private void writeJsonString(
            PrintWriter out,
            String value) {

        if (value == null) {
            out.print("null");
            return;
        }

        out.print("\"");
        out.print(
                escapeJson(value)
        );
        out.print("\"");
    }

    private String escapeJson(
            String value) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < value.length();
             i++) {

            char c =
                    value.charAt(i);

            switch (c) {

                case '"' ->
                        result.append("\\\"");

                case '\\' ->
                        result.append("\\\\");

                case '\b' ->
                        result.append("\\b");

                case '\f' ->
                        result.append("\\f");

                case '\n' ->
                        result.append("\\n");

                case '\r' ->
                        result.append("\\r");

                case '\t' ->
                        result.append("\\t");

                default -> {

                    if (c < 0x20) {

                        result.append(
                                String.format(
                                        "\\u%04x",
                                        (int) c
                                )
                        );

                    } else {

                        result.append(c);
                    }
                }
            }
        }

        return result.toString();
    }
}