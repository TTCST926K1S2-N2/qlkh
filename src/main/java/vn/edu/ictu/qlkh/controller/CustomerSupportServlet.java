package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.CustomerSupportRequest;
import vn.edu.ictu.qlkh.service.CustomerSupportService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/v1/customer-support/*")
public class CustomerSupportServlet extends HttpServlet {

    private CustomerSupportService service;
    private ObjectMapper mapper;

    @Override
    public void init() {
        service = new CustomerSupportService();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private record Actor(long id, String role) {}

    private Actor actor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            throw new SecurityException("Chua dang nhap");
        }

        Object id = session.getAttribute("userId");
        Object role = session.getAttribute("userRole");

        if (id == null || role == null) {
            throw new SecurityException("Chua dang nhap");
        }

        long userId;

        try {
            userId = Long.parseLong(id.toString());
        } catch (NumberFormatException e) {
            throw new SecurityException("Phien dang nhap khong hop le");
        }

        String r = role.toString().trim().toUpperCase(Locale.ROOT);

        if (userId <= 0 ||
                !Set.of("SALES", "MANAGER", "ADMIN").contains(r)) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        return new Actor(userId, r);
    }

    private void checkSameOrigin(
            HttpServletRequest req) {

        String origin = req.getHeader("Origin");
        String referer = req.getHeader("Referer");

        String expected = req.getScheme()
                + "://" + req.getServerName()
                + (req.getServerPort() == 80 && "http".equals(req.getScheme())
                    || req.getServerPort() == 443 && "https".equals(req.getScheme())
                    ? "" : ":" + req.getServerPort());

        if (origin != null && !origin.isBlank()) {
            if (!expected.equals(origin)) {
                throw new SecurityException("Nguon yeu cau khong hop le");
            }
            return;
        }

        if (referer != null && !referer.isBlank()) {
            if (!referer.equals(expected)
                    && !referer.startsWith(expected + "/")) {
                throw new SecurityException("Nguon yeu cau khong hop le");
            }
            return;
        }

        throw new SecurityException("Thieu thong tin xac thuc nguon yeu cau");
    }
    private long positiveId(String value) {
        if (value == null || !value.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException("ID khong hop le");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID khong hop le");
        }
    }

    private long requestId(HttpServletRequest req) {
        String path = req.getPathInfo();

        if (path == null || !path.matches("/[1-9][0-9]*")) {
            throw new IllegalArgumentException("Duong dan khong hop le");
        }

        return positiveId(path.substring(1));
    }

    private boolean isRoot(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null || "/".equals(path);
    }

    private void send(
            HttpServletResponse res,
            int status,
            Object data) throws IOException {

        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        mapper.writeValue(res.getWriter(), data);
    }

    private void error(
            HttpServletResponse res,
            int status,
            String message) throws IOException {

        send(res, status, Map.of("error", message));
    }

    private void handleError(
            HttpServletResponse res,
            Exception e) throws IOException {

        if (e instanceof SecurityException) {
            error(res, 403, e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            error(res, 400, e.getMessage());
        } else {
            getServletContext().log("Customer support error", e);
            error(res, 500, "Loi he thong khi xu ly yeu cau");
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try {
            Actor a = actor(req);
            String path = req.getPathInfo();

            if (isRoot(req)) {
                long customerId = positiveId(
                        req.getParameter("customerId"));

                send(res, 200, service.listByCustomer(
                        customerId, a.id(), a.role()));

            } else if ("/risk".equals(path)) {
                long customerId = positiveId(
                        req.getParameter("customerId"));

                int unresolved = service.countUnresolved(
                        customerId, a.id(), a.role());

                send(res, 200, Map.of(
                        "customerId", customerId,
                        "unresolvedCount", unresolved,
                        "atRisk", unresolved >= 3
                ));

            } else {
                CustomerSupportRequest result = service.detail(
                        requestId(req), a.id(), a.role());

                if (result == null) {
                    error(res, 404, "Khong tim thay yeu cau");
                    return;
                }

                send(res, 200, result);
            }

        } catch (Exception e) {
            handleError(res, e);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try {
            Actor a = actor(req);
            checkSameOrigin(req);

            if (!isRoot(req)) {
                error(res, 404, "Duong dan khong hop le");
                return;
            }

            CustomerSupportRequest input = mapper.readValue(
                    req.getInputStream(), CustomerSupportRequest.class);

            CustomerSupportRequest created = service.create(
                    input, a.id(), a.role());

            send(res, 201, created);

        } catch (Exception e) {
            handleError(res, e);
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try {
            Actor a = actor(req);
            checkSameOrigin(req);
            long id = requestId(req);

            CustomerSupportRequest input = mapper.readValue(
                    req.getInputStream(), CustomerSupportRequest.class);

            CustomerSupportRequest updated = service.update(
                    id,
                    input.getStatus(),
                    input.getPriority(),
                    input.getAssignedTo(),
                    input.getResolutionNote(),
                    a.id(),
                    a.role()
            );

            if (updated == null) {
                error(res, 404, "Khong tim thay yeu cau");
                return;
            }

            send(res, 200, updated);

        } catch (Exception e) {
            handleError(res, e);
        }
    }
}