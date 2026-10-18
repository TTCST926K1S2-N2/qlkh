package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.LeadConversionService;
import vn.edu.ictu.qlkh.service.LeadConversionService.ConversionResult;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

@WebServlet(
        name = "ConvertLeadServlet",
        urlPatterns = {
                "/leads/convert",
                "/leads/execute-convert",
                "/api/v1/leads/convert"
        }
)
public class ConvertLeadServlet extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("SALES", "MANAGER", "ADMIN");

    private static final Set<String> CONVERTIBLE_STATUSES =
            Set.of("NEW", "ASSIGNED", "IN_PROGRESS");

    private final LeadConversionService conversionService;

    public ConvertLeadServlet() {
        this(new LeadConversionService());
    }

    ConvertLeadServlet(LeadConversionService conversionService) {
        this.conversionService = conversionService;
    }

    private record Actor(long id, String role) {
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String action = request.getServletPath();

        if ("/api/v1/leads/convert".equals(action)) {
            sendJson(
                    response,
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    jsonError(
                            "METHOD_NOT_ALLOWED",
                            "API này chỉ hỗ trợ POST."
                    )
            );
            return;
        }

        Actor actor = actor(request);

        if (actor == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if (!ALLOWED_ROLES.contains(actor.role())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        if (!"/leads/convert".equals(action)) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        String idStr = request.getParameter("id");

        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/leads?error=not_found"
            );
            return;
        }

        try {
            long leadId = Long.parseLong(idStr);

            try (Connection conn =
                         DBConnection.getConnection()) {

                LeadInfo lead =
                        getLeadById(conn, leadId);

                if (lead == null) {

                    request.setAttribute(
                            "errorMessage",
                            "Không tìm thấy thông tin Lead trong hệ thống."
                    );

                } else {

                    boolean isQualified =
                            CONVERTIBLE_STATUSES.contains(
                                    normalizeStatus(
                                            lead.getStatus()
                                    )
                            )
                            && lead.getConvertedCustomerId() == null
                            && lead.getConvertedContactId() == null;

                    request.setAttribute(
                            "lead",
                            lead
                    );

                    request.setAttribute(
                            "isQualified",
                            isQualified
                    );
                }
            }

        } catch (NumberFormatException ex) {

            request.setAttribute(
                    "errorMessage",
                    "Mã Lead không hợp lệ."
            );

        } catch (SQLException ex) {

            ex.printStackTrace();

            request.setAttribute(
                    "errorMessage",
                    "Lỗi cơ sở dữ liệu khi tải thông tin Lead."
            );
        }

        request.getRequestDispatcher(
                "/lead/convert.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getServletPath();
        boolean api =
                "/api/v1/leads/convert".equals(action);

        if (!api
                && !"/leads/execute-convert".equals(action)) {

            if (api) {
                sendJson(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        jsonError(
                                "NOT_FOUND",
                                "Không tìm thấy API."
                        )
                );
            } else {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
            }

            return;
        }

        Actor actor = actor(request);

        if (actor == null) {

            if (api) {
                sendJson(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        jsonError(
                                "UNAUTHORIZED",
                                "Chưa đăng nhập."
                        )
                );
            } else {
                response.sendRedirect(
                        request.getContextPath() + "/login"
                );
            }

            return;
        }

        String leadIdRaw =
                request.getParameter("leadId");

        long leadId;

        try {
            leadId = Long.parseLong(
                    leadIdRaw == null
                            ? ""
                            : leadIdRaw.trim()
            );

        } catch (NumberFormatException ex) {

            if (api) {
                sendJson(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        jsonError(
                                "INVALID_REQUEST",
                                "leadId không hợp lệ."
                        )
                );
            } else {
                response.sendRedirect(
                        request.getContextPath()
                                + "/leads?error=invalid_lead"
                );
            }

            return;
        }

        try {

            ConversionResult result =
                    conversionService.convert(
                            leadId,
                            actor.id(),
                            actor.role()
                    );

            if (api) {

                sendJson(
                        response,
                        httpStatus(result),
                        toJson(result)
                );

                return;
            }

            if (result.success()) {

                response.sendRedirect(
                        request.getContextPath()
                                + "/leads?message=convert_success"
                                + "&customerId="
                                + result.customerId()
                );

            } else {

                response.sendRedirect(
                        request.getContextPath()
                                + "/leads/convert?id="
                                + leadId
                                + "&error="
                                + result.code()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                );
            }

        } catch (SQLException ex) {

            ex.printStackTrace();

            if (api) {

                sendJson(
                        response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        jsonError(
                                "DATABASE_ERROR",
                                "Không thể chuyển đổi Lead do lỗi cơ sở dữ liệu."
                        )
                );

            } else {

                response.sendRedirect(
                        request.getContextPath()
                                + "/leads?error=db_error"
                );
            }
        }
    }

    private Actor actor(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId =
                session.getAttribute("userId");

        Object userRole =
                session.getAttribute("userRole");

        if (userId == null || userRole == null) {
            return null;
        }

        try {

            long id =
                    Long.parseLong(
                            userId.toString()
                    );

            if (id <= 0) {
                return null;
            }

            String role =
                    userRole.toString()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

            return new Actor(
                    id,
                    role
            );

        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LeadInfo getLeadById(
            Connection conn,
            long id
    ) throws SQLException {

        String sql = """
                SELECT
                    l.id,
                    l.full_name,
                    l.email,
                    l.phone,
                    l.company,
                    l.source,
                    l.status,
                    l.assigned_to,
                    l.converted_customer_id,
                    l.converted_contact_id,
                    u.full_name AS owner_name
                FROM leads l
                LEFT JOIN users u
                  ON u.id = l.assigned_to
                WHERE l.id = ?
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return new LeadInfo(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("company"),
                        rs.getString("source"),
                        rs.getString("status"),
                        nullableLong(
                                rs,
                                "assigned_to"
                        ),
                        nullableLong(
                                rs,
                                "converted_customer_id"
                        ),
                        nullableLong(
                                rs,
                                "converted_contact_id"
                        ),
                        rs.getString("owner_name")
                );
            }
        }
    }

    private Long nullableLong(
            ResultSet rs,
            String column
    ) throws SQLException {

        long value =
                rs.getLong(column);

        return rs.wasNull()
                ? null
                : value;
    }

    private int httpStatus(
            ConversionResult result
    ) {

        if (result.success()) {
            return HttpServletResponse.SC_OK;
        }

        return switch (result.code()) {

            case "INVALID_REQUEST",
                 "NOT_ELIGIBLE" ->
                    HttpServletResponse.SC_BAD_REQUEST;

            case "FORBIDDEN" ->
                    HttpServletResponse.SC_FORBIDDEN;

            case "NOT_FOUND",
                 "CUSTOMER_NOT_FOUND" ->
                    HttpServletResponse.SC_NOT_FOUND;

            case "ALREADY_CONVERTED",
                 "DUPLICATE_CONFLICT" ->
                    HttpServletResponse.SC_CONFLICT;

            default ->
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        };
    }

    private String toJson(
            ConversionResult result
    ) {

        return "{"
                + "\"success\":"
                + result.success()
                + ",\"code\":\""
                + escapeJson(result.code())
                + "\""
                + ",\"message\":\""
                + escapeJson(result.message())
                + "\""
                + ",\"leadId\":"
                + result.leadId()
                + ",\"customerId\":"
                + nullableNumber(
                        result.customerId()
                )
                + ",\"contactId\":"
                + nullableNumber(
                        result.contactId()
                )
                + ",\"customerCreated\":"
                + result.customerCreated()
                + ",\"contactCreated\":"
                + result.contactCreated()
                + "}";
    }

    private String jsonError(
            String code,
            String message
    ) {

        return "{"
                + "\"success\":false"
                + ",\"code\":\""
                + escapeJson(code)
                + "\""
                + ",\"message\":\""
                + escapeJson(message)
                + "\""
                + "}";
    }

    private String nullableNumber(
            Long value
    ) {

        return value == null
                ? "null"
                : value.toString();
    }

    private void sendJson(
            HttpServletResponse response,
            int status,
            String json
    ) throws IOException {

        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "application/json;charset=UTF-8"
        );
        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        response.getWriter().write(json);
    }

    private String normalizeStatus(
            String status
    ) {

        return status == null
                ? ""
                : status.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );
    }

    private String escapeJson(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public static class LeadInfo {

        private final long id;
        private final String fullName;
        private final String email;
        private final String phone;
        private final String company;
        private final String source;
        private final String status;
        private final Long assignedTo;
        private final Long convertedCustomerId;
        private final Long convertedContactId;
        private final String owner;

        public LeadInfo(
                long id,
                String fullName,
                String email,
                String phone,
                String company,
                String source,
                String status,
                Long assignedTo,
                Long convertedCustomerId,
                Long convertedContactId,
                String owner
        ) {

            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.company = company;
            this.source = source;
            this.status = status;
            this.assignedTo = assignedTo;
            this.convertedCustomerId =
                    convertedCustomerId;
            this.convertedContactId =
                    convertedContactId;
            this.owner = owner;
        }

        public long getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getCompany() {
            return company;
        }

        public String getSource() {
            return source;
        }

        public String getStatus() {
            return status;
        }

        public Long getAssignedTo() {
            return assignedTo;
        }

        public Long getConvertedCustomerId() {
            return convertedCustomerId;
        }

        public Long getConvertedContactId() {
            return convertedContactId;
        }

        public String getOwner() {
            return owner == null
                    ? "Chưa phân công"
                    : owner;
        }
    }
}