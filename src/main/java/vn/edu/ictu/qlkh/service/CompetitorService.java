package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CompetitorDAO;
import vn.edu.ictu.qlkh.model.Competitor;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class CompetitorService {

    private final CompetitorDAO competitorDAO;

    public CompetitorService() {
        this(
                new CompetitorDAO()
        );
    }

    public CompetitorService(
            CompetitorDAO competitorDAO
    ) {

        if (competitorDAO == null) {
            throw new IllegalArgumentException(
                    "DAO không được để trống."
            );
        }

        this.competitorDAO =
                competitorDAO;
    }

    public List<Competitor> getAllCompetitors()
            throws SQLException {

        return competitorDAO.findAll();
    }

    public Competitor getCompetitorById(
            long id
    ) throws SQLException {

        validateId(id);

        return competitorDAO.findById(id);
    }

    public void addCompetitor(
            Competitor competitor
    ) throws SQLException {

        validate(competitor);

        if (
                competitorDAO.exists(
                        competitor.getCode(),
                        0
                )
        ) {

            throw new IllegalArgumentException(
                    "Mã đối thủ đã tồn tại."
            );
        }

        competitorDAO.insert(
                competitor
        );
    }

    public void updateCompetitor(
            Competitor competitor
    ) throws SQLException {

        if (
                competitor == null
                || competitor.getId() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Đối thủ không hợp lệ."
            );
        }

        validate(competitor);

        if (
                competitorDAO.exists(
                        competitor.getCode(),
                        competitor.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Mã đối thủ đã tồn tại."
            );
        }

        boolean updated =
                competitorDAO.update(
                        competitor
                );

        if (!updated) {

            throw new IllegalArgumentException(
                    "Không tìm thấy đối thủ."
            );
        }
    }

    public void deleteCompetitor(
            long id
    ) throws SQLException {

        validateId(id);

        boolean deleted =
                competitorDAO.delete(id);

        if (!deleted) {

            throw new IllegalArgumentException(
                    "Không tìm thấy đối thủ."
            );
        }
    }

    public void validate(
            Competitor competitor
    ) {

        if (competitor == null) {

            throw new IllegalArgumentException(
                    "Đối thủ không được để trống."
            );
        }

        competitor.setName(
                trim(
                        competitor.getName()
                )
        );

        competitor.setCode(
                trim(
                        competitor.getCode()
                ).toUpperCase(Locale.ROOT)
        );

        competitor.setStatus(
                trim(
                        competitor.getStatus()
                ).toUpperCase(Locale.ROOT)
        );

        competitor.setDescription(
                trimNullable(
                        competitor.getDescription()
                )
        );

        if (
                competitor.getName().isEmpty()
                || competitor.getName().length() > 255
        ) {

            throw new IllegalArgumentException(
                    "Tên đối thủ không hợp lệ."
            );
        }

        if (
                competitor.getCode().isEmpty()
                || competitor.getCode().length() > 100
                || !competitor.getCode()
                    .matches("[A-Z][A-Z0-9_]*")
        ) {

            throw new IllegalArgumentException(
                    "Mã đối thủ không hợp lệ."
            );
        }

        if (
                !"ACTIVE".equals(
                        competitor.getStatus()
                )
                && !"INACTIVE".equals(
                        competitor.getStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Trạng thái đối thủ không hợp lệ."
            );
        }

        if (
                competitor.getDescription() != null
                && competitor.getDescription().length() > 1000
        ) {

            throw new IllegalArgumentException(
                    "Mô tả đối thủ tối đa 1000 ký tự."
            );
        }
    }

    private void validateId(
            long id
    ) {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "ID đối thủ không hợp lệ."
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