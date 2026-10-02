package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Category;
import vn.edu.ictu.qlkh.service.CategoryService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/categories/*")
public class CategoryServlet extends HttpServlet {

    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        categoryService = new CategoryService();
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
                    "Bạn không có quyền quản lý danh mục."
            );
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<Category> categories =
                        categoryService.getAllCategories();

                sendCategories(response, categories);
                return;
            }

            long id = parseId(pathInfo);

            Category category =
                    categoryService.getCategoryById(id);

            if (category == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy danh mục."
                );
                return;
            }

            sendCategory(response, category);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID danh mục không hợp lệ."
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
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
                    "Bạn không có quyền quản lý danh mục."
            );
            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {
            Category category = readCategory(request);

            categoryService.addCategory(category);

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            sendCategory(response, category);

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
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
                    "Bạn không có quyền quản lý danh mục."
            );
            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            long id = parseId(pathInfo);

            Category category = readCategory(request);
            category.setId(id);

            categoryService.updateCategory(category);

            sendCategory(response, category);

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID danh mục không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
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
                    "Bạn không có quyền quản lý danh mục."
            );
            return;
        }

        try {
            String pathInfo = request.getPathInfo();
            long id = parseId(pathInfo);

            categoryService.deleteCategory(id);

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID danh mục không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
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
                roleObject.toString()
        );
    }

    private Category readCategory(
            HttpServletRequest request) throws IOException {

        java.util.Map<String, String> bodyParams =
                new java.util.HashMap<>();

        String contentType = request.getContentType();

        if ("PUT".equalsIgnoreCase(request.getMethod())
                && contentType != null
                && contentType.toLowerCase()
                        .startsWith("application/x-www-form-urlencoded")) {

            StringBuilder body = new StringBuilder();
            String line;

            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            if (body.length() > 0) {
                for (String pair : body.toString().split("&")) {

                    if (pair.isEmpty()) {
                        continue;
                    }

                    String[] parts = pair.split("=", 2);

                    String key = java.net.URLDecoder.decode(
                            parts[0],
                            java.nio.charset.StandardCharsets.UTF_8
                    );

                    String value = parts.length > 1
                            ? java.net.URLDecoder.decode(
                                    parts[1],
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                            : "";

                    bodyParams.put(key, value);
                }
            }
        }

        String categoryType =
                getFormValue(request, bodyParams, "categoryType");

        String categoryCode =
                getFormValue(request, bodyParams, "categoryCode");

        String categoryName =
                getFormValue(request, bodyParams, "categoryName");

        String description =
                getFormValue(request, bodyParams, "description");

        String statusParameter =
                getFormValue(request, bodyParams, "status");

        boolean status = true;

        if (statusParameter != null
                && !statusParameter.isBlank()) {

            status = Boolean.parseBoolean(statusParameter);
        }

        Category category = new Category();

        category.setCategoryType(categoryType);
        category.setCategoryCode(categoryCode);
        category.setCategoryName(categoryName);
        category.setDescription(description);
        category.setStatus(status);

        return category;
    }

    private String getFormValue(
            HttpServletRequest request,
            java.util.Map<String, String> bodyParams,
            String name) {

        if (bodyParams.containsKey(name)) {
            return bodyParams.get(name);
        }

        return request.getParameter(name);
    }
    private long parseId(String pathInfo) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || pathInfo.length() <= 1) {

            throw new NumberFormatException();
        }

        String idText =
                pathInfo.substring(1);

        if (idText.contains("/")) {
            idText =
                    idText.substring(
                            0,
                            idText.indexOf("/")
                    );
        }

        return Long.parseLong(idText);
    }

    private void sendCategory(
            HttpServletResponse response,
            Category category)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        String json =
                "{"
                        + "\"id\":" + category.getId() + ","
                        + "\"categoryType\":\""
                        + escapeJson(category.getCategoryType())
                        + "\","
                        + "\"categoryCode\":\""
                        + escapeJson(category.getCategoryCode())
                        + "\","
                        + "\"categoryName\":\""
                        + escapeJson(category.getCategoryName())
                        + "\","
                        + "\"description\":"
                        + nullableJson(category.getDescription())
                        + ","
                        + "\"status\":"
                        + category.isStatus()
                        + "}";

        response.getWriter().write(json);
    }

    private void sendCategories(
            HttpServletResponse response,
            List<Category> categories)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < categories.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            Category category =
                    categories.get(i);

            json.append("{")
                    .append("\"id\":")
                    .append(category.getId())
                    .append(",")
                    .append("\"categoryType\":\"")
                    .append(
                            escapeJson(
                                    category.getCategoryType()
                            )
                    )
                    .append("\",")
                    .append("\"categoryCode\":\"")
                    .append(
                            escapeJson(
                                    category.getCategoryCode()
                            )
                    )
                    .append("\",")
                    .append("\"categoryName\":\"")
                    .append(
                            escapeJson(
                                    category.getCategoryName()
                            )
                    )
                    .append("\",")
                    .append("\"description\":")
                    .append(
                            nullableJson(
                                    category.getDescription()
                            )
                    )
                    .append(",")
                    .append("\"status\":")
                    .append(category.isStatus())
                    .append("}");
        }

        json.append("]");

        response.getWriter().write(
                json.toString()
        );
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                "{"
                        + "\"success\":false,"
                        + "\"message\":\""
                        + escapeJson(message)
                        + "\""
                        + "}"
        );
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