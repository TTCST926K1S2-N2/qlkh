package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.service.Customer360Service;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

@WebServlet("/api/v1/customer-attachments/*")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class Customer360AttachmentsServlet extends HttpServlet {
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "docx", "xlsx");
    private final Customer360Service service = new Customer360Service();

    private record Route(long customerId, Long fileId) {}

    private Route route(HttpServletRequest request, boolean download) {
        String path = request.getPathInfo();
        String pattern = download ? "/[1-9][0-9]*/[1-9][0-9]*" : "/[1-9][0-9]*";
        if (path == null || !path.matches(pattern)) throw new IllegalArgumentException("Duong dan khong hop le");
        String[] parts = path.substring(1).split("/");
        try {
            return new Route(Long.parseLong(parts[0]), download ? Long.parseLong(parts[1]) : null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID khong hop le");
        }
    }

    private boolean authorize(HttpServletRequest req, HttpServletResponse res, long customerId) throws IOException, SQLException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null || session.getAttribute("userRole") == null) {
            res.sendError(401); return false;
        }
        long userId;
        try { userId = Long.parseLong(session.getAttribute("userId").toString()); }
        catch (NumberFormatException e) { res.sendError(401); return false; }
        String role = session.getAttribute("userRole").toString().trim().toUpperCase(Locale.ROOT);
        if (userId <= 0 || !Set.of("SALES", "MANAGER", "ADMIN").contains(role)) {
            res.sendError(403); return false;
        }
        try {
            if (service.get(customerId, userId, role) == null) { res.sendError(404); return false; }
        } catch (SecurityException e) { res.sendError(404); return false; }
        return true;
    }

    private Path storageRoot() throws IOException {
        String configured = System.getenv("QLKH_UPLOAD_DIR");
        Path root = (configured == null || configured.isBlank())
                ? Path.of(System.getProperty("user.home"), "qlkh-uploads", "customer-attachments")
                : Path.of(configured, "customer-attachments");
        root = root.toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root;
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        Path saved = null;
        try {
            Route route = route(req, false);
            if (!authorize(req, res, route.customerId())) return;
            Part part = req.getPart("file");
            if (part == null || part.getSize() <= 0 || part.getSize() > MAX_SIZE) {
                res.sendError(400, "Tep trong hoac qua 10 MB"); return;
            }
            String original = part.getSubmittedFileName();
            if (original == null || original.isBlank() || original.length() > 255
                    || original.contains("/") || original.contains("\\") || original.contains("..")
                    || original.chars().anyMatch(c -> c < 32)) {
                res.sendError(400, "Ten tep khong hop le"); return;
            }
            String ext = extension(original);
            if (!EXTENSIONS.contains(ext)) { res.sendError(400, "Loai tep khong duoc ho tro"); return; }
            // A file extension is not a guarantee of content safety; downloads are forced as attachments.
            Path root = storageRoot();
            saved = root.resolve(UUID.randomUUID() + "." + ext).normalize();
            if (!saved.startsWith(root)) throw new IOException("Duong dan tep khong hop le");
            try (var input = part.getInputStream()) {
                Files.copy(input, saved);
            }
            String sql = "INSERT INTO attachments (customer_id, file_name, storage_path, content_type, file_size, uploaded_by) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, route.customerId());
                ps.setString(2, original);
                ps.setString(3, saved.getFileName().toString());
                ps.setString(4, "application/octet-stream");
                ps.setLong(5, part.getSize());
                ps.setLong(6, Long.parseLong(req.getSession(false).getAttribute("userId").toString()));
                if (ps.executeUpdate() != 1) throw new SQLException("Khong luu duoc tep");
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Khong lay duoc ID tep");
                    res.setStatus(201);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"success\":true,\"id\":" + keys.getLong(1) + "}");
                }
            }
            saved = null;
        } catch (IllegalArgumentException e) {
            res.sendError(400, e.getMessage());
        } catch (IllegalStateException e) {
            res.sendError(413, "Tep vuot qua gioi han");
        } catch (SQLException e) {
            log("Attachment upload database error", e);
            res.sendError(500, "Loi co so du lieu");
        } finally {
            if (saved != null) Files.deleteIfExists(saved);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Route route = route(req, true);
            if (!authorize(req, res, route.customerId())) return;
            String sql = "SELECT file_name, storage_path FROM attachments WHERE id = ? AND customer_id = ?";
            try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, route.fileId());
                ps.setLong(2, route.customerId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { res.sendError(404); return; }
                    String name = rs.getString("file_name");
                    String storage = rs.getString("storage_path");
                    Path root = storageRoot();
                    Path file = root.resolve(storage).normalize();
                    if (!file.startsWith(root) || !Files.isRegularFile(file)) { res.sendError(404); return; }
                    String safeName = name.replaceAll("[\\r\\n\\\"]", "_");
                    String encoded = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
                    res.setContentType("application/octet-stream");
                    res.setHeader("X-Content-Type-Options", "nosniff");
                    res.setHeader("Cache-Control", "no-store");
                    res.setHeader("Content-Disposition", "attachment; filename=\"" + safeName + "\"; filename*=UTF-8''" + encoded);
                    res.setContentLengthLong(Files.size(file));
                    Files.copy(file, res.getOutputStream());
                }
            }
        } catch (IllegalArgumentException e) {
            res.sendError(400, e.getMessage());
        } catch (SQLException e) {
            log("Attachment download database error", e);
            res.sendError(500, "Loi co so du lieu");
        }
    }
}