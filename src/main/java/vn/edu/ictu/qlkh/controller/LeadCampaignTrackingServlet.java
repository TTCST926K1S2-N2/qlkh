package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/v1/leads/tracking")
public class LeadCampaignTrackingServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String role = String.valueOf(session.getAttribute("userRole"))
                .trim().toUpperCase(Locale.ROOT);

        if (!Set.of("ADMIN", "MANAGER", "SALES").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String campaign = request.getParameter("campaign");
        String source = request.getParameter("source");

        StringBuilder sql = new StringBuilder(
                "SELECT full_name, phone, campaign_id, source, status " +
                "FROM leads WHERE 1=1"
        );

        List<Object> parameters = new ArrayList<>();

        if (campaign != null && !campaign.isBlank()) {
            long campaignId;
            try {
                campaignId = Long.parseLong(campaign.trim());
                if (campaignId <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Campaign ID khong hop le"
                );
                return;
            }

            sql.append(" AND campaign_id = ?");
            parameters.add(campaignId);
        }

        if (source != null && !source.isBlank()) {
            sql.append(" AND UPPER(source) = ?");
            parameters.add(source.trim().toUpperCase(Locale.ROOT));
        }

        sql.append(" ORDER BY id DESC LIMIT 1000");

        List<Map<String, Object>> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> lead = new LinkedHashMap<>();

                    lead.put("name", rs.getString("full_name"));
                    lead.put("phone", rs.getString("phone"));

                    Object campaignId = rs.getObject("campaign_id");
                    lead.put(
                            "campaign",
                            campaignId == null
                                    ? null
                                    : campaignId.toString()
                    );

                    lead.put("source", rs.getString("source"));
                    lead.put("status", rs.getString("status"));

                    items.add(lead);
                }
            }

            long totalLeads = 0;

            String countSql = sql.toString()
                    .replaceFirst(
                            "SELECT full_name, phone, campaign_id, source, status",
                            "SELECT COUNT(*)"
                    )
                    .replace(" ORDER BY id DESC LIMIT 1000", "");

            try (PreparedStatement countStatement =
                         connection.prepareStatement(countSql)) {

                for (int i = 0; i < parameters.size(); i++) {
                    countStatement.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet countResult = countStatement.executeQuery()) {
                    if (countResult.next()) {
                        totalLeads = countResult.getLong(1);
                    }
                }
            }

            List<String> campaigns = new ArrayList<>();
            List<String> sources = new ArrayList<>();

            try (PreparedStatement campaignStatement =
                         connection.prepareStatement(
                                 "SELECT DISTINCT campaign_id FROM leads " +
                                 "WHERE campaign_id IS NOT NULL ORDER BY campaign_id"
                         );
                 ResultSet campaignResult = campaignStatement.executeQuery()) {

                while (campaignResult.next()) {
                    campaigns.add(campaignResult.getString(1));
                }
            }

            try (PreparedStatement sourceStatement =
                         connection.prepareStatement(
                                 "SELECT DISTINCT source FROM leads " +
                                 "WHERE source IS NOT NULL ORDER BY source"
                         );
                 ResultSet sourceResult = sourceStatement.executeQuery()) {

                while (sourceResult.next()) {
                    sources.add(sourceResult.getString(1));
                }
            }

            long totalOpportunities = 0;
            java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;

            String opportunitySql =
                    "SELECT COUNT(*), " +
                    "COALESCE(SUM(CASE WHEN UPPER(status) = 'WON' " +
                    "THEN expected_value ELSE 0 END), 0) " +
                    "FROM opportunities WHERE 1=1";

            if (campaign != null && !campaign.isBlank()) {
                opportunitySql += " AND campaign_id = ?";
            }

            try (PreparedStatement opportunityStatement =
                         connection.prepareStatement(opportunitySql)) {

                if (campaign != null && !campaign.isBlank()) {
                    opportunityStatement.setLong(
                            1, Long.parseLong(campaign.trim()));
                }

                try (ResultSet opportunityResult =
                             opportunityStatement.executeQuery()) {
                    if (opportunityResult.next()) {
                        totalOpportunities = opportunityResult.getLong(1);
                        totalRevenue = opportunityResult.getBigDecimal(2);

                        if (totalRevenue == null) {
                            totalRevenue = java.math.BigDecimal.ZERO;
                        }
                    }
                }
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("totalLeads", totalLeads);
            summary.put("totalOpportunities", totalOpportunities);
            summary.put("totalRevenue", totalRevenue);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("summary", summary);
            result.put("items", items);
            result.put("campaigns", campaigns);
            result.put("sources", sources);

            mapper.writeValue(response.getWriter(), result);

        } catch (SQLException ex) {
            getServletContext().log("Lead tracking query failed", ex);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Khong the tai du lieu Lead"
            );
        }
    }
}
