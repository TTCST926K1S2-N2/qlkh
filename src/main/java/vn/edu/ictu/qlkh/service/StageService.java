package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.StageDAO;
import vn.edu.ictu.qlkh.model.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class StageService {

    private final StageDAO stageDAO;

    public StageService() {
        this(new StageDAO());
    }

    public StageService(StageDAO stageDAO) {
        if (stageDAO == null) {
            throw new IllegalArgumentException(
                    "StageDAO không được null.");
        }
        this.stageDAO = stageDAO;
    }

    public List<Stage> getAllStages() throws SQLException {
        return stageDAO.findAll();
    }

    public Stage getStageById(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID stage phải lớn hơn 0.");
        }

        return stageDAO.findById(id);
    }

    public void addStage(Stage stage) throws SQLException {
        validateStage(stage);

        if (stageDAO.exists(stage.getCode(), 0)) {
            throw new IllegalArgumentException(
                    "Code stage đã tồn tại.");
        }

        if (!stageDAO.insert(stage)) {
            throw new SQLException(
                    "Không thể thêm stage.");
        }
    }

    public void updateStage(Stage stage) throws SQLException {
        validateStage(stage);

        if (stage.getId() <= 0) {
            throw new IllegalArgumentException(
                    "ID stage phải lớn hơn 0.");
        }

        Stage existing = stageDAO.findById(stage.getId());

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy stage.");
        }

        if (stageDAO.exists(stage.getCode(), stage.getId())) {
            throw new IllegalArgumentException(
                    "Code stage đã tồn tại.");
        }

        if (!stageDAO.update(stage)) {
            throw new SQLException(
                    "Không thể cập nhật stage.");
        }
    }

    public void deleteStage(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID stage phải lớn hơn 0.");
        }

        Stage existing = stageDAO.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy stage.");
        }

        if (!stageDAO.delete(id)) {
            throw new SQLException(
                    "Không thể xóa stage.");
        }
    }

    public void validateStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException(
                    "Stage không được null.");
        }

        String name = stage.getName() == null
                ? ""
                : stage.getName().trim();

        String code = stage.getCode() == null
                ? ""
                : stage.getCode().trim().toUpperCase(Locale.ROOT);

        String status = stage.getStatus() == null
                ? ""
                : stage.getStatus().trim().toUpperCase(Locale.ROOT);

        String description = stage.getDescription() == null
                ? null
                : stage.getDescription().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên stage không được để trống.");
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Tên stage không được vượt quá 255 ký tự.");
        }

        if (code.isEmpty()) {
            throw new IllegalArgumentException(
                    "Code stage không được để trống.");
        }

        if (code.length() > 100) {
            throw new IllegalArgumentException(
                    "Code stage không được vượt quá 100 ký tự.");
        }

        if (!code.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Code stage chỉ được chứa A-Z, 0-9, _, và phải bắt đầu bằng chữ.");
        }

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException(
                    "Status stage phải là ACTIVE hoặc INACTIVE.");
        }

        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException(
                    "Description không được vượt quá 1000 ký tự.");
        }

        stage.setName(name);
        stage.setCode(code);
        stage.setStatus(status);
        stage.setDescription(description);
    }
}
