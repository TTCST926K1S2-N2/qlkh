package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/v1/campaigns/link")
public class CampaignLinkServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(401);
            return;
        }

        String role = String.valueOf(session.getAttribute("userRole"))
                .trim().toUpperCase(Locale.ROOT);

        if (!"ADMIN".equals(role)) {
            response.sendError(403);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String type = request.getParameter("type");

        if (!"lead".equals(type) && !"opportunity".equals(type)) {
            response.sendError(400, "Loai lien ket khong hop le");
            return;
        }

        long recordId;
        Long campaignId;

        try {
            recordId = Long.parseLong(request.getParameter("recordId"));

            String campaignText = request.getParameter("campaignId");

            campaignId = campaignText == null || campaignText.isBlank()
                    ? null
                    : Long.parseLong(campaignText);

            if (recordId <= 0 || (campaignId != null && campaignId <= 0)) {
                throw new NumberFormatException();
            }
        } catch (RuntimeException ex) {
            response.sendError(400, "ID khong hop le");
            return;
        }

        String table = "lead".equals(type) ? "leads" : "opportunities";

        try (Connection connection = DBConnection.getConnection()) {

            if (campaignId != null) {
                try (PreparedStatement check = connection.prepareStatement(
                        "SELECT id FROM campaigns WHERE id = ?")) {

                    check.setLong(1, campaignId);

                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) {
                            response.sendError(404, "Khong tim thay chien dich");
                            return;
                        }
                    }
                }
            }

            String sql = "UPDATE " + table
                    + " SET campaign_id = ? WHERE id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                if (campaignId == null) {
                    ps.setNull(1, Types.BIGINT);
                } else {
                    ps.setLong(1, campaignId);
                }

                ps.setLong(2, recordId);

                if (ps.executeUpdate() == 0) {
                    response.sendError(404, "Khong tim thay ban ghi");
                    return;
                }
            }

            mapper.writeValue(response.getWriter(),
                    Map.of("success", true,
                           "message", "Cap nhat lien ket thanh cong"));

        } catch (SQLException ex) {
            getServletContext().log("Campaign linking failed", ex);
            response.sendError(500, "Khong the cap nhat lien ket");
        }
    }
}