package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.service.LeadConversionService;
import vn.edu.ictu.qlkh.service.LeadConversionService.ConversionResult;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConvertLeadServletTest {

    private LeadConversionService service;
    private ConvertLeadServlet servlet;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    private StringWriter body;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {

        service = mock(LeadConversionService.class);
        servlet = new ConvertLeadServlet(service);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        body = new StringWriter();
        writer = new PrintWriter(body);

        when(response.getWriter())
                .thenReturn(writer);

        when(request.getServletPath())
                .thenReturn("/api/v1/leads/convert");
    }

    @Test
    void apiConvertSuccessReturnsCustomerData()
            throws Exception {

        authenticated(7L, "SALES");

        when(request.getParameter("leadId"))
                .thenReturn("10");

        when(service.convert(
                10L,
                7L,
                "SALES"
        )).thenReturn(
                new ConversionResult(
                        true,
                        "CONVERTED",
                        "Chuyển đổi Lead thành khách hàng thành công.",
                        10L,
                        100L,
                        200L,
                        true,
                        true
                )
        );

        servlet.doPost(request, response);

        writer.flush();

        verify(response).setStatus(
                HttpServletResponse.SC_OK
        );

        verify(response).setContentType(
                "application/json;charset=UTF-8"
        );

        String json = body.toString();

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"leadId\":10"));
        assertTrue(json.contains("\"customerId\":100"));
        assertTrue(json.contains("\"contactId\":200"));
        assertTrue(json.contains("\"customerCreated\":true"));
        assertTrue(json.contains("\"contactCreated\":true"));
    }

    @Test
    void apiRejectsInvalidLeadId()
            throws Exception {

        authenticated(7L, "SALES");

        when(request.getParameter("leadId"))
                .thenReturn("abc");

        servlet.doPost(request, response);

        writer.flush();

        verify(response).setStatus(
                HttpServletResponse.SC_BAD_REQUEST
        );

        assertTrue(
                body.toString().contains(
                        "\"code\":\"INVALID_REQUEST\""
                )
        );

        verify(service, never()).convert(
                anyLong(),
                anyLong(),
                anyString()
        );
    }

    @Test
    void apiRequiresAuthenticatedSession()
            throws Exception {

        when(request.getSession(false))
                .thenReturn(null);

        servlet.doPost(request, response);

        writer.flush();

        verify(response).setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        assertTrue(
                body.toString().contains(
                        "\"code\":\"UNAUTHORIZED\""
                )
        );

        verify(service, never()).convert(
                anyLong(),
                anyLong(),
                anyString()
        );
    }

    @Test
    void apiReturnsBadRequestWhenLeadNotEligible()
            throws Exception {

        authenticated(7L, "SALES");

        when(request.getParameter("leadId"))
                .thenReturn("20");

        when(service.convert(
                20L,
                7L,
                "SALES"
        )).thenReturn(
                new ConversionResult(
                        false,
                        "NOT_ELIGIBLE",
                        "Trạng thái Lead hiện tại không cho phép chuyển đổi.",
                        20L,
                        null,
                        null,
                        false,
                        false
                )
        );

        servlet.doPost(request, response);

        writer.flush();

        verify(response).setStatus(
                HttpServletResponse.SC_BAD_REQUEST
        );

        assertTrue(
                body.toString().contains(
                        "\"code\":\"NOT_ELIGIBLE\""
                )
        );
    }

    @Test
    void apiReturnsConflictWhenLeadAlreadyConverted()
            throws Exception {

        authenticated(7L, "SALES");

        when(request.getParameter("leadId"))
                .thenReturn("30");

        when(service.convert(
                30L,
                7L,
                "SALES"
        )).thenReturn(
                new ConversionResult(
                        false,
                        "ALREADY_CONVERTED",
                        "Lead đã được chuyển đổi trước đó.",
                        30L,
                        null,
                        null,
                        false,
                        false
                )
        );

        servlet.doPost(request, response);

        writer.flush();

        verify(response).setStatus(
                HttpServletResponse.SC_CONFLICT
        );

        assertTrue(
                body.toString().contains(
                        "\"code\":\"ALREADY_CONVERTED\""
                )
        );
    }

    @Test
    void apiReturnsConflictForDuplicateCustomerConflict()
            throws Exception {

        authenticated(9L, "MANAGER");

        when(request.getParameter("leadId"))
                .thenReturn("40");

        when(service.convert(
                40L,
                9L,
                "MANAGER"
        )).thenReturn(
                new ConversionResult(
                        false,
                        "DUPLICATE_CONFLICT",
                        "Thông tin Lead đang khớp với nhiều khách hàng khác nhau.",
                        40L,
                        null,
                        null,
                        false,
                        false
                )
        );

        servlet.doPost(request, response);

        writer.flush();

        verify(response).setStatus(
                HttpServletResponse.SC_CONFLICT
        );

        assertTrue(
                body.toString().contains(
                        "\"code\":\"DUPLICATE_CONFLICT\""
                )
        );
    }

    private void authenticated(
            long userId,
            String role
    ) {

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute("userId"))
                .thenReturn(userId);

        when(session.getAttribute("userRole"))
                .thenReturn(role);
    }
}