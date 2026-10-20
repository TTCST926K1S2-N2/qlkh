package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.AssignmentRuleDAO;
import vn.edu.ictu.qlkh.model.AssignmentRule;
import java.util.List;

public class AssignmentRuleService {
    private final AssignmentRuleDAO assignmentRuleDAO;

    public AssignmentRuleService() {
        this(new AssignmentRuleDAO());
    }

    public AssignmentRuleService(AssignmentRuleDAO assignmentRuleDAO) {
        this.assignmentRuleDAO = java.util.Objects.requireNonNull(assignmentRuleDAO);
    }

    public List<AssignmentRule> getAllRules() {
        return assignmentRuleDAO.getAllRules();
    }

    public boolean createRule(AssignmentRule rule) {
        if (!isValidRule(rule)) {
            return false;
        }
        return assignmentRuleDAO.addRule(rule);
    }

    public Long determineAssignedUserId(String region, String source) {
        return determineAssignedUserId(region, source, null);
    }

    public Long determineAssignedUserId(String region, String source, String industry) {
        List<AssignmentRule> activeRules = assignmentRuleDAO.getActiveRulesSortedByPriority();

        for (AssignmentRule rule : activeRules) {
            String type = rule.getConditionType();
            String value = rule.getConditionValue();

            if (type == null || value == null) {
                continue;
            }

            if ("REGION".equalsIgnoreCase(type)
                    && region != null
                    && value.equalsIgnoreCase(region.trim())) {
                return rule.getAssignToUserId();
            }

            if ("SOURCE".equalsIgnoreCase(type)
                    && source != null
                    && value.equalsIgnoreCase(source.trim())) {
                return rule.getAssignToUserId();
            }

            if ("INDUSTRY".equalsIgnoreCase(type)
                    && industry != null
                    && value.equalsIgnoreCase(industry.trim())) {
                return rule.getAssignToUserId();
            }
        }

        return null;
    }

    public boolean updateRule(AssignmentRule rule) {
        if (!isValidRule(rule) || rule.getId() == null || rule.getId() <= 0) {
            return false;
        }
        return assignmentRuleDAO.updateRule(rule);
    }

    public boolean deleteRule(long id) {
        if (id <= 0) {
            return false;
        }
        return assignmentRuleDAO.deleteRule(id);
    }

    private boolean isValidRule(AssignmentRule rule) {
        if (rule == null
                || rule.getName() == null || rule.getName().trim().isEmpty()
                || rule.getConditionValue() == null
                || rule.getConditionValue().trim().isEmpty()
                || rule.getAssignToUserId() == null
                || rule.getAssignToUserId() <= 0
                || rule.getPriority() == null
                || rule.getPriority() <= 0) {
            return false;
        }

        String type = rule.getConditionType();
        if (!("SOURCE".equals(type)
                || "REGION".equals(type)
                || "INDUSTRY".equals(type))) {
            return false;
        }

        String method = rule.getAssignmentMethod();
        return "DIRECT".equals(method);
    }
}