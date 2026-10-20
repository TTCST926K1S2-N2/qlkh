package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.AccountHandoverDAO;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HTQLKH-10:
 * Xử lý khóa/mở khóa tài khoản và ghi nhận bàn giao.
 *
 * Hiện tại hệ thống chưa có bảng khách hàng/cơ hội,
 * vì vậy service chỉ ghi nhận người tiếp nhận vào lịch sử.
 * Không tự tạo hoặc giả lập dữ liệu nghiệp vụ chưa tồn tại.
 */
public class AccountHandoverService {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";

    private final AccountHandoverDAO handoverDAO;

    public AccountHandoverService() {
        this(new AccountHandoverDAO());
    }

    AccountHandoverService(
            AccountHandoverDAO handoverDAO) {

        this.handoverDAO = handoverDAO;
    }

    /**
     * Thay đổi trạng thái tài khoản.
     *
     * LOCKED:
     * - bắt buộc có người tiếp nhận;
     * - người tiếp nhận phải khác tài khoản bị khóa;
     * - người tiếp nhận phải ACTIVE;
     * - cập nhật trạng thái và ghi log trong cùng transaction.
     *
     * ACTIVE:
     * - mở khóa tài khoản;
     * - không yêu cầu người tiếp nhận;
     * - ghi log trong cùng transaction.
     */
    public HandoverResult changeStatus(
            long sourceUserId,
            String requestedStatus,
            Long targetUserId,
            long performedBy)
            throws SQLException {

        String newStatus =
                normalizeStatus(requestedStatus);

        if (sourceUserId <= 0) {
            return HandoverResult.failed(
                    "Tài khoản cần xử lý không hợp lệ."
            );
        }

        if (performedBy <= 0) {
            return HandoverResult.failed(
                    "Không xác định được người thực hiện."
            );
        }

        if (newStatus == null) {
            return HandoverResult.failed(
                    "Trạng thái tài khoản không hợp lệ."
            );
        }

        if (STATUS_LOCKED.equals(newStatus)) {

            if (targetUserId == null
                    || targetUserId <= 0) {

                return HandoverResult.failed(
                        "Vui lòng chọn người tiếp nhận trước khi khóa tài khoản."
                );
            }

            if (sourceUserId == targetUserId) {

                return HandoverResult.failed(
                        "Người tiếp nhận phải khác tài khoản bị khóa."
                );
            }
        }

        try (Connection connection =
                     DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                String previousStatus =
                        handoverDAO.findUserStatusForUpdate(
                                connection,
                                sourceUserId
                        );

                if (previousStatus == null) {

                    connection.rollback();

                    return HandoverResult.failed(
                            "Không tìm thấy tài khoản cần xử lý."
                    );
                }

                if (newStatus.equalsIgnoreCase(
                        previousStatus)) {

                    connection.rollback();

                    if (STATUS_LOCKED.equals(newStatus)) {
                        return HandoverResult.failed(
                                "Tài khoản đã ở trạng thái khóa."
                        );
                    }

                    return HandoverResult.failed(
                            "Tài khoản đang ở trạng thái hoạt động."
                    );
                }

                if (STATUS_LOCKED.equals(newStatus)) {

                    boolean targetActive =
                            handoverDAO.isActiveUser(
                                    connection,
                                    targetUserId
                            );

                    if (!targetActive) {

                        connection.rollback();

                        return HandoverResult.failed(
                                "Người tiếp nhận không tồn tại hoặc không ở trạng thái hoạt động."
                        );
                    }
                }

                boolean updated =
                        handoverDAO.updateUserStatus(
                                connection,
                                sourceUserId,
                                newStatus
                        );

                if (!updated) {

                    connection.rollback();

                    return HandoverResult.failed(
                            "Không thể cập nhật trạng thái tài khoản."
                    );
                }

                String action;
                String note;

                if (STATUS_LOCKED.equals(newStatus)) {

                    action = "LOCK_AND_HANDOVER";

                    note =
                            "Khóa tài khoản và ghi nhận người tiếp nhận.";

                } else {

                    action = "UNLOCK";

                    note =
                            "Mở khóa tài khoản.";
                }

                handoverDAO.insertHandoverLog(
                        connection,
                        sourceUserId,
                        STATUS_LOCKED.equals(newStatus)
                                ? targetUserId
                                : null,
                        performedBy,
                        action,
                        previousStatus,
                        newStatus,
                        note
                );

                connection.commit();

                if (STATUS_LOCKED.equals(newStatus)) {

                    return HandoverResult.success(
                            true,
                            "Khóa tài khoản và ghi nhận bàn giao thành công."
                    );
                }

                return HandoverResult.success(
                        false,
                        "Mở khóa tài khoản thành công."
                );

            } catch (SQLException e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(
                            rollbackException
                    );
                }

                throw e;
            }
        }
    }

    private String normalizeStatus(
            String status) {

        if (status == null
                || status.isBlank()) {

            return null;
        }

        String normalized =
                status.trim().toUpperCase();

        if (!STATUS_ACTIVE.equals(normalized)
                && !STATUS_LOCKED.equals(normalized)) {

            return null;
        }

        return normalized;
    }

    /**
     * Kết quả xử lý khóa/mở khóa.
     *
     * revokeSessions = true khi vừa khóa thành công.
     * Servlet sẽ dùng giá trị này để thu hồi toàn bộ
     * phiên đăng nhập của tài khoản bị khóa sau commit.
     */
    public static final class HandoverResult {

        private final boolean success;
        private final boolean revokeSessions;
        private final String message;

        private HandoverResult(
                boolean success,
                boolean revokeSessions,
                String message) {

            this.success = success;
            this.revokeSessions =
                    revokeSessions;
            this.message = message;
        }

        public static HandoverResult success(
                boolean revokeSessions,
                String message) {

            return new HandoverResult(
                    true,
                    revokeSessions,
                    message
            );
        }

        public static HandoverResult failed(
                String message) {

            return new HandoverResult(
                    false,
                    false,
                    message
            );
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isRevokeSessions() {
            return revokeSessions;
        }

        public String getMessage() {
            return message;
        }
    }
}