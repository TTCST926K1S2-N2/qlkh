package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.service.ContactService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/v1/contacts/*")
public class ContactServlet extends HttpServlet {

    private ContactService service;

    @Override
    public void init() {
        service = new ContactService();
    }

    private record Actor(long id, String role) {}

    private Actor actor(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {
            throw new SecurityException("Chua dang nhap");
        }

        long id = Long.parseLong(
                session.getAttribute("userId").toString()
        );

        String role = String.valueOf(
                session.getAttribute("userRole")
        ).toUpperCase(Locale.ROOT);

        if (id <= 0 ||
                !List.of("ADMIN", "MANAGER", "SALES")
                        .contains(role)) {
            throw new SecurityException("Khong co quyen");
        }

        return new Actor(id, role);
    }

    private Long pathId(HttpServletRequest req) {
        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            return null;
        }

        if (!path.matches("/[1-9][0-9]*")) {
            throw new IllegalArgumentException(
                    "Duong dan khong hop le"
            );
        }

        return Long.parseLong(path.substring(1));
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        try {
            Actor actor = actor(req);
            Long id = pathId(req);

            if (id == null) {
                String value = req.getParameter("customerId");

                if (value == null ||
                        !value.matches("[1-9][0-9]*")) {
                    throw new IllegalArgumentException(
                            "Thieu customerId"
                    );
                }

                long customerId = Long.parseLong(value);

                List<Contact> contacts = service.list(
                        customerId,
                        actor.id(),
                        actor.role()
                );

                if (contacts == null) {
                    error(resp, 404, "Khong tim thay");
                    return;
                }

                StringBuilder json = new StringBuilder("[");

                for (Contact contact : contacts) {
                    if (json.length() > 1) {
                        json.append(",");
                    }

                    json.append(toJson(contact));
                }

                json.append("]");
                send(resp, 200, json.toString());
                return;
            }

            Contact contact = service.detail(
                    id,
                    actor.id(),
                    actor.role()
            );

            if (contact == null) {
                error(resp, 404, "Khong tim thay");
                return;
            }

            send(resp, 200, toJson(contact));

        } catch (Exception ex) {
            handle(resp, ex);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        try {
            Actor actor = actor(req);

            if (pathId(req) != null) {
                error(resp, 404, "Duong dan khong hop le");
                return;
            }

            Contact contact = readContact(req);

            long id = service.create(
                    contact,
                    actor.id(),
                    actor.role()
            );

            Contact created = service.detail(
                    id,
                    actor.id(),
                    actor.role()
            );

            if (created == null) {
                error(resp, 500, "Khong doc duoc lien he");
                return;
            }

            send(resp, 201, toJson(created));

        } catch (Exception ex) {
            handle(resp, ex);
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        try {
            Actor actor = actor(req);
            Long id = pathId(req);

            if (id == null) {
                throw new IllegalArgumentException(
                        "Thieu ID lien he"
                );
            }

            Contact contact = readContact(req);

            boolean updated = service.update(
                    id,
                    contact,
                    actor.id(),
                    actor.role()
            );

            if (!updated) {
                error(resp, 404, "Khong tim thay");
                return;
            }

            Contact result = service.detail(
                    id,
                    actor.id(),
                    actor.role()
            );

            if (result == null) {
                error(resp, 404, "Khong tim thay");
                return;
            }

            send(resp, 200, toJson(result));

        } catch (Exception ex) {
            handle(resp, ex);
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        try {
            Actor actor = actor(req);
            Long id = pathId(req);

            if (id == null) {
                throw new IllegalArgumentException(
                        "Thieu ID lien he"
                );
            }

            boolean deleted = service.delete(
                    id,
                    actor.id(),
                    actor.role()
            );

            if (!deleted) {
                error(resp, 404, "Khong tim thay");
                return;
            }

            send(resp, 200, "{\"success\":true}");

        } catch (Exception ex) {
            handle(resp, ex);
        }
    }

    private Contact readContact(HttpServletRequest req)
            throws IOException {

        String contentType = req.getContentType();

        if (contentType == null ||
                !contentType.toLowerCase(Locale.ROOT)
                        .matches("^application/json(?:\\s*;.*)?$")) {
            throw new IllegalArgumentException(
                    "Yeu cau application/json"
            );
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        mapper.enable(
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS
        );

        JsonNode root;

        try {
            root = mapper.readTree(req.getReader());
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException(
                    "JSON khong hop le", ex
            );
        }

        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException(
                    "JSON phai la object"
            );
        }

        java.util.Set<String> allowed = java.util.Set.of(
                "customerId",
                "fullName",
                "jobTitle",
                "email",
                "phone",
                "decisionRole",
                "isPrimary"
        );

        java.util.Iterator<String> fields = root.fieldNames();

        while (fields.hasNext()) {
            String field = fields.next();

            if (!allowed.contains(field)) {
                throw new IllegalArgumentException(
                        "Truong khong hop le: " + field
                );
            }
        }

        Contact contact = new Contact();

        JsonNode customerId = root.get("customerId");

        if (customerId != null && !customerId.isNull()) {
            if (!customerId.isIntegralNumber() ||
                    !customerId.canConvertToLong() ||
                    customerId.longValue() <= 0) {
                throw new IllegalArgumentException(
                        "customerId khong hop le"
                );
            }

            contact.setCustomerId(customerId.longValue());
        }

        contact.setFullName(
                stringField(root, "fullName")
        );
        contact.setJobTitle(
                stringField(root, "jobTitle")
        );
        contact.setEmail(
                stringField(root, "email")
        );
        contact.setPhone(
                stringField(root, "phone")
        );
        contact.setDecisionRole(
                stringField(root, "decisionRole")
        );

        JsonNode primary = root.get("isPrimary");

        if (primary != null && !primary.isNull()) {
            if (!primary.isBoolean()) {
                throw new IllegalArgumentException(
                        "isPrimary phai la boolean"
                );
            }

            contact.setPrimary(primary.booleanValue());
        } else if (primary != null) {
            throw new IllegalArgumentException(
                    "isPrimary phai la boolean"
            );
        }

        return contact;
    }

    private String stringField(JsonNode root, String name) {
        JsonNode value = root.get(name);

        if (value == null || value.isNull()) {
            return null;
        }

        if (!value.isTextual()) {
            throw new IllegalArgumentException(
                    name + " phai la chuoi"
            );
        }

        return value.textValue();
    }
    private String toJson(Contact c) {
        return "{"
                + "\"id\":" + c.getId()
                + ",\"customerId\":" + c.getCustomerId()
                + ",\"fullName\":" + quote(c.getFullName())
                + ",\"jobTitle\":" + quote(c.getJobTitle())
                + ",\"email\":" + quote(c.getEmail())
                + ",\"phone\":" + quote(c.getPhone())
                + ",\"decisionRole\":" + quote(c.getDecisionRole())
                + ",\"isPrimary\":" + c.isPrimary()
                + ",\"createdAt\":" + quote(
                        c.getCreatedAt() == null
                                ? null
                                : c.getCreatedAt().toString())
                + ",\"updatedAt\":" + quote(
                        c.getUpdatedAt() == null
                                ? null
                                : c.getUpdatedAt().toString())
                + "}";
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }

        StringBuilder result = new StringBuilder("\"");

        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 32) {
                        result.append(
                                String.format("\\u%04x", (int) c)
                        );
                    } else {
                        result.append(c);
                    }
                }
            }
        }

        return result.append('"').toString();
    }

    private void send(
            HttpServletResponse resp,
            int status,
            String json) throws IOException {

        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(json);
    }

    private void error(
            HttpServletResponse resp,
            int status,
            String message) throws IOException {

        send(
                resp,
                status,
                "{\"success\":false,\"message\":"
                        + quote(message) + "}"
        );
    }

    private void handle(
            HttpServletResponse resp,
            Exception ex) throws IOException {

        if (ex instanceof SecurityException) {
            error(resp, 403, ex.getMessage());
        } else if (ex instanceof IllegalArgumentException) {
            error(resp, 400, ex.getMessage());
        } else if (ex instanceof SQLException) {
            error(resp, 500, "Loi co so du lieu");
            log("Contact API database error", ex);
        } else {
            error(resp, 500, "Loi he thong");
            log("Contact API error", ex);
        }
    }
}