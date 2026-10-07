package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerMergeDAO {

    /**
     * Gộp source customer vào target customer.
     *
     * sourceId: customer bị gộp và xóa sau khi merge.
     * targetId: customer được giữ lại.
     *
     * Toàn bộ thao tác nằm trong một transaction.
     */
    public void merge(
            long sourceId,
            long targetId,
            String username,
            String oldValue,
            String newValue,
            String details)
            throws SQLException {

        if (sourceId <= 0 || targetId <= 0) {
            throw new IllegalArgumentException(
                    "ID khach hang khong hop le."
            );
        }

        if (sourceId == targetId) {
            throw new IllegalArgumentException(
                    "Khong the gop mot khach hang vao chinh no."
            );
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                /*
                 * Khóa hai customer theo thứ tự ID tăng dần
                 * để giảm nguy cơ deadlock.
                 */
                long firstId = Math.min(sourceId, targetId);
                long secondId = Math.max(sourceId, targetId);

                if (findCustomerForUpdate(conn, firstId) == false) {
                    throw new IllegalArgumentException(
                            "Khong tim thay khach hang."
                    );
                }

                if (findCustomerForUpdate(conn, secondId) == false) {
                    throw new IllegalArgumentException(
                            "Khong tim thay khach hang."
                    );
                }

                /*
                 * Chuyển toàn bộ contact từ source sang target.
                 */
                transferContacts(
                        conn,
                        sourceId,
                        targetId
                );

                /*
                 * Xóa customer nguồn.
                 */
                deleteCustomer(
                        conn,
                        sourceId
                );

                /*
                 * Ghi audit trong cùng transaction.
                 */
                AuditLogDAO auditLogDAO =
                        new AuditLogDAO();

                auditLogDAO.logAction(
                        conn,
                        username,
                        "CUSTOMER_MERGE",
                        "CUSTOMER:" + targetId,
                        oldValue,
                        newValue,
                        details
                );

                conn.commit();

            } catch (SQLException | RuntimeException ex) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }

                throw ex;

            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Khóa customer bằng SELECT ... FOR UPDATE.
     */
    private boolean findCustomerForUpdate(
            Connection conn,
            long customerId)
            throws SQLException {

        String sql =
                "SELECT id FROM customers " +
                "WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Chuyển contact từ source sang target.
     *
     * Đồng thời lưu lịch sử chuyển customer.
     */
    private void transferContacts(
            Connection conn,
            long sourceId,
            long targetId)
            throws SQLException {

        String historySql = """
                INSERT INTO contact_customer_history
                (contact_id, old_customer_id, new_customer_id)
                SELECT id, customer_id, ?
                FROM contacts
                WHERE customer_id = ?
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(historySql)) {

            ps.setLong(1, targetId);
            ps.setLong(2, sourceId);

            ps.executeUpdate();
        }

        /*
         * Không để contact cũ của source trở thành
         * primary contact của target, tránh trường hợp
         * target có nhiều primary contact.
         */
        String updateSql = """
                UPDATE contacts
                SET customer_id = ?,
                    is_primary = FALSE
                WHERE customer_id = ?
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(updateSql)) {

            ps.setLong(1, targetId);
            ps.setLong(2, sourceId);

            ps.executeUpdate();
        }
    }

    /**
     * Xóa customer nguồn.
     */
    private void deleteCustomer(
            Connection conn,
            long customerId)
            throws SQLException {

        String sql =
                "DELETE FROM customers WHERE id = ?";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            int changed = ps.executeUpdate();

            if (changed != 1) {
                throw new SQLException(
                        "Khong the xoa khach hang nguon."
                );
            }
        }
    }
}
