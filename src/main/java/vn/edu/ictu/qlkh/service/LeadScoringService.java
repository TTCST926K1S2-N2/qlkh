package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dto.LeadScoringConfig;
import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;

public class LeadScoringService {
    private static final ObjectMapper JSON = new ObjectMapper();

    private static Set<String> targetIndustries(LeadScoringConfig config) {
        if (config.targetIndustries == null) {
            return Set.of();
        }
        return new HashSet<>(config.targetIndustries);
    }

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
        String sql = """
            SELECT expected_value_weight,
                   completeness_weight,
                   open_opportunity_weight,
                   industry_weight,
                   company_size_weight,
                   source_weight,
                   interest_weight,
                   hot_threshold,
                   warm_threshold,
                   target_industries
            FROM lead_scoring_config
            WHERE id = 1
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return new LeadScoringConfig(50, 30, 20);
            }

            LeadScoringConfig config = new LeadScoringConfig(
                    rs.getDouble("expected_value_weight"),
                    rs.getDouble("completeness_weight"),
                    rs.getDouble("open_opportunity_weight")
            );

            config.industryWeight = rs.getDouble("industry_weight");
            config.companySizeWeight = rs.getDouble("company_size_weight");
            config.sourceWeight = rs.getDouble("source_weight");
            config.interestWeight = rs.getDouble("interest_weight");
            config.hotThreshold = rs.getDouble("hot_threshold");
            config.warmThreshold = rs.getDouble("warm_threshold");

            String industriesJson = rs.getString("target_industries");
            if (industriesJson != null && !industriesJson.isBlank()) {
                try {
                    config.targetIndustries = JSON.readValue(
                            industriesJson,
                            new TypeReference<List<String>>() {});
                } catch (JsonProcessingException e) {
                    throw new SQLException("Danh sach nganh nghe khong hop le", e);
                }
            }
            return config;
        }
    }
    public void saveConfig(LeadScoringConfig x, long userId) throws SQLException {
        validateLeadConfig(x);

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                try (PreparedStatement ps = connection.prepareStatement("""
                        UPDATE lead_scoring_config
                        SET industry_weight = ?,
                            company_size_weight = ?,
                            source_weight = ?,
                            interest_weight = ?,
                            hot_threshold = ?,
                            warm_threshold = ?,
                            target_industries = ?,
                            updated_by = ?
                        WHERE id = 1
                        """)) {

                    ps.setDouble(1, x.industryWeight);
                    ps.setDouble(2, x.companySizeWeight);
                    ps.setDouble(3, x.sourceWeight);
                    ps.setDouble(4, x.interestWeight);
                    ps.setDouble(5, x.hotThreshold);
                    ps.setDouble(6, x.warmThreshold);
                    try {
                        ps.setString(7, JSON.writeValueAsString(
                                x.targetIndustries == null
                                        ? List.of() : x.targetIndustries));
                    } catch (JsonProcessingException e) {
                        throw new SQLException("Khong the luu danh sach nganh nghe", e);
                    }
                    ps.setLong(8, userId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Khong tim thay cau hinh");
                    }
                }

                try (PreparedStatement select = connection.prepareStatement("""
                        SELECT id, industry, company_size, source, interest
                        FROM leads
                        WHERE status NOT IN ('MERGED', 'CONVERTED')
                        ORDER BY id
                        FOR UPDATE
                        """);
                     ResultSet rs = select.executeQuery();
                     PreparedStatement update = connection.prepareStatement("""
                        UPDATE leads SET score = ? WHERE id = ?
                        """)) {

                    while (rs.next()) {
                        double score = calculateLeadScore(
                                x,
                                LeadScoringRules.industryScore(
                                        rs.getString("industry"), targetIndustries(x)),
                                LeadScoringRules.companySizeScore(
                                        rs.getString("company_size")),
                                LeadScoringRules.sourceScore(
                                        rs.getString("source")),
                                LeadScoringRules.interestScore(
                                        rs.getString("interest")));

                        update.setDouble(1, score);
                        update.setLong(2, rs.getLong("id"));
                        update.addBatch();
                    }

                    update.executeBatch();
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
    private static void validateTargetIndustries(LeadScoringConfig x) {
        if (x.targetIndustries == null) {
            throw new IllegalArgumentException("Danh sach nganh nghe khong duoc null");
        }

        if (x.targetIndustries.size() > 100) {
            throw new IllegalArgumentException("Toi da 100 nganh nghe");
        }

        Set<String> unique = new HashSet<>();

        for (String industry : x.targetIndustries) {
            if (industry == null || industry.isBlank()) {
                throw new IllegalArgumentException("Nganh nghe khong duoc rong");
            }

            String normalized = industry.trim().toLowerCase(Locale.ROOT);

            if (industry.length() > 150) {
                throw new IllegalArgumentException("Ten nganh nghe toi da 150 ky tu");
            }

            if (!unique.add(normalized)) {
                throw new IllegalArgumentException("Nganh nghe bi trung lap");
            }
        }
    }
    public static void validateLeadConfig(LeadScoringConfig x) {
        if (x == null) {
            throw new IllegalArgumentException("Thieu cau hinh");
        }
        validateTargetIndustries(x);

        double[] weights = {
            x.industryWeight,
            x.companySizeWeight,
            x.sourceWeight,
            x.interestWeight
        };

        double total = 0;

        for (double weight : weights) {
            if (!Double.isFinite(weight) || weight < 0 || weight > 100) {
                throw new IllegalArgumentException("Trong so khong hop le");
            }
            total += weight;
        }

        if (Math.abs(total - 100.0) > 0.000001) {
            throw new IllegalArgumentException("Tong trong so phai bang 100");
        }

        if (!Double.isFinite(x.hotThreshold)
                || !Double.isFinite(x.warmThreshold)
                || x.warmThreshold < 0
                || x.hotThreshold > 100
                || x.warmThreshold >= x.hotThreshold) {
            throw new IllegalArgumentException("Nguong phan loai khong hop le");
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

        String ownerFilter = "SALES".equalsIgnoreCase(role)
                ? " AND l.assigned_to = ? " : "";

        String sql = """
            SELECT l.id, l.full_name, l.email, l.phone,
                   l.company, l.interest, l.industry,
                   l.company_size, l.source,
                   l.assigned_to, l.status
            FROM leads l
            WHERE l.status NOT IN ('MERGED', 'CONVERTED')
            """ + ownerFilter + """
            ORDER BY l.id DESC
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (!ownerFilter.isEmpty()) {
                ps.setLong(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double industryScore =
                            LeadScoringRules.industryScore(
                                    rs.getString("industry"), targetIndustries(config));

                    double companySizeScore =
                            LeadScoringRules.companySizeScore(
                                    rs.getString("company_size"));

                    double sourceScore =
                            LeadScoringRules.sourceScore(
                                    rs.getString("source"));

                    double interestScore =
                            LeadScoringRules.interestScore(
                                    rs.getString("interest"));

                    double score = calculateLeadScore(
                            config,
                            industryScore,
                            companySizeScore,
                            sourceScore,
                            interestScore);

                    Map<String, Object> lead = new LinkedHashMap<>();
                    lead.put("id", rs.getLong("id"));
                    lead.put("fullName", rs.getString("full_name"));
                    lead.put("email", rs.getString("email"));
                    lead.put("phone", rs.getString("phone"));
                    lead.put("company", rs.getString("company"));
                    lead.put("status", rs.getString("status"));
                    lead.put("score", score);
                    lead.put("classification", classifyLead(config, score));

                    result.add(lead);
                }
            }
        }

        return result;
    }
    /**
     * Calculates lead score using four configurable criteria.
     * Each criterion is normalized to a value between 0 and 100.
     */
    /**
     * Recalculate and persist a single lead score.
     * The update is performed in one database transaction.
     */
    public double recalculateLeadScore(long leadId) throws SQLException {
        if (leadId <= 0) {
            throw new IllegalArgumentException("Lead ID khong hop le");
        }

        LeadScoringConfig config = getConfig();

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                double score;

                try (PreparedStatement ps = connection.prepareStatement("""
                        SELECT industry, company_size, source, interest
                        FROM leads
                        WHERE id = ?
                        FOR UPDATE
                        """)) {

                    ps.setLong(1, leadId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Khong tim thay Lead");
                        }

                        score = calculateLeadScore(
                                config,
                                LeadScoringRules.industryScore(
                                        rs.getString("industry"), targetIndustries(config)),
                                LeadScoringRules.companySizeScore(
                                        rs.getString("company_size")),
                                LeadScoringRules.sourceScore(
                                        rs.getString("source")),
                                LeadScoringRules.interestScore(
                                        rs.getString("interest")));
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement("""
                        UPDATE leads
                        SET score = ?
                        WHERE id = ?
                        """)) {

                    ps.setDouble(1, score);
                    ps.setLong(2, leadId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Khong the cap nhat diem Lead");
                    }
                }

                connection.commit();
                return score;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
    /**
     * Recalculate scores for all active leads.
     * Intended for controlled use after scoring configuration changes.
     */
    public int recalculateAllLeadScores() throws SQLException {
        List<Long> leadIds = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                     SELECT id
                     FROM leads
                     WHERE status NOT IN ('MERGED', 'CONVERTED')
                     ORDER BY id
                     """);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                leadIds.add(rs.getLong("id"));
            }
        }

        int updated = 0;

        for (Long leadId : leadIds) {
            recalculateLeadScore(leadId);
            updated++;
        }

        return updated;
    }
    public static double calculateLeadScore(
            LeadScoringConfig config,
            double industryMatch,
            double companySizeMatch,
            double sourceMatch,
            double interestLevel) {

        if (config == null) {
            throw new IllegalArgumentException("Thieu cau hinh");
        }

        double[] weights = {
            config.industryWeight,
            config.companySizeWeight,
            config.sourceWeight,
            config.interestWeight
        };

        double total = 0;

        for (double weight : weights) {
            if (!Double.isFinite(weight) || weight < 0 || weight > 100) {
                throw new IllegalArgumentException("Trong so khong hop le");
            }
            total += weight;
        }

        if (Math.abs(total - 100.0) > 0.000001) {
            throw new IllegalArgumentException("Tong trong so phai bang 100");
        }

        double[] criteria = {
            industryMatch,
            companySizeMatch,
            sourceMatch,
            interestLevel
        };

        double score = 0;

        for (int i = 0; i < criteria.length; i++) {
            double value = criteria[i];

            if (!Double.isFinite(value) || value < 0 || value > 100) {
                throw new IllegalArgumentException(
                    "Muc do phu hop phai tu 0 den 100"
                );
            }

            score += value * weights[i] / 100.0;
        }

        return Math.round(score * 100.0) / 100.0;
    }

    public static String classifyLead(
            LeadScoringConfig config,
            double score) {

        if (config == null ||
                !Double.isFinite(score) ||
                score < 0 || score > 100 ||
                !Double.isFinite(config.hotThreshold) ||
                !Double.isFinite(config.warmThreshold) ||
                config.warmThreshold < 0 ||
                config.hotThreshold > 100 ||
                config.warmThreshold >= config.hotThreshold) {

            throw new IllegalArgumentException("Nguong phan loai khong hop le");
        }

        if (score >= config.hotThreshold) {
            return "HOT";
        }

        if (score >= config.warmThreshold) {
            return "WARM";
        }

        return "COLD";
    }
}
