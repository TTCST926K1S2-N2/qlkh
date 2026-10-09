package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.BusinessGroupDAO;
import vn.edu.ictu.qlkh.model.BusinessGroup;

import java.sql.SQLException;
import java.util.List;

/**
 * Xử lý nghiệp vụ nhóm kinh doanh - HTQLKH-9.
 */
public class BusinessGroupService {

    private final BusinessGroupDAO businessGroupDAO;

    public BusinessGroupService() {
        this(new BusinessGroupDAO());
    }

    public BusinessGroupService(
            BusinessGroupDAO businessGroupDAO
    ) {
        if (businessGroupDAO == null) {
            throw new IllegalArgumentException(
                    "BusinessGroupDAO không được null."
            );
        }

        this.businessGroupDAO = businessGroupDAO;
    }

    /**
     * Lấy toàn bộ nhóm kinh doanh.
     */
    public List<BusinessGroup> getAllGroups()
            throws SQLException {

        return businessGroupDAO.findAll();
    }

    /**
     * Tìm nhóm theo ID.
     */
    public BusinessGroup getGroupById(long groupId)
            throws SQLException {

        if (groupId <= 0) {
            return null;
        }

        return businessGroupDAO.findById(groupId);
    }

    /**
     * Lấy nhóm hiện tại của một người dùng.
     */
    public BusinessGroup getGroupByUserId(long userId)
            throws SQLException {

        if (userId <= 0) {
            return null;
        }

        return businessGroupDAO.findByUserId(userId);
    }

    /**
     * Kiểm tra nhóm có tồn tại.
     */
    public boolean groupExists(long groupId)
            throws SQLException {

        return groupId > 0
                && businessGroupDAO.existsById(groupId);
    }

    /**
     * Gán người dùng vào một nhóm kinh doanh.
     */
    public void assignUserToGroup(
            long userId,
            long groupId
    ) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ."
            );
        }

        if (groupId <= 0) {
            throw new IllegalArgumentException(
                    "Nhóm kinh doanh không hợp lệ."
            );
        }

        if (!businessGroupDAO.existsById(groupId)) {
            throw new IllegalArgumentException(
                    "Nhóm kinh doanh không tồn tại."
            );
        }

        businessGroupDAO.assignUserToGroup(
                userId,
                groupId
        );
    }

    /**
     * Gỡ người dùng khỏi nhóm kinh doanh.
     */
    public void removeUserFromGroup(long userId)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ."
            );
        }

        businessGroupDAO.removeUserFromGroup(userId);
    }
    /**
     * Cập nhật công ty/nhóm kinh doanh mẹ (Parent Group)
     */
    public void updateParentGroup(Long groupId, Long parentId) throws SQLException {
        // 1. Kiểm tra ID nhóm hợp lệ
        if (groupId == null || groupId <= 0) {
            throw new IllegalArgumentException("Nhóm kinh doanh không hợp lệ.");
        }

        // 2. Kiểm tra nhóm tồn tại trong DB
        if (!businessGroupDAO.existsById(groupId)) {
            throw new IllegalArgumentException("Nhóm kinh doanh không tồn tại.");
        }

        // 3. Nếu gán parentId khác null, kiểm tra parentId có tồn tại không
        if (parentId != null) {
            if (parentId <= 0 || !businessGroupDAO.existsById(parentId)) {
                throw new IllegalArgumentException("Nhóm kinh doanh mẹ không tồn tại.");
            }

            // 4. Kiểm tra vòng lặp (Chặn tự làm mẹ của chính mình hoặc vòng A -> B -> A)
            if (businessGroupDAO.isCircularParent(groupId, parentId)) {
                throw new IllegalArgumentException("Không thể chọn nhóm kinh doanh mẹ gây ra quan hệ vòng.");
            }
        }

        // 5. Cập nhật vào DB
        boolean updated = businessGroupDAO.updateParentGroup(groupId, parentId);
        if (!updated) {
            throw new SQLException("Cập nhật nhóm kinh doanh mẹ thất bại.");
        }
    }
}