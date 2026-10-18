package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.util.DBConnection;
import vn.edu.ictu.qlkh.util.Customer360Json;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

@WebServlet("/api/v1/customers/360/stages")
public class Customer360StagesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        res.setContentType("application/json;charset=UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || session.getAttribute("userRole") == null) {
            res.setStatus(401);
            res.getWriter().write("{\"message\":\"Chua dang nhap\"}");
            return;
        }

        String role = session.getAttribute("userRole")
                .toString().trim().toUpperCase(Locale.ROOT);

        if (!Set.of("SALES", "MANAGER", "ADMIN").contains(role)) {
            res.setStatus(403);
            res.getWriter().write("{\"message\":\"Khong co quyen\"}");
            return;
        }

        String sql = "SELECT id, name FROM stages "
                + "WHERE status = 'ACTIVE' ORDER BY stage_order ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder json = new StringBuilder("[");

            while (rs.next()) {
                if (json.length() > 1) {
                    json.append(",");
                }

                json.append("{\"id\":")
                    .append(rs.getLong("id"))
                    .append(",\"name\":")
                    .append(Customer360Json.quote(rs.getString("name")))
                    .append("}");
            }

            json.append("]");
            res.getWriter().write(json.toString());

        } catch (SQLException ex) {
            log("Cannot load opportunity stages", ex);
            res.setStatus(500);
            res.getWriter().write(
                    "{\"message\":\"Loi co so du lieu\"}");
        }
    }
}