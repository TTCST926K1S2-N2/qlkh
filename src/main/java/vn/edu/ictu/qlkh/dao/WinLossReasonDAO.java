package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.WinLossReason;
import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.util.*;

public class WinLossReasonDAO {
    public List<WinLossReason> findAll() throws SQLException {
        List<WinLossReason> list = new ArrayList<>();
        String sql = "SELECT id,name,code,type,status,description FROM win_loss_reasons ORDER BY id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public WinLossReason findById(long id) throws SQLException {
        String sql = "SELECT id,name,code,type,status,description FROM win_loss_reasons WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1,id);
            try (ResultSet rs=ps.executeQuery()) { return rs.next()?mapRow(rs):null; }
        }
    }

    public boolean exists(String code,long excludeId) throws SQLException {
        String sql="SELECT 1 FROM win_loss_reasons WHERE code=? AND id<>? LIMIT 1";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,code); ps.setLong(2,excludeId);
            try(ResultSet rs=ps.executeQuery()){return rs.next();}
        }
    }

    public void insert(WinLossReason x) throws SQLException {
        String sql="INSERT INTO win_loss_reasons(name,code,type,status,description) VALUES(?,?,?,?,?)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,x.getName()); ps.setString(2,x.getCode()); ps.setString(3,x.getType());
            ps.setString(4,x.getStatus()); ps.setString(5,x.getDescription()); ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())x.setId(rs.getLong(1));}
        }
    }

    public void update(WinLossReason x) throws SQLException {
        String sql="UPDATE win_loss_reasons SET name=?,code=?,type=?,status=?,description=? WHERE id=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,x.getName()); ps.setString(2,x.getCode()); ps.setString(3,x.getType());
            ps.setString(4,x.getStatus()); ps.setString(5,x.getDescription()); ps.setLong(6,x.getId()); ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM win_loss_reasons WHERE id=?")){
            ps.setLong(1,id); ps.executeUpdate();
        }
    }

    private WinLossReason mapRow(ResultSet rs)throws SQLException{
        WinLossReason x=new WinLossReason();
        x.setId(rs.getLong("id")); x.setName(rs.getString("name")); x.setCode(rs.getString("code"));
        x.setType(rs.getString("type")); x.setStatus(rs.getString("status")); x.setDescription(rs.getString("description"));
        return x;
    }
}
