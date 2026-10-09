package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.service.CustomerDuplicateService;
import vn.edu.ictu.qlkh.service.CustomerMergeService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/v1/customers/duplicates/*")
public class CustomerDuplicateServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();
    private CustomerDuplicateService duplicateService;
    private CustomerMergeService mergeService;

    @Override
    public void init() {
        duplicateService = new CustomerDuplicateService();
        mergeService = new CustomerMergeService();
    }

    private record Actor(long id, String role, String username) {}

    private Actor actor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null
                || session.getAttribute("userRole") == null)
            throw new SecurityException("Chua dang nhap");
        long id;
        try {
            id = Long.parseLong(session.getAttribute("userId").toString());
        } catch (NumberFormatException e) {
            throw new SecurityException("Phien dang nhap khong hop le");
        }
        String role = session.getAttribute("userRole").toString().trim().toUpperCase(Locale.ROOT);
        if (id <= 0 || !List.of("SALES", "MANAGER", "ADMIN").contains(role))
            throw new SecurityException("Khong co quyen truy cap");
        Object username = session.getAttribute("userEmail");
        if (username == null) username = session.getAttribute("userName");
        return new Actor(id, role, username == null ? "unknown" : username.toString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Actor a = actor(req);
            String path = req.getPathInfo();
            if (path == null) path = "/";

            if (path.matches("/check/[1-9][0-9]*")) {
                long id;
                try {
                    id = Long.parseLong(path.substring("/check/".length()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("ID khong hop le");
                }
                List<Customer> duplicates = duplicateService.findDuplicates(id, a.id(), a.role());
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("customerId", id);
                result.put("count", duplicates.size());
                result.put("duplicates", duplicates);
                send(res, 200, result);
                return;
            }

            if ("/compare".equals(path)) {
                long sourceId = queryId(req, "sourceId");
                long targetId = queryId(req, "targetId");
                Customer[] pair = duplicateService.compare(sourceId, targetId, a.id(), a.role());
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("source", pair[0]);
                result.put("target", pair[1]);
                result.put("isDuplicate", duplicateService.areDuplicates(pair[0], pair[1]));
                send(res, 200, result);
                return;
            }
            sendError(res, 404, "Duong dan khong hop le");
        } catch (Exception e) {
            handle(res, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Actor a = actor(req);
            if (!"/merge".equals(req.getPathInfo())) {
                sendError(res, 404, "Duong dan khong hop le");
                return;
            }
            String contentType = req.getContentType();
            if (contentType == null
                    || !contentType.toLowerCase(Locale.ROOT).startsWith("application/json"))
                throw new IllegalArgumentException("Content-Type phai la application/json");

            req.setCharacterEncoding("UTF-8");
            Map<String, Object> body = mapper.readValue(
                    req.getInputStream(), new TypeReference<Map<String, Object>>() {});
            if (body == null || !body.keySet().equals(Set.of("sourceCustomerId", "targetCustomerId")))
                throw new IllegalArgumentException(
                        "JSON phai gom sourceCustomerId va targetCustomerId");

            long sourceId = bodyId(body.get("sourceCustomerId"), "sourceCustomerId");
            long targetId = bodyId(body.get("targetCustomerId"), "targetCustomerId");
            mergeService.merge(sourceId, targetId, a.id(), a.role(), a.username());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("sourceCustomerId", sourceId);
            result.put("targetCustomerId", targetId);
            result.put("message", "Gop khach hang thanh cong");
            send(res, 200, result);
        } catch (Exception e) {
            handle(res, e);
        }
    }

    private long bodyId(Object value, String name) {
        if (!(value instanceof Number n))
            throw new IllegalArgumentException(name + " phai la so nguyen duong");
        long id = n.longValue();
        if (n.doubleValue() != id || id <= 0)
            throw new IllegalArgumentException(name + " phai la so nguyen duong");
        return id;
    }

    private long queryId(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || !value.matches("[1-9][0-9]*"))
            throw new IllegalArgumentException(name + " khong hop le");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " khong hop le");
        }
    }

    private void send(HttpServletResponse res, int status, Object body) throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(res.getWriter(), body);
    }

    private void sendError(HttpServletResponse res, int status, String message) throws IOException {
        send(res, status, Map.of("success", false, "message", message));
    }

    private void handle(HttpServletResponse res, Exception e) throws IOException {
        if (e instanceof SecurityException) sendError(res, 403, e.getMessage());
        else if (e instanceof IllegalArgumentException) sendError(res, 400, e.getMessage());
        else if (e instanceof IllegalStateException) sendError(res, 409, e.getMessage());
        else if (e instanceof SQLException)
            sendError(res, 500, "Loi co so du lieu; thao tac da rollback neu dang trong transaction");
        else sendError(res, 500, "Loi he thong");
    }
}
