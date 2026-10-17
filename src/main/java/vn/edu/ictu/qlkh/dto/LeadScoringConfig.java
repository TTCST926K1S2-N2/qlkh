package vn.edu.ictu.qlkh.dto;

public class LeadScoringConfig {
    public double expectedValueWeight;
    public double completenessWeight;
    public double openOpportunityWeight;

    public LeadScoringConfig() {}

    public LeadScoringConfig(double expectedValueWeight, double completenessWeight,
                             double openOpportunityWeight) {
        this.expectedValueWeight = expectedValueWeight;
        this.completenessWeight = completenessWeight;
        this.openOpportunityWeight = openOpportunityWeight;
    }
}
