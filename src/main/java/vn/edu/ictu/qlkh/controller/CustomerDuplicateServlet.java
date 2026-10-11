package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.service.CustomerDuplicateService;
import vn.edu.ictu.qlkh.util.Customer360Json;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/api/v1/customers/duplicates")
public class CustomerDuplicateServlet extends HttpServlet {

    private CustomerDuplicateService service;

    @Override
    public void init() {
        service = new CustomerDuplicateService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession(false);

        if (session == null) {
            error(res, 401, "Chua dang nhap");
            return;
        }

        Object idValue = session.getAttribute("userId");
        Object roleValue = session.getAttribute("userRole");

        if (idValue == null || roleValue == null) {
            error(res, 401, "Chua dang nhap");
            return;
        }

        long userId;

        try {
            userId = Long.parseLong(idValue.toString());
        } catch (NumberFormatException ex) {
            error(res, 401, "Phien dang nhap khong hop le");
            return;
        }

        if (userId <= 0) {
            error(res, 401, "Phien dang nhap khong hop le");
            return;
        }

        String role = roleValue.toString()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!Set.of("MANAGER", "ADMIN").contains(role)) {
            error(res, 403, "Khong co quyen truy cap");
            return;
        }

        try {
            List<CustomerDuplicateService.DuplicatePair> pairs =
                    service.findDuplicates(userId, role);

            StringBuilder json = new StringBuilder();

            json.append("{\"success\":true,\"total\":")
                    .append(pairs.size())
                    .append(",\"data\":[");

            for (int i = 0; i < pairs.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }

                var pair = pairs.get(i);

                json.append("{\"first\":")
                        .append(customerJson(pair.first()))
                        .append(",\"second\":")
                        .append(customerJson(pair.second()))
                        .append(",\"score\":")
                        .append(pair.score())
                        .append(",\"reasons\":[");

                for (int j = 0; j < pair.reasons().size(); j++) {
                    if (j > 0) {
                        json.append(",");
                    }

                    json.append(Customer360Json.quote(
                            pair.reasons().get(j)));
                }

                json.append("]}");
            }

            json.append("]}");

            send(res, 200, json.toString());

        } catch (SQLException ex) {
            log("S3-04 duplicate detection database error", ex);
            error(res, 500, "Loi co so du lieu");
        } catch (RuntimeException ex) {
            log("S3-04 duplicate detection error", ex);
            error(res, 500, "Loi he thong");
        }
    }

    private String customerJson(Customer c) {
        return "{"
                + "\"id\":" + c.getId()
                + ",\"companyName\":"
                + Customer360Json.quote(c.getCompanyName())
                + ",\"taxCode\":"
                + Customer360Json.quote(c.getTaxCode())
                + ",\"website\":"
                + Customer360Json.quote(c.getWebsite())
                + ",\"industry\":"
                + Customer360Json.quote(c.getIndustry())
                + ",\"companySize\":"
                + Customer360Json.quote(c.getCompanySize())
                + ",\"address\":"
                + Customer360Json.quote(c.getAddress())
                + ",\"ownerId\":" + c.getOwnerId()
                + ",\"status\":"
                + Customer360Json.quote(c.getStatus())
                + "}";
    }

    private void error(
            HttpServletResponse res,
            int status,
            String message) throws IOException {

        send(res, status,
                "{\"success\":false,\"message\":"
                        + Customer360Json.quote(message) + "}");
    }

    private void send(
            HttpServletResponse res,
            int status,
            String json) throws IOException {

        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        res.getWriter().write(json);
    }
}