package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;

/** Read-only aggregates; values never include cancelled contracts. */
public class ContractSummaryDAO {
    public record Summary(BigDecimal signed, BigDecimal unsigned, long signedCount, long unsignedCount) {}

    public Summary forCustomer(long customerId) throws SQLException {
        String sql = "SELECT " +
            "COALESCE(SUM(CASE WHEN status='SIGNED' THEN contract_value ELSE 0 END),0) signed_value," +
            "COALESCE(SUM(CASE WHEN status IN ('DRAFT','PENDING') THEN contract_value ELSE 0 END),0) unsigned_value," +
            "COUNT(CASE WHEN status='SIGNED' THEN 1 END) signed_count," +
            "COUNT(CASE WHEN status IN ('DRAFT','PENDING') THEN 1 END) unsigned_count " +
            "FROM contracts WHERE customer_id=?";
        try(Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setLong(1,customerId);
            try(ResultSet rs=ps.executeQuery()) { rs.next(); return new Summary(rs.getBigDecimal(1),rs.getBigDecimal(2),rs.getLong(3),rs.getLong(4)); }
        }
    }

    /** Recursive company hierarchy; includes root once and every descendant once. */
    public Summary forGroup(long customerId) throws SQLException {
        String sql = "WITH RECURSIVE family AS (" +
                "SELECT id, parent_id FROM customers WHERE id=? " +
                "UNION ALL SELECT c.id,c.parent_id FROM customers c JOIN family f ON c.parent_id=f.id" +
                ") SELECT COALESCE(SUM(CASE WHEN ct.status='SIGNED' THEN ct.contract_value ELSE 0 END),0)," +
                "COALESCE(SUM(CASE WHEN ct.status IN ('DRAFT','PENDING') THEN ct.contract_value ELSE 0 END),0)," +
                "COUNT(CASE WHEN ct.status='SIGNED' THEN 1 END)," +
                "COUNT(CASE WHEN ct.status IN ('DRAFT','PENDING') THEN 1 END) " +
                "FROM family f LEFT JOIN contracts ct ON ct.customer_id=f.id";
        try (Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setLong(1,customerId);
            try(ResultSet rs=ps.executeQuery()) {rs.next();return new Summary(rs.getBigDecimal(1),rs.getBigDecimal(2),rs.getLong(3),rs.getLong(4));}
        }
    }

}
