package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.WinLossReasonDAO;
import vn.edu.ictu.qlkh.model.WinLossReason;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class WinLossReasonService {

    private final WinLossReasonDAO reasonDAO;

    public WinLossReasonService() {
        this(
                new WinLossReasonDAO()
        );
    }

    public WinLossReasonService(
            WinLossReasonDAO reasonDAO
    ) {

        if (reasonDAO == null) {

            throw new IllegalArgumentException(
                    "DAO không được để trống."
            );
        }

        this.reasonDAO =
                reasonDAO;
    }

    public List<WinLossReason> getAllReasons()
            throws SQLException {

        return reasonDAO.findAll();
    }

    public List<WinLossReason> getReasonsByType(
            String type
    ) throws SQLException {

        String normalized =
                normalizeType(type);

        return reasonDAO.findByType(
                normalized
        );
    }

    public WinLossReason getReasonById(
            long id
    ) throws SQLException {

        validateId(id);

        return reasonDAO.findById(id);
    }

    public void addReason(
            WinLossReason reason
    ) throws SQLException {

        validate(reason);

        if (
                reasonDAO.exists(
                        reason.getCode(),
                        0
                )
        ) {

            throw new IllegalArgumentException(
                    "Mã lý do đã tồn tại."
            );
        }

        reasonDAO.insert(reason);
    }

    public void updateReason(
            WinLossReason reason
    ) throws SQLException {

        if (
                reason == null
                || reason.getId() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Lý do không hợp lệ."
            );
        }

        validate(reason);

        if (
                reasonDAO.exists(
                        reason.getCode(),
                        reason.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Mã lý do đã tồn tại."
            );
        }

        boolean updated =
                reasonDAO.update(reason);

        if (!updated) {

            throw new IllegalArgumentException(
                    "Không tìm thấy lý do."
            );
        }
    }

    public void deleteReason(
            long id
    ) throws SQLException {

        validateId(id);

        boolean deleted =
                reasonDAO.delete(id);

        if (!deleted) {

            throw new IllegalArgumentException(
                    "Không tìm thấy lý do."
            );
        }
    }

    public void validate(
            WinLossReason reason
    ) {

        if (reason == null) {

            throw new IllegalArgumentException(
                    "Lý do không được để trống."
            );
        }

        reason.setName(
                trim(
                        reason.getName()
                )
        );

        reason.setCode(
                trim(
                        reason.getCode()
                ).toUpperCase(Locale.ROOT)
        );

        reason.setType(
                normalizeType(
                        reason.getType()
                )
        );

        reason.setStatus(
                trim(
                        reason.getStatus()
                ).toUpperCase(Locale.ROOT)
        );

        reason.setDescription(
                trimNullable(
                        reason.getDescription()
                )
        );

        if (
                reason.getName().isEmpty()
                || reason.getName().length() > 255
        ) {

            throw new IllegalArgumentException(
                    "Tên lý do không hợp lệ."
            );
        }

        if (
                reason.getCode().isEmpty()
                || reason.getCode().length() > 100
                || !reason.getCode()
                    .matches("[A-Z][A-Z0-9_]*")
        ) {

            throw new IllegalArgumentException(
                    "Mã lý do không hợp lệ."
            );
        }

        if (
                !"ACTIVE".equals(
                        reason.getStatus()
                )
                && !"INACTIVE".equals(
                        reason.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Trạng thái lý do không hợp lệ."
            );
        }

        if (
                reason.getDescription() != null
                && reason.getDescription().length() > 1000
        ) {

            throw new IllegalArgumentException(
                    "Mô tả lý do tối đa 1000 ký tự."
            );
        }
    }

    private String normalizeType(
            String type
    ) {

        String normalized =
                trim(type)
                    .toUpperCase(Locale.ROOT);

        if (
                !"WON".equals(normalized)
                && !"LOST".equals(normalized)
        ) {

            throw new IllegalArgumentException(
                    "Loại lý do phải là WON hoặc LOST."
            );
        }

        return normalized;
    }

    private void validateId(
            long id
    ) {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "ID lý do không hợp lệ."
            );
        }
    }

    private String trim(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    private String trimNullable(
            String value
    ) {

        if (
                value == null
                || value.trim().isEmpty()
        ) {
            return null;
        }

        return value.trim();
    }
}