package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    private static final String COLUMNS =
            "id, customer_id, full_name, job_title, email, phone, " +
            "decision_role, is_primary, created_at, updated_at";

    private Contact map(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setId(rs.getLong("id"));
        c.setCustomerId(rs.getLong("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setJobTitle(rs.getString("job_title"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setDecisionRole(rs.getString("decision_role"));
        c.setPrimary(rs.getBoolean("is_primary"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }

        if (updated != null) {
            c.setUpdatedAt(updated.toLocalDateTime());
        }

        return c;
    }

    public List<Contact> findByCustomer(long customerId)
            throws SQLException {

        String sql = "SELECT " + COLUMNS +
                " FROM contacts WHERE customer_id = ? " +
                "ORDER BY is_primary DESC, id ASC";

        List<Contact> contacts = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contacts.add(map(rs));
                }
            }
        }

        return contacts;
    }

    public Contact findById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS +
                " FROM contacts WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public long insert(Contact c) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (c.isPrimary()) {
                    lockCustomer(conn, c.getCustomerId());
                    clearPrimary(conn, c.getCustomerId());
                }

                String sql = """
                    INSERT INTO contacts
                    (customer_id, full_name, job_title, email,
                     phone, decision_role, is_primary)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

                long id;

                try (PreparedStatement ps = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    fill(ps, c);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Missing contact ID");
                        }
                        id = keys.getLong(1);
                    }
                }

                conn.commit();
                return id;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public boolean update(Contact c) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock both customer rows in stable order.
                Contact old = findByIdForUpdate(conn, c.getId());

                if (old == null) {
                    conn.rollback();
                    return false;
                }

                long oldCustomer = old.getCustomerId();
                long newCustomer = c.getCustomerId();

                if (oldCustomer != newCustomer) {
                    long first = Math.min(oldCustomer, newCustomer);
                    long second = Math.max(oldCustomer, newCustomer);
                    lockCustomer(conn, first);
                    lockCustomer(conn, second);
                } else {
                    lockCustomer(conn, newCustomer);
                }

                if (c.isPrimary()) {
                    clearPrimary(conn, newCustomer);
                }

                String sql = """
                    UPDATE contacts
                    SET customer_id = ?, full_name = ?, job_title = ?,
                        email = ?, phone = ?, decision_role = ?,
                        is_primary = ?
                    WHERE id = ?
                    """;

                int changed;

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    fill(ps, c);
                    ps.setLong(8, c.getId());
                    changed = ps.executeUpdate();
                }

                if (changed > 0 && oldCustomer != newCustomer) {
                    String historySql = """
                        INSERT INTO contact_customer_history
                        (contact_id, old_customer_id, new_customer_id)
                        VALUES (?, ?, ?)
                        """;

                    try (PreparedStatement history =
                                 conn.prepareStatement(historySql)) {
                        history.setLong(1, c.getId());
                        history.setLong(2, oldCustomer);
                        history.setLong(3, newCustomer);
                        history.executeUpdate();
                    }
                }

                conn.commit();
                return changed > 0;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM contacts WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void fill(PreparedStatement ps, Contact c)
            throws SQLException {

        ps.setLong(1, c.getCustomerId());
        ps.setString(2, c.getFullName());
        ps.setString(3, c.getJobTitle());
        ps.setString(4, c.getEmail());
        ps.setString(5, c.getPhone());
        ps.setString(6, c.getDecisionRole());
        ps.setBoolean(7, c.isPrimary());
    }

    private void clearPrimary(Connection conn, long customerId)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE contacts SET is_primary = FALSE " +
                "WHERE customer_id = ? AND is_primary = TRUE")) {

            ps.setLong(1, customerId);
            ps.executeUpdate();
        }
    }

    private void lockCustomer(Connection conn, long customerId)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM customers WHERE id = ? FOR UPDATE")) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Customer not found");
                }
            }
        }
    }

    private Contact findByIdForUpdate(Connection conn, long id)
            throws SQLException {

        String sql = "SELECT " + COLUMNS +
                " FROM contacts WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }
}