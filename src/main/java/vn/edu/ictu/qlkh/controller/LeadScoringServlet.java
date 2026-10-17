package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.dto.LeadScoringConfig;
import vn.edu.ictu.qlkh.service.LeadScoringService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/v1/lead-scoring/*")
public class LeadScoringServlet extends HttpServlet {
    private final LeadScoringService service = new LeadScoringService();
    private final ObjectMapper mapper = new ObjectMapper();

    private String role(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Object v = s == null ? null : s.getAttribute("userRole");
        return v == null ? "" : v.toString().trim().toUpperCase(Locale.ROOT);
    }
    private long userId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Object v = s == null ? null : s.getAttribute("userId");
        if (v == null) return -1;
        try { return Long.parseLong(v.toString()); }
        catch (NumberFormatException e) { return -1; }
    }
    private void send(HttpServletResponse res, int status, Object body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(res.getOutputStream(), body);
    }
    private Map<String,Object> message(boolean success, String message) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("success", success);
        m.put("message", message);
        return m;
    }
    private boolean authenticated(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (userId(req) <= 0 || role(req).isBlank()) {
            send(res, 401, message(false, "Chua dang nhap"));
            return false;
        }
        return true;
    }
    private boolean canConfigure(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!authenticated(req, res)) return false;
        if (!Set.of("ADMIN", "MANAGER").contains(role(req))) {
            send(res, 403, message(false, "Khong co quyen cau hinh"));
            return false;
        }
        return true;
    }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!authenticated(req, res)) return;
        try {
            String path = req.getPathInfo();
            if ("/config".equals(path)) send(res, 200, service.getConfig());
            else {
                Map<String,Object> body = new LinkedHashMap<>();
                body.put("success", true);
                body.put("data", service.getLeadScores(userId(req), role(req)));
                send(res, 200, body);
            }
        } catch (SQLException e) {
            send(res, 500, message(false, "Loi truy cap database"));
        }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        save(req, res);
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        save(req, res);
    }
    private void save(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!canConfigure(req, res)) return;
        if (!"/config".equals(req.getPathInfo())) {
            send(res, 404, message(false, "Endpoint khong ton tai"));
            return;
        }
        try {
            LeadScoringConfig config = mapper.readValue(req.getInputStream(), LeadScoringConfig.class);
            LeadScoringService.validate(config);
            service.saveConfig(config, userId(req));
            send(res, 200, message(true, "Da luu cau hinh cham diem"));
        } catch (IllegalArgumentException e) {
            send(res, 400, message(false, e.getMessage()));
        } catch (SQLException e) {
            send(res, 500, message(false, "Loi luu cau hinh database"));
        }
    }
}
