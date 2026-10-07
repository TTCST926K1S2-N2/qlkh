package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.dto.CustomerCareDTO;
import vn.edu.ictu.qlkh.service.CustomerCareService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/customer-care")
public class CustomerCareServlet extends HttpServlet {

    private final CustomerCareService service =
            new CustomerCareService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || session.getAttribute("userRole") == null) {

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(
                    "{\"error\":\"Chua dang nhap\"}"
            );
            return;
        }

        long userId =
                ((Number) session.getAttribute("userId")).longValue();

        String role =
                String.valueOf(session.getAttribute("userRole"));

        try {
            List<CustomerCareDTO> list =
                    service.list(userId, role);

            StringBuilder json =
                    new StringBuilder("[");

            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }

                CustomerCareDTO c = list.get(i);

                json.append("{")
                        .append("\"customerId\":")
                        .append(c.customerId())
                        .append(",")

                        .append("\"companyName\":\"")
                        .append(escape(c.companyName()))
                        .append("\",")

                        .append("\"ownerId\":")
                        .append(c.ownerId())
                        .append(",")

                        .append("\"status\":\"")
                        .append(escape(c.status()))
                        .append("\"")

                        .append("}");
            }

            json.append("]");

            resp.getWriter().write(
                    json.toString()
            );

        } catch (SecurityException e) {

            resp.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            resp.getWriter().write(
                    "{\"error\":\"Khong co quyen truy cap\"}"
            );

        } catch (Exception e) {

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            resp.getWriter().write(
                    "{\"error\":\"Loi he thong\"}"
            );
        }
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}