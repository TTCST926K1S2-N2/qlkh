package vn.edu.ictu.qlkh.dto;

public class LeadScoringConfig {
    public java.util.List<String> targetIndustries = new java.util.ArrayList<>();

    // Legacy fields - kept for compatibility
    public double expectedValueWeight;
    public double completenessWeight;
    public double openOpportunityWeight;

    // Sprint 4 - S4-05 scoring criteria
    public double industryWeight = 30;
    public double companySizeWeight = 25;
    public double sourceWeight = 20;
    public double interestWeight = 25;

    // Configurable classification thresholds
    public double hotThreshold = 70;
    public double warmThreshold = 40;

    public LeadScoringConfig() {
    }

    public LeadScoringConfig(
            double expectedValueWeight,
            double completenessWeight,
            double openOpportunityWeight) {

        this.expectedValueWeight = expectedValueWeight;
        this.completenessWeight = completenessWeight;
        this.openOpportunityWeight = openOpportunityWeight;
    }
}