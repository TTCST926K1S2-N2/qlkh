package com.qlkh.service;

import com.qlkh.dto.SalesOrgDTO;
import com.qlkh.model.SalesOrganization;
import com.qlkh.repository.SalesOrgRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesOrgServiceImpl implements SalesOrgService {
    private SalesOrgRepository repo = new SalesOrgRepository();

    @Override
    public List<SalesOrgDTO> getAllSalesOrgs() {
        List<SalesOrganization> entities = repo.findAll();
        List<SalesOrgDTO> dtos = new ArrayList<>();
        for (SalesOrganization e : entities) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    @Override
    public List<SalesOrgDTO> getSalesOrgTree() {
        List<SalesOrganization> entities = repo.findAll();
        Map<Integer, SalesOrgDTO> dtoMap = new HashMap<>();
        List<SalesOrgDTO> roots = new ArrayList<>();

        for (SalesOrganization e : entities) {
            dtoMap.put(e.getId(), toDTO(e));
        }

        for (SalesOrganization e : entities) {
            SalesOrgDTO dto = dtoMap.get(e.getId());
            if (e.getParentId() == null || e.getParentId() == 0) {
                roots.add(dto);
            } else {
                SalesOrgDTO parent = dtoMap.get(e.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    @Override
    public SalesOrgDTO getSalesOrgById(int id) {
        SalesOrganization e = repo.findById(id);
        return e != null ? toDTO(e) : null;
    }

    private SalesOrgDTO toDTO(SalesOrganization e) {
        return new SalesOrgDTO(e.getId(), e.getOrgCode(), e.getOrgName(), e.getParentId(), e.getStatus());
    }
}
