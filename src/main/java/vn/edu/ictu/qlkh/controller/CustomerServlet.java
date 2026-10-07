package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.CustomerDuplicate;
import vn.edu.ictu.qlkh.service.CustomerDuplicateService;
import vn.edu.ictu.qlkh.service.CustomerMergeService;
import vn.edu.ictu.qlkh.service.CustomerService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;

@WebServlet("/api/v1/customers/*")
public class CustomerServlet extends HttpServlet {

    private CustomerService service;
    private CustomerDuplicateService duplicateService;
    private CustomerMergeService mergeService;

    @Override
    public void init() {
        service = new CustomerService();
        duplicateService = new CustomerDuplicateService();
        mergeService = new CustomerMergeService();
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
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try {
            Actor a = actor(req);
            String path = req.getPathInfo();

            /*
             * GET /api/v1/customers/{id}/duplicates
             */
            if (path != null &&
                    path.matches("/[1-9][0-9]*/duplicates")) {

                long customerId = Long.parseLong(
                        path.substring(1, path.length()
                                - "/duplicates".length()));

                List<CustomerDuplicate> duplicates =
                        duplicateService.findDuplicates(
                                customerId,
                                a.id(),
                                a.role());

                StringJoiner json =
                        new StringJoiner(",", "[", "]");

                for (CustomerDuplicate item : duplicates) {
                    json.add(duplicateToJson(item));
                }

                send(res, 200, json.toString());
                return;
            }

            /*
             * GET /api/v1/customers/{id}/duplicates/{otherId}
             *
             * API nay tra ve du lieu so sanh giua
             * customer nguon va customer trung.
             */
            if (path != null &&
                    path.matches(
                            "/[1-9][0-9]*/duplicates/[1-9][0-9]*")) {

                String[] parts = path.substring(1).split("/");

                long sourceId = Long.parseLong(parts[0]);
                long targetId = Long.parseLong(parts[2]);

                List<CustomerDuplicate> duplicates =
                        duplicateService.findDuplicates(
                                sourceId,
                                a.id(),
                                a.role());

                CustomerDuplicate matched = null;

                for (CustomerDuplicate item : duplicates) {
                    if (item.getCustomer() != null &&
                            item.getCustomer().getId() == targetId) {
                        matched = item;
                        break;
                    }
                }

                if (matched == null) {
                    error(
                            res,
                            404,
                            "Khong tim thay khach hang trung de so sanh"
                    );
                    return;
                }

                Customer source =
                        service.detail(
                                sourceId,
                                a.id(),
                                a.role());

                if (source == null) {
                    error(
                            res,
                            404,
                            "Khong tim thay khach hang nguon"
                    );
                    return;
                }

                send(
                        res,
                        200,
                        comparisonToJson(
                                source,
                                matched.getCustomer(),
                                matched.getMatchedFields()
                        )
                );
                return;
            }

            /*
             * CRUD GET hien tai.
             */
            if (isList(req)) {
                List<Customer> customers =
                        service.list(a.id(), a.role());

                StringJoiner json =
                        new StringJoiner(",", "[", "]");

                for (Customer c : customers) {
                    json.add(toJson(c));
                }

                send(res, 200, json.toString());
            } else {
                Customer c = service.detail(
                        pathId(req),
                        a.id(),
                        a.role());

                if (c == null) {
                    error(
                            res,
                            404,
                            "Khong tim thay khach hang");
                    return;
                }

                send(res, 200, toJson(c));
            }

        } catch (Exception e) {
            handle(res, e);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try {
            Actor a = actor(req);

            /*
             * POST /api/v1/customers/merge
             */
            if ("/merge".equals(req.getPathInfo())) {

                Map<String, String> values =
                        readSimpleJson(req);

                Set<String> allowed =
                        Set.of("sourceId", "targetId");

                for (String key : values.keySet()) {
                    if (!allowed.contains(key)) {
                        throw new IllegalArgumentException(
                                "Truong khong hop le: " + key);
                    }
                }

                if (!values.containsKey("sourceId") ||
                        !values.containsKey("targetId")) {

                    throw new IllegalArgumentException(
                            "Phai co sourceId va targetId");
                }

                long sourceId;
                long targetId;

                try {
                    sourceId = Long.parseLong(
                            values.get("sourceId"));

                    targetId = Long.parseLong(
                            values.get("targetId"));

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "sourceId va targetId phai la so nguyen");
                }

                mergeService.merge(
                        sourceId,
                        targetId,
                        a.id(),
                        a.role());

                send(
                        res,
                        200,
                        "{"
                                + "\"success\":true,"
                                + "\"sourceId\":" + sourceId + ","
                                + "\"targetId\":" + targetId + ","
                                + "\"message\":"
                                + quote(
                                "Gop khach hang thanh cong")
                                + "}"
                );

                return;
            }

            /*
             * CRUD POST hien tai.
             */
            if (!isList(req)) {
                error(
                        res,
                        404,
                        "Duong dan khong hop le");
                return;
            }

            Customer c = readCustomer(req);

            long id = service.create(
                    c,
                    a.id(),
                    a.role());

            Customer saved =
                    service.detail(
                            id,
                            a.id(),
                            a.role());

            if (saved == null) {
                error(
                        res,
                        500,
                        "Khong the doc khach hang vua tao");
                return;
            }

            send(
                    res,
                    201,
                    toJson(saved));

        } catch (Exception e) {
            handle(res, e);
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        try {
            Actor a = actor(req);

            long id = pathId(req);
            Customer c = readCustomer(req);

            if (!service.update(
                    id,
                    c,
                    a.id(),
                    a.role())) {

                error(
                        res,
                        404,
                        "Khong tim thay khach hang");
                return;
            }

            Customer updated =
                    service.detail(
                            id,
                            a.id(),
                            a.role());

            if (updated == null) {
                error(
                        res,
                        404,
                        "Khach hang khong con trong pham vi truy cap");
                return;
            }

            send(
                    res,
                    200,
                    toJson(updated));

        } catch (Exception e) {
            handle(res, e);
        }
    }

    private Customer readCustomer(
            HttpServletRequest req)
            throws IOException {

        String contentType =
                req.getContentType();

        if (contentType == null ||
                !contentType
                        .toLowerCase(Locale.ROOT)
                        .startsWith("application/json")) {

            throw new IllegalArgumentException(
                    "Content-Type phai la application/json");
        }

        req.setCharacterEncoding("UTF-8");

        String body =
                req.getReader()
                        .lines()
                        .reduce(
                                "",
                                (a, b) -> a + b + "\n");

        Map<String, String> values =
                parseJson(body);

        Set<String> allowed =
                Set.of(
                        "companyName",
                        "taxCode",
                        "industry",
                        "companySize",
                        "website",
                        "address",
                        "ownerId",
                        "status");

        for (String key : values.keySet()) {
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException(
                        "Truong khong hop le: " + key);
            }
        }

        Customer c = new Customer();

        c.setCompanyName(
                values.get("companyName"));

        c.setTaxCode(
                values.get("taxCode"));

        c.setIndustry(
                values.get("industry"));

        c.setCompanySize(
                values.get("companySize"));

        c.setWebsite(
                values.get("website"));

        c.setAddress(
                values.get("address"));

        c.setStatus(
                values.get("status"));

        String owner =
                values.get("ownerId");

        if (owner != null) {
            try {
                c.setOwnerId(
                        Long.parseLong(owner));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "ownerId phai la so nguyen");
            }
        }

