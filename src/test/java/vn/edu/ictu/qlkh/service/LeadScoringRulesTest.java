package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeadScoringRulesTest {

    @Test
    void targetIndustryGetsFullScore() {
        assertEquals(100,
                LeadScoringRules.industryScore(
                        "Cong nghe thong tin",
                        Set.of("Cong nghe thong tin")));
    }

    @Test
    void otherIndustryGetsPartialScore() {
        assertEquals(50,
                LeadScoringRules.industryScore(
                        "San xuat",
                        Set.of("Cong nghe thong tin")));
    }

    @Test
    void unknownIndustryGetsZero() {
        assertEquals(0,
                LeadScoringRules.industryScore(
                        null,
                        Set.of("Cong nghe thong tin")));
        assertEquals(0,
                LeadScoringRules.industryScore(
                        "San xuat",
                        Set.of()));
    }

    @Test
    void companySizeScoresByLowerBound() {
        assertEquals(100,
                LeadScoringRules.companySizeScore("50-101"));
        assertEquals(70,
                LeadScoringRules.companySizeScore("10-49"));
        assertEquals(40,
                LeadScoringRules.companySizeScore("1-9"));
    }

    @Test
    void invalidCompanySizeGetsZero() {
        assertEquals(0,
                LeadScoringRules.companySizeScore(null));
        assertEquals(0,
                LeadScoringRules.companySizeScore("Khong ro"));
    }

    @Test
    void sourceScoresAreExplicit() {
        assertEquals(80,
                LeadScoringRules.sourceScore("WEBSITE"));
        assertEquals(70,
                LeadScoringRules.sourceScore("MANUAL"));
        assertEquals(50,
                LeadScoringRules.sourceScore("EXCEL"));
        assertEquals(0,
                LeadScoringRules.sourceScore("TEST"));
    }

    @Test
    void interestLevelsAreExplicit() {
        assertEquals(100,
                LeadScoringRules.interestScore("HIGH"));
        assertEquals(60,
                LeadScoringRules.interestScore("MEDIUM"));
        assertEquals(20,
                LeadScoringRules.interestScore("LOW"));
    }

    @Test
    void freeTextInterestIsNotGuessed() {
        assertEquals(0,
                LeadScoringRules.interestScore(
                        "Quan tam san pham"));
        assertEquals(0,
                LeadScoringRules.interestScore(null));
    }
}