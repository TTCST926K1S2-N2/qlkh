package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.service.LeadDuplicateService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/leads/duplicates")
public class LeadDuplicateServlet extends HttpServlet {

    private final LeadDuplicateService service =
            new LeadDuplicateService();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null
                || session.getAttribute("userRole") == null) {

            sendError(res, 401, "Vui long dang nhap");
            return;
        }

        try {
            long userId = Long.parseLong(
                    session.getAttribute("userId").toString()
            );

            String role = session.getAttribute("userRole")
                    .toString().trim().toUpperCase();

            String email = req.getParameter("email");
            String phone = req.getParameter("phone");

            List<LeadDuplicateService.DuplicateLead> duplicates =
                    service.findDuplicates(
                            userId,
                            role,
                            email,
                            phone
                    );

            List<Map<String, Object>> data = duplicates.stream()
                    .map(this::toResponse)
                    .toList();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("total", data.size());
            response.put("data", data);

            mapper.writeValue(res.getWriter(), response);

        } catch (NumberFormatException e) {
            sendError(res, 401, "Tai khoan khong hop le");

        } catch (SecurityException e) {
            sendError(res, 403, e.getMessage());

        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());

        } catch (SQLException e) {
            getServletContext().log(
                    "Loi truy van khi kiem tra lead trung", e
            );
            sendError(res, 500, "Khong the kiem tra lead trung");
        }
    }

    private Map<String, Object> toResponse(
            LeadDuplicateService.DuplicateLead duplicate) {

        Customer customer = duplicate.customer();
        Contact contact = duplicate.contact();

        Map<String, Object> customerData = new LinkedHashMap<>();
        customerData.put("id", customer.getId());
        customerData.put("companyName", customer.getCompanyName());
        customerData.put("taxCode", customer.getTaxCode());
        customerData.put("status", customer.getStatus());
        customerData.put("ownerId", customer.getOwnerId());

        Map<String, Object> contactData = new LinkedHashMap<>();
        contactData.put("id", contact.getId());
        contactData.put("fullName", contact.getFullName());
        contactData.put("email", contact.getEmail());
        contactData.put("phone", contact.getPhone());

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customer", customerData);
        item.put("contact", contactData);
        item.put("reasons", duplicate.reasons());

        return item;
    }

    private void sendError(
            HttpServletResponse res,
            int status,
            String message) throws IOException {

        res.setStatus(status);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);

        mapper.writeValue(res.getWriter(), error);
    }
}