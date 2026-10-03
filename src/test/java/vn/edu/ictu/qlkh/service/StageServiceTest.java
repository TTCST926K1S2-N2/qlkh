package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.Stage;

import static org.junit.jupiter.api.Assertions.*;

class StageServiceTest {

    private final StageService service =
            new StageService();

    @Test
    void validateStage_shouldNormalizeValues() {

        Stage stage = new Stage();

        stage.setName("  Tiềm năng  ");
        stage.setCode(" potential ");
        stage.setStatus(" active ");
        stage.setDescription("  Mô tả  ");

        service.validateStage(stage);

        assertEquals(
                "Tiềm năng",
                stage.getName()
        );

        assertEquals(
                "POTENTIAL",
                stage.getCode()
        );

        assertEquals(
                "ACTIVE",
                stage.getStatus()
        );

        assertEquals(
                "Mô tả",
                stage.getDescription()
        );
    }

    @Test
    void validateStage_shouldRejectBlankName() {

        Stage stage = new Stage();

        stage.setName(" ");
        stage.setCode("POTENTIAL");
        stage.setStatus("ACTIVE");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectInvalidCode() {

        Stage stage = new Stage();

        stage.setName("Tiềm năng");
        stage.setCode("123ABC");
        stage.setStatus("ACTIVE");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectInvalidStatus() {

        Stage stage = new Stage();

        stage.setName("Tiềm năng");
        stage.setCode("POTENTIAL");
        stage.setStatus("DISABLED");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateStage(stage)
        );
    }

    @Test
    void validateStage_shouldRejectNullStage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateStage(null)
        );
    }
}
