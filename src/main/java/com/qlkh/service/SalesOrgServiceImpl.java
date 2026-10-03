package com.qlkh.service;

import com.qlkh.dto.SalesOrgDTO;
import com.qlkh.model.SalesOrganization;
import com.qlkh.repository.SalesOrgRepository;

import java.sql.SQLException;
import java.util.*;

public class SalesOrgServiceImpl implements SalesOrgService {

    private final SalesOrgRepository repository = new SalesOrgRepository();

    @Override
    public List<SalesOrgDTO> getOrgTree() throws SQLException {
        List<SalesOrganization> rawList = repository.findAll();
        Map<String, SalesOrgDTO> dtoMap = new HashMap<>();
        List<SalesOrgDTO> rootNodes = new ArrayList<>();

        for (SalesOrganization org : rawList) {
            SalesOrgDTO dto = new SalesOrgDTO(
                org.getOrgCode(),
                org.getOrgName(),
                org.getParentOrgCode(),
                org.getDescription(),
                org.getStatus()
            );
            dtoMap.put(org.getOrgCode(), dto);
        }

        for (SalesOrgDTO dto : dtoMap.values()) {
            String parentCode = dto.getParentOrgCode();
            if (parentCode == null || parentCode.trim().isEmpty() || !dtoMap.containsKey(parentCode)) {
                rootNodes.add(dto);
            } else {
                SalesOrgDTO parent = dtoMap.get(parentCode);
                parent.addChild(dto);
            }
        }
        return rootNodes;
    }

    @Override
    public List<SalesOrganization> getAllOrgs() throws SQLException {
        return repository.findAll();
    }

    @Override
    public SalesOrganization getOrgByCode(String code) throws SQLException {
        return repository.findByCode(code);
    }

    @Override
    public boolean createOrg(SalesOrganization org) throws SQLException {
        if (org.getOrgCode() == null || org.getOrgCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tổ chức không được để trống!");
        }
        return repository.insert(org);
    }

    @Override
    public boolean updateOrg(SalesOrganization org) throws SQLException {
        return repository.update(org);
    }

    @Override
    public boolean deleteOrg(String code) throws SQLException {
        return repository.delete(code);
    }
}
