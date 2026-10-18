package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dto.SalesOrgDTO;
import vn.edu.ictu.qlkh.model.Region;
import vn.edu.ictu.qlkh.model.User;

import java.sql.SQLException;
import java.util.List;

public interface SalesOrgService {

    List<SalesOrgDTO> getAllSalesOrgs()
            throws SQLException;

    List<SalesOrgDTO> getSalesOrgTree()
            throws SQLException;

    SalesOrgDTO getSalesOrgById(long id)
            throws SQLException;

    SalesOrgDTO createSalesOrg(
            SalesOrgDTO input
    ) throws SQLException;

    SalesOrgDTO updateSalesOrg(
            long id,
            SalesOrgDTO input
    ) throws SQLException;

    boolean deactivateSalesOrg(long id)
            throws SQLException;

    List<Region> getRegions()
            throws SQLException;

    List<User> getMembers(long groupId) throws SQLException;

    List<User> getManagers()
            throws SQLException;
}