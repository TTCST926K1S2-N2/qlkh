package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.NotificationService;
import vn.edu.ictu.qlkh.service.SessionService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(urlPatterns = {
        "/api/notifications",
        "/api/notifications/unread-count",
        "/api/notifications/read",
        "/api/notifications/read-all"
})
public class NotificationServlet extends HttpServlet {
    private final NotificationService service = new NotificationService();
    private final ObjectMapper json = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepare(resp);
        Long userId = currentUser(req);
        if (userId == null) { write(resp, 401, Map.of("success", false, "message", "Unauthorized")); return; }
        try {
            String path = req.getServletPath();
            if ("/api/notifications".equals(path)) {
                write(resp, 200, Map.of("success", true, "data", service.latest(userId),
                        "unreadCount", service.unreadCount(userId)));
            } else if ("/api/notifications/unread-count".equals(path)) {
                write(resp, 200, Map.of("success", true, "unreadCount", service.unreadCount(userId)));
            } else {
                write(resp, 404, Map.of("success", false, "message", "Not found"));
            }
        } catch (SQLException e) {
            getServletContext().log("Cannot load notifications", e);
            write(resp, 500, Map.of("success", false, "message", "Database error"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepare(resp);
        Long userId = currentUser(req);
        if (userId == null) { write(resp, 401, Map.of("success", false, "message", "Unauthorized")); return; }
        // Same-origin requests only. No cross-origin state changes.
        String origin = req.getHeader("Origin");
        if (origin != null) {
            String expected = req.getScheme() + "://" + req.getServerName() +
                    ((req.getServerPort() == 80 && "http".equals(req.getScheme())) ||
                     (req.getServerPort() == 443 && "https".equals(req.getScheme()))
                            ? "" : ":" + req.getServerPort());
            if (!expected.equals(origin)) {
                write(resp, 403, Map.of("success", false, "message", "Invalid origin"));
                return;
            }
        }
        String fetchSite = req.getHeader("Sec-Fetch-Site");
        if ("cross-site".equalsIgnoreCase(fetchSite)) {
            write(resp, 403, Map.of("success", false, "message", "Cross-site request denied"));
            return;
        }
        try {
            String path = req.getServletPath();
            if ("/api/notifications/read".equals(path)) {
                long id;
                try { id = Long.parseLong(req.getParameter("id")); }
                catch (NumberFormatException e) {
                    write(resp, 400, Map.of("success", false, "message", "Invalid id")); return;
                }
                if (id <= 0) { write(resp, 400, Map.of("success", false, "message", "Invalid id")); return; }
                boolean changed = service.markRead(userId, id);
                write(resp, 200, Map.of("success", true, "updated", changed));
            } else if ("/api/notifications/read-all".equals(path)) {
                int changed = service.markAllRead(userId);
                write(resp, 200, Map.of("success", true, "updatedCount", changed));
            } else {
                write(resp, 404, Map.of("success", false, "message", "Not found"));
            }
        } catch (SQLException e) {
            getServletContext().log("Cannot update notifications", e);
            write(resp, 500, Map.of("success", false, "message", "Database error"));
        }
    }

    private Long currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (!SessionService.isAuthenticated(session)) return null;
        Object id = session.getAttribute("userId");
        if (!(id instanceof Number)) return null;
        long value = ((Number) id).longValue();
        return value > 0 ? value : null;
    }

    private void prepare(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("X-Content-Type-Options", "nosniff");
    }

    private void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        json.writeValue(resp.getWriter(), body);
    }
}
