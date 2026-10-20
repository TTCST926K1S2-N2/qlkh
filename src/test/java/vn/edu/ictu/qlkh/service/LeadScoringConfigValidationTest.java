package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dto.LeadScoringConfig;

import static org.junit.jupiter.api.Assertions.*;

class LeadScoringConfigValidationTest {

    private LeadScoringConfig validConfig() {
        LeadScoringConfig config = new LeadScoringConfig();
        config.industryWeight = 30;
        config.companySizeWeight = 25;
        config.sourceWeight = 20;
        config.interestWeight = 25;
        config.hotThreshold = 70;
        config.warmThreshold = 40;
        return config;
    }

    @Test
    void validConfigIsAccepted() {
        assertDoesNotThrow(() ->
                LeadScoringService.validateLeadConfig(validConfig()));
    }

    @Test
    void nullConfigIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                LeadScoringService.validateLeadConfig(null));
    }

    @Test
    void incorrectWeightTotalIsRejected() {
        LeadScoringConfig config = validConfig();
        config.industryWeight = 40;

        assertThrows(IllegalArgumentException.class, () ->
                LeadScoringService.validateLeadConfig(config));
    }

    @Test
    void negativeWeightIsRejected() {
        LeadScoringConfig config = validConfig();
        config.sourceWeight = -1;

        assertThrows(IllegalArgumentException.class, () ->
                LeadScoringService.validateLeadConfig(config));
    }

    @Test
    void invalidThresholdOrderIsRejected() {
        LeadScoringConfig config = validConfig();
        config.warmThreshold = 80;

        assertThrows(IllegalArgumentException.class, () ->
                LeadScoringService.validateLeadConfig(config));
    }

    @Test
    void equalThresholdsAreRejected() {
        LeadScoringConfig config = validConfig();
        config.warmThreshold = 70;

        assertThrows(IllegalArgumentException.class, () ->
                LeadScoringService.validateLeadConfig(config));
    }

    @Test
    void thresholdOutsideRangeIsRejected() {
        LeadScoringConfig config = validConfig();
        config.hotThreshold = 110;

        assertThrows(IllegalArgumentException.class, () ->
                LeadScoringService.validateLeadConfig(config));
    }
}