package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.model.AssignmentRule;
import vn.edu.ictu.qlkh.service.AssignmentRuleService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/assignment-rules")
public class AssignmentRuleController extends HttpServlet {
    private final AssignmentRuleService assignmentRuleService = new AssignmentRuleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        try {
            List<AssignmentRule> rules = assignmentRuleService.getAllRules();

            StringBuilder json = new StringBuilder();
            json.append("{\"status\":\"success\",\"total\":")
                .append(rules.size())
                .append(",\"rules\":[");

            for (int i = 0; i < rules.size(); i++) {
                AssignmentRule rule = rules.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":").append(rule.getId()).append(",")
                    .append("\"name\":\"").append(escapeJson(rule.getName())).append("\",")
                    .append("\"conditionType\":\"").append(escapeJson(rule.getConditionType())).append("\",")
                    .append("\"conditionValue\":\"").append(escapeJson(rule.getConditionValue())).append("\",")
                    .append("\"assignToUserId\":").append(rule.getAssignToUserId()).append(",")
                    .append("\"priority\":").append(rule.getPriority()).append(",")
                    .append("\"active\":").append(Boolean.TRUE.equals(rule.getActive())).append(",")
                    .append("\"assignmentMethod\":\"").append(escapeJson(rule.getAssignmentMethod())).append("\"")
                    .append("}");
            }

            json.append("]}");
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            getServletContext().log("Cannot load assignment rules", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(
                "{\"status\":\"error\",\"message\":\"Không thể tải danh sách quy tắc\"}"
            );
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }

        return result.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            String name = req.getParameter("name");
            String conditionType = req.getParameter("conditionType");
            String conditionValue = req.getParameter("conditionValue");
            String userIdText = req.getParameter("assignToUserId");
            String priorityText = req.getParameter("priority");
            String activeText = req.getParameter("active");
            String method = req.getParameter("assignmentMethod");

            if (name == null || name.trim().isEmpty()
                    || conditionValue == null || conditionValue.trim().isEmpty()
                    || conditionType == null
                    || !("SOURCE".equals(conditionType)
                         || "REGION".equals(conditionType)
                         || "INDUSTRY".equals(conditionType))) {

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"Thông tin quy tắc không hợp lệ\"}"
                );
                return;
            }

            long userId;
            int priority = 1;

            try {
                userId = Long.parseLong(userIdText);

                if (priorityText != null && !priorityText.trim().isEmpty()) {
                    priority = Integer.parseInt(priorityText);
                }

            } catch (NumberFormatException | NullPointerException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"ID nhân viên hoặc độ ưu tiên không hợp lệ\"}"
                );
                return;
            }

            if (userId <= 0 || priority <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"ID nhân viên và độ ưu tiên phải lớn hơn 0\"}"
                );
                return;
            }

            if (method == null || method.trim().isEmpty()) {
                method = "DIRECT";
            }

            if (!("DIRECT".equals(method) || "ROUND_ROBIN".equals(method))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"Phương thức phân bổ không hợp lệ\"}"
                );
                return;
            }

            AssignmentRule rule = new AssignmentRule();
            rule.setName(name.trim());
            rule.setConditionType(conditionType);
            rule.setConditionValue(conditionValue.trim());
            rule.setAssignToUserId(userId);
            rule.setPriority(priority);
            rule.setActive(activeText == null || Boolean.parseBoolean(activeText));
            rule.setAssignmentMethod(method);

            boolean created = assignmentRuleService.createRule(rule);

            if (created) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(
                    "{\"status\":\"success\",\"message\":\"Tạo quy tắc thành công\",\"id\":"
                    + rule.getId() + "}"
                );
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"Không thể tạo quy tắc\"}"
                );
            }

        } catch (IllegalStateException e) {
            getServletContext().log("Cannot create assignment rule", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(
                "{\"status\":\"error\",\"message\":\"Không thể lưu quy tắc vào database\"}"
            );
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        try {
            java.util.Map<String, String> params = readFormBody(req);

            long id = Long.parseLong(params.get("id"));
            long userId = Long.parseLong(params.get("assignToUserId"));
            int priority = Integer.parseInt(params.getOrDefault("priority", "1"));

            String name = params.get("name");
            String conditionType = params.get("conditionType");
            String conditionValue = params.get("conditionValue");
            String method = params.getOrDefault("assignmentMethod", "DIRECT");
            boolean active = Boolean.parseBoolean(
                params.getOrDefault("active", "true")
            );

            AssignmentRule rule = new AssignmentRule();
            rule.setId(id);
            rule.setName(name);
            rule.setConditionType(conditionType);
            rule.setConditionValue(conditionValue);
            rule.setAssignToUserId(userId);
            rule.setPriority(priority);
            rule.setActive(active);
            rule.setAssignmentMethod(method);

            if (id <= 0 || userId <= 0 || priority <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"Dữ liệu không hợp lệ\"}"
                );
                return;
            }

            boolean updated = assignmentRuleService.updateRule(rule);

            if (updated) {
                resp.getWriter().write(
                    "{\"status\":\"success\",\"message\":\"Cập nhật quy tắc thành công\"}"
                );
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"Không tìm thấy quy tắc hoặc dữ liệu không hợp lệ\"}"
                );
            }

        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(
                "{\"status\":\"error\",\"message\":\"ID hoặc độ ưu tiên không hợp lệ\"}"
            );
        } catch (IllegalStateException e) {
            getServletContext().log("Cannot update assignment rule", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(
                "{\"status\":\"error\",\"message\":\"Không thể cập nhật quy tắc\"}"
            );
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        try {
            java.util.Map<String, String> params = readFormBody(req);
            String idText = params.get("id");

            if (idText == null || idText.trim().isEmpty()) {
                idText = req.getParameter("id");
            }

            long id = Long.parseLong(idText);

            if (id <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"ID không hợp lệ\"}"
                );
                return;
            }

            boolean deleted = assignmentRuleService.deleteRule(id);

            if (deleted) {
                resp.getWriter().write(
                    "{\"status\":\"success\",\"message\":\"Xóa quy tắc thành công\"}"
                );
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"Không tìm thấy quy tắc\"}"
                );
            }

        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(
                "{\"status\":\"error\",\"message\":\"ID không hợp lệ\"}"
            );
        } catch (IllegalStateException e) {
            getServletContext().log("Cannot delete assignment rule", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(
                "{\"status\":\"error\",\"message\":\"Không thể xóa quy tắc\"}"
            );
        }
    }

    private java.util.Map<String, String> readFormBody(HttpServletRequest req)
            throws IOException {

        java.util.Map<String, String> params = new java.util.HashMap<>();
        String body = req.getReader().lines()
                .collect(java.util.stream.Collectors.joining("&"));

        if (body.isEmpty()) {
            return params;
        }

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = java.net.URLDecoder.decode(
                parts[0], java.nio.charset.StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                    ? java.net.URLDecoder.decode(
                        parts[1], java.nio.charset.StandardCharsets.UTF_8
                      )
                    : "";

            params.put(key, value);
        }

        return params;
    }
}