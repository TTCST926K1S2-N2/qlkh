package com.qlkh.repository;

import com.qlkh.model.SalesOrganization;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrgRepository {

    public List<SalesOrganization> findAll() {
        List<SalesOrganization> list = new ArrayList<>();
        String sql = "SELECT * FROM sales_organization";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public SalesOrganization findById(int id) {
        String sql = "SELECT * FROM sales_organization WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SalesOrganization mapResultSetToEntity(ResultSet rs) throws SQLException {
        SalesOrganization entity = new SalesOrganization();
        entity.setId(rs.getInt("id"));
        entity.setOrgCode(rs.getString("org_code"));
        entity.setOrgName(rs.getString("org_name"));
        int parentId = rs.getInt("parent_id");
        entity.setParentId(rs.wasNull() ? null : parentId);
        entity.setStatus(rs.getString("status"));
        entity.setCreatedAt(rs.getTimestamp("created_at"));
        return entity;
    }
}
