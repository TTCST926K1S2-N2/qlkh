package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Competitor;
import vn.edu.ictu.qlkh.service.CompetitorService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@WebServlet(
        urlPatterns = {
                "/api/v1/competitors/*",
                "/competitors/*"
        }
)
public class CompetitorServlet extends HttpServlet {

    private CompetitorService service;

    @Override
    public void init() throws ServletException {
        service = new CompetitorService();
    }

    private boolean canManage(
            HttpServletRequest request
    ) {

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

        String role =
                roleObject.toString();

        return "ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role);
    }

    private Long extractId(
            HttpServletRequest request
    ) {

        try {

            String pathInfo =
                    request.getPathInfo();

            if (
                    pathInfo == null
                    || pathInfo.isBlank()
                    || "/".equals(pathInfo)
            ) {
                return null;
            }

            String value =
                    pathInfo.startsWith("/")
                            ? pathInfo.substring(1)
                            : pathInfo;

            if (value.contains("/")) {
                value =
                        value.substring(
                                0,
                                value.indexOf("/")
                        );
            }

            return Long.parseLong(value);

        } catch (Exception ex) {

            return null;
        }
    }

    private void prepareResponse(
            HttpServletResponse response
    ) {

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                "application/json;charset=UTF-8"
        );
    }

    private void writeJson(
            HttpServletResponse response,
            int status,
            String body
    ) throws IOException {

        prepareResponse(response);

        response.setStatus(status);

        response.getWriter().write(body);
    }

    private String escape(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String toJson(
            Competitor competitor
    ) {

        return "{"
                + "\"id\":" + competitor.getId() + ","
                + "\"name\":\"" + escape(competitor.getName()) + "\","
                + "\"code\":\"" + escape(competitor.getCode()) + "\","
                + "\"status\":\"" + escape(competitor.getStatus()) + "\","
                + "\"description\":"
                + (
                    competitor.getDescription() == null
                            ? "null"
                            : "\"" + escape(
                                    competitor.getDescription()
                            ) + "\""
                )
                + "}";
    }

    private String listToJson(
            List<Competitor> competitors
    ) {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < competitors.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    toJson(
                            competitors.get(i)
                    )
            );
        }

        json.append("]");

        return json.toString();
    }

    private Map<String, String> readFormBody(
            HttpServletRequest request
    ) throws IOException {

        Map<String, String> values =
                new HashMap<>();

        String body =
                request.getReader()
                        .lines()
                        .reduce(
                                "",
                                (left, right) ->
                                        left + right
                        );

        if (
                body == null
                || body.isBlank()
        ) {
            return values;
        }

        String[] pairs =
                body.split("&");

        for (String pair : pairs) {

            if (
                    pair == null
                    || pair.isBlank()
            ) {
                continue;
            }

            String[] parts =
                    pair.split(
                            "=",
                            2
                    );

            String key =
                    URLDecoder.decode(
                            parts[0],
                            StandardCharsets.UTF_8
                    );

            String value =
                    parts.length > 1
                            ? URLDecoder.decode(
                                    parts[1],
                                    StandardCharsets.UTF_8
                            )
                            : "";

            values.put(
                    key,
                    value
            );
        }

        return values;
    }

    private String parameter(
            HttpServletRequest request,
            String name
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        return request.getParameter(name);
    }
    private String parameter(
            HttpServletRequest request,
            Map<String, String> form,
            String name
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        String value =
                request.getParameter(name);

        if (value != null) {
            return value;
        }

        return form.get(name);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!canManage(request)) {

            writeJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "{\"success\":false,"
                            + "\"message\":\"Bạn không có quyền thực hiện thao tác này.\"}"
            );

            return;
        }

        try {

            Long id =
                    extractId(request);

            if (id == null) {

                writeJson(
                        response,
                        HttpServletResponse.SC_OK,
                        listToJson(
                                service.getAllCompetitors()
                        )
                );

                return;
            }

            Competitor competitor =
                    service.getCompetitorById(id);

            if (competitor == null) {

                writeJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"success\":false,"
                                + "\"message\":\"Không tìm thấy đối thủ.\"}"
                );

                return;
            }

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    toJson(competitor)
            );

        } catch (IllegalArgumentException ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,"
                            + "\"message\":\""
                            + escape(ex.getMessage())
                            + "\"}"
            );

        } catch (Exception ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"success\":false,"
                            + "\"message\":\"Không thể tải danh sách đối thủ.\"}"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!canManage(request)) {

            writeJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "{\"success\":false,"
                            + "\"message\":\"Bạn không có quyền thực hiện thao tác này.\"}"
            );

            return;
        }

        try {

            Competitor competitor =
                    new Competitor();

            competitor.setName(
                    parameter(request, "name")
            );

            competitor.setCode(
                    parameter(request, "code")
            );

            competitor.setStatus(
                    parameter(request, "status")
            );

            competitor.setDescription(
                    parameter(
                            request,
                            "description"
                    )
            );

            service.addCompetitor(
                    competitor
            );

            writeJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    toJson(competitor)
            );

        } catch (IllegalArgumentException ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,"
                            + "\"message\":\""
                            + escape(ex.getMessage())
                            + "\"}"
            );

        } catch (Exception ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"success\":false,"
                            + "\"message\":\"Không thể thêm đối thủ.\"}"
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!canManage(request)) {

            writeJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "{\"success\":false,"
                            + "\"message\":\"Bạn không có quyền thực hiện thao tác này.\"}"
            );

            return;
        }

        Long id =
                extractId(request);

        if (id == null) {

            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,"
                            + "\"message\":\"ID đối thủ không hợp lệ.\"}"
            );

            return;
        }

        try {

            Map<String, String> form =
                    readFormBody(request);

            Competitor competitor =
                    service.getCompetitorById(id);

            if (competitor == null) {

                writeJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"success\":false,"
                                + "\"message\":\"Không tìm thấy đối thủ.\"}"
                );

                return;
            }

            competitor.setName(
                    parameter(request, form, "name")
            );

            competitor.setCode(
                    parameter(request, form, "code")
            );

            competitor.setStatus(
                    parameter(request, form, "status")
            );

            competitor.setDescription(
                    parameter(
                            request,
                            form,
                            "description"
                    )
            );

            service.updateCompetitor(
                    competitor
            );

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    toJson(competitor)
            );

        } catch (IllegalArgumentException ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,"
                            + "\"message\":\""
                            + escape(ex.getMessage())
                            + "\"}"
            );

        } catch (Exception ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"success\":false,"
                            + "\"message\":\"Không thể cập nhật đối thủ.\"}"
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!canManage(request)) {

            writeJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "{\"success\":false,"
                            + "\"message\":\"Bạn không có quyền thực hiện thao tác này.\"}"
            );

            return;
        }

        Long id =
                extractId(request);

        if (id == null) {

            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,"
                            + "\"message\":\"ID đối thủ không hợp lệ.\"}"
            );

            return;
        }

        try {

            Map<String, String> form =
                    readFormBody(request);

            Competitor competitor =
                    service.getCompetitorById(id);

            if (competitor == null) {

                writeJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"success\":false,"
                                + "\"message\":\"Không tìm thấy đối thủ.\"}"
                );

                return;
            }

            service.deleteCompetitor(id);

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    "{\"success\":true,"
                            + "\"message\":\"Đã xóa đối thủ.\"}"
            );

        } catch (IllegalArgumentException ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "{\"success\":false,"
                            + "\"message\":\""
                            + escape(ex.getMessage())
                            + "\"}"
            );

        } catch (Exception ex) {

            writeJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"success\":false,"
                            + "\"message\":\"Không thể xóa đối thủ.\"}"
            );
        }
    }
}