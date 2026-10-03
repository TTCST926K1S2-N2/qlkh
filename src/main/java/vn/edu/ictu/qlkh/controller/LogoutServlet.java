package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.SessionService;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Lấy session hiện tại.
         * Không tạo session mới nếu session không tồn tại.
         */
        HttpSession session =
                request.getSession(false);

        /*
         * HTQLKH-2:
         * Đăng xuất phải làm mất hiệu lực phiên
         * ngay lập tức phía server.
         */
        SessionService.invalidateSession(session);

        /*
         * Không cho trình duyệt cache kết quả logout.
         */
        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        /*
         * Kiểm tra request có được gửi từ session.js
         * bằng fetch() hay không.
         */
        boolean ajaxRequest =
                "XMLHttpRequest".equalsIgnoreCase(
                        request.getHeader(
                                "X-Requested-With"
                        )
                );

        /*
         * =====================================================
         * TRƯỜNG HỢP 1:
         * Logout từ JavaScript / modal cảnh báo session
         * =====================================================
         *
         * session.js cần HTTP 200 + JSON.
         */
        if (ajaxRequest) {

            response.setCharacterEncoding("UTF-8");

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    "{\"success\":true,"
                    + "\"message\":\"Đăng xuất thành công.\"}"
            );

            return;
        }

        /*
         * =====================================================
         * TRƯỜNG HỢP 2:
         * Logout từ form POST trong sidebar.jsp
         * =====================================================
         *
         * Sau khi invalidate session,
         * chuyển người dùng về LoginServlet.
         */
        response.sendRedirect(
                request.getContextPath() + "/login"
        );
    }
}