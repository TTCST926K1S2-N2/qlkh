package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dto.LeadScoringConfig;
import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.util.*;

public class LeadScoringService {
    public static void validate(LeadScoringConfig x) {
        if (x == null) throw new IllegalArgumentException("Thieu cau hinh");
        double[] values = {x.expectedValueWeight, x.completenessWeight, x.openOpportunityWeight};
        double sum = 0;
        for (double v : values) {
            if (!Double.isFinite(v) || v < 0 || v > 100)
                throw new IllegalArgumentException("Trong so phai tu 0 den 100");
            sum += v;
        }
        if (Math.abs(sum - 100.0) > 0.000001)
            throw new IllegalArgumentException("Tong trong so phai bang 100");
    }

    public LeadScoringConfig getConfig() throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("""
                 SELECT expected_value_weight, completeness_weight, open_opportunity_weight
                 FROM lead_scoring_config WHERE id=1
                 """);
             ResultSet r = p.executeQuery()) {
            if (!r.next()) return new LeadScoringConfig(50, 30, 20);
            return new LeadScoringConfig(r.getDouble(1), r.getDouble(2), r.getDouble(3));
        }
    }

    public void saveConfig(LeadScoringConfig x, long userId) throws SQLException {
        validate(x);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("""
                 UPDATE lead_scoring_config SET expected_value_weight=?,
                 completeness_weight=?, open_opportunity_weight=?, updated_by=? WHERE id=1
                 """)) {
            p.setDouble(1, x.expectedValueWeight);
            p.setDouble(2, x.completenessWeight);
            p.setDouble(3, x.openOpportunityWeight);
            p.setLong(4, userId);
            if (p.executeUpdate() == 0) {
                try (PreparedStatement i = c.prepareStatement("""
                    INSERT INTO lead_scoring_config
                    (id, expected_value_weight, completeness_weight, open_opportunity_weight, updated_by)
                    VALUES (1, ?, ?, ?, ?)
                    """)) {
                    i.setDouble(1, x.expectedValueWeight);
                    i.setDouble(2, x.completenessWeight);
                    i.setDouble(3, x.openOpportunityWeight);
                    i.setLong(4, userId);
                    i.executeUpdate();
                }
            }
        }
    }

    public static double calculateScore(LeadScoringConfig x, double expectedValue,
                                        int completedFields, boolean hasOpenOpportunity) {
        validate(x);
        double valueScore = Math.min(100, Math.max(0, expectedValue / 10000.0));
        double profileScore = Math.min(5, Math.max(0, completedFields)) * 20.0;
        double opportunityScore = hasOpenOpportunity ? 100.0 : 0.0;
        double weightedScore = (valueScore * x.expectedValueWeight
                + profileScore * x.completenessWeight
                + opportunityScore * x.openOpportunityWeight) / 100.0;
        return Math.round(weightedScore * 100.0) / 100.0;
    }

    public List<Map<String, Object>> getLeadScores(long userId, String role) throws SQLException {
        LeadScoringConfig config = getConfig();
        List<Map<String, Object>> result = new ArrayList<>();
        String ownerFilter = "SALES".equals(role) ? " AND c.owner_id = ? " : "";
        String sql = """
            SELECT c.id, c.company_name, c.owner_id, c.tax_code, c.industry, c.website, c.address,
                   COALESCE(SUM(CASE WHEN o.status='OPEN'
                       THEN COALESCE(o.expected_value,0) ELSE 0 END),0) AS total_value,
                   COALESCE(SUM(CASE WHEN o.status='OPEN' THEN 1 ELSE 0 END),0) AS open_count
            FROM customers c
            LEFT JOIN opportunities o ON o.customer_id=c.id
            WHERE c.status='POTENTIAL'
            """ + ownerFilter + """
            GROUP BY c.id, c.company_name, c.owner_id, c.tax_code, c.industry, c.website, c.address
            ORDER BY c.id
            """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            if (!ownerFilter.isEmpty()) p.setLong(1, userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    double value = r.getDouble("total_value");
                    int count = 0;
                    for (String field : List.of("company_name", "tax_code", "industry", "website", "address")) {
                        String v = r.getString(field);
                        if (v != null && !v.isBlank()) count++;
                    }
                    boolean hasOpen = r.getInt("open_count") > 0;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("customerId", r.getLong("id"));
                    item.put("companyName", r.getString("company_name"));
                    item.put("ownerId", r.getLong("owner_id"));
                    item.put("expectedValue", value);
                    item.put("openOpportunityCount", r.getInt("open_count"));
                    item.put("score", calculateScore(config, value, count, hasOpen));
                    result.add(item);
                }
            }
        }
        return result;
    }
}
