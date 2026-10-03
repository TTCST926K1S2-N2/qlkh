package com.qlkh.repository;

import com.qlkh.model.SalesOrganization;
import com.qlkh.util.DBConnection; // Đảm bảo lớp DBConnection tồn tại trong dự án của bạn

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrgRepository {

    public List<SalesOrganization> findAll() throws SQLException {
        List<SalesOrganization> list = new ArrayList<>();
        String sql = "SELECT org_code, org_name, parent_org_code, description, status, created_at, updated_at FROM sales_organization ORDER BY org_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SalesOrganization org = new SalesOrganization();
                org.setOrgCode(rs.getString("org_code"));
                org.setOrgName(rs.getString("org_name"));
                org.setParentOrgCode(rs.getString("parent_org_code"));
                org.setDescription(rs.getString("description"));
                org.setStatus(rs.getString("status"));
                org.setCreatedAt(rs.getTimestamp("created_at"));
                org.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(org);
            }
        }
        return list;
    }

    public SalesOrganization findByCode(String orgCode) throws SQLException {
        String sql = "SELECT org_code, org_name, parent_org_code, description, status, created_at, updated_at FROM sales_organization WHERE org_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orgCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SalesOrganization org = new SalesOrganization();
                    org.setOrgCode(rs.getString("org_code"));
                    org.setOrgName(rs.getString("org_name"));
                    org.setParentOrgCode(rs.getString("parent_org_code"));
                    org.setDescription(rs.getString("description"));
                    org.setStatus(rs.getString("status"));
                    org.setCreatedAt(rs.getTimestamp("created_at"));
                    org.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return org;
                }
            }
        }
        return null;
    }

    public boolean insert(SalesOrganization org) throws SQLException {
        String sql = "INSERT INTO sales_organization (org_code, org_name, parent_org_code, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, org.getOrgCode());
            ps.setString(2, org.getOrgName());
            ps.setString(3, org.getParentOrgCode());
            ps.setString(4, org.getDescription());
            ps.setString(5, org.getStatus() != null ? org.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(SalesOrganization org) throws SQLException {
        String sql = "UPDATE sales_organization SET org_name = ?, parent_org_code = ?, description = ?, status = ?, updated_at = NOW() WHERE org_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, org.getOrgName());
            ps.setString(2, org.getParentOrgCode());
            ps.setString(3, org.getDescription());
            ps.setString(4, org.getStatus());
            ps.setString(5, org.getOrgCode());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String orgCode) throws SQLException {
        String sql = "DELETE FROM sales_organization WHERE org_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orgCode);
            return ps.executeUpdate() > 0;
        }
    }
}