        return c;
    }

    private Map<String, String> readSimpleJson(
            HttpServletRequest req)
            throws IOException {

        String contentType =
                req.getContentType();

        if (contentType == null ||
                !contentType
                        .toLowerCase(Locale.ROOT)
                        .startsWith("application/json")) {

            throw new IllegalArgumentException(
                    "Content-Type phai la application/json");
        }

        req.setCharacterEncoding("UTF-8");

        String body =
                req.getReader()
                        .lines()
                        .reduce(
                                "",
                                (a, b) -> a + b + "\n");

        return parseJson(body);
    }

    private Map<String, String> parseJson(
            String body) {

        if (body == null) {
            throw new IllegalArgumentException(
                    "JSON trong");
        }

        String s = body.trim();

        if (!s.startsWith("{") ||
                !s.endsWith("}")) {

            throw new IllegalArgumentException(
                    "JSON khong hop le");
        }

        s = s.substring(
                1,
                s.length() - 1);

        Map<String, String> result =
                new HashMap<>();

        Pattern p = Pattern.compile(
                "\\s*\"([A-Za-z][A-Za-z0-9]*)\"\\s*:\\s*" +
                "(\"(?:\\\\.|[^\"\\\\])*\"|null|-?[0-9]+)\\s*");

        int pos = 0;

        while (pos < s.length()) {

            Matcher m = p.matcher(s);

            m.region(
                    pos,
                    s.length());

            if (!m.lookingAt()) {

                if (s.substring(pos).isBlank()) {
                    break;
                }

                throw new IllegalArgumentException(
                        "JSON khong hop le");
            }

            String key = m.group(1);
            String raw = m.group(2);

            if (result.containsKey(key)) {
                throw new IllegalArgumentException(
                        "Trung truong: " + key);
            }

            String value =
                    "null".equals(raw)
                            ? null
                            : raw.startsWith("\"")
                            ? unescape(
                            raw.substring(
                                    1,
                                    raw.length() - 1))
                            : raw;

            result.put(key, value);

            pos = m.end();

            if (pos < s.length()) {

                if (s.charAt(pos) != ',') {
                    throw new IllegalArgumentException(
                            "JSON khong hop le");
                }

                pos++;

                if (s.substring(pos).isBlank()) {
                    throw new IllegalArgumentException(
                            "JSON du dau phay");
                }
            }
        }

        return result;
    }

    private String unescape(String s) {

        StringBuilder out =
                new StringBuilder();

        for (int i = 0;
             i < s.length();
             i++) {

            char c = s.charAt(i);

            if (c != '\\') {

                if (c < 0x20) {
                    throw new IllegalArgumentException(
                            "JSON khong hop le");
                }

                out.append(c);
                continue;
            }

            if (++i >= s.length()) {
                throw new IllegalArgumentException(
                        "JSON khong hop le");
            }

            char e = s.charAt(i);

            switch (e) {

                case '"' ->
                        out.append('"');

                case '\\' ->
                        out.append('\\');

                case '/' ->
                        out.append('/');

                case 'n' ->
                        out.append('\n');

                case 'r' ->
                        out.append('\r');

                case 't' ->
                        out.append('\t');

                case 'b' ->
                        out.append('\b');

                case 'f' ->
                        out.append('\f');

                case 'u' -> {

                    if (i + 4 >= s.length()) {
                        throw new IllegalArgumentException(
                                "Unicode sai");
                    }

                    try {
                        out.append(
                                (char) Integer.parseInt(
                                        s.substring(
                                                i + 1,
                                                i + 5),
                                        16));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                "Unicode sai");
                    }

                    i += 4;
                }

                default ->
                        throw new IllegalArgumentException(
                                "Ky tu escape khong hop le");
            }
        }

        return out.toString();
    }

    private String quote(String s) {

        if (s == null) {
            return "null";
        }

        StringBuilder b =
                new StringBuilder("\"");

        for (char c : s.toCharArray()) {

            switch (c) {

                case '"' ->
                        b.append("\\\"");

                case '\\' ->
                        b.append("\\\\");

                case '\n' ->
                        b.append("\\n");

                case '\r' ->
                        b.append("\\r");

                case '\t' ->
                        b.append("\\t");

                default -> {

                    if (c < 0x20) {
                        b.append(
                                String.format(
                                        "\\u%04x",
                                        (int) c));
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
                + ",\"companyName\":"
                + quote(c.getCompanyName())
                + ",\"taxCode\":"
                + quote(c.getTaxCode())
                + ",\"industry\":"
                + quote(c.getIndustry())
                + ",\"companySize\":"
                + quote(c.getCompanySize())
                + ",\"website\":"
                + quote(c.getWebsite())
                + ",\"address\":"
                + quote(c.getAddress())
                + ",\"ownerId\":"
                + c.getOwnerId()
                + ",\"status\":"
                + quote(c.getStatus())
                + ",\"createdAt\":"
                + quote(
                        c.getCreatedAt() == null
                                ? null
                                : c.getCreatedAt().toString())
                + ",\"updatedAt\":"
                + quote(
                        c.getUpdatedAt() == null
                                ? null
                                : c.getUpdatedAt().toString())
                + "}";
    }

    private String duplicateToJson(
            CustomerDuplicate duplicate) {

        Customer customer =
                duplicate.getCustomer();

        StringJoiner matched =
                new StringJoiner(",", "[", "]");

        for (String field :
                duplicate.getMatchedFields()) {

            matched.add(quote(field));
        }

        return "{"
                + "\"customer\":"
                + toJson(customer)
                + ",\"matchedFields\":"
                + matched
                + "}";
    }

    private String comparisonToJson(
            Customer source,
            Customer target,
            List<String> matchedFields) {

        StringJoiner matched =
                new StringJoiner(",", "[", "]");

        for (String field : matchedFields) {
            matched.add(quote(field));
        }

        return "{"
                + "\"source\":"
                + toJson(source)
                + ",\"target\":"
                + toJson(target)
                + ",\"matchedFields\":"
                + matched
                + "}";
    }

    private void send(
            HttpServletResponse res,
            int status,
            String json)
            throws IOException {

        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType(
                "application/json;charset=UTF-8");

        res.getWriter().write(json);
    }

    private void error(
            HttpServletResponse res,
            int status,
            String message)
            throws IOException {

        send(
                res,
                status,
                "{\"success\":false,\"message\":"
                        + quote(message)
                        + "}");
    }

    private void handle(
            HttpServletResponse res,
            Exception e)
            throws IOException {

        if (e instanceof SecurityException) {

            error(
                    res,
                    403,
                    e.getMessage());

        } else if (e instanceof IllegalArgumentException) {

            error(
                    res,
                    400,
                    e.getMessage());

        } else if (e instanceof SQLException sql) {

            if ("23000".equals(sql.getSQLState())
                    || "23505".equals(sql.getSQLState())) {

                error(
                        res,
                        409,
                        "Du lieu trung hoac vi pham rang buoc");

            } else {

                error(
                        res,
                        500,
                        "Loi co so du lieu");
            }

        } else {

            error(
                    res,
                    500,
                    "Loi he thong");
        }
    }
}
