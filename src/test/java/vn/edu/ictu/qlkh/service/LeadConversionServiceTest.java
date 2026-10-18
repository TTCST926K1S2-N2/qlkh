package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LeadConversionServiceTest {

    @Test
    void convertCreatesCustomerContactAndUpdatesLead()
            throws Exception {

        Connection conn = newConnection();

        stubLead(
                conn,
                10L,
                "NEW",
                7L,
                null,
                null
        );

        PreparedStatement findCustomers = mock(PreparedStatement.class);
        ResultSet customerRows = mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "SELECT DISTINCT customer_id"
                        )
                )
        )).thenReturn(findCustomers);

        when(findCustomers.executeQuery())
                .thenReturn(customerRows);

        when(customerRows.next())
                .thenReturn(false);

        PreparedStatement insertCustomer =
                mock(PreparedStatement.class);

        ResultSet customerKeys =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "INSERT INTO customers"
                        )
                ),
                eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(insertCustomer);

        when(insertCustomer.executeUpdate())
                .thenReturn(1);

        when(insertCustomer.getGeneratedKeys())
                .thenReturn(customerKeys);

        when(customerKeys.next())
                .thenReturn(true);

        when(customerKeys.getLong(1))
                .thenReturn(100L);

        PreparedStatement findContact =
                mock(PreparedStatement.class);

        ResultSet contactRows =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains("SELECT id")
                                && sql.contains("FROM contacts")
                                && sql.contains("ORDER BY id")
                )
        )).thenReturn(findContact);

        when(findContact.executeQuery())
                .thenReturn(contactRows);

        when(contactRows.next())
                .thenReturn(false);

        PreparedStatement hasContact =
                mock(PreparedStatement.class);

        ResultSet hasContactRows =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains("SELECT 1")
                                && sql.contains("FROM contacts")
                )
        )).thenReturn(hasContact);

        when(hasContact.executeQuery())
                .thenReturn(hasContactRows);

        when(hasContactRows.next())
                .thenReturn(false);

        PreparedStatement insertContact =
                mock(PreparedStatement.class);

        ResultSet contactKeys =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "INSERT INTO contacts"
                        )
                ),
                eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(insertContact);

        when(insertContact.executeUpdate())
                .thenReturn(1);

        when(insertContact.getGeneratedKeys())
                .thenReturn(contactKeys);

        when(contactKeys.next())
                .thenReturn(true);

        when(contactKeys.getLong(1))
                .thenReturn(200L);

        PreparedStatement updateLead =
                mock(PreparedStatement.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains("UPDATE leads")
                                && sql.contains(
                                "converted_customer_id"
                        )
                )
        )).thenReturn(updateLead);

        when(updateLead.executeUpdate())
                .thenReturn(1);

        LeadConversionService service =
                service(conn);

        LeadConversionService.ConversionResult result =
                service.convert(
                        10L,
                        7L,
                        "SALES"
                );

        assertTrue(result.success());
        assertEquals(
                "CONVERTED",
                result.code()
        );

        assertEquals(
                100L,
                result.customerId()
        );

        assertEquals(
                200L,
                result.contactId()
        );

        assertTrue(result.customerCreated());
        assertTrue(result.contactCreated());

        verify(conn).commit();
        verify(conn, never()).rollback();

        verify(updateLead).executeUpdate();
    }

    @Test
    void convertReusesExistingCustomerAndContact()
            throws Exception {

        Connection conn = newConnection();

        stubLead(
                conn,
                20L,
                "ASSIGNED",
                7L,
                null,
                null
        );

        PreparedStatement findCustomers =
                mock(PreparedStatement.class);

        ResultSet customerRows =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "SELECT DISTINCT customer_id"
                        )
                )
        )).thenReturn(findCustomers);

        when(findCustomers.executeQuery())
                .thenReturn(customerRows);

        when(customerRows.next())
                .thenReturn(true, false);

        when(customerRows.getLong("customer_id"))
                .thenReturn(300L);

        PreparedStatement customerOwner =
                mock(PreparedStatement.class);

        ResultSet ownerRows =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "SELECT owner_id"
                        )
                                && sql.contains(
                                "FROM customers"
                        )
                )
        )).thenReturn(customerOwner);

        when(customerOwner.executeQuery())
                .thenReturn(ownerRows);

        when(ownerRows.next())
                .thenReturn(true);

        when(ownerRows.getLong("owner_id"))
                .thenReturn(7L);

        PreparedStatement findContact =
                mock(PreparedStatement.class);

        ResultSet contactRows =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains("SELECT id")
                                && sql.contains("FROM contacts")
                                && sql.contains("ORDER BY id")
                )
        )).thenReturn(findContact);

        when(findContact.executeQuery())
                .thenReturn(contactRows);

        when(contactRows.next())
                .thenReturn(true);

        when(contactRows.getLong("id"))
                .thenReturn(400L);

        PreparedStatement updateLead =
                mock(PreparedStatement.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains("UPDATE leads")
                                && sql.contains(
                                "converted_customer_id"
                        )
                )
        )).thenReturn(updateLead);

        when(updateLead.executeUpdate())
                .thenReturn(1);

        LeadConversionService service =
                service(conn);

        LeadConversionService.ConversionResult result =
                service.convert(
                        20L,
                        7L,
                        "SALES"
                );

        assertTrue(result.success());

        assertEquals(
                300L,
                result.customerId()
        );

        assertEquals(
                400L,
                result.contactId()
        );

        assertFalse(result.customerCreated());
        assertFalse(result.contactCreated());

        verify(
                conn,
                never()
        ).prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "INSERT INTO customers"
                        )
                ),
                eq(Statement.RETURN_GENERATED_KEYS)
        );

        verify(conn).commit();
    }

    @Test
    void rejectLeadWithNonConvertibleStatus()
            throws Exception {

        Connection conn = newConnection();

        stubLead(
                conn,
                30L,
                "LOST",
                7L,
                null,
                null
        );

        LeadConversionService service =
                service(conn);

        LeadConversionService.ConversionResult result =
                service.convert(
                        30L,
                        7L,
                        "SALES"
                );

        assertFalse(result.success());

        assertEquals(
                "NOT_ELIGIBLE",
                result.code()
        );

        verify(conn).rollback();
        verify(conn, never()).commit();
    }

    @Test
    void rejectAlreadyConvertedLead()
            throws Exception {

        Connection conn = newConnection();

        stubLead(
                conn,
                40L,
                "CONVERTED",
                7L,
                500L,
                600L
        );

        LeadConversionService service =
                service(conn);

        LeadConversionService.ConversionResult result =
                service.convert(
                        40L,
                        7L,
                        "SALES"
                );

        assertFalse(result.success());

        assertEquals(
                "ALREADY_CONVERTED",
                result.code()
        );

        verify(conn).rollback();
        verify(conn, never()).commit();
    }

    @Test
    void rejectDuplicateConflictAcrossCustomers()
            throws Exception {

        Connection conn = newConnection();

        stubLead(
                conn,
                50L,
                "IN_PROGRESS",
                7L,
                null,
                null
        );

        PreparedStatement findCustomers =
                mock(PreparedStatement.class);

        ResultSet customerRows =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains(
                                "SELECT DISTINCT customer_id"
                        )
                )
        )).thenReturn(findCustomers);

        when(findCustomers.executeQuery())
                .thenReturn(customerRows);

        when(customerRows.next())
                .thenReturn(
                        true,
                        true,
                        false
                );

        when(customerRows.getLong("customer_id"))
                .thenReturn(
                        700L,
                        701L
                );

        LeadConversionService service =
                service(conn);

        LeadConversionService.ConversionResult result =
                service.convert(
                        50L,
                        7L,
                        "SALES"
                );

        assertFalse(result.success());

        assertEquals(
                "DUPLICATE_CONFLICT",
                result.code()
        );

        verify(conn).rollback();
        verify(conn, never()).commit();
    }

    private Connection newConnection()
            throws Exception {

        Connection conn =
                mock(Connection.class);

        when(conn.getAutoCommit())
                .thenReturn(true);

        return conn;
    }

    private LeadConversionService service(
            Connection conn
    ) {

        return new LeadConversionService(
                () -> conn
        );
    }

    private void stubLead(
            Connection conn,
            long id,
            String status,
            Long assignedTo,
            Long convertedCustomerId,
            Long convertedContactId
    ) throws Exception {

        PreparedStatement ps =
                mock(PreparedStatement.class);

        ResultSet rs =
                mock(ResultSet.class);

        when(conn.prepareStatement(
                argThat(sql ->
                        sql != null
                                && sql.contains("FROM leads")
                                && sql.contains("FOR UPDATE")
                )
        )).thenReturn(ps);

        when(ps.executeQuery())
                .thenReturn(rs);

        when(rs.next())
                .thenReturn(true);

        when(rs.getLong("id"))
                .thenReturn(id);

        when(rs.getString("full_name"))
                .thenReturn("Nguyen Van A");

        when(rs.getString("email"))
                .thenReturn("a@example.com");

        when(rs.getString("phone"))
                .thenReturn("0901234567");

        when(rs.getString("company"))
                .thenReturn("Cong ty ABC");

        when(rs.getString("job_title"))
                .thenReturn("Truong phong");

        when(rs.getString("tax_code"))
                .thenReturn(null);

        when(rs.getString("industry"))
                .thenReturn("Cong nghe");

        when(rs.getString("company_size"))
                .thenReturn("50-100");

        when(rs.getString("website"))
                .thenReturn("https://example.vn");

        when(rs.getString("address"))
                .thenReturn("Ha Noi");

        when(rs.getString("status"))
                .thenReturn(status);

        when(rs.getLong("assigned_to"))
                .thenReturn(
                        assignedTo == null
                                ? 0L
                                : assignedTo
                );

        when(rs.getLong(
                "converted_customer_id"
        )).thenReturn(
                convertedCustomerId == null
                        ? 0L
                        : convertedCustomerId
        );

        when(rs.getLong(
                "converted_contact_id"
        )).thenReturn(
                convertedContactId == null
                        ? 0L
                        : convertedContactId
        );

        when(rs.wasNull())
                .thenReturn(
                        assignedTo == null,
                        convertedCustomerId == null,
                        convertedContactId == null
                );
    }
}