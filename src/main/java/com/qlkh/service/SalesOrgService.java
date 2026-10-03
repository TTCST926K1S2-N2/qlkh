package com.qlkh.service;

import com.qlkh.dto.SalesOrgDTO;
import com.qlkh.model.SalesOrganization;
import java.sql.SQLException;
import java.util.List;

public interface SalesOrgService {
    List<SalesOrgDTO> getOrgTree() throws SQLException;
    List<SalesOrganization> getAllOrgs() throws SQLException;
    SalesOrganization getOrgByCode(String code) throws SQLException;
    boolean createOrg(SalesOrganization org) throws SQLException;
    boolean updateOrg(SalesOrganization org) throws SQLException;
    boolean deleteOrg(String code) throws SQLException;
}
