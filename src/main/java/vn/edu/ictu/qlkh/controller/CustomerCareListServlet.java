package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.service.CustomerCareListService;
import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {"/customer-care-list", "/api/v1/customer-care-list/*"})
public class CustomerCareListServlet extends HttpServlet {
    private final CustomerCareListService service = new CustomerCareListService();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private record Actor(long id, String role) {}

    private Actor actor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) throw new SecurityException("Chua dang nhap");
        Object id = session.getAttribute("userId");
        Object role = session.getAttribute("userRole");
        if (id == null || role == null) throw new SecurityException("Chua dang nhap");
        try {
            long userId = Long.parseLong(id.toString());
            String r = role.toString().trim().toUpperCase(Locale.ROOT);
            if (userId <= 0 || !Set.of("ADMIN", "MANAGER", "SALES").contains(r))
                throw new SecurityException("Khong co quyen truy cap");
            return new Actor(userId, r);
        } catch (NumberFormatException e) { throw new SecurityException("Phien dang nhap khong hop le"); }
    }
    private void json(HttpServletResponse res, int status, Object body) throws IOException {
        res.setStatus(status); res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store"); mapper.writeValue(res.getWriter(), body);
    }
    private void failure(HttpServletResponse res, Exception e) throws IOException {
        int status = e instanceof SecurityException ? 403 : e instanceof IllegalArgumentException ? 400 : 500;
        if (status == 500) getServletContext().log("S3-09 customer care list", e);
        json(res, status, Map.of("error", status == 500 ? "Loi he thong" : e.getMessage()));
    }
    private void checkOrigin(HttpServletRequest req) {
        String origin = req.getHeader("Origin"), referer = req.getHeader("Referer");
        String expected = req.getScheme() + "://" + req.getServerName() +
                ((req.getServerPort() == 80 && "http".equals(req.getScheme())) ||
                 (req.getServerPort() == 443 && "https".equals(req.getScheme())) ? "" : ":" + req.getServerPort());
        if (origin != null && origin.equals(expected)) return;
        if (origin == null && referer != null && (referer.equals(expected) || referer.startsWith(expected + "/"))) return;
        throw new SecurityException("Nguon yeu cau khong hop le");
    }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        try {
            Actor a = actor(req);
            if (req.getServletPath().equals("/customer-care-list")) {
                res.setCharacterEncoding("UTF-8");
                res.setContentType("text/html;charset=UTF-8");
                req.getRequestDispatcher("/views/sales/customer-care-list.jsp").forward(req, res);
                return;
            }
            String path = req.getPathInfo();
            if (path != null && !"/".equals(path)) { json(res, 404, Map.of("error", "Khong tim thay")); return; }
            String raw = req.getParameter("days");
            int days;
            try { days = raw == null ? 30 : Integer.parseInt(raw); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("So ngay khong hop le"); }
            json(res, 200, service.list(days, a.id(), a.role()));
        } catch (Exception e) { failure(res, e); }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Actor a = actor(req); checkOrigin(req);
            String path = req.getPathInfo();
            if (path == null || !path.matches("/[1-9][0-9]*/contact")) {
                json(res, 404, Map.of("error", "Duong dan khong hop le")); return;
            }
            long id;
            try { id = Long.parseLong(path.split("/")[1]); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("ID khong hop le"); }
            service.contacted(id, a.id(), a.role());
            json(res, 200, Map.of("success", true));
        } catch (Exception e) { failure(res, e); }
    }
}
