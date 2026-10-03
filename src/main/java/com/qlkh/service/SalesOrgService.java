package com.qlkh.service;

import com.qlkh.dto.SalesOrgDTO;
import java.util.List;

public interface SalesOrgService {
    List<SalesOrgDTO> getAllSalesOrgs();
    List<SalesOrgDTO> getSalesOrgTree();
    SalesOrgDTO getSalesOrgById(int id);
}
