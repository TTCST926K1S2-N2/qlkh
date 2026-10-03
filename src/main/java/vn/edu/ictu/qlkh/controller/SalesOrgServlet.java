package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.dto.SalesOrgDTO;
import vn.edu.ictu.qlkh.model.Region;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.service.SalesOrgService;
import vn.edu.ictu.qlkh.service.SalesOrgServiceImpl;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/v1/sales-orgs/*")
public class SalesOrgServlet extends HttpServlet {

    private final SalesOrgService salesOrgService =
            new SalesOrgServiceImpl();


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareJson(response);

        if (!canAccess(request)) {

            writeError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền xem cơ cấu kinh doanh."
            );

            return;
        }

        try {

            String path =
                    normalizePath(
                            request.getPathInfo()
                    );

            if (path.isEmpty()) {

                writeJson(
                        response,
                        HttpServletResponse.SC_OK,
                        toJson(
                                salesOrgService
                                        .getAllSalesOrgs()
                        )
                );

                return;
            }


            if ("tree".equalsIgnoreCase(path)) {

                writeJson(
                        response,
                        HttpServletResponse.SC_OK,
                        toJson(
                                salesOrgService
                                        .getSalesOrgTree()
                        )
                );

                return;
            }


            if ("regions".equalsIgnoreCase(path)) {

                writeJson(
                        response,
                        HttpServletResponse.SC_OK,
                        regionsToJson(
                                salesOrgService
                                        .getRegions()
                        )
                );

                return;
            }


            if ("managers".equalsIgnoreCase(path)) {

                writeJson(
                        response,
                        HttpServletResponse.SC_OK,
                        managersToJson(
                                salesOrgService
                                        .getManagers()
                        )
                );

                return;
            }


            long id =
                    parseId(path);

            SalesOrgDTO dto =
                    salesOrgService
                            .getSalesOrgById(id);

            if (dto == null) {

                writeError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy nhóm kinh doanh."
                );

                return;
            }

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    toJson(dto)
            );

        } catch (NumberFormatException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID nhóm không hợp lệ."
            );

        } catch (SQLException e) {

            e.printStackTrace();

            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể truy cập dữ liệu cơ cấu kinh doanh."
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareJson(response);

        if (!canManage(request)) {

            writeError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền khai báo cơ cấu kinh doanh."
            );

            return;
        }

        try {

            String path =
                    normalizePath(
                            request.getPathInfo()
                    );

            if (!path.isEmpty()) {

                writeError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "API không tồn tại."
                );

                return;
            }

            String body =
                    readBody(request);

            SalesOrgDTO input =
                    parseSalesOrg(body);

            SalesOrgDTO created =
                    salesOrgService
                            .createSalesOrg(input);

            writeJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    toJson(created)
            );

        } catch (IllegalArgumentException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể tạo nhóm kinh doanh."
            );
        }
    }


    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareJson(response);

        if (!canManage(request)) {

            writeError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền sửa cơ cấu kinh doanh."
            );

            return;
        }

        try {

            String path =
                    normalizePath(
                            request.getPathInfo()
                    );

            long id =
                    parseId(path);

            String body =
                    readBody(request);

            SalesOrgDTO input =
                    parseSalesOrg(body);

            SalesOrgDTO updated =
                    salesOrgService
                            .updateSalesOrg(
                                    id,
                                    input
                            );

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    toJson(updated)
            );

        } catch (NumberFormatException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID nhóm không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể cập nhật nhóm kinh doanh."
            );
        }
    }


    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        prepareJson(response);

        if (!canManage(request)) {

            writeError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền ngừng nhóm kinh doanh."
            );

            return;
        }

        try {

            String path =
                    normalizePath(
                            request.getPathInfo()
                    );

            long id =
                    parseId(path);

            boolean success =
                    salesOrgService
                            .deactivateSalesOrg(id);

            if (!success) {

                writeError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy nhóm kinh doanh."
                );

                return;
            }

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (NumberFormatException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID nhóm không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể ngừng nhóm kinh doanh."
            );
        }
    }


    private SalesOrgDTO parseSalesOrg(
            String body
    ) {

        SalesOrgDTO dto =
                new SalesOrgDTO();

        dto.setOrgCode(
                extractString(
                        body,
                        "orgCode",
                        true
                )
        );

        dto.setOrgName(
                extractString(
                        body,
                        "orgName",
                        true
                )
        );

        dto.setParentId(
                extractLong(
                        body,
                        "parentId",
                        false
                )
        );

        dto.setLeaderId(
                extractLong(
                        body,
                        "leaderId",
                        true
                )
        );

        dto.setRegionId(
                extractLong(
                        body,
                        "regionId",
                        true
                )
        );

        dto.setStatus(
                extractString(
                        body,
                        "status",
                        false
                )
        );

        return dto;
    }


    private String extractString(
            String json,
            String key,
            boolean required
    ) {

        Pattern pattern =
                Pattern.compile(
                        "\"" + Pattern.quote(key)
                                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(json);

        if (!matcher.find()) {

            if (required) {
                throw new IllegalArgumentException(
                        "Thiếu trường " + key + "."
                );
            }

            return null;
        }

        return unescapeJson(
                matcher.group(1)
        );
    }


    private Long extractLong(
            String json,
            String key,
            boolean required
    ) {

        Pattern nullPattern =
                Pattern.compile(
                        "\"" + Pattern.quote(key)
                                + "\"\\s*:\\s*null",
                        Pattern.CASE_INSENSITIVE
                );

        if (
            nullPattern.matcher(json)
                    .find()
        ) {

            if (required) {
                throw new IllegalArgumentException(
                        "Trường " + key
                                + " không được null."
                );
            }

            return null;
        }


        Pattern numberPattern =
                Pattern.compile(
                        "\"" + Pattern.quote(key)
                                + "\"\\s*:\\s*(-?\\d+)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                numberPattern.matcher(json);

        if (!matcher.find()) {

            if (required) {
                throw new IllegalArgumentException(
                        "Thiếu trường " + key + "."
                );
            }

            return null;
        }

        try {

            return Long.parseLong(
                    matcher.group(1)
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Trường " + key
                            + " không hợp lệ."
            );
        }
    }


    private String readBody(
            HttpServletRequest request
    ) throws IOException {

        StringBuilder body =
                new StringBuilder();

        try (BufferedReader reader =
                     request.getReader()) {

            String line;

            while (
                (line = reader.readLine())
                != null
            ) {
                body.append(line);
            }
        }

        return body.toString();
    }


    private String normalizePath(
            String path
    ) {

        if (
            path == null
            || path.isBlank()
            || "/".equals(path)
        ) {
            return "";
        }

        String value =
                path.startsWith("/")
                        ? path.substring(1)
                        : path;

        if (value.endsWith("/")) {
            value =
                    value.substring(
                            0,
                            value.length() - 1
                    );
        }

        return value;
    }


    private long parseId(
            String value
    ) {

        if (
            value == null
            || value.isBlank()
            || value.contains("/")
        ) {
            throw new NumberFormatException();
        }

        return Long.parseLong(value);
    }


    private boolean canAccess(
            HttpServletRequest request
    ) {

        String role =
                getRole(request);

        return "MANAGER".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role);
    }


    private boolean canManage(
            HttpServletRequest request
    ) {

        String role =
                getRole(request);

        return "MANAGER".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role);
    }


    private String getRole(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object role =
                session.getAttribute(
                        "userRole"
                );

        if (role == null) {
            role =
                    session.getAttribute(
                            "role"
                    );
        }

        return role == null
                ? null
                : role.toString();
    }


    private void prepareJson(
            HttpServletResponse response
    ) {

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );
    }


    private void writeError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        writeJson(
                response,
                status,
                "{"
                        + "\"success\":false,"
                        + "\"message\":"
                        + jsonString(message)
                        + "}"
        );
    }


    private void writeJson(
            HttpServletResponse response,
            int status,
            String json
    ) throws IOException {

        response.setStatus(status);

        PrintWriter writer =
                response.getWriter();

        writer.print(json);
        writer.flush();
    }


    private String toJson(
            Object value
    ) {

        if (value == null) {
            return "null";
        }

        if (value instanceof List<?> list) {

            StringBuilder json =
                    new StringBuilder("[");

            for (
                int i = 0;
                i < list.size();
                i++
            ) {

                json.append(
                        toJson(
                                list.get(i)
                        )
                );

                if (
                    i < list.size() - 1
                ) {
                    json.append(",");
                }
            }

            json.append("]");

            return json.toString();
        }


        if (value instanceof SalesOrgDTO dto) {

            return "{"
                    + "\"id\":"
                    + dto.getId()
                    + ","
                    + "\"orgCode\":"
                    + jsonString(
                            dto.getOrgCode()
                    )
                    + ","
                    + "\"orgName\":"
                    + jsonString(
                            dto.getOrgName()
                    )
                    + ","
                    + "\"parentId\":"
                    + jsonNumber(
                            dto.getParentId()
                    )
                    + ","
                    + "\"leaderId\":"
                    + jsonNumber(
                            dto.getLeaderId()
                    )
                    + ","
                    + "\"leaderName\":"
                    + jsonString(
                            dto.getLeaderName()
                    )
                    + ","
                    + "\"regionId\":"
                    + jsonNumber(
                            dto.getRegionId()
                    )
                    + ","
                    + "\"regionName\":"
                    + jsonString(
                            dto.getRegionName()
                    )
                    + ","
                    + "\"status\":"
                    + jsonString(
                            dto.getStatus()
                    )
                    + ","
                    + "\"children\":"
                    + toJson(
                            dto.getChildren()
                    )
                    + "}";
        }

        return jsonString(
                value.toString()
        );
    }


    private String regionsToJson(
            List<Region> regions
    ) {

        StringBuilder json =
                new StringBuilder("[");

        for (
            int i = 0;
            i < regions.size();
            i++
        ) {

            Region region =
                    regions.get(i);

            json.append("{")
                    .append("\"id\":")
                    .append(region.getId())
                    .append(",")
                    .append("\"code\":")
                    .append(
                            jsonString(
                                    region.getCode()
                            )
                    )
                    .append(",")
                    .append("\"name\":")
                    .append(
                            jsonString(
                                    region.getName()
                            )
                    )
                    .append(",")
                    .append("\"status\":")
                    .append(
                            jsonString(
                                    region.getStatus()
                            )
                    )
                    .append("}");

            if (
                i < regions.size() - 1
            ) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }


    private String managersToJson(
            List<User> managers
    ) {

        StringBuilder json =
                new StringBuilder("[");

        for (
            int i = 0;
            i < managers.size();
            i++
        ) {

            User user =
                    managers.get(i);

            json.append("{")
                    .append("\"id\":")
                    .append(user.getId())
                    .append(",")
                    .append("\"fullName\":")
                    .append(
                            jsonString(
                                    user.getFullName()
                            )
                    )
                    .append(",")
                    .append("\"email\":")
                    .append(
                            jsonString(
                                    user.getEmail()
                            )
                    )
                    .append(",")
                    .append("\"role\":")
                    .append(
                            jsonString(
                                    user.getRole()
                            )
                    )
                    .append(",")
                    .append("\"status\":")
                    .append(
                            jsonString(
                                    user.getStatus()
                            )
                    )
                    .append("}");

            if (
                i < managers.size() - 1
            ) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }


    private String jsonNumber(
            Long value
    ) {

        return value == null
                ? "null"
                : value.toString();
    }


    private String jsonString(
            String value
    ) {

        if (value == null) {
            return "null";
        }

        return "\""
                + escapeJson(value)
                + "\"";
    }


    private String escapeJson(
            String value
    ) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    private String unescapeJson(
            String value
    ) {

        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}