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
}