package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.util.*;

public class NotificationDAO {
    public List<Map<String, Object>> findLatest(long userId) throws SQLException {
        String sql = "SELECT id, title, content, type, target_url, is_read, created_at, read_at " +
                "FROM notifications WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT 30";
        List<Map<String, Object>> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getLong("id"));
                    item.put("title", rs.getString("title"));
                    item.put("content", rs.getString("content"));
                    item.put("type", rs.getString("type"));
                    item.put("targetUrl", rs.getString("target_url"));
                    item.put("read", rs.getBoolean("is_read"));
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp read = rs.getTimestamp("read_at");
                    item.put("createdAt", created == null ? null : created.toLocalDateTime().toString());
                    item.put("readAt", read == null ? null : read.toLocalDateTime().toString());
                    items.add(item);
                }
            }
        }
        return items;
    }

    public long unreadCount(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public boolean markRead(long userId, long notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1, read_at = COALESCE(read_at, CURRENT_TIMESTAMP) " +
                "WHERE id = ? AND user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int markAllRead(long userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1, read_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate();
        }
    }
}
