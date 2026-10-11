package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOIntegrationTest {

    private final CustomerDAO dao = new CustomerDAO();

    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> groupIds = new ArrayList<>();
    private final List<Long> customerIds = new ArrayList<>();

    private long userA;
    private long userB;
    private long userC;

    private long customerA;
    private long customerB;
    private long customerC;

    private String suffix;

    @BeforeEach
    void setUp() throws Exception {
        suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12);

        try (Connection conn = DBConnection.getConnection()) {
            String url = conn.getMetaData().getURL();

            if (url == null || !url.contains("qlkh_s209_test")) {
                throw new IllegalStateException(
                        "Tu choi tao du lieu test. Database hien tai: " + url
                        + ". Chi duoc phep dung qlkh_s209_test."
                );
            }

            userA = insertUser(conn, "SALES", "A");
            userB = insertUser(conn, "SALES", "B");
            userC = insertUser(conn, "SALES", "C");

            long group1 = insertGroup(conn, "S307_G1_" + suffix);
            long group2 = insertGroup(conn, "S307_G2_" + suffix);

            insertUserGroup(conn, userA, group1);
            insertUserGroup(conn, userB, group1);
            insertUserGroup(conn, userC, group2);

            customerA = insertCustomer(
                    conn, "S307 Cong ty Alpha " + suffix,
                    "S307A" + suffix, "Cong nghe", "POTENTIAL", userA
            );

            customerB = insertCustomer(
                    conn, "S307 Cong ty Beta " + suffix,
                    "S307B" + suffix, "Thuong mai", "CUSTOMER", userB
            );

            customerC = insertCustomer(
                    conn, "S307 Cong ty Gamma " + suffix,
                    "S307C" + suffix, "Cong nghe", "INACTIVE", userC
            );

            insertContact(
                    conn, customerA, "Lien he Alpha " + suffix,
                    "0901" + suffix.substring(0, 6)
            );

            insertContact(
                    conn, customerB, "Lien he Beta " + suffix,
                    "0902" + suffix.substring(0, 6)
            );

            insertContact(
                    conn, customerC, "Lien he Gamma " + suffix,
                    "0903" + suffix.substring(0, 6)
            );
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (userIds.isEmpty() && groupIds.isEmpty()
                && customerIds.isEmpty()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String url = conn.getMetaData().getURL();

            if (url == null || !url.contains("qlkh_s209_test")) {
                throw new IllegalStateException(
                        "Tu choi don dep: ket noi khong phai qlkh_s209_test."
                );
            }

            conn.setAutoCommit(false);

            try {
                // Xoa lien he truoc khach hang de ton trong khoa ngoai.
                deleteByIds(
                        conn,
                        "DELETE FROM contacts WHERE customer_id = ?",
                        customerIds
                );

                deleteByIds(
                        conn,
                        "DELETE FROM customers WHERE id = ?",
                        customerIds
                );

                deleteByIds(
                        conn,
                        "DELETE FROM user_business_groups WHERE user_id = ?",
                        userIds
                );

                deleteByIds(
                        conn,
                        "DELETE FROM users WHERE id = ?",
                        userIds
                );

                deleteByIds(
                        conn,
                        "DELETE FROM business_groups WHERE id = ?",
                        groupIds
                );

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Test
    void customerTableExists() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM customers")) {

            assertTrue(rs.next());
            assertTrue(rs.getLong(1) >= 0);
        }
    }

    @Test
    void customerListQueryWorks() throws Exception {
        assertNotNull(dao.findVisible(1L, "ALL"));
        assertNotNull(dao.findVisible(1L, "MY"));
        assertNotNull(dao.findVisible(1L, "TEAM"));
    }

    @Test
    void customerDetailQueryWorks() throws Exception {
        assertNull(dao.findVisibleById(
                Long.MAX_VALUE, 1L, "ALL"));
    }

    @Test
    void taxCodeLookupWorks() throws Exception {
        assertFalse(dao.existsTaxCode(
                "S301_TEST_NONEXISTENT_987654321", null));
    }

    @Test
    void searchMyScopeReturnsOnlyOwnCustomers() throws Exception {
        CustomerDAO.SearchPageData result = search(
                userA, "MY", null, null, null, null,
                null, null, null, 1, 20
        );

        List<Long> ids = idsOf(result.getItems());

        assertTrue(ids.contains(customerA));
        assertFalse(ids.contains(customerB));
        assertFalse(ids.contains(customerC));
        assertEquals(1L, result.getTotalItems());
    }

    @Test
    void searchTeamScopeReturnsCustomersFromSameGroup()
            throws Exception {
        CustomerDAO.SearchPageData result = search(
                userA, "TEAM", null, null, null, null,
                null, null, null, 1, 20
        );

        List<Long> ids = idsOf(result.getItems());

        assertTrue(ids.contains(customerA));
        assertTrue(ids.contains(customerB));
        assertFalse(ids.contains(customerC));
        assertEquals(2L, result.getTotalItems());
    }

    @Test
    void searchAllScopeReturnsAllFixtureCustomers()
            throws Exception {
        CustomerDAO.SearchPageData result = search(
                userA, "ALL", null, null, null, null,
                null, null, null, 1, 100
        );

        List<Long> ids = idsOf(result.getItems());

        assertTrue(ids.contains(customerA));
        assertTrue(ids.contains(customerB));
        assertTrue(ids.contains(customerC));
        assertEquals(3L, result.getTotalItems());
    }

    @Test
    void searchFindsCustomerByContactPhone() throws Exception {
        String phone = "0902" + suffix.substring(0, 6);

        CustomerDAO.SearchPageData result = search(
                userA, "ALL", null, null, null, phone,
                null, null, null, 1, 20
        );

        assertEquals(1L, result.getTotalItems());
        assertEquals(
                List.of(customerB),
                idsOf(result.getItems())
        );
    }

    @Test
    void searchCombinesFiltersWithAnd() throws Exception {
        CustomerDAO.SearchPageData result = search(
                userA,
                "ALL",
                null,
                "Beta " + suffix,
                "S307B" + suffix,
                null,
                "Thuong mai",
                "CUSTOMER",
                userB,
                1,
                20
        );

        assertEquals(1L, result.getTotalItems());
        assertEquals(
                List.of(customerB),
                idsOf(result.getItems())
        );
    }

    @Test
    void searchPaginationReturnsCorrectItemsAndTotal()
            throws Exception {
        CustomerDAO.SearchPageData page1 = search(
                userA, "ALL", "Cong ty", null, null, null,
                null, null, null, 1, 2
        );

        CustomerDAO.SearchPageData page2 = search(
                userA, "ALL", "Cong ty", null, null, null,
                null, null, null, 2, 2
        );

        assertEquals(3L, page1.getTotalItems());
        assertEquals(3L, page2.getTotalItems());

        assertEquals(2, page1.getItems().size());
        assertEquals(1, page2.getItems().size());

        List<Long> combined = new ArrayList<>();
        combined.addAll(idsOf(page1.getItems()));
        combined.addAll(idsOf(page2.getItems()));

        assertEquals(3, combined.stream().distinct().count());
        assertTrue(combined.contains(customerA));
        assertTrue(combined.contains(customerB));
        assertTrue(combined.contains(customerC));
    }

    @Test
    void searchWithNoMatchingResultReturnsEmptyPage()
            throws Exception {
        CustomerDAO.SearchPageData result = search(
                userA, "ALL",
                "S307_NO_MATCH_" + suffix,
                null, null, null,
                null, null, null, 1, 20
        );

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotalItems());
    }

    private CustomerDAO.SearchPageData search(
            long userId,
            String scope,
            String keyword,
            String companyName,
            String taxCode,
            String phone,
            String industry,
            String status,
            Long ownerId,
            int page,
            int size) throws SQLException {

        return dao.searchVisible(
                userId, scope, keyword, companyName,
                taxCode, phone, industry, status,
                ownerId, page, size
        );
    }

    private List<Long> idsOf(List<Customer> customers) {
        return customers.stream()
                .map(Customer::getId)
                .collect(Collectors.toList());
    }

    private long insertUser(
            Connection conn, String role, String label)
            throws SQLException {

        String email = "s307_" + label + "_" + suffix + "@test.local";

        String sql = """
                INSERT INTO users
                    (email, password_hash, full_name, role, status)
                VALUES (?, ?, ?, ?, 'ACTIVE')
                """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, email);
            ps.setString(2, "integration-test-hash");
            ps.setString(3, "S307 Test " + label + " " + suffix);
            ps.setString(4, role);
            ps.executeUpdate();

            long id = generatedId(ps);
            userIds.add(id);
            return id;
        }
    }

    private long insertGroup(Connection conn, String name)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO business_groups (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.executeUpdate();

            long id = generatedId(ps);
            groupIds.add(id);
            return id;
        }
    }

    private void insertUserGroup(
            Connection conn, long userId, long groupId)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_business_groups (user_id, group_id) "
                        + "VALUES (?, ?)")) {

            ps.setLong(1, userId);
            ps.setLong(2, groupId);
            ps.executeUpdate();
        }
    }

    private long insertCustomer(
            Connection conn,
            String companyName,
            String taxCode,
            String industry,
            String status,
            long ownerId) throws SQLException {

        String sql = """
                INSERT INTO customers
                    (company_name, tax_code, industry, owner_id, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, companyName);
            ps.setString(2, taxCode);
            ps.setString(3, industry);
            ps.setLong(4, ownerId);
            ps.setString(5, status);
            ps.executeUpdate();

            long id = generatedId(ps);
            customerIds.add(id);
            return id;
        }
    }

    private void insertContact(
            Connection conn,
            long customerId,
            String fullName,
            String phone) throws SQLException {

        String sql = """
                INSERT INTO contacts (customer_id, full_name, phone)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setString(2, fullName);
            ps.setString(3, phone);
            ps.executeUpdate();
        }
    }

    private long generatedId(PreparedStatement ps)
            throws SQLException {

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) {
                throw new SQLException(
                        "Khong lay duoc ID cua ban ghi vua tao."
                );
            }
            return rs.getLong(1);
        }
    }

    private void deleteByIds(
            Connection conn,
            String sql,
            List<Long> ids) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long id : ids) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
    }
}
