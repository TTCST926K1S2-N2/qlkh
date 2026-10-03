package com.qlkh.servlet;

import com.qlkh.dto.SalesOrgDTO;
import com.qlkh.service.SalesOrgService;
import com.qlkh.service.SalesOrgServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/v1/sales-orgs/*")
public class SalesOrgServlet extends HttpServlet {
    private SalesOrgService salesOrgService = new SalesOrgServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        String mode = req.getParameter("mode");

        if (pathInfo == null || pathInfo.equals("/")) {
            if ("tree".equalsIgnoreCase(mode)) {
                List<SalesOrgDTO> tree = salesOrgService.getSalesOrgTree();
                out.print(toJson(tree));
            } else {
                List<SalesOrgDTO> list = salesOrgService.getAllSalesOrgs();
                out.print(toJson(list));
            }
        } else {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                SalesOrgDTO dto = salesOrgService.getSalesOrgById(id);
                if (dto != null) {
                    out.print(toJson(dto));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Sales Organization not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid ID format\"}");
            }
        }
        out.flush();
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof SalesOrgDTO) {
            SalesOrgDTO dto = (SalesOrgDTO) obj;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":").append(dto.getId()).append(",");
            sb.append("\"orgCode\":").append(dto.getOrgCode() != null ? "\"" + escapeJson(dto.getOrgCode()) + "\"" : "null").append(",");
            sb.append("\"orgName\":").append(dto.getOrgName() != null ? "\"" + escapeJson(dto.getOrgName()) + "\"" : "null").append(",");
            sb.append("\"parentId\":").append(dto.getParentId() != null ? dto.getParentId() : "null").append(",");
            sb.append("\"status\":").append(dto.getStatus() != null ? "\"" + escapeJson(dto.getStatus()) + "\"" : "null").append(",");
            sb.append("\"children\":").append(toJson(dto.getChildren()));
            sb.append("}");
            return sb.toString();
        }
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
