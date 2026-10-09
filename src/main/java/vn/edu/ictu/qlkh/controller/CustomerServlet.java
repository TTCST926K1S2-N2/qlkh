package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.dao.CustomerDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.service.CustomerService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;

@WebServlet("/api/v1/customers/*")
public class CustomerServlet extends HttpServlet {

    private CustomerService service;

    @Override
    public void init() {
        service = new CustomerService();
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

        if (userId <= 0 || !Set.of(
                "SALES", "MANAGER", "ADMIN").contains(r)) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        return new Actor(userId, r);
    }

    private long pathId(HttpServletRequest req) {
        String path = req.getPathInfo();

        if (path == null || !path.matches("/[1-9][0-9]*")) {
            throw new IllegalArgumentException("ID khong hop le");
        }

        try {
            return Long.parseLong(path.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID khong hop le");
        }
    }

    private boolean isList(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path == null || path.equals("/");
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        try {
            Actor a = actor(req);

            if (isList(req)) {
                if (hasSearchParameters(req)) {
                    int page = parsePageParameter(req, "page", 1);
                    int size = parsePageParameter(req, "size", 20);
                    Long ownerId = parseOptionalLongParameter(req, "ownerId");

                    CustomerDAO.SearchPageData result = service.searchCustomers(
                            a.id(),
                            a.role(),
                            req.getParameter("keyword"),
                            req.getParameter("companyName"),
                            req.getParameter("taxCode"),
                            req.getParameter("phone"),
                            req.getParameter("industry"),
                            req.getParameter("status"),
                            ownerId,
                            page,
                            size
                    );

                    StringJoiner items = new StringJoiner(",", "[", "]");
                    for (Customer c : result.getItems()) {
                        items.add(toJson(c));
                    }

                    int totalPages = (int) Math.ceil(
                            (double) result.getTotalItems() / size
                    );

                    String json = "{\"items\":" + items
                            + ",\"page\":" + page
                            + ",\"size\":" + size
                            + ",\"totalItems\":" + result.getTotalItems()
                            + ",\"totalPages\":" + totalPages + "}";

                    send(res, 200, json);
                } else {
                    List<Customer> customers = service.list(a.id(), a.role());
                    StringJoiner json = new StringJoiner(",", "[", "]");

                    for (Customer c : customers) {
                        json.add(toJson(c));
                    }

                    send(res, 200, json.toString());
                }
            } else {
                Customer c = service.detail(
                        pathId(req), a.id(), a.role());

                if (c == null) {
                    error(res, 404, "Khong tim thay khach hang");
                    return;
                }

                send(res, 200, toJson(c));
            }
        } catch (Exception e) {
            handle(res, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {
        try {
            Actor a = actor(req);

            if (!isList(req)) {
                error(res, 404, "Duong dan khong hop le");
                return;
            }

            Customer c = readCustomer(req);
            long id = service.create(c, a.id(), a.role());

            Customer saved = service.detail(id, a.id(), a.role());

            if (saved == null) {
                error(res, 500, "Khong the doc khach hang vua tao");
                return;
            }

            send(res, 201, toJson(saved));
        } catch (Exception e) {
            handle(res, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        try {
            Actor a = actor(req);
            long id = pathId(req);
            Customer c = readCustomer(req);

            if (!service.update(id, c, a.id(), a.role())) {
                error(res, 404, "Khong tim thay khach hang");
                return;
            }

            Customer updated = service.detail(id, a.id(), a.role());

            if (updated == null) {
                error(res, 404, "Khach hang khong con trong pham vi truy cap");
                return;
            }

            send(res, 200, toJson(updated));
        } catch (Exception e) {
            handle(res, e);
        }
    }

    private boolean hasSearchParameters(HttpServletRequest req) {
        String[] names = {
                "keyword", "companyName", "taxCode", "phone",
                "industry", "status", "ownerId", "page", "size"
        };

        for (String name : names) {
            if (req.getParameter(name) != null) {
                return true;
            }
        }

        return false;
    }

    private int parsePageParameter(
            HttpServletRequest req, String name, int defaultValue) {
        String value = req.getParameter(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Tham so " + name + " khong hop le"
            );
        }
    }

    private Long parseOptionalLongParameter(
            HttpServletRequest req, String name) {
        String value = req.getParameter(name);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            long parsed = Long.parseLong(value.trim());

            if (parsed <= 0) {
                throw new IllegalArgumentException(
                        "Tham so " + name + " phai lon hon 0"
                );
            }

            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Tham so " + name + " khong hop le"
            );
        }
    }
    private Customer readCustomer(HttpServletRequest req)
            throws IOException {

        String contentType = req.getContentType();

        if (contentType == null ||
                !contentType.toLowerCase(Locale.ROOT)
                        .startsWith("application/json")) {
            throw new IllegalArgumentException(
                    "Content-Type phai la application/json");
        }

        req.setCharacterEncoding("UTF-8");

        String body = req.getReader().lines()
                .reduce("", (a, b) -> a + b + "\n");

        Map<String, String> values = parseJson(body);

        Set<String> allowed = Set.of(
                "companyName", "taxCode", "industry",
                "companySize", "website", "address",
                "ownerId", "status");

        for (String key : values.keySet()) {
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException(
                        "Truong khong hop le: " + key);
            }
        }

        Customer c = new Customer();
        c.setCompanyName(values.get("companyName"));
        c.setTaxCode(values.get("taxCode"));
        c.setIndustry(values.get("industry"));
        c.setCompanySize(values.get("companySize"));
        c.setWebsite(values.get("website"));
        c.setAddress(values.get("address"));
        c.setStatus(values.get("status"));

        String owner = values.get("ownerId");

        if (owner != null) {
            try {
                c.setOwnerId(Long.parseLong(owner));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "ownerId phai la so nguyen");
            }
        }

        return c;
    }

    // Parser for flat JSON objects only.
    // Strings, null and integer values are supported.
    private Map<String, String> parseJson(String body) {
        if (body == null) {
            throw new IllegalArgumentException("JSON trong");
        }

        String s = body.trim();

        if (!s.startsWith("{") || !s.endsWith("}")) {
            throw new IllegalArgumentException("JSON khong hop le");
        }

        s = s.substring(1, s.length() - 1);
        Map<String, String> result = new HashMap<>();

        Pattern p = Pattern.compile(
                "\\s*\"([A-Za-z][A-Za-z0-9]*)\"\\s*:\\s*" +
                "(\"(?:\\\\.|[^\"\\\\])*\"|null|-?[0-9]+)\\s*");

        int pos = 0;

        while (pos < s.length()) {
            Matcher m = p.matcher(s);
            m.region(pos, s.length());

            if (!m.lookingAt()) {
                if (s.substring(pos).isBlank()) {
                    break;
                }
                throw new IllegalArgumentException("JSON khong hop le");
            }

            String key = m.group(1);
            String raw = m.group(2);

            if (result.containsKey(key)) {
                throw new IllegalArgumentException("Trung truong: " + key);
            }

            String value = "null".equals(raw)
                    ? null
                    : raw.startsWith("\"")
                    ? unescape(raw.substring(1, raw.length() - 1))
                    : raw;

            if (value != null && raw.startsWith("\"") == false
                    && !"ownerId".equals(key)) {
                throw new IllegalArgumentException(
                        "Truong " + key + " phai la chuoi");
            }

            if ("ownerId".equals(key) && value != null
                    && raw.startsWith("\"")) {
                throw new IllegalArgumentException(
                        "ownerId phai la so nguyen");
            }

            result.put(key, value);
            pos = m.end();

            if (pos < s.length()) {
                if (s.charAt(pos) != ',') {
                    throw new IllegalArgumentException("JSON khong hop le");
                }
                pos++;
                if (s.substring(pos).isBlank()) {
                    throw new IllegalArgumentException("JSON du dau phay");
                }
            }
        }

        return result;
    }

    private String unescape(String s) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c != '\\') {
                if (c < 0x20) {
                    throw new IllegalArgumentException("JSON khong hop le");
                }
                out.append(c);
                continue;
            }

