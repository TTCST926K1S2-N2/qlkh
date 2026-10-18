package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.AssignmentRuleDAO;
import vn.edu.ictu.qlkh.model.AssignmentRule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssignmentRuleServiceTest {

    private AssignmentRuleDAO dao;
    private AssignmentRuleService service;

    @BeforeEach
    void setUp() {
        dao = mock(AssignmentRuleDAO.class);
        service = new AssignmentRuleService(dao);
    }

    private AssignmentRule validRule() {
        AssignmentRule rule = new AssignmentRule();
        rule.setName("Test assignment rule");
        rule.setConditionType("SOURCE");
        rule.setConditionValue("Website");
        rule.setAssignToUserId(1L);
        rule.setPriority(1);
        rule.setActive(true);
        rule.setAssignmentMethod("DIRECT");
        return rule;
    }

    @Test
    void createValidRule() {
        when(dao.addRule(any(AssignmentRule.class))).thenReturn(true);

        assertTrue(service.createRule(validRule()));

        verify(dao).addRule(any(AssignmentRule.class));
    }

    @Test
    void rejectEmptyName() {
        AssignmentRule rule = validRule();
        rule.setName("  ");

        assertFalse(service.createRule(rule));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectInvalidConditionType() {
        AssignmentRule rule = validRule();
        rule.setConditionType("INVALID");

        assertFalse(service.createRule(rule));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectInvalidUserId() {
        AssignmentRule rule = validRule();
        rule.setAssignToUserId(0L);

        assertFalse(service.createRule(rule));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectInvalidPriority() {
        AssignmentRule rule = validRule();
        rule.setPriority(0);

        assertFalse(service.createRule(rule));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectRoundRobinUntilImplemented() {
        AssignmentRule rule = validRule();
        rule.setAssignmentMethod("ROUND_ROBIN");

        assertFalse(service.createRule(rule));
        verifyNoInteractions(dao);
    }

    @Test
    void updateValidRule() {
        AssignmentRule rule = validRule();
        rule.setId(10L);

        when(dao.updateRule(any(AssignmentRule.class))).thenReturn(true);

        assertTrue(service.updateRule(rule));

        verify(dao).updateRule(any(AssignmentRule.class));
    }

    @Test
    void rejectUpdateWithoutId() {
        AssignmentRule rule = validRule();

        assertFalse(service.updateRule(rule));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectDeleteWithInvalidId() {
        assertFalse(service.deleteRule(0));
        verifyNoInteractions(dao);
    }

    @Test
    void assignsLeadBySource() {
        AssignmentRule rule = validRule();
        rule.setAssignToUserId(5L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(rule));

        assertEquals(5L, service.determineAssignedUserId(null, "WEBSITE"));

        verify(dao).getActiveRulesSortedByPriority();
    }

    @Test
    void assignsLeadByRegion() {
        AssignmentRule rule = validRule();
        rule.setConditionType("REGION");
        rule.setConditionValue("Ha Noi");
        rule.setAssignToUserId(8L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(rule));

        assertEquals(8L, service.determineAssignedUserId("ha noi", null));
    }

    @Test
    void usesFirstMatchingRuleInPriorityOrder() {
        AssignmentRule first = validRule();
        first.setPriority(1);
        first.setAssignToUserId(10L);

        AssignmentRule second = validRule();
        second.setPriority(2);
        second.setAssignToUserId(20L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(first, second));

        assertEquals(10L, service.determineAssignedUserId(null, "Website"));
    }

    @Test
    void skipsNonMatchingRule() {
        AssignmentRule first = validRule();
        first.setConditionValue("FACEBOOK");
        first.setAssignToUserId(10L);

        AssignmentRule second = validRule();
        second.setConditionValue("WEBSITE");
        second.setAssignToUserId(20L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(first, second));

        assertEquals(20L, service.determineAssignedUserId(null, "WEBSITE"));
    }

    @Test
    void returnsNullWhenNoRuleMatches() {
        AssignmentRule rule = validRule();
        rule.setConditionValue("FACEBOOK");

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(rule));

        assertNull(service.determineAssignedUserId(null, "WEBSITE"));
    }

    @Test
    void returnsNullWhenNoActiveRules() {
        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of());

        assertNull(service.determineAssignedUserId(null, "WEBSITE"));
    }
    @Test
    void assignsLeadByIndustry() {
        AssignmentRule rule = validRule();
        rule.setConditionType("INDUSTRY");
        rule.setConditionValue("Technology");
        rule.setAssignToUserId(15L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(rule));

        assertEquals(15L,
                service.determineAssignedUserId(null, null, "technology"));
    }

    @Test
    void industryRuleDoesNotMatchMissingIndustry() {
        AssignmentRule rule = validRule();
        rule.setConditionType("INDUSTRY");
        rule.setConditionValue("Technology");

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(rule));

        assertNull(service.determineAssignedUserId(null, null, null));
    }

    @Test
    void oldMethodRemainsCompatible() {
        AssignmentRule rule = validRule();
        rule.setAssignToUserId(21L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(rule));

        assertEquals(21L,
                service.determineAssignedUserId(null, "Website"));
    }

    @Test
    void industryRuleRespectsPriorityOrder() {
        AssignmentRule first = validRule();
        first.setConditionType("INDUSTRY");
        first.setConditionValue("Technology");
        first.setAssignToUserId(30L);

        AssignmentRule second = validRule();
        second.setConditionType("SOURCE");
        second.setConditionValue("EXCEL");
        second.setAssignToUserId(40L);

        when(dao.getActiveRulesSortedByPriority())
                .thenReturn(java.util.List.of(first, second));

        assertEquals(30L,
                service.determineAssignedUserId(null, "EXCEL", "Technology"));
    }
    @Test
    void deleteValidRule() {
        when(dao.deleteRule(10L)).thenReturn(true);

        assertTrue(service.deleteRule(10L));

        verify(dao).deleteRule(10L);
    }
}