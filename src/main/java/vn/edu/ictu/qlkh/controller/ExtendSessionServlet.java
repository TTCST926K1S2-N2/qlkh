package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.SessionService;

import java.io.IOException;

@WebServlet("/extend-session")
public class ExtendSessionServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (!SessionService.extendSession(session)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Phiên đăng nhập đã hết hạn.\"}"
            );
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(
                "{\"success\":true,\"message\":\"Phiên đăng nhập đã được gia hạn.\"}"
        );
    }
}