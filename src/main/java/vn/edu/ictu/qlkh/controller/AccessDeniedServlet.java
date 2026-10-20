package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * HTQLKH-7
 *
 * Hiển thị trang thông báo khi người dùng đã đăng nhập
 * nhưng không có quyền truy cập chức năng.
 */
@WebServlet("/access-denied")
public class AccessDeniedServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        showAccessDenied(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        showAccessDenied(request, response);
    }

    private void showAccessDenied(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Bắt buộc trả HTTP 403 Forbidden.
         */
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        if (request.getAttribute("accessDeniedMessage") == null) {
            request.setAttribute(
                    "accessDeniedMessage",
                    "Bạn không có quyền truy cập chức năng này."
            );
        }

        request.getRequestDispatcher(
                "/views/errors/access-denied.jsp"
        ).forward(request, response);
    }
}