package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.WinLossReason;
import vn.edu.ictu.qlkh.service.WinLossReasonService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@WebServlet(
        urlPatterns = {
                "/api/v1/win-loss-reasons/*",
                "/win-loss-reasons/*"
        }
)
public class WinLossReasonServlet extends HttpServlet {

    private WinLossReasonService service;

    @Override
    public void init() throws ServletException {
        service = new WinLossReasonService();
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

    private void writeJson(
            HttpServletResponse response,
            int status,
            String body
    ) throws IOException {

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                "application/json;charset=UTF-8"
        );

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
            WinLossReason reason
    ) {

        return "{"
                + "\"id\":" + reason.getId() + ","
                + "\"name\":\"" + escape(reason.getName()) + "\","
                + "\"code\":\"" + escape(reason.getCode()) + "\","
                + "\"type\":\"" + escape(reason.getType()) + "\","
                + "\"status\":\"" + escape(reason.getStatus()) + "\","
                + "\"description\":"
                + (
                    reason.getDescription() == null
                            ? "null"
                            : "\"" + escape(
                                    reason.getDescription()
                            ) + "\""
                )
                + "}";
    }

    private String listToJson(
            List<WinLossReason> reasons
    ) {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < reasons.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    toJson(
                            reasons.get(i)
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

            if (id != null) {

                WinLossReason reason =
                        service.getReasonById(id);

                if (reason == null) {

                    writeJson(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "{\"success\":false,"
                                    + "\"message\":\"Không tìm thấy lý do.\"}"
                    );

                    return;
                }

                writeJson(
                        response,
                        HttpServletResponse.SC_OK,
                        toJson(reason)
                );

                return;
            }

            List<WinLossReason> reasons =
                    service.getAllReasons();

            String type =
                    request.getParameter("type");

            if (
                    type != null
                    && !type.isBlank()
            ) {

                String normalizedType =
                        type.trim().toUpperCase();

                if (
                        !"WON".equals(normalizedType)
                        && !"LOST".equals(normalizedType)
                ) {

                    writeJson(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "{\"success\":false,"
                                    + "\"message\":\"Loại lý do phải là WON hoặc LOST.\"}"
                    );

                    return;
                }

                List<WinLossReason> filtered =
                        new ArrayList<>();

                for (WinLossReason reason : reasons) {

                    if (
                            normalizedType.equalsIgnoreCase(
                                    reason.getType()
                            )
                    ) {
                        filtered.add(reason);
                    }
                }

                reasons = filtered;
            }

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    listToJson(reasons)
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
                            + "\"message\":\"Không thể tải danh sách lý do.\"}"
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

            WinLossReason reason =
                    new WinLossReason();

            reason.setName(
                    parameter(request, "name")
            );

            reason.setCode(
                    parameter(request, "code")
            );

            reason.setType(
                    parameter(request, "type")
            );

            reason.setStatus(
                    parameter(request, "status")
            );

            reason.setDescription(
                    parameter(
                            request,
                            "description"
                    )
            );

            service.addReason(reason);

            writeJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    toJson(reason)
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
                            + "\"message\":\"Không thể thêm lý do.\"}"
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
                            + "\"message\":\"ID lý do không hợp lệ.\"}"
            );

            return;
        }

        try {

            Map<String, String> form =
                    readFormBody(request);

            WinLossReason reason =
                    service.getReasonById(id);

            if (reason == null) {

                writeJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"success\":false,"
                                + "\"message\":\"Không tìm thấy lý do.\"}"
                );

                return;
            }

            reason.setName(
                    parameter(request, form, "name")
            );

            reason.setCode(
                    parameter(request, form, "code")
            );

            reason.setType(
                    parameter(request, form, "type")
            );

            reason.setStatus(
                    parameter(request, form, "status")
            );

            reason.setDescription(
                    parameter(
                            request,
                            form,
                            "description"
                    )
            );

            service.updateReason(reason);

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    toJson(reason)
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
                            + "\"message\":\"Không thể cập nhật lý do.\"}"
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
                            + "\"message\":\"ID lý do không hợp lệ.\"}"
            );

            return;
        }

        try {

            Map<String, String> form =
                    readFormBody(request);

            WinLossReason reason =
                    service.getReasonById(id);

            if (reason == null) {

                writeJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "{\"success\":false,"
                                + "\"message\":\"Không tìm thấy lý do.\"}"
                );

                return;
            }

            service.deleteReason(id);

            writeJson(
                    response,
                    HttpServletResponse.SC_OK,
                    "{\"success\":true,"
                            + "\"message\":\"Đã xóa lý do.\"}"
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
                            + "\"message\":\"Không thể xóa lý do.\"}"
            );
        }
    }
}