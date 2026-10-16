package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ictu.qlkh.dao.LeadDAO;
import vn.edu.ictu.qlkh.service.LeadService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/v1/leads")
public class LeadServlet extends HttpServlet {

    private final LeadService leadService = new LeadService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        Object userIdValue = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("userId");

        Object userRoleValue = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("userRole");

        if (!(userIdValue instanceof Number)) {
            sendError(response, 401, "Unauthenticated");
            return;
        }

        if (!(userRoleValue instanceof String)) {
            sendError(response, 403, "Forbidden");
            return;
        }

        long userId = ((Number) userIdValue).longValue();
        String role = (String) userRoleValue;

        try {
            int page = parseInteger(request, "page", 1);
            int size = parseInteger(request, "size", 10);

            if (page < 1 || size < 1 || size > 100) {
                sendError(response, 400, "Invalid pagination parameters");
                return;
            }

            Long assignedTo = parseOptionalLong(
                    request.getParameter("assignedTo"));

            if (assignedTo != null && assignedTo <= 0) {
                sendError(response, 400, "Invalid assignedTo");
                return;
            }

            LeadDAO.SearchPage result = leadService.search(
                    userId,
                    role,
                    request.getParameter("keyword"),
                    request.getParameter("status"),
                    request.getParameter("source"),
                    assignedTo,
                    page,
                    size
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(toJson(result));

        } catch (IllegalArgumentException e) {
            sendError(response, 400, "Invalid query parameters");

        } catch (SecurityException e) {
            sendError(response, 403, "Forbidden");

        } catch (SQLException e) {
            getServletContext().log("Failed to list leads", e);
            sendError(response, 500, "Internal server error");
        }
    }

    private int parseInteger(
            HttpServletRequest request,
            String name,
            int defaultValue
    ) {
        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }

    private Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid assignedTo");
        }
    }

    private String toJson(LeadDAO.SearchPage result) {
        StringBuilder json = new StringBuilder();

        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < result.items().size(); i++) {
            if (i > 0) {
                json.append(',');
            }

            LeadDAO.LeadItem lead = result.items().get(i);

            json.append('{')
                    .append("\"id\":").append(lead.id()).append(',')
                    .append("\"fullName\":").append(quote(lead.fullName())).append(',')
                    .append("\"email\":").append(quote(lead.email())).append(',')
                    .append("\"phone\":").append(quote(lead.phone())).append(',')
                    .append("\"company\":").append(quote(lead.company())).append(',')
                    .append("\"interest\":").append(quote(lead.interest())).append(',')
                    .append("\"status\":").append(quote(lead.status())).append(',')
                    .append("\"source\":").append(quote(lead.source())).append(',')
                    .append("\"sourceForm\":").append(quote(lead.sourceForm())).append(',')
                    .append("\"assignedTo\":")
                    .append(lead.assignedTo() == null ? "null" : lead.assignedTo())
                    .append(',')
                    .append("\"score\":")
                    .append(lead.score() == null ? "null" : lead.score())
                    .append(',')
                    .append("\"createdAt\":")
                    .append(quote(lead.createdAt() == null ? null : lead.createdAt().toString()))
                    .append(',')
                    .append("\"updatedAt\":")
                    .append(quote(lead.updatedAt() == null ? null : lead.updatedAt().toString()))
                    .append('}');
        }

        json.append("],")
                .append("\"pagination\":{")
                .append("\"page\":").append(result.page()).append(',')
                .append("\"size\":").append(result.size()).append(',')
                .append("\"totalItems\":").append(result.totalItems()).append(',')
                .append("\"totalPages\":")
                .append(result.totalItems() == 0 ? 0 :
                        (result.totalItems() + result.size() - 1) / result.size())
                .append("}}");

        return json.toString();
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }

        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replaceAll("[\\x00-\\x1F]", " ")
                + "\"";
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.getWriter().write(
                "{\"success\":false,\"message\":" + quote(message) + "}");
    }
}