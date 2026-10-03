package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Competitor;
import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.util.*;

public class CompetitorDAO {
    public List<Competitor> findAll() throws SQLException {
        List<Competitor> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement("SELECT id,name,code,status,description FROM competitors ORDER BY id");
            ResultSet rs=ps.executeQuery()){
            while(rs.next())list.add(mapRow(rs));
        }
        return list;
    }

    public Competitor findById(long id)throws SQLException{
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement("SELECT id,name,code,status,description FROM competitors WHERE id=?")){
            ps.setLong(1,id);
            try(ResultSet rs=ps.executeQuery()){return rs.next()?mapRow(rs):null;}
        }
    }

    public boolean exists(String code,long excludeId)throws SQLException{
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement("SELECT 1 FROM competitors WHERE code=? AND id<>? LIMIT 1")){
            ps.setString(1,code);ps.setLong(2,excludeId);
            try(ResultSet rs=ps.executeQuery()){return rs.next();}
        }
    }

    public void insert(Competitor x)throws SQLException{
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement("INSERT INTO competitors(name,code,status,description) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,x.getName());ps.setString(2,x.getCode());ps.setString(3,x.getStatus());ps.setString(4,x.getDescription());ps.executeUpdate();
            try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())x.setId(rs.getLong(1));}
        }
    }

    public void update(Competitor x)throws SQLException{
        try(Connection c=DBConnection.getConnection();
            PreparedStatement ps=c.prepareStatement("UPDATE competitors SET name=?,code=?,status=?,description=? WHERE id=?")){
            ps.setString(1,x.getName());ps.setString(2,x.getCode());ps.setString(3,x.getStatus());ps.setString(4,x.getDescription());ps.setLong(5,x.getId());ps.executeUpdate();
        }
    }

    public void delete(long id)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM competitors WHERE id=?")){
            ps.setLong(1,id);ps.executeUpdate();
        }
    }

    private Competitor mapRow(ResultSet rs)throws SQLException{
        Competitor x=new Competitor();
        x.setId(rs.getLong("id"));x.setName(rs.getString("name"));x.setCode(rs.getString("code"));
        x.setStatus(rs.getString("status"));x.setDescription(rs.getString("description"));return x;
    }
}
