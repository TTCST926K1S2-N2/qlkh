package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dto.LeadScoringConfig;

import static org.junit.jupiter.api.Assertions.*;

class LeadScoringServiceTest {

    @Test
    void acceptsWeightsTotaling100() {
        assertDoesNotThrow(() ->
            LeadScoringService.validate(new LeadScoringConfig(50, 30, 20))
        );
    }

    @Test
    void rejectsWeightsNotTotaling100() {
        assertThrows(IllegalArgumentException.class,
            () -> LeadScoringService.validate(new LeadScoringConfig(50, 30, 10))
        );
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class,
            () -> LeadScoringService.validate(new LeadScoringConfig(-1, 51, 50))
        );
    }

    @Test
    void calculatesScoreUsingSavedWeights() {
        var config = new LeadScoringConfig(50, 30, 20);

        assertEquals(70.0,
            LeadScoringService.calculateScore(config, 800000, 5, false));

        assertEquals(100.0,
            LeadScoringService.calculateScore(config, 1000000, 5, true));
    }
}