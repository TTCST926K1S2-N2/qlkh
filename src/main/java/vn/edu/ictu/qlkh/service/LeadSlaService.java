package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.LeadSlaDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LeadSlaService {

    private final LeadSlaDAO dao;

    public LeadSlaService() {
        this(new LeadSlaDAO());
    }

    public LeadSlaService(LeadSlaDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public List<Map<String, Object>> getAssignedLeads(long userId)
            throws SQLException {

        validateUserId(userId);

        return dao.findAssigned(userId);
    }

    public boolean acceptLead(long leadId, long userId)
            throws SQLException {

        validateLeadId(leadId);
        validateUserId(userId);

        return dao.accept(leadId, userId);
    }

    public boolean rejectLead(
            long leadId,
            long userId,
            String reason
    ) throws SQLException {

        validateLeadId(leadId);
        validateUserId(userId);

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Bat buoc nhap ly do tu choi Lead"
            );
        }

        String normalizedReason = reason.trim();

        if (normalizedReason.length() > 1000) {
            throw new IllegalArgumentException(
                    "Ly do tu choi khong duoc vuot qua 1000 ky tu"
            );
        }

        return dao.reject(
                leadId,
                userId,
                normalizedReason
        );
    }

    public boolean assignLead(
            long leadId,
            long actorId,
            String actorRole,
            long assigneeId
    ) throws SQLException {

        validateLeadId(leadId);
        validateUserId(actorId);
        validateUserId(assigneeId);

        if (!dao.canAssign(actorId, actorRole, assigneeId)) {
            throw new SecurityException(
                    "Khong co quyen phan cong Lead cho nhan vien nay"
            );
        }

        return dao.assign(leadId, assigneeId);
    }
    private void validateLeadId(long leadId) {
        if (leadId <= 0) {
            throw new IllegalArgumentException(
                    "ID Lead khong hop le"
            );
        }
    }

    private void validateUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "ID nguoi dung khong hop le"
            );
        }
    }
}