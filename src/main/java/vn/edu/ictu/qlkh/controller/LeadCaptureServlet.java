package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ictu.qlkh.service.LeadCaptureService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/v1/lead-capture")
public class LeadCaptureServlet extends HttpServlet {

    private static final int MAX_BODY_SIZE = 16384;
    private final LeadCaptureService service = new LeadCaptureService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String contentType = request.getContentType();

        if (contentType == null ||
                !contentType.toLowerCase(java.util.Locale.ROOT)
                        .split(";", 2)[0].trim()
                        .equals("application/x-www-form-urlencoded")) {
            send(response, 415,
                    "{\"success\":false,\"message\":\"Unsupported media type\"}");
            return;
        }

        if (request.getContentLengthLong() > MAX_BODY_SIZE) {
            send(response, 413,
                    "{\"success\":false,\"message\":\"Request too large\"}");
            return;
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int total = 0;
        int read;

        while ((read = request.getInputStream().read(chunk)) != -1) {
            total += read;

            if (total > MAX_BODY_SIZE) {
                send(response, 413,
                        "{\"success\":false,\"message\":\"Request too large\"}");
                return;
            }

            buffer.write(chunk, 0, read);
        }

        Map<String, String> params;

        try {
            params = parseForm(buffer.toString(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            send(response, 400,
                    "{\"success\":false,\"message\":\"Invalid form encoding\"}");
            return;
        }

        try {
            long id = service.submit(
                    params.get("fullName"),
                    params.get("email"),
                    params.get("phone"),
                    params.get("company"),
                    params.get("interest"),
                    params.get("sourceForm"),
                    params.get("website"),
                    request.getRemoteAddr()
            );

            send(response, 201,
                    "{\"success\":true,\"id\":" + id + "}");

        } catch (LeadCaptureService.ValidationException e) {
            send(response, e.getStatus(),
                    "{\"success\":false,\"message\":\"Invalid submission or rate limit\"}");

        } catch (SQLException e) {
            getServletContext().log("Lead capture database error", e);
            send(response, 500,
                    "{\"success\":false,\"message\":\"Server error\"}");
        }
    }

    private Map<String, String> parseForm(String body) {

        Map<String, String> params = new HashMap<>();

        if (body.isEmpty()) {
            return params;
        }

        for (String pair : body.split("&", -1)) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0], StandardCharsets.UTF_8);

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            if (params.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate field");
            }

            params.put(key, value);
        }

        return params;
    }

    private void send(HttpServletResponse response, int status, String json)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }
}