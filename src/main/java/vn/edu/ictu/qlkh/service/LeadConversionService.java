package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * S4-08 - Chuyển đổi Lead thành Customer + Contact.
 *
 * Toàn bộ thao tác được thực hiện trên cùng một JDBC transaction:
 *
 * Lead -> Customer -> Contact -> cập nhật Lead.
 */
public class LeadConversionService {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("SALES", "MANAGER", "ADMIN");

    private static final Set<String> CONVERTIBLE_STATUSES =
            Set.of("NEW", "ASSIGNED", "IN_PROGRESS");

    @FunctionalInterface
    interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    private final ConnectionProvider connectionProvider;

    public LeadConversionService() {
        this(DBConnection::getConnection);
    }

    LeadConversionService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public ConversionResult convert(
            long leadId,
            long actorId,
            String rawRole
    ) throws SQLException {

        String role = normalizeRole(rawRole);

        if (leadId <= 0 || actorId <= 0) {
            return fail(
                    "INVALID_REQUEST",
                    "Lead hoặc người thực hiện không hợp lệ.",
                    leadId
            );
        }

        if (!ALLOWED_ROLES.contains(role)) {
            return fail(
                    "FORBIDDEN",
                    "Bạn không có quyền chuyển đổi Lead.",
                    leadId
            );
        }

        try (Connection conn = connectionProvider.getConnection()) {

            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                LeadRecord lead = findLeadForUpdate(conn, leadId);

                if (lead == null) {
                    conn.rollback();

                    return fail(
                            "NOT_FOUND",
                            "Không tìm thấy Lead.",
                            leadId
                    );
                }

                if ("CONVERTED".equalsIgnoreCase(lead.status())
                        || lead.convertedCustomerId() != null
                        || lead.convertedContactId() != null) {

                    conn.rollback();

                    return fail(
                            "ALREADY_CONVERTED",
                            "Lead đã được chuyển đổi trước đó.",
                            leadId
                    );
                }

                String status = normalizeStatus(lead.status());

                if (!CONVERTIBLE_STATUSES.contains(status)) {
                    conn.rollback();

                    return fail(
                            "NOT_ELIGIBLE",
                            "Trạng thái Lead hiện tại không cho phép chuyển đổi.",
                            leadId
                    );
                }

                if (isBlank(lead.fullName())) {
                    conn.rollback();

                    return fail(
                            "NOT_ELIGIBLE",
                            "Lead chưa có họ tên người liên hệ.",
                            leadId
                    );
                }

                if (isBlank(lead.company())) {
                    conn.rollback();

                    return fail(
                            "NOT_ELIGIBLE",
                            "Lead chưa có tên doanh nghiệp.",
                            leadId
                    );
                }

                if (isBlank(lead.email()) && isBlank(lead.phone())) {
                    conn.rollback();

                    return fail(
                            "NOT_ELIGIBLE",
                            "Lead phải có email hoặc số điện thoại.",
                            leadId
                    );
                }

                if (!canAccessLead(conn, role, actorId, lead.assignedTo())) {
                    conn.rollback();

                    return fail(
                            "FORBIDDEN",
                            "Bạn không có quyền chuyển đổi Lead này.",
                            leadId
                    );
                }

                ExistingCustomerMatch existing =
                        resolveExistingCustomer(conn, lead);

                if (existing.conflict()) {
                    conn.rollback();

                    return fail(
                            "DUPLICATE_CONFLICT",
                            "Thông tin Lead đang khớp với nhiều khách hàng khác nhau.",
                            leadId
                    );
                }

                Long customerId = existing.customerId();
                boolean customerCreated = false;

                if (customerId != null) {
                    Long customerOwnerId =
                            findCustomerOwnerId(conn, customerId);

                    if (customerOwnerId == null) {
                        conn.rollback();

                        return fail(
                                "CUSTOMER_NOT_FOUND",
                                "Khách hàng liên kết không còn tồn tại.",
                                leadId
                        );
                    }

                    if (!canAccessOwner(
                            conn,
                            role,
                            actorId,
                            customerOwnerId)) {

                        conn.rollback();

                        return fail(
                                "FORBIDDEN",
                                "Bạn không có quyền liên kết Lead với khách hàng hiện có.",
                                leadId
                        );
                    }

                } else {

                    long ownerId = lead.assignedTo() != null
                            ? lead.assignedTo()
                            : actorId;

                    customerId =
                            insertCustomer(conn, lead, ownerId);

                    customerCreated = true;
                }

                Long contactId =
                        findMatchingContact(conn, customerId, lead);

                boolean contactCreated = false;

                if (contactId == null) {

                    boolean makePrimary =
                            !customerHasContact(conn, customerId);

                    contactId =
                            insertContact(
                                    conn,
                                    customerId,
                                    lead,
                                    makePrimary
                            );

                    contactCreated = true;
                }

                int updated =
                        markLeadConverted(
                                conn,
                                leadId,
                                customerId,
                                contactId
                        );

                if (updated != 1) {
                    conn.rollback();

                    return fail(
                            "ALREADY_CONVERTED",
                            "Lead đã được xử lý bởi yêu cầu khác.",
                            leadId
                    );
                }

                conn.commit();

                return new ConversionResult(
                        true,
                        "CONVERTED",
                        customerCreated
                                ? "Chuyển đổi Lead thành khách hàng thành công."
                                : "Lead đã được liên kết với khách hàng hiện có.",
                        leadId,
                        customerId,
                        contactId,
                        customerCreated,
                        contactCreated
                );

            } catch (SQLException | RuntimeException ex) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }

