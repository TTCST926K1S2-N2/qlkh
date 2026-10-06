package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.dto.Customer360DTO;
import vn.edu.ictu.qlkh.service.Customer360Service;
import vn.edu.ictu.qlkh.util.Customer360Json;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * GET /api/v1/customers/360/{id}
 * A longer prefix than CustomerServlet's /api/v1/customers/*, so no collision.
 */
@WebServlet("/api/v1/customers/360/*")
public class Customer360Servlet extends HttpServlet {
    private Customer360Service service;

    public Customer360Servlet() {}

    // Dependency injection for tests; container uses the no-arg constructor.
    public Customer360Servlet(Customer360Service service) {
        this.service = Objects.requireNonNull(service);
    }

    @Override
    public void init() {
        if (service == null) {
            service = new Customer360Service();
        }
    }

    private record Actor(long id, String role) {}

    private Actor actor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object id = session.getAttribute("userId");
        Object role = session.getAttribute("userRole");
        if (id == null || role == null) return null;
        long userId;
        try {
            userId = Long.parseLong(id.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
        if (userId <= 0) return null;
        return new Actor(userId, role.toString().trim().toUpperCase(Locale.ROOT));
    }

    private long pathId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]*")) {
            throw new IllegalArgumentException("ID khach hang khong hop le");
        }
        try {
            return Long.parseLong(path.substring(1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("ID khach hang khong hop le");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        try {
            Actor actor = actor(req);
            if (actor == null) {
                error(res, 401, "Chua dang nhap");
                return;
            }
            if (!Set.of("SALES", "MANAGER", "ADMIN").contains(actor.role())) {
                error(res, 403, "Khong co quyen truy cap");
                return;
            }
            long customerId = pathId(req);
            Customer360DTO result = service.get(customerId, actor.id(), actor.role());
            if (result == null) {
                // 404 for both absent and out-of-scope customers.
                error(res, 404, "Khong tim thay khach hang");
                return;
            }
            send(res, 200, Customer360Json.toJson(result));
        } catch (IllegalArgumentException ex) {
            error(res, 400, ex.getMessage());
        } catch (SecurityException ex) {
            error(res, 403, "Khong co quyen truy cap");
        } catch (SQLException ex) {
            log("S3-03 Customer 360 database error", ex);
            error(res, 500, "Loi co so du lieu");
        } catch (RuntimeException ex) {
            log("S3-03 Customer 360 error", ex);
            error(res, 500, "Loi he thong");
        }
    }

    private void error(HttpServletResponse res, int status, String message)
            throws IOException {
        send(res, status, "{\"success\":false,\"message\":"
                + Customer360Json.quote(message) + "}");
    }

    private void send(HttpServletResponse res, int status, String json)
            throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        res.getWriter().write(json);
    }
}
