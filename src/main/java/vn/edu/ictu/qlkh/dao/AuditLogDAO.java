package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.AuditLog;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public boolean logAction(
            String username,
            String actionType,
            String targetObject,
            String oldValue,
            String newValue,
            String details) {

        try (Connection conn = DBConnection.getConnection()) {

            logAction(
                    conn,
                    username,
                    actionType,
                    targetObject,
                    oldValue,
                    newValue,
                    details
            );

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logAction(
            String username,
            String actionType,
            String targetObject,
            String details) {

        return logAction(
                username,
                actionType,
                targetObject,
                null,
                null,
                details
        );
    }

    /**
     * Ghi audit bằng connection của transaction hiện tại.
     * Không commit/rollback tại đây.
     */
    public void logAction(
            Connection conn,
            String username,
            String actionType,
            String targetObject,
            String oldValue,
            String newValue,
            String details)
            throws SQLException {

        String sql = """
                INSERT INTO audit_logs
                (username, action_type, target_object,
                 old_value, new_value, details)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, actionType);
            stmt.setString(3, targetObject);
            stmt.setString(4, oldValue);
            stmt.setString(5, newValue);
            stmt.setString(6, details);

            if (stmt.executeUpdate() != 1) {
                throw new SQLException(
                        "Không thể ghi audit log."
                );
            }
        }
    }

    /**
     * Giữ tương thích với FE hiện tại.
     */
    public List<AuditLog> getAuditLogs(
            String keyword,
            String action,
            String startDate,
            String endDate)
            throws SQLException {

        return getAuditLogs(
                keyword,
                null,
                action,
                null,
                startDate,
                endDate
        );
    }

    /**
     * Bộ lọc đầy đủ cho S2-04:
     * - keyword
     * - username
     * - action
     * - targetObject
     * - khoảng thời gian
     */
    public List<AuditLog> getAuditLogs(
            String keyword,
            String username,
            String action,
            String targetObject,
            String startDate,
            String endDate)
            throws SQLException {

        List<AuditLog> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       username,
                       action_type,
                       target_object,
                       old_value,
                       new_value,
                       details,
                       action_time
                FROM audit_logs
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {

            sql.append("""
                     AND (
                         username LIKE ?
                         OR target_object LIKE ?
                         OR details LIKE ?
                     )
                    """);

            String value =
                    "%" + keyword.trim() + "%";

            params.add(value);
            params.add(value);
            params.add(value);
        }

        if (username != null && !username.isBlank()) {

            sql.append(" AND username LIKE ?");

            params.add(
                    "%" + username.trim() + "%"
            );
        }

        if (action != null && !action.isBlank()) {

            sql.append(" AND action_type = ?");

            params.add(action.trim());
        }

        if (targetObject != null
                && !targetObject.isBlank()) {

            sql.append(" AND target_object = ?");

            params.add(targetObject.trim());
        }

        if (startDate != null
                && !startDate.isBlank()) {

            params.add(
                    Timestamp.valueOf(
                            LocalDate
                                    .parse(startDate.trim())
                                    .atStartOfDay()
                    )
            );

            sql.append(" AND action_time >= ?");
        }

        if (endDate != null
                && !endDate.isBlank()) {

            params.add(
                    Timestamp.valueOf(
                            LocalDate
                                    .parse(endDate.trim())
                                    .plusDays(1)
                                    .atStartOfDay()
                    )
            );

            sql.append(" AND action_time < ?");
        }

        sql.append(
                " ORDER BY action_time DESC, id DESC"
        );

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(
                             sql.toString()
                     )) {

            for (int i = 0;
                 i < params.size();
                 i++) {

                stmt.setObject(
                        i + 1,
                        params.get(i)
                );
            }

            try (ResultSet rs =
                         stmt.executeQuery()) {

                while (rs.next()) {

                    AuditLog log =
                            new AuditLog();

                    log.setId(
                            rs.getLong("id")
                    );

                    log.setUsername(
                            rs.getString("username")
                    );

                    log.setActionType(
                            rs.getString(
                                    "action_type"
                            )
                    );

                    log.setTargetObject(
                            rs.getString(
                                    "target_object"
                            )
                    );

                    log.setOldValue(
                            rs.getString(
                                    "old_value"
                            )
                    );

                    log.setNewValue(
                            rs.getString(
                                    "new_value"
                            )
                    );

                    log.setDetails(
                            rs.getString(
                                    "details"
                            )
                    );

                    log.setActionTime(
                            rs.getTimestamp(
                                    "action_time"
                            )
                    );

                    list.add(log);
                }
            }
        }

        return list;
    }
}