            if (++i >= s.length()) {
                throw new IllegalArgumentException("JSON khong hop le");
            }

            char e = s.charAt(i);

            switch (e) {
                case '"' -> out.append('"');
                case '\\' -> out.append('\\');
                case '/' -> out.append('/');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'u' -> {
                    if (i + 4 >= s.length()) {
                        throw new IllegalArgumentException("Unicode sai");
                    }
                    try {
                        out.append((char) Integer.parseInt(
                                s.substring(i + 1, i + 5), 16));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Unicode sai");
                    }
                    i += 4;
                }
                default -> throw new IllegalArgumentException(
                        "Ky tu escape khong hop le");
            }
        }

        return out.toString();
    }

    private String quote(String s) {
        if (s == null) {
            return "null";
        }

        StringBuilder b = new StringBuilder("\"");

        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }

        return b.append('"').toString();
    }

    private String toJson(Customer c) {
        return "{"
                + "\"id\":" + c.getId()
                + ",\"companyName\":" + quote(c.getCompanyName())
                + ",\"taxCode\":" + quote(c.getTaxCode())
                + ",\"industry\":" + quote(c.getIndustry())
                + ",\"companySize\":" + quote(c.getCompanySize())
                + ",\"website\":" + quote(c.getWebsite())
                + ",\"address\":" + quote(c.getAddress())
                + ",\"ownerId\":" + c.getOwnerId()
                + ",\"status\":" + quote(c.getStatus())
                + ",\"createdAt\":" + quote(
                        c.getCreatedAt() == null
                                ? null : c.getCreatedAt().toString())
                + ",\"updatedAt\":" + quote(
                        c.getUpdatedAt() == null
                                ? null : c.getUpdatedAt().toString())
                + "}";
    }

    private void send(HttpServletResponse res, int status, String json)
            throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(json);
    }

    private void error(HttpServletResponse res, int status, String message)
            throws IOException {
        send(res, status,
                "{\"success\":false,\"message\":"
                        + quote(message) + "}");
    }

    private void handle(HttpServletResponse res, Exception e)
            throws IOException {

        if (e instanceof SecurityException) {
            error(res, 403, e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            error(res, 400, e.getMessage());
        } else if (e instanceof SQLException sql) {
            if ("23000".equals(sql.getSQLState())
                    || "23505".equals(sql.getSQLState())) {
                error(res, 409, "Du lieu trung hoac vi pham rang buoc");
            } else {
                error(res, 500, "Loi co so du lieu");
            }
        } else {
            error(res, 500, "Loi he thong");
        }
    }
}