package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.model.Stage;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StageServiceTest {

    private final StageService service =
            new StageService();

    private Stage validStage() {

        Stage stage = new Stage();

        stage.setName("Tiếp cận");
        stage.setCode("CONTACT");
        stage.setStageOrder(1);

        stage.setWinProbability(
                new BigDecimal("10")
        );

        stage.setExitCondition(
                "Phải có ít nhất một lần liên hệ."
        );

        stage.setStatus("ACTIVE");

        stage.setDescription(
                "Giai đoạn đầu pipeline."
        );

        return stage;
    }

    @Test
    void validateStage_shouldNormalizeValues() {

        Stage stage = validStage();

        stage.setName(
                "  Tiếp cận  "
        );

        stage.setCode(
                " contact "
        );

        stage.setStatus(
                " active "
        );

        stage.setExitCondition(
                "  Có cuộc gọi  "
        );

        service.validateStage(stage);

        assertEquals(
                "Tiếp cận",
                stage.getName()
        );

        assertEquals(
                "CONTACT",
                stage.getCode()
        );

        assertEquals(
                "ACTIVE",
                stage.getStatus()
        );

        assertEquals(
                "Có cuộc gọi",
                stage.getExitCondition()
        );
    }

    @Test
    void validateStage_shouldRejectBlankName() {

        Stage stage = validStage();
        stage.setName(" ");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectInvalidCode() {

        Stage stage = validStage();
        stage.setCode("123ABC");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectInvalidOrder() {

        Stage stage = validStage();
        stage.setStageOrder(0);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectNegativeProbability() {

        Stage stage = validStage();

        stage.setWinProbability(
                new BigDecimal("-1")
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectProbabilityOver100() {

        Stage stage = validStage();

        stage.setWinProbability(
                new BigDecimal("101")
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectInvalidStatus() {

        Stage stage = validStage();
        stage.setStatus("DISABLED");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectNullStage() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                    service.validateStage(null)
        );
    }
}
