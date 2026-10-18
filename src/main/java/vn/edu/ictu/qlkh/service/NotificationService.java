package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.NotificationDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private final NotificationDAO dao = new NotificationDAO();

    public List<Map<String, Object>> latest(long userId) throws SQLException {
        return dao.findLatest(userId);
    }

    public long unreadCount(long userId) throws SQLException {
        return dao.unreadCount(userId);
    }

    public boolean markRead(long userId, long notificationId) throws SQLException {
        return dao.markRead(userId, notificationId);
    }

    public int markAllRead(long userId) throws SQLException {
        return dao.markAllRead(userId);
    }
}
