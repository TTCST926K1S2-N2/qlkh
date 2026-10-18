package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dto.LeadScoringConfig;

import static org.junit.jupiter.api.Assertions.*;

class LeadScoringCriteriaTest {

    private LeadScoringConfig config() {
        LeadScoringConfig c = new LeadScoringConfig();
        c.industryWeight = 30;
        c.companySizeWeight = 25;
        c.sourceWeight = 20;
        c.interestWeight = 25;
        c.hotThreshold = 70;
        c.warmThreshold = 40;
        return c;
    }

    @Test
    void allCriteriaMatchReturns100() {
        assertEquals(100.0,
                LeadScoringService.calculateLeadScore(
                        config(), 100, 100, 100, 100));
    }

    @Test
    void missingCriteriaReturnsZero() {
        assertEquals(0.0,
                LeadScoringService.calculateLeadScore(
                        config(), 0, 0, 0, 0));
    }

    @Test
    void weightedCriteriaReturnsExpectedScore() {
        assertEquals(55.0,
                LeadScoringService.calculateLeadScore(
                        config(), 100, 0, 0, 100));
    }

    @Test
    void classificationRespectsThresholds() {
        assertEquals("HOT",
                LeadScoringService.classifyLead(config(), 70));
        assertEquals("WARM",
                LeadScoringService.classifyLead(config(), 40));
        assertEquals("COLD",
                LeadScoringService.classifyLead(config(), 39.99));
    }

    @Test
    void invalidWeightsAreRejected() {
        LeadScoringConfig c = config();
        c.industryWeight = 50;

        assertThrows(IllegalArgumentException.class,
                () -> LeadScoringService.calculateLeadScore(
                        c, 100, 100, 100, 100));
    }

    @Test
    void invalidCriteriaAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> LeadScoringService.calculateLeadScore(
                        config(), -1, 100, 100, 100));
    }

    @Test
    void invalidThresholdsAreRejected() {
        LeadScoringConfig c = config();
        c.warmThreshold = 80;

        assertThrows(IllegalArgumentException.class,
                () -> LeadScoringService.classifyLead(c, 70));
    }
}