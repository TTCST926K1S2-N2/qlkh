package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class ContractDAO {
    public record Contract(long id, String code, long customerId, String name,
                           BigDecimal value, String status, LocalDate signedDate) {}

    private Contract read(ResultSet rs) throws SQLException {
        java.sql.Date signed = rs.getDate("signed_date");
        return new Contract(rs.getLong("id"), rs.getString("contract_code"),
                rs.getLong("customer_id"), rs.getString("contract_name"),
                rs.getBigDecimal("contract_value"), rs.getString("status"),
                signed == null ? null : signed.toLocalDate());
    }
    public List<Contract> list(long customerId) throws SQLException {
        String sql = "SELECT * FROM contracts WHERE customer_id=? ORDER BY id DESC";
        List<Contract> list = new ArrayList<>();
        try(Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try(ResultSet rs=ps.executeQuery()) {while(rs.next()) list.add(read(rs));}
        }
        return list;
    }
    public Contract find(long id) throws SQLException {
        try(Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT * FROM contracts WHERE id=?")) {
            ps.setLong(1,id);
            try(ResultSet rs=ps.executeQuery()) {return rs.next()?read(rs):null;}
        }
    }
    public long create(Contract c) throws SQLException {
        String sql="INSERT INTO contracts(contract_code,customer_id,contract_name,contract_value,status,signed_date) VALUES(?,?,?,?,?,?)";
        try(Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            bind(ps,c,false);ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()) {if(rs.next())return rs.getLong(1);}
        }
        throw new SQLException("Missing generated ID");
    }
    public boolean update(Contract c) throws SQLException {
        String sql="UPDATE contracts SET contract_code=?,customer_id=?,contract_name=?,contract_value=?,status=?,signed_date=? WHERE id=?";
        try(Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) {
            bind(ps,c,false);ps.setLong(7,c.id());return ps.executeUpdate()>0;
        }
    }
    private void bind(PreparedStatement ps,Contract c,boolean unused) throws SQLException {
        ps.setString(1,c.code());ps.setLong(2,c.customerId());ps.setString(3,c.name());
        ps.setBigDecimal(4,c.value());ps.setString(5,c.status());
        if(c.signedDate()==null)ps.setNull(6,Types.DATE);else ps.setDate(6,java.sql.Date.valueOf(c.signedDate()));
    }
}
