package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.AssignmentRule;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AssignmentRuleDAO {

    private AssignmentRule mapRow(ResultSet rs) throws SQLException {
        AssignmentRule rule = new AssignmentRule();
        rule.setId(rs.getLong("id"));
        rule.setName(rs.getString("rule_name"));
        rule.setConditionType(rs.getString("condition_type"));
        rule.setConditionValue(rs.getString("condition_value"));
        rule.setAssignToUserId(rs.getLong("assign_to_user_id"));
        rule.setPriority(rs.getInt("priority"));
        rule.setActive(rs.getBoolean("is_active"));
        rule.setAssignmentMethod(rs.getString("assignment_method"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rule.setCreatedAt(createdAt.toLocalDateTime());
        }

        return rule;
    }

    public List<AssignmentRule> getAllRules() {
        String sql = "SELECT * FROM lead_assignment_rules ORDER BY priority ASC, id ASC";
        List<AssignmentRule> rules = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rules.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load assignment rules", e);
        }

        return rules;
    }

    public List<AssignmentRule> getActiveRulesSortedByPriority() {
        String sql = "SELECT * FROM lead_assignment_rules " +
                     "WHERE is_active = TRUE ORDER BY priority ASC, id ASC";

        List<AssignmentRule> rules = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rules.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load active assignment rules", e);
        }

        return rules;
    }

    public boolean addRule(AssignmentRule rule) {
        String sql = "INSERT INTO lead_assignment_rules " +
                     "(rule_name, condition_type, condition_value, " +
                     "assign_to_user_id, priority, is_active, assignment_method) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, rule.getName());
            ps.setString(2, rule.getConditionType());
            ps.setString(3, rule.getConditionValue());
            ps.setLong(4, rule.getAssignToUserId());
            ps.setInt(5, rule.getPriority() == null ? 1 : rule.getPriority());
            ps.setBoolean(6, rule.getActive() == null || rule.getActive());
            ps.setString(7, rule.getAssignmentMethod() == null
                    ? "DIRECT" : rule.getAssignmentMethod());

            int affected = ps.executeUpdate();

            if (affected == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        rule.setId(keys.getLong(1));
                    }
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create assignment rule", e);
        }
    }

    public boolean updateRule(AssignmentRule rule) {
        String sql = "UPDATE lead_assignment_rules SET " +
                     "rule_name = ?, condition_type = ?, condition_value = ?, " +
                     "assign_to_user_id = ?, priority = ?, is_active = ?, " +
                     "assignment_method = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rule.getName());
            ps.setString(2, rule.getConditionType());
            ps.setString(3, rule.getConditionValue());
            ps.setLong(4, rule.getAssignToUserId());
            ps.setInt(5, rule.getPriority());
            ps.setBoolean(6, Boolean.TRUE.equals(rule.getActive()));
            ps.setString(7, rule.getAssignmentMethod());
            ps.setLong(8, rule.getId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot update assignment rule", e);
        }
    }

    public boolean deleteRule(long id) {
        String sql = "DELETE FROM lead_assignment_rules WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot delete assignment rule", e);
        }
    }
}