package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.dao.CustomerMergeDAO;
import vn.edu.ictu.qlkh.service.CustomerMergeService;
import vn.edu.ictu.qlkh.util.Customer360Json;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

@WebServlet("/api/v1/customers/merge")
public class CustomerMergeServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private CustomerMergeService service;

    @Override
    public void init() {
        service = new CustomerMergeService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession(false);

        if (session == null) {
            error(res, 401, "Chua dang nhap");
            return;
        }

        Object userIdValue = session.getAttribute("userId");
        Object roleValue = session.getAttribute("userRole");

        if (userIdValue == null || roleValue == null) {
            error(res, 401, "Chua dang nhap");
            return;
        }

        long actorId;

        try {
            actorId = Long.parseLong(userIdValue.toString());
        } catch (NumberFormatException ex) {
            error(res, 401, "Phien dang nhap khong hop le");
            return;
        }

        if (actorId <= 0) {
            error(res, 401, "Phien dang nhap khong hop le");
            return;
        }

        String role = roleValue.toString()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!Set.of("MANAGER", "ADMIN").contains(role)) {
            error(res, 403, "Khong co quyen gop khach hang");
            return;
        }

        // S3-04: Reject cross-origin state-changing requests.
        String origin = req.getHeader("Origin");
        String fetchSite = req.getHeader("Sec-Fetch-Site");

        String expectedOrigin =
                req.getScheme() + "://" + req.getServerName()
                + ((req.getServerPort() == 80
                        && "http".equals(req.getScheme()))
                    || (req.getServerPort() == 443
                        && "https".equals(req.getScheme()))
                    ? "" : ":" + req.getServerPort());

        if (origin != null && !expectedOrigin.equals(origin)) {
            error(res, 403, "Nguon yeu cau khong hop le");
            return;
        }

        if (fetchSite != null
                && !"same-origin".equalsIgnoreCase(fetchSite)) {
            error(res, 403, "Yeu cau khac nguon bi tu choi");
            return;
        }

        if (origin == null && fetchSite == null) {
            error(res, 403, "Khong xac minh duoc nguon yeu cau");
            return;
        }

        String contentType = req.getContentType();

        if (contentType == null
                || !contentType.toLowerCase(Locale.ROOT)
                        .startsWith("application/json")) {
            error(res, 415, "Content-Type phai la application/json");
            return;
        }

        try {
            JsonNode body = mapper.readTree(req.getInputStream());

            if (body == null || !body.isObject()
                    || body.size() != 2
                    || !body.has("sourceId")
                    || !body.has("targetId")) {
                throw new IllegalArgumentException(
                        "Can sourceId va targetId");
            }

            long sourceId = positiveId(body.get("sourceId"));
            long targetId = positiveId(body.get("targetId"));

            CustomerMergeDAO.MergeResult result = service.merge(
                    sourceId, targetId, actorId, role);

            send(res, 200, "{"
                    + "\"success\":true"
                    + ",\"sourceId\":" + result.sourceId()
                    + ",\"targetId\":" + result.targetId()
                    + ",\"contacts\":" + result.contacts()
                    + ",\"opportunities\":" + result.opportunities()
                    + ",\"attachments\":" + result.attachments()
                    + "}");

        } catch (IllegalArgumentException ex) {
            error(res, 400, ex.getMessage());
        } catch (SecurityException ex) {
            error(res, 403, "Khong co quyen gop khach hang");
        } catch (SQLException ex) {
            log("S3-04 merge database error", ex);
            error(res, 500, "Khong the gop khach hang");
        } catch (IOException ex) {
            error(res, 400, "JSON khong hop le");
        } catch (RuntimeException ex) {
            log("S3-04 merge error", ex);
            error(res, 500, "Loi he thong");
        }
    }

    private long positiveId(JsonNode value) {
        if (value == null || !value.isIntegralNumber()
                || !value.canConvertToLong()
                || value.longValue() <= 0) {
            throw new IllegalArgumentException(
                    "ID khach hang phai la so nguyen duong");
        }
        return value.longValue();
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