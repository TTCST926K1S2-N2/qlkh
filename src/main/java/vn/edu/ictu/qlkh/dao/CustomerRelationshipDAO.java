package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.util.*;

public class CustomerRelationshipDAO {
    public record Link(long id, String name, Long parentId, String taxCode, String status) {}

    public List<Link> all() throws SQLException {
        String sql = "SELECT c.id,c.company_name,c.parent_id,c.tax_code,c.status FROM customers c " +
                "WHERE NOT EXISTS (SELECT 1 FROM customer_merges m WHERE m.source_customer_id=c.id) ORDER BY c.id";
        try (Connection con=DBConnection.getConnection();
             PreparedStatement ps=con.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {
            List<Link> links=new ArrayList<>();
            while(rs.next()) {
                long p=rs.getLong("parent_id");
                links.add(new Link(rs.getLong("id"),rs.getString("company_name"),rs.wasNull()?null:p,rs.getString("tax_code"),rs.getString("status")));
            }
            return links;
        }
    }

    public void setParent(long childId, Long parentId) throws SQLException {
        try (Connection con=DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Serializes relationship writes, preventing concurrent cycles.
                try (PreparedStatement ps=con.prepareStatement(
                        "SELECT id FROM customers ORDER BY id FOR UPDATE");
                     ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) { rs.getLong(1); }
                }
                Map<Long,Long> parents=new HashMap<>();
                try (PreparedStatement ps=con.prepareStatement(
                        "SELECT c.id,c.parent_id FROM customers c WHERE NOT EXISTS " +
                        "(SELECT 1 FROM customer_merges m WHERE m.source_customer_id=c.id)");
                     ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) {
                        long p=rs.getLong(2);
                        parents.put(rs.getLong(1),rs.wasNull()?null:p);
                    }
                }
                if (!parents.containsKey(childId) ||
                        (parentId!=null && !parents.containsKey(parentId))) {
                    throw new IllegalArgumentException("Cong ty khong ton tai hoac da sap nhap");
                }
                Set<Long> visited=new HashSet<>();
                Long current=parentId;
                while(current!=null) {
                    if(current==childId || !visited.add(current)) {
                        throw new IllegalArgumentException("Quan he vong khong hop le");
                    }
                    current=parents.get(current);
                }
                try (PreparedStatement ps=con.prepareStatement(
                        "UPDATE customers SET parent_id=? WHERE id=?")) {
                    if(parentId==null) ps.setNull(1,Types.BIGINT);
                    else ps.setLong(1,parentId);
                    ps.setLong(2,childId);
                    if(ps.executeUpdate()!=1) throw new SQLException("Khong the cap nhat cong ty");
                }
                con.commit();
            } catch(SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally { con.setAutoCommit(true); }
        }
    }
}
