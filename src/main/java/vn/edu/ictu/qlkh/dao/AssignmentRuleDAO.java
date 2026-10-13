package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.AssignmentRule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AssignmentRuleDAO {
    private static final List<AssignmentRule> mockRules = new ArrayList<>();

    public AssignmentRuleDAO() {
        if (mockRules.isEmpty()) {
            mockRules.add(new AssignmentRule(1L, "Phân bổ khu vực Hà Nội", "REGION", "Hà Nội", 101L, 1, true));
            mockRules.add(new AssignmentRule(2L, "Phân bổ nguồn Website", "SOURCE", "Website", 102L, 2, true));
        }
    }

    public List<AssignmentRule> getAllRules() {
        return new ArrayList<>(mockRules);
    }

    public List<AssignmentRule> getActiveRulesSortedByPriority() {
        List<AssignmentRule> activeRules = new ArrayList<>();
        for (AssignmentRule rule : mockRules) {
            if (Boolean.TRUE.equals(rule.getActive())) {
                activeRules.add(rule);
            }
        }
        activeRules.sort(Comparator.comparingInt(AssignmentRule::getPriority));
        return activeRules;
    }

    public boolean addRule(AssignmentRule rule) {
        rule.setId((long) (mockRules.size() + 1));
        return mockRules.add(rule);
    }
}