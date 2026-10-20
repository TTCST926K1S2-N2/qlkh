package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * HTQLKH-10:
 * Điểm vào chức năng Khóa tài khoản & Bàn giao.
 *
 * Admin được chuyển tới danh sách tài khoản để
 * chọn tài khoản cần khóa hoặc mở khóa.
 */
@WebServlet("/lock-transfer")
public class AccountLockServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/users"
        );
    }
}