                throw ex;

            } finally {
                try {
                    conn.setAutoCommit(oldAutoCommit);
                } catch (SQLException ignored) {
                    // Connection sẽ được đóng bởi try-with-resources.
                }
            }
        }
    }

    private LeadRecord findLeadForUpdate(
            Connection conn,
            long leadId
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    full_name,
                    email,
                    phone,
                    company,
                    job_title,
                    tax_code,
                    industry,
                    company_size,
                    website,
                    address,
                    status,
                    assigned_to,
                    converted_customer_id,
                    converted_contact_id
                FROM leads
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, leadId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return new LeadRecord(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("company"),
                        rs.getString("job_title"),
                        rs.getString("tax_code"),
                        rs.getString("industry"),
                        rs.getString("company_size"),
                        rs.getString("website"),
                        rs.getString("address"),
                        rs.getString("status"),
                        nullableLong(rs, "assigned_to"),
                        nullableLong(rs, "converted_customer_id"),
                        nullableLong(rs, "converted_contact_id")
                );
            }
        }
    }

    private boolean canAccessLead(
            Connection conn,
            String role,
            long actorId,
            Long assignedTo
    ) throws SQLException {

        if ("ADMIN".equals(role)) {
            return true;
        }

        if (assignedTo == null) {
            return false;
        }

        return canAccessOwner(
                conn,
                role,
                actorId,
                assignedTo
        );
    }

    private boolean canAccessOwner(
            Connection conn,
            String role,
            long actorId,
            long ownerId
    ) throws SQLException {

        if ("ADMIN".equals(role)) {
            return true;
        }

        if ("SALES".equals(role)) {
            return actorId == ownerId;
        }

        if (!"MANAGER".equals(role)) {
            return false;
        }

        if (actorId == ownerId) {
            return true;
        }

        String sql = """
                SELECT 1
                FROM user_business_groups me
                JOIN user_business_groups owner_group
                  ON owner_group.group_id = me.group_id
                WHERE me.user_id = ?
                  AND owner_group.user_id = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, actorId);
            ps.setLong(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private ExistingCustomerMatch resolveExistingCustomer(
            Connection conn,
            LeadRecord lead
    ) throws SQLException {

        Long taxCustomer =
                findCustomerByTaxCode(conn, lead.taxCode());

        Set<Long> contactCustomers =
                findCustomerIdsByContact(
                        conn,
                        lead.email(),
                        lead.phone()
                );

        if (taxCustomer != null) {

            if (!contactCustomers.isEmpty()
                    && (contactCustomers.size() > 1
                    || !contactCustomers.contains(taxCustomer))) {

                return new ExistingCustomerMatch(null, true);
            }

            return new ExistingCustomerMatch(
                    taxCustomer,
                    false
            );
        }

        if (contactCustomers.size() > 1) {
            return new ExistingCustomerMatch(null, true);
        }

        if (contactCustomers.size() == 1) {
            return new ExistingCustomerMatch(
                    contactCustomers.iterator().next(),
                    false
            );
        }

        return new ExistingCustomerMatch(null, false);
    }

    private Long findCustomerByTaxCode(
            Connection conn,
            String taxCode
    ) throws SQLException {

        String value = blankToNull(taxCode);

        if (value == null) {
            return null;
        }

        String sql = """
                SELECT id
                FROM customers
                WHERE tax_code = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getLong("id")
                        : null;
            }
        }
    }

    private Set<Long> findCustomerIdsByContact(
            Connection conn,
            String email,
            String phone
    ) throws SQLException {

        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);

        if (normalizedEmail.isEmpty()
                && normalizedPhone.isEmpty()) {
            return Set.of();
        }

        String sql = """
                SELECT DISTINCT customer_id
                FROM contacts
                WHERE
                    (? IS NOT NULL
                        AND LOWER(TRIM(email)) = ?)
                    OR
                    (? IS NOT NULL
                        AND REPLACE(
                            REPLACE(
                                REPLACE(
                                    REPLACE(
                                        REPLACE(
                                            REPLACE(phone, ' ', ''),
                                        '-', ''),
                                    '.', ''),
                                '(', ''),
                            ')', ''),
                        '+', '') = ?)
                LIMIT 2
                """;

        Set<Long> ids = new HashSet<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            setNullableString(
                    ps,
                    1,
                    emptyToNull(normalizedEmail)
            );

            setNullableString(
                    ps,
                    2,
                    emptyToNull(normalizedEmail)
            );

            setNullableString(
                    ps,
                    3,
                    emptyToNull(normalizedPhone)
            );

            setNullableString(
                    ps,
                    4,
                    emptyToNull(normalizedPhone)
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    ids.add(rs.getLong("customer_id"));
                }
            }
        }

        return ids;
    }

    private Long findCustomerOwnerId(
            Connection conn,
            long customerId
    ) throws SQLException {

        String sql = """
                SELECT owner_id
                FROM customers
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getLong("owner_id")
                        : null;
            }
        }
    }

    private long insertCustomer(
            Connection conn,
            LeadRecord lead,
            long ownerId
    ) throws SQLException {

        String sql = """
                INSERT INTO customers (
                    company_name,
                    tax_code,
                    industry,
                    company_size,
                    website,
                    address,
                    owner_id,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 'POTENTIAL')
                """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, lead.company());

            setNullableString(
                    ps,
                    2,
                    blankToNull(lead.taxCode())
            );

            setNullableString(
                    ps,
                    3,
                    blankToNull(lead.industry())
            );

            setNullableString(
                    ps,
                    4,
                    blankToNull(lead.companySize())
            );

            setNullableString(
                    ps,
                    5,
                    blankToNull(lead.website())
            );

            setNullableString(
                    ps,
                    6,
                    blankToNull(lead.address())
            );

            ps.setLong(7, ownerId);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Missing generated customer ID"
                    );
                }

                return keys.getLong(1);
            }
        }
    }

    private Long findMatchingContact(
            Connection conn,
            long customerId,
            LeadRecord lead
    ) throws SQLException {

        String normalizedEmail =
                normalizeEmail(lead.email());

        String normalizedPhone =
                normalizePhone(lead.phone());

        String sql = """
                SELECT id
                FROM contacts
                WHERE customer_id = ?
                  AND (
                        (? IS NOT NULL
                            AND LOWER(TRIM(email)) = ?)
                     OR (? IS NOT NULL
                            AND REPLACE(
                                REPLACE(
                                    REPLACE(
                                        REPLACE(
                                            REPLACE(
                                                REPLACE(phone, ' ', ''),
                                            '-', ''),
                                        '.', ''),
                                    '(', ''),
                                ')', ''),
                            '+', '') = ?)
                  )
                ORDER BY id
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            setNullableString(
                    ps,
                    2,
                    emptyToNull(normalizedEmail)
            );

            setNullableString(
                    ps,
                    3,
                    emptyToNull(normalizedEmail)
            );

            setNullableString(
                    ps,
                    4,
                    emptyToNull(normalizedPhone)
            );

            setNullableString(
                    ps,
                    5,
                    emptyToNull(normalizedPhone)
            );

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getLong("id")
                        : null;
            }
        }
    }

    private boolean customerHasContact(
            Connection conn,
            long customerId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM contacts
                WHERE customer_id = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private long insertContact(
            Connection conn,
            long customerId,
            LeadRecord lead,
            boolean primary
    ) throws SQLException {

        String sql = """
                INSERT INTO contacts (
                    customer_id,
                    full_name,
                    job_title,
                    email,
                    phone,
                    decision_role,
                    is_primary
                )
                VALUES (?, ?, ?, ?, ?, NULL, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, customerId);
            ps.setString(2, lead.fullName());

            setNullableString(
                    ps,
                    3,
                    blankToNull(lead.jobTitle())
            );

            setNullableString(
                    ps,
                    4,
                    blankToNull(lead.email())
            );

            setNullableString(
                    ps,
                    5,
                    blankToNull(lead.phone())
            );

            ps.setBoolean(6, primary);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Missing generated contact ID"
                    );
                }

                return keys.getLong(1);
            }
        }
    }

    private int markLeadConverted(
            Connection conn,
            long leadId,
            long customerId,
            long contactId
    ) throws SQLException {

        String sql = """
                UPDATE leads
                SET status = 'CONVERTED',
                    converted_customer_id = ?,
                    converted_contact_id = ?
                WHERE id = ?
                  AND status IN ('NEW', 'ASSIGNED', 'IN_PROGRESS')
                  AND converted_customer_id IS NULL
                  AND converted_contact_id IS NULL
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);
            ps.setLong(2, contactId);
            ps.setLong(3, leadId);

            return ps.executeUpdate();
        }
    }

    private Long nullableLong(
            ResultSet rs,
            String column
    ) throws SQLException {

        long value = rs.getLong(column);

        return rs.wasNull()
                ? null
                : value;
    }

    private void setNullableString(
            PreparedStatement ps,
            int index,
            String value
    ) throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private String normalizeRole(String role) {

        return role == null
                ? ""
                : role.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {

        return status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {

        return email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {

        return phone == null
                ? ""
                : phone.replaceAll("\\D", "");
    }

    private String blankToNull(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private String emptyToNull(String value) {

        return value == null || value.isEmpty()
                ? null
                : value;
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    private ConversionResult fail(
            String code,
            String message,
            long leadId
    ) {

        return new ConversionResult(
                false,
                code,
                message,
                leadId,
                null,
                null,
                false,
                false
        );
    }

    private record LeadRecord(
            long id,
            String fullName,
            String email,
            String phone,
            String company,
            String jobTitle,
            String taxCode,
            String industry,
            String companySize,
            String website,
            String address,
            String status,
            Long assignedTo,
            Long convertedCustomerId,
            Long convertedContactId
    ) {
    }

    private record ExistingCustomerMatch(
            Long customerId,
            boolean conflict
    ) {
    }

    public record ConversionResult(
            boolean success,
            String code,
            String message,
            long leadId,
            Long customerId,
            Long contactId,
            boolean customerCreated,
            boolean contactCreated
    ) {
    }
}