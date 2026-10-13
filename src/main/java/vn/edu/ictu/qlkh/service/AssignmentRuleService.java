package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.AssignmentRuleDAO;
import vn.edu.ictu.qlkh.model.AssignmentRule;
import java.util.List;

public class AssignmentRuleService {
    private final AssignmentRuleDAO assignmentRuleDAO = new AssignmentRuleDAO();

    public List<AssignmentRule> getAllRules() {
        return assignmentRuleDAO.getAllRules();
    }

    public boolean createRule(AssignmentRule rule) {
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            return false;
        }
        return assignmentRuleDAO.addRule(rule);
    }

    public Long determineAssignedUserId(String region, String source) {
        List<AssignmentRule> activeRules = assignmentRuleDAO.getActiveRulesSortedByPriority();
        for (AssignmentRule rule : activeRules) {
            if ("REGION".equalsIgnoreCase(rule.getConditionType()) && rule.getConditionValue().equalsIgnoreCase(region)) {
                return rule.getAssignToUserId();
            }
            if ("SOURCE".equalsIgnoreCase(rule.getConditionType()) && rule.getConditionValue().equalsIgnoreCase(source)) {
                return rule.getAssignToUserId();
            }
        }
        return null;
    }
}