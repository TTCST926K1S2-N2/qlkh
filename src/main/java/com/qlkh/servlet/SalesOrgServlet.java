package com.qlkh.servlet;

import com.google.gson.Gson;
import com.qlkh.dto.SalesOrgDTO;
import com.qlkh.model.SalesOrganization;
import com.qlkh.service.SalesOrgService;
import com.qlkh.service.SalesOrgServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/sales-orgs/*")
public class SalesOrgServlet extends HttpServlet {

    private final SalesOrgService salesOrgService = new SalesOrgServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                String mode = req.getParameter("mode");
                if ("tree".equalsIgnoreCase(mode)) {
                    List<SalesOrgDTO> tree = salesOrgService.getOrgTree();
                    out.print(gson.toJson(tree));
                } else {
                    List<SalesOrganization> list = salesOrgService.getAllOrgs();
                    out.print(gson.toJson(list));
                }
            } else {
                String code = pathInfo.substring(1);
                SalesOrganization org = salesOrgService.getOrgByCode(code);
                if (org != null) {
                    out.print(gson.toJson(org));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Không tìm thấy tổ chức bán hàng\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            SalesOrganization org = parseRequestBody(req, SalesOrganization.class);
            boolean created = salesOrgService.createOrg(org);
            if (created) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\": \"Tạo thành công tổ chức bán hàng\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Tạo mới thất bại\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            SalesOrganization org = parseRequestBody(req, SalesOrganization.class);
            boolean updated = salesOrgService.updateOrg(org);
            if (updated) {
                out.print("{\"message\": \"Cập nhật thành công\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Cập nhật thất bại\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Mã tổ chức bán hàng là bắt buộc\"}");
            return;
        }

        try {
            String code = pathInfo.substring(1);
            boolean deleted = salesOrgService.deleteOrg(code);
            if (deleted) {
                out.print("{\"message\": \"Xóa thành công\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Mã không tồn tại\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private <T> T parseRequestBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }
}
