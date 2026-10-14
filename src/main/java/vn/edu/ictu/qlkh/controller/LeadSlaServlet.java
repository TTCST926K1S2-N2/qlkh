package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.LeadSlaService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet({
        "/api/v1/leads/sla",
        "/api/v1/leads/*"
})
public class LeadSlaServlet extends HttpServlet {

    private LeadSlaService service;

    @Override
    public void init() {
        service = new LeadSlaService();
    }

    private record Actor(long id, String role) {}

    private Actor actor(HttpServletRequest req) {

        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object id = session.getAttribute("userId");
        Object role = session.getAttribute("userRole");

        if (id == null || role == null) {
            return null;
        }

        try {
            long userId = Long.parseLong(id.toString());

            if (userId <= 0) {
                return null;
            }

            return new Actor(
                    userId,
                    role.toString()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {

        Actor actor = actor(req);

        if (actor == null) {
            error(res, 401, "Chua dang nhap");
            return;
        }

        if (!Set.of("SALES", "MANAGER", "ADMIN")
                .contains(actor.role())) {
            error(res, 403, "Khong co quyen truy cap");
            return;
        }

        if (!"/api/v1/leads/sla".equals(req.getServletPath())) {
            error(res, 404, "API khong ton tai");
            return;
        }

        try {
            List<Map<String, Object>> leads =
                    service.getAssignedLeads(actor.id());

            StringBuilder json = new StringBuilder();

            json.append("{\"success\":true,\"leads\":[");

            for (int i = 0; i < leads.size(); i++) {

                if (i > 0) {
                    json.append(",");
                }

                Map<String, Object> lead = leads.get(i);

                json.append("{");

                int index = 0;

                for (Map.Entry<String, Object> entry
                        : lead.entrySet()) {

                    if (index++ > 0) {
                        json.append(",");
                    }

                    json.append("\"")
                            .append(escape(entry.getKey()))
                            .append("\":");

                    Object value = entry.getValue();

                    if (value == null) {
                        json.append("null");
                    } else if (value instanceof Number
                            || value instanceof Boolean) {
                        json.append(value);
                    } else {
                        json.append("\"")
                                .append(escape(value.toString()))
                                .append("\"");
                    }
                }

                json.append("}");
            }

            json.append("]}");

            send(res, 200, json.toString());

        } catch (SQLException ex) {
            log("S4-07 database error", ex);
            error(res, 500, "Loi co so du lieu");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws IOException {

        Actor actor = actor(req);

        if (actor == null) {
            error(res, 401, "Chua dang nhap");
            return;
        }

        if (!Set.of("SALES", "MANAGER", "ADMIN")
                .contains(actor.role())) {
            error(res, 403, "Khong co quyen truy cap");
            return;
        }

        String path = req.getPathInfo();

        if (path == null
                || !path.matches("/[1-9][0-9]*/(accept|reject|assign)")) {
            error(res, 404, "API khong ton tai");
            return;
        }

        String[] parts = path.split("/");

        try {
            long leadId = Long.parseLong(parts[1]);
            String action = parts[2];

            boolean updated;

            if ("assign".equals(action)) {

                if (!Set.of("MANAGER", "ADMIN")
                        .contains(actor.role())) {
                    error(res, 403, "Khong co quyen phan cong");
                    return;
                }

                String rawAssigneeId =
                        req.getParameter("assigneeId");

                if (rawAssigneeId == null
                        || !rawAssigneeId.matches("[1-9][0-9]*")) {
                    error(res, 400, "ID nhan vien khong hop le");
                    return;
                }

                long assigneeId =
                        Long.parseLong(rawAssigneeId);

                updated = service.assignLead(
                        leadId,
                        actor.id(),
                        actor.role(),
                        assigneeId
                );

            } else if ("accept".equals(action)) {

                updated = service.acceptLead(
                        leadId,
                        actor.id()
                );

            } else {

                req.setCharacterEncoding("UTF-8");

                String reason = req.getParameter("reason");

                updated = service.rejectLead(
                        leadId,
                        actor.id(),
                        reason
                );
            }

            if (!updated) {
                error(res, 409,
                        "Lead khong ton tai hoac khong the thao tac");
                return;
            }

            send(res, 200,
                    "{\"success\":true,\"message\":\"Thanh cong\"}");

        } catch (SecurityException ex) {
            error(res, 403, "Khong co quyen phan cong Lead");

        } catch (NumberFormatException ex) {
            error(res, 400, "ID Lead hoac nhan vien khong hop le");

        } catch (IllegalArgumentException ex) {
            error(res, 400, ex.getMessage());

        } catch (SQLException ex) {
            log("S4-07 database error", ex);
            error(res, 500, "Loi co so du lieu");
        }
    }

    private void send(
            HttpServletResponse res,
            int status,
            String json
    ) throws IOException {

        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    private void error(
            HttpServletResponse res,
            int status,
            String message
    ) throws IOException {

        send(
                res,
                status,
                "{\"success\":false,\"message\":\""
                        + escape(message)
                        + "\"}"
        );
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20) {
                        result.append(
                                String.format("\\u%04x", (int) c)
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