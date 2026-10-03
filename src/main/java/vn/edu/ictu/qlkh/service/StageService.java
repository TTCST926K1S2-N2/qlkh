package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.StageDAO;
import vn.edu.ictu.qlkh.model.Stage;

import java.math.BigDecimal;
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
                    "StageDAO không được null."
            );
        }

        this.stageDAO = stageDAO;
    }


    public List<Stage> getAllStages()
            throws SQLException {

        return stageDAO.findAll();
    }


    public Stage getStageById(long id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID giai đoạn phải lớn hơn 0."
            );
        }

        return stageDAO.findById(id);
    }


    public void addStage(Stage stage)
            throws SQLException {

        validateStage(stage);

        if (stageDAO.existsCode(
                stage.getCode(),
                0)) {

            throw new IllegalArgumentException(
                    "Mã giai đoạn đã tồn tại."
            );
        }

        if (stageDAO.existsOrder(
                stage.getStageOrder(),
                0)) {

            throw new IllegalArgumentException(
                    "Thứ tự giai đoạn đã tồn tại."
            );
        }

        if (!stageDAO.insert(stage)) {
            throw new SQLException(
                    "Không thể thêm giai đoạn."
            );
        }
    }


    public void updateStage(Stage stage)
            throws SQLException {

        validateStage(stage);

        if (stage.getId() <= 0) {
            throw new IllegalArgumentException(
                    "ID giai đoạn phải lớn hơn 0."
            );
        }

        Stage existing =
                stageDAO.findById(
                        stage.getId()
                );

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy giai đoạn."
            );
        }

        if (stageDAO.existsCode(
                stage.getCode(),
                stage.getId())) {

            throw new IllegalArgumentException(
                    "Mã giai đoạn đã tồn tại."
            );
        }

        if (stageDAO.existsOrder(
                stage.getStageOrder(),
                stage.getId())) {

            throw new IllegalArgumentException(
                    "Thứ tự giai đoạn đã tồn tại."
            );
        }

        if (!stageDAO.update(stage)) {
            throw new SQLException(
                    "Không thể cập nhật giai đoạn."
            );
        }
    }


    public void deactivateStage(long id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID giai đoạn phải lớn hơn 0."
            );
        }

        Stage existing =
                stageDAO.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy giai đoạn."
            );
        }

        if ("INACTIVE".equalsIgnoreCase(
                existing.getStatus())) {

            return;
        }

        if (!stageDAO.deactivate(id)) {
            throw new SQLException(
                    "Không thể ngừng hoạt động giai đoạn."
            );
        }
    }


    public void validateStage(Stage stage) {

        if (stage == null) {
            throw new IllegalArgumentException(
                    "Stage không được null."
            );
        }

        String name =
                stage.getName() == null
                        ? ""
                        : stage.getName().trim();

        String code =
                stage.getCode() == null
                        ? ""
                        : stage.getCode()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

        String status =
                stage.getStatus() == null
                        ? ""
                        : stage.getStatus()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

        String exitCondition =
                normalizeOptional(
                        stage.getExitCondition()
                );

        String description =
                normalizeOptional(
                        stage.getDescription()
                );

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên giai đoạn không được để trống."
            );
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException(
                    "Tên giai đoạn tối đa 255 ký tự."
            );
        }

        if (code.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mã giai đoạn không được để trống."
            );
        }

        if (code.length() > 100) {
            throw new IllegalArgumentException(
                    "Mã giai đoạn tối đa 100 ký tự."
            );
        }

        if (!code.matches(
                "[A-Z][A-Z0-9_]*")) {

            throw new IllegalArgumentException(
                    "Mã giai đoạn chỉ gồm A-Z, 0-9, _ và phải bắt đầu bằng chữ."
            );
        }

        if (stage.getStageOrder() <= 0) {
            throw new IllegalArgumentException(
                    "Thứ tự giai đoạn phải lớn hơn 0."
            );
        }

        BigDecimal probability =
                stage.getWinProbability();

        if (probability == null) {
            throw new IllegalArgumentException(
                    "Xác suất thắng không được để trống."
            );
        }

        if (probability.compareTo(
                BigDecimal.ZERO) < 0
                ||
                probability.compareTo(
                        new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "Xác suất thắng phải từ 0 đến 100."
            );
        }

        if (exitCondition != null
                && exitCondition.length() > 1000) {

            throw new IllegalArgumentException(
                    "Điều kiện rời giai đoạn tối đa 1000 ký tự."
            );
        }

        if (!"ACTIVE".equals(status)
                &&
                !"INACTIVE".equals(status)) {

            throw new IllegalArgumentException(
                    "Trạng thái phải là ACTIVE hoặc INACTIVE."
            );
        }

        if (description != null
                && description.length() > 1000) {

            throw new IllegalArgumentException(
                    "Mô tả tối đa 1000 ký tự."
            );
        }

        stage.setName(name);
        stage.setCode(code);
        stage.setWinProbability(
                probability.stripTrailingZeros()
        );
        stage.setExitCondition(exitCondition);
        stage.setStatus(status);
        stage.setDescription(description);
    }


    private String normalizeOptional(
            String value) {

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
