package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.RegionDAO;
import vn.edu.ictu.qlkh.dao.SalesOrgDAO;
import vn.edu.ictu.qlkh.dao.SalesOrgLookupDAO;
import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.dto.SalesOrgDTO;
import vn.edu.ictu.qlkh.model.Region;
import vn.edu.ictu.qlkh.model.SalesOrganization;
import vn.edu.ictu.qlkh.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesOrgServiceImpl
        implements SalesOrgService {

    private final SalesOrgDAO salesOrgDAO;
    private final RegionDAO regionDAO;
    private final UserDAO userDAO;
    private final SalesOrgLookupDAO lookupDAO;


    public SalesOrgServiceImpl() {

        this(
                new SalesOrgDAO(),
                new RegionDAO(),
                new UserDAO(),
                new SalesOrgLookupDAO()
        );
    }


    public SalesOrgServiceImpl(
            SalesOrgDAO salesOrgDAO,
            RegionDAO regionDAO,
            UserDAO userDAO,
            SalesOrgLookupDAO lookupDAO
    ) {

        if (
            salesOrgDAO == null
            || regionDAO == null
            || userDAO == null
            || lookupDAO == null
        ) {
            throw new IllegalArgumentException(
                    "DAO của SalesOrg không được null."
            );
        }

        this.salesOrgDAO = salesOrgDAO;
        this.regionDAO = regionDAO;
        this.userDAO = userDAO;
        this.lookupDAO = lookupDAO;
    }


    @Override
    public List<SalesOrgDTO> getAllSalesOrgs()
            throws SQLException {

        List<SalesOrgDTO> result =
                new ArrayList<>();

        for (
            SalesOrganization entity
            : salesOrgDAO.findAll()
        ) {

            result.add(
                    toDTO(entity)
            );
        }

        return result;
    }


    @Override
    public List<SalesOrgDTO> getSalesOrgTree()
            throws SQLException {

        List<SalesOrganization> entities =
                salesOrgDAO.findAll();

        Map<Long, SalesOrgDTO> map =
                new HashMap<>();

        List<SalesOrgDTO> roots =
                new ArrayList<>();

        for (SalesOrganization entity : entities) {

            map.put(
                    entity.getId(),
                    toDTO(entity)
            );
        }

        for (SalesOrganization entity : entities) {

            SalesOrgDTO dto =
                    map.get(entity.getId());

            Long parentId =
                    entity.getParentId();

            if (
                parentId == null
                || parentId <= 0
            ) {

                roots.add(dto);
                continue;
            }

            SalesOrgDTO parent =
                    map.get(parentId);

            if (parent == null) {
                roots.add(dto);
            } else {
                parent.getChildren()
                        .add(dto);
            }
        }

        return roots;
    }


    @Override
    public SalesOrgDTO getSalesOrgById(long id)
            throws SQLException {

        if (id <= 0) {
            return null;
        }

        SalesOrganization entity =
                salesOrgDAO.findById(id);

        return entity == null
                ? null
                : toDTO(entity);
    }


    @Override
    public SalesOrgDTO createSalesOrg(
            SalesOrgDTO input
    ) throws SQLException {

        validateCommon(
                input,
                null
        );

        SalesOrganization entity =
                toEntity(input);

        entity.setStatus(
                normalizeStatus(
                        input.getStatus()
                )
        );

        return toDTO(
                salesOrgDAO.insert(entity)
        );
    }


    @Override
    public SalesOrgDTO updateSalesOrg(
            long id,
            SalesOrgDTO input
    ) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID nhóm không hợp lệ."
            );
        }

        SalesOrganization current =
                salesOrgDAO.findById(id);

        if (current == null) {
            throw new IllegalArgumentException(
                    "Nhóm kinh doanh không tồn tại."
            );
        }

        validateCommon(
                input,
                id
        );

        Long parentId =
                input.getParentId();

        if (
            parentId != null
            && parentId == id
        ) {
            throw new IllegalArgumentException(
                    "Nhóm không thể là cha của chính nó."
            );
        }

        if (
            parentId != null
            && salesOrgDAO.isDescendant(
                    parentId,
                    id
            )
        ) {
            throw new IllegalArgumentException(
                    "Không thể tạo vòng lặp trong cây tổ chức."
            );
        }

        SalesOrganization entity =
                toEntity(input);

        entity.setId(id);

        entity.setStatus(
                normalizeStatus(
                        input.getStatus()
                )
        );

        SalesOrganization updated =
                salesOrgDAO.update(entity);

        if (updated == null) {
            throw new IllegalArgumentException(
                    "Không cập nhật được nhóm kinh doanh."
            );
        }

        return toDTO(updated);
    }


    @Override
    public boolean deactivateSalesOrg(long id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID nhóm không hợp lệ."
            );
        }

        SalesOrganization current =
                salesOrgDAO.findById(id);

        if (current == null) {
            throw new IllegalArgumentException(
                    "Nhóm kinh doanh không tồn tại."
            );
        }

        return salesOrgDAO.deactivate(id);
    }


    @Override
    public List<Region> getRegions()
            throws SQLException {

        List<Region> result =
                new ArrayList<>();

        for (Region region : regionDAO.findAll()) {

            if (
                "ACTIVE".equalsIgnoreCase(
                        region.getStatus()
                )
            ) {
                result.add(region);
            }
        }

        return result;
    }


    @Override
    public List<User> getManagers()
            throws SQLException {

        return lookupDAO.findActiveManagers();
    }


    @Override
    public List<User> getMembers(long groupId)
            throws SQLException {
        if (groupId <= 0) {
            throw new IllegalArgumentException(
                    "ID nhóm không hợp lệ."
            );
        }

        if (salesOrgDAO.findById(groupId) == null) {
            throw new IllegalArgumentException(
                    "Nhóm kinh doanh không tồn tại."
            );
        }

        return salesOrgDAO.findMembersByGroupId(groupId);
    }

    private void validateCommon(
            SalesOrgDTO input,
            Long excludeId
    ) throws SQLException {

        if (input == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu nhóm không được null."
            );
        }

        String code =
                normalize(
                        input.getOrgCode()
                );

        String name =
                normalize(
                        input.getOrgName()
                );

        if (code == null) {
            throw new IllegalArgumentException(
                    "Mã nhóm không được để trống."
            );
        }

        if (name == null) {
            throw new IllegalArgumentException(
                    "Tên nhóm không được để trống."
            );
        }

        if (
            salesOrgDAO.existsByCode(
                    code,
                    excludeId
            )
        ) {
            throw new IllegalArgumentException(
                    "Mã nhóm đã tồn tại."
            );
        }

        input.setOrgCode(code);
        input.setOrgName(name);

        validateParent(
                input.getParentId()
        );

        validateLeader(
                input.getLeaderId()
        );

        validateRegion(
                input.getRegionId()
        );
    }


    private void validateParent(
            Long parentId
    ) throws SQLException {

        if (parentId == null) {
            return;
        }

        if (parentId <= 0) {
            throw new IllegalArgumentException(
                    "Nhóm cha không hợp lệ."
            );
        }

        SalesOrganization parent =
                salesOrgDAO.findById(parentId);

        if (parent == null) {
            throw new IllegalArgumentException(
                    "Nhóm cha không tồn tại."
            );
        }

        if (
            !"ACTIVE".equalsIgnoreCase(
                    parent.getStatus()
            )
        ) {
            throw new IllegalArgumentException(
                    "Nhóm cha đã ngừng hoạt động."
            );
        }
    }


    private void validateLeader(
            Long leaderId
    ) throws SQLException {

        if (leaderId == null) {
            throw new IllegalArgumentException(
                    "Phải chọn trưởng nhóm."
            );
        }

        User leader =
                userDAO.findById(leaderId);

        if (leader == null) {
            throw new IllegalArgumentException(
                    "Trưởng nhóm không tồn tại."
            );
        }

        if (
            !"MANAGER".equalsIgnoreCase(
                    leader.getRole()
            )
        ) {
            throw new IllegalArgumentException(
                    "Trưởng nhóm phải có vai trò MANAGER."
            );
        }

        if (
            !"ACTIVE".equalsIgnoreCase(
                    leader.getStatus()
            )
        ) {
            throw new IllegalArgumentException(
                    "Trưởng nhóm phải đang hoạt động."
            );
        }
    }


    private void validateRegion(
            Long regionId
    ) throws SQLException {

        if (regionId == null) {
            throw new IllegalArgumentException(
                    "Phải chọn khu vực."
            );
        }

        Region region =
                regionDAO.findById(regionId);

        if (region == null) {
            throw new IllegalArgumentException(
                    "Khu vực không tồn tại."
            );
        }

        if (
            !"ACTIVE".equalsIgnoreCase(
                    region.getStatus()
            )
        ) {
            throw new IllegalArgumentException(
                    "Khu vực đã ngừng hoạt động."
            );
        }
    }


    private SalesOrganization toEntity(
            SalesOrgDTO dto
    ) {

        SalesOrganization entity =
                new SalesOrganization();

        entity.setCode(
                dto.getOrgCode()
        );

        entity.setName(
                dto.getOrgName()
        );

        entity.setParentId(
                dto.getParentId()
        );

        entity.setLeaderId(
                dto.getLeaderId()
        );

        entity.setRegionId(
                dto.getRegionId()
        );

        entity.setStatus(
                normalizeStatus(
                        dto.getStatus()
                )
        );

        return entity;
    }


    private SalesOrgDTO toDTO(
            SalesOrganization entity
    ) {

        SalesOrgDTO dto =
                new SalesOrgDTO();

        dto.setId(
                entity.getId()
        );

        dto.setOrgCode(
                entity.getCode()
        );

        dto.setOrgName(
                entity.getName()
        );

        dto.setParentId(
                entity.getParentId()
        );

        dto.setLeaderId(
                entity.getLeaderId()
        );

        dto.setLeaderName(
                entity.getLeaderName()
        );

        dto.setRegionId(
                entity.getRegionId()
        );

        dto.setRegionName(
                entity.getRegionName()
        );

        dto.setStatus(
                entity.getStatus()
        );

        return dto;
    }


    private String normalizeStatus(
            String status
    ) {

        if (
            status == null
            || status.isBlank()
        ) {
            return "ACTIVE";
        }

        String normalized =
                status.trim()
                        .toUpperCase();

        if (
            !"ACTIVE".equals(normalized)
            && !"INACTIVE".equals(normalized)
        ) {
            throw new IllegalArgumentException(
                    "Trạng thái nhóm không hợp lệ."
            );
        }

        return normalized;
    }


    private String normalize(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
