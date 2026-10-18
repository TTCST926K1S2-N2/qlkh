package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/v1/campaigns")
public class CampaignServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    private String role(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        return String.valueOf(session.getAttribute("userRole"))
                .trim().toUpperCase(Locale.ROOT);
    }

    private boolean authorize(HttpServletRequest request,
                              HttpServletResponse response,
                              boolean write) throws IOException {
        String role = role(request);
        if (role == null) {
            response.sendError(401);
            return false;
        }
        if (!Set.of("ADMIN", "MANAGER", "SALES").contains(role)
                || (write && !Set.of("ADMIN", "MANAGER").contains(role))) {
            response.sendError(403);
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (!authorize(request, response, false)) return;

        response.setContentType("application/json;charset=UTF-8");

        String sql = """
                SELECT c.id, c.name, c.budget, c.start_date,
                       c.end_date, c.channel,
                       (SELECT COUNT(*) FROM leads l
                        WHERE l.campaign_id = c.id) AS total_leads,
                       (SELECT COUNT(*) FROM opportunities o
                        WHERE o.campaign_id = c.id) AS total_opportunities,
                       (SELECT COALESCE(SUM(o.expected_value), 0)
                        FROM opportunities o
                        WHERE o.campaign_id = c.id
                          AND UPPER(o.status) = 'WON') AS won_value
                FROM campaigns c
                ORDER BY c.id DESC
                """;

        List<Map<String, Object>> campaigns = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", rs.getLong("id"));
                item.put("name", rs.getString("name"));
                item.put("budget", rs.getBigDecimal("budget"));
                item.put("startDate", rs.getString("start_date"));
                item.put("endDate", rs.getString("end_date"));
                item.put("channel", rs.getString("channel"));
                item.put("totalLeads", rs.getLong("total_leads"));
                item.put("totalOpportunities",
                        rs.getLong("total_opportunities"));
                item.put("wonValue", rs.getBigDecimal("won_value"));
                campaigns.add(item);
            }

            mapper.writeValue(response.getWriter(), campaigns);

        } catch (SQLException ex) {
            getServletContext().log("Campaign query failed", ex);
            response.sendError(500, "Khong the tai chien dich");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        if (!authorize(request, response, true)) return;

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String name = request.getParameter("name");
        String channel = request.getParameter("channel");

        if (name == null || name.isBlank() || name.length() > 255
                || channel == null || channel.isBlank()
                || channel.length() > 100) {
            response.sendError(400, "Ten hoac kenh khong hop le");
            return;
        }

        BigDecimal budget;
        LocalDate startDate;
        LocalDate endDate;

        try {
            budget = new BigDecimal(request.getParameter("budget"));
            startDate = LocalDate.parse(request.getParameter("startDate"));
            endDate = LocalDate.parse(request.getParameter("endDate"));

            if (budget.signum() < 0 || budget.scale() > 2
                    || budget.precision() > 18
                    || endDate.isBefore(startDate)) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException ex) {
            response.sendError(400, "Ngan sach hoac ngay khong hop le");
            return;
        }

        String sql = """
                INSERT INTO campaigns
                    (name, budget, start_date, end_date, channel)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setBigDecimal(2, budget);
            ps.setDate(3, Date.valueOf(startDate));
            ps.setDate(4, Date.valueOf(endDate));
            ps.setString(5, channel.trim());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    response.sendError(500);
                    return;
                }
                response.setStatus(201);
                mapper.writeValue(response.getWriter(),
                        Map.of("id", keys.getLong(1),
                               "message", "Tao chien dich thanh cong"));
            }

        } catch (SQLException ex) {
            getServletContext().log("Campaign creation failed", ex);
            response.sendError(500, "Khong the tao chien dich");
        }
    }
}