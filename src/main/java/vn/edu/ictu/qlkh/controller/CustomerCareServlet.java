package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.dao.AuditLogDAO;
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

    private final CustomerCareService service = new CustomerCareService();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        prepareResponse(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chua dang nhap");
            return;
        }

        try {
            long userId = ((Number) session.getAttribute("userId")).longValue();
            String role = String.valueOf(session.getAttribute("userRole"));
            List<CustomerCareDTO> list = service.list(userId, role);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                CustomerCareDTO c = list.get(i);
                if (i > 0) json.append(",");

                json.append("{")
                    .append("\"customerId\":").append(c.customerId()).append(",")
                    .append("\"companyName\":\"").append(escape(c.companyName())).append("\",")
                    .append("\"ownerId\":").append(c.ownerId()).append(",")
                    .append("\"status\":\"").append(escape(c.status())).append("\",")
                    .append("\"contractValue\":")
                    .append(c.contractValue() == null
                            ? "null" : c.contractValue().toPlainString())
                    .append(",")
                    .append("\"daysInactive\":").append(c.daysInactive()).append(",")
                    .append("\"extraWarning\":").append(c.extraWarning())
                    .append("}");
            }

            resp.getWriter().write(json.append("]").toString());

        } catch (SecurityException e) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN,
                    "Khong co quyen truy cap");
        } catch (Exception e) {
            getServletContext().log("Customer Care GET failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Loi he thong");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        prepareResponse(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chua dang nhap");
            return;
        }

        long customerId;
        try {
            customerId = Long.parseLong(req.getParameter("customerId"));
            if (customerId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException | NullPointerException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "customerId khong hop le");
            return;
        }

        try {
            long userId = ((Number) session.getAttribute("userId")).longValue();
            String role = String.valueOf(session.getAttribute("userRole"));

            boolean hasAccess = service.list(userId, role).stream()
                    .anyMatch(c -> c.customerId() == customerId);

            if (!hasAccess) {
                writeError(resp, HttpServletResponse.SC_FORBIDDEN,
                        "Khong co quyen thao tac khach hang nay");
                return;
            }

            Object sessionName = session.getAttribute("userName");
            String username = sessionName == null
                    ? String.valueOf(userId) : String.valueOf(sessionName);

            boolean logged = auditLogDAO.logAction(
                    username,
                    "CUSTOMER_CONTACTED",
                    "CUSTOMER:" + customerId,
                    "Da lien he khach hang"
            );

            if (!logged) {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Khong the ghi nhat ky lien he");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(
                    "{\"success\":true,\"message\":\"Da ghi nhan lien he\"}");

        } catch (SecurityException e) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN,
                    "Khong co quyen truy cap");
        } catch (Exception e) {
            getServletContext().log("Customer Care POST failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Loi he thong");
        }
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute("userId") instanceof Number
                && session.getAttribute("userRole") != null;
    }

    private void prepareResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    private void writeError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"error\":\"" + escape(message) + "\"}");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}