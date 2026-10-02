package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.CustomField;
import vn.edu.ictu.qlkh.service.CustomFieldService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/custom-fields/*")
public class CustomFieldServlet extends HttpServlet {

    private CustomFieldService customFieldService;

    @Override
    public void init() throws ServletException {
        customFieldService = new CustomFieldService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý trường tùy chỉnh.");
            return;
        }

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || "/".equals(pathInfo)) {

                String entityType =
                        request.getParameter("entityType");

                if (entityType == null || entityType.isBlank()) {
                    sendFields(
                            response,
                            customFieldService.getAllFields());
                } else {
                    sendFields(
                            response,
                            customFieldService
                                    .getFieldsByEntityType(entityType));
                }

                return;
            }

            long id = parseId(pathInfo);

            CustomField field =
                    customFieldService.getFieldById(id);

            if (field == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy trường tùy chỉnh.");
                return;
            }

            sendField(response, field);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID trường tùy chỉnh không hợp lệ.");

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý trường tùy chỉnh.");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {
            CustomField field = readField(request);

            customFieldService.addField(field);

            response.setStatus(
                    HttpServletResponse.SC_CREATED);

            sendField(response, field);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu.");
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý trường tùy chỉnh.");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {
            long id = parseId(request.getPathInfo());

            CustomField field = readField(request);
            field.setId(id);

            customFieldService.updateField(field);

            sendField(response, field);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID trường tùy chỉnh không hợp lệ.");

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu.");
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý trường tùy chỉnh.");
            return;
        }

        try {
            long id = parseId(request.getPathInfo());

            customFieldService.deleteField(id);

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID trường tùy chỉnh không hợp lệ.");

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage());

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu.");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        Object roleObject =
                session.getAttribute("userRole");

        if (roleObject == null) {
            return false;
        }

        return "ADMIN".equalsIgnoreCase(
                roleObject.toString());
    }

    private CustomField readField(
            HttpServletRequest request) {

        CustomField field = new CustomField();

        field.setEntityType(
                request.getParameter("entityType"));

        field.setFieldKey(
                request.getParameter("fieldKey"));

        field.setFieldName(
                request.getParameter("fieldName"));

        field.setFieldType(
                request.getParameter("fieldType"));

        field.setRequired(
                parseBoolean(
                        request.getParameter("required"),
                        false));

        field.setStatus(
                parseBoolean(
                        request.getParameter("status"),
                        true));

        field.setDisplayOrder(
                parseInt(
                        request.getParameter("displayOrder"),
                        0));

        field.setConfig(
                request.getParameter("config"));

        return field;
    }

    private boolean parseBoolean(
            String value,
            boolean defaultValue) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        if ("true".equalsIgnoreCase(value)
                || "1".equals(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)
                || "0".equals(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Giá trị boolean không hợp lệ.");
    }

    private int parseInt(
            String value,
            int defaultValue) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Thứ tự hiển thị phải là số nguyên.");
        }
    }

    private long parseId(String pathInfo) {

        if (pathInfo == null
                || "/".equals(pathInfo)
                || pathInfo.length() <= 1) {

            throw new NumberFormatException();
        }

        String idText = pathInfo.substring(1);

        if (idText.contains("/")) {
            idText = idText.substring(
                    0,
                    idText.indexOf("/"));
        }

        return Long.parseLong(idText);
    }

    private void sendField(
            HttpServletResponse response,
            CustomField field)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8");

        String json =
                "{"
                + "\"id\":" + field.getId() + ","
                + "\"entityType\":\""
                + escapeJson(field.getEntityType()) + "\","
                + "\"fieldKey\":\""
                + escapeJson(field.getFieldKey()) + "\","
                + "\"fieldName\":\""
                + escapeJson(field.getFieldName()) + "\","
                + "\"fieldType\":\""
                + escapeJson(field.getFieldType()) + "\","
                + "\"required\":"
                + field.isRequired() + ","
                + "\"status\":"
                + field.isStatus() + ","
                + "\"displayOrder\":"
                + field.getDisplayOrder() + ","
                + "\"config\":"
                + nullableJson(field.getConfig())
                + "}";

        response.getWriter().write(json);
    }

    private void sendFields(
            HttpServletResponse response,
            List<CustomField> fields)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8");

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < fields.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            CustomField field = fields.get(i);

            json.append("{")
                    .append("\"id\":")
                    .append(field.getId())
                    .append(",")

                    .append("\"entityType\":\"")
                    .append(escapeJson(
                            field.getEntityType()))
                    .append("\",")

                    .append("\"fieldKey\":\"")
                    .append(escapeJson(
                            field.getFieldKey()))
                    .append("\",")

                    .append("\"fieldName\":\"")
                    .append(escapeJson(
                            field.getFieldName()))
                    .append("\",")

                    .append("\"fieldType\":\"")
                    .append(escapeJson(
                            field.getFieldType()))
                    .append("\",")

                    .append("\"required\":")
                    .append(field.isRequired())
                    .append(",")

                    .append("\"status\":")
                    .append(field.isStatus())
                    .append(",")

                    .append("\"displayOrder\":")
                    .append(field.getDisplayOrder())
                    .append(",")

                    .append("\"config\":")
                    .append(nullableJson(
                            field.getConfig()))

                    .append("}");
        }

        json.append("]");

        response.getWriter().write(
                json.toString());
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.setContentType(
                "application/json;charset=UTF-8");

        response.getWriter().write(
                "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(message)
                + "\""
                + "}");
    }

    private String nullableJson(String value) {

        if (value == null) {
            return "null";
        }

        return "\""
                + escapeJson(value)
                + "\"";
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
