package vn.edu.ictu.qlkh.servlet;

import vn.edu.ictu.qlkh.service.BusinessGroupService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/business-group/update-parent")
public class BusinessGroupServlet extends HttpServlet {

    private final BusinessGroupService businessGroupService = new BusinessGroupService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String groupIdParam = request.getParameter("id");
        String parentIdParam = request.getParameter("parentId");

        try {
            if (groupIdParam == null || groupIdParam.trim().isEmpty()) {
                throw new IllegalArgumentException("Thiếu ID nhóm kinh doanh.");
            }

            Long groupId = Long.parseLong(groupIdParam);
            Long parentId = (parentIdParam != null && !parentIdParam.trim().isEmpty()) 
                            ? Long.parseLong(parentIdParam) 
                            : null;

            // Gọi Service thực hiện cập nhật và kiểm tra quan hệ vòng
            businessGroupService.updateParentGroup(groupId, parentId);

            // Trả về kết quả thành công
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\": \"success\", \"message\": \"Cập nhật nhóm mẹ thành công!\"}");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"ID không đúng định dạng số.\"}");
        } catch (IllegalArgumentException e) {
            // Bắt lỗi validation (lặp vòng, ID không tồn tại...)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Lỗi cơ sở dữ liệu.\"}");
        }
    }
}