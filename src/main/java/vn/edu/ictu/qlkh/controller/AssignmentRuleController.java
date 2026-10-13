package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.model.AssignmentRule;
import vn.edu.ictu.qlkh.service.AssignmentRuleService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/assignment-rules")
public class AssignmentRuleController extends HttpServlet {
    private final AssignmentRuleService assignmentRuleService = new AssignmentRuleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        List<AssignmentRule> rules = assignmentRuleService.getAllRules();
        out.write("{\"status\": \"success\", \"total\": " + rules.size() + "}");
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        String name = req.getParameter("name");
        String conditionType = req.getParameter("conditionType");
        String conditionValue = req.getParameter("conditionValue");
        String assignToUserIdStr = req.getParameter("assignToUserId");

        if (name != null && assignToUserIdStr != null) {
            AssignmentRule rule = new AssignmentRule();
            rule.setName(name);
            rule.setConditionType(conditionType);
            rule.setConditionValue(conditionValue);
            rule.setAssignToUserId(Long.parseLong(assignToUserIdStr));
            rule.setPriority(1);
            rule.setActive(true);

            boolean created = assignmentRuleService.createRule(rule);
            if (created) {
                out.write("{\"status\": \"success\", \"message\": \"Tạo quy tắc thành công\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"status\": \"error\", \"message\": \"Không thể tạo quy tắc\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"status\": \"error\", \"message\": \"Thiếu thông tin dữ liệu\"}");
        }
        out.flush();
    }
}