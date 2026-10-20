package vn.edu.ictu.qlkh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.Lead;
import vn.edu.ictu.qlkh.service.LeadDuplicateService;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeadDuplicateServletTest {

    private LeadDuplicateService service;
    private LeadDuplicateServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseWriter;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        service = mock(LeadDuplicateService.class);
        servlet = new LeadDuplicateServlet(service);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        responseWriter = new StringWriter();

        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    private void prepareValidSession(long userId, String role) {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(userId);
        when(session.getAttribute("userRole")).thenReturn(role);
    }

    @Test
    @DisplayName("Trả về 401 khi chưa đăng nhập (không có session)")
    void shouldReturn401WhenNotLoggedIn() throws IOException {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(401);
        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(false, result.get("success"));
        assertEquals("Vui long dang nhap", result.get("message"));
    }

    @Test
    @DisplayName("Trả về 401 khi userId trong session không hợp lệ")
    void shouldReturn401WhenUserIdInvalid() throws IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(-5L);
        when(session.getAttribute("userRole")).thenReturn("SALES");

        servlet.doGet(request, response);

        verify(response).setStatus(401);
        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(false, result.get("success"));
        assertEquals("Tai khoan khong hop le", result.get("message"));
    }

    @Test
    @DisplayName("Trả về 403 khi người dùng không đủ quyền hạn truy cập (SecurityException)")
    void shouldReturn403WhenPermissionDenied() throws Exception {
        prepareValidSession(7L, "GUEST");
        when(request.getParameter("email")).thenReturn("lead@example.com");

        when(service.findDuplicates(7L, "GUEST", "lead@example.com", null))
                .thenThrow(new SecurityException("Khong co quyen truy cap"));

        servlet.doGet(request, response);

        verify(response).setStatus(403);
        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(false, result.get("success"));
        assertEquals("Khong co quyen truy cap", result.get("message"));
    }

    @Test
    @DisplayName("Trả về 400 khi thiếu cả email và số điện thoại (IllegalArgumentException)")
    void shouldReturn400WhenMissingBothEmailAndPhone() throws Exception {
        prepareValidSession(7L, "SALES");
        when(request.getParameter("email")).thenReturn("   ");
        when(request.getParameter("phone")).thenReturn(null);

        when(service.findDuplicates(7L, "SALES", "   ", null))
                .thenThrow(new IllegalArgumentException("Can cung cap email hoac so dien thoai"));

        servlet.doGet(request, response);

        verify(response).setStatus(400);
        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(false, result.get("success"));
        assertEquals("Can cung cap email hoac so dien thoai", result.get("message"));
    }

    @Test
    @DisplayName("Trả về 200 và danh sách lead trùng khi phát hiện trùng lặp qua GET")
    void shouldReturn200WithDuplicatesFound() throws Exception {
        prepareValidSession(7L, "SALES");
        when(request.getParameter("email")).thenReturn("lead@example.com");
        when(request.getParameter("phone")).thenReturn("0987654321");

        Lead lead = new Lead(10L, "Tran Van B", "lead@example.com", "0987654321", "Cong ty B", "NEW");
        LeadDuplicateService.DuplicateLead dup =
                new LeadDuplicateService.DuplicateLead(lead, List.of("EMAIL", "PHONE"));

        when(service.findDuplicates(7L, "SALES", "lead@example.com", "0987654321"))
                .thenReturn(List.of(dup));

        servlet.doGet(request, response);

        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(true, result.get("success"));
        assertEquals(1, result.get("total"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
        assertEquals(1, data.size());
        assertEquals("LEAD", data.get(0).get("duplicateType"));
        assertEquals("Tran Van B", data.get(0).get("fullName"));
        assertEquals(List.of("EMAIL", "PHONE"), data.get(0).get("reasons"));
    }

    @Test
    @DisplayName("Trả về 200 và danh sách rỗng khi không có lead trùng qua GET")
    void shouldReturn200WhenNoDuplicate() throws Exception {
        prepareValidSession(7L, "SALES");
        when(request.getParameter("email")).thenReturn("unique@example.com");

        when(service.findDuplicates(7L, "SALES", "unique@example.com", null))
                .thenReturn(List.of());

        servlet.doGet(request, response);

        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(true, result.get("success"));
        assertEquals(0, result.get("total"));

        @SuppressWarnings("unchecked")
        List<?> data = (List<?>) result.get("data");
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("Hỗ trợ kiểm tra trùng qua POST với JSON body")
    void shouldSupportPostWithJsonBody() throws Exception {
        prepareValidSession(7L, "SALES");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        String jsonInput = "{\"email\":\"post@example.com\",\"phone\":\"0988888888\"}";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonInput.getBytes());
        ServletInputStream sis = new DelegatingServletInputStream(bais);
        when(request.getInputStream()).thenReturn(sis);

        Lead lead = new Lead(22L, "Nguyen Post", "post@example.com", "0988888888", "Post Corp", "NEW");
        when(service.findDuplicates(7L, "SALES", "post@example.com", "0988888888"))
                .thenReturn(List.of(new LeadDuplicateService.DuplicateLead(lead, List.of("EMAIL"))));

        servlet.doPost(request, response);

        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(true, result.get("success"));
        assertEquals(1, result.get("total"));
    }

    @Test
    @DisplayName("Trả về 500 khi xảy ra lỗi cơ sở dữ liệu (SQLException)")
    void shouldReturn500OnDatabaseError() throws Exception {
        prepareValidSession(7L, "SALES");
        when(request.getParameter("email")).thenReturn("error@example.com");

        when(service.findDuplicates(7L, "SALES", "error@example.com", null))
                .thenThrow(new SQLException("DB connection failure"));

        jakarta.servlet.ServletContext context = mock(jakarta.servlet.ServletContext.class);
        servlet.init(new jakarta.servlet.ServletConfig() {
            @Override
            public String getServletName() { return "LeadDuplicateServlet"; }
            @Override
            public jakarta.servlet.ServletContext getServletContext() { return context; }
            @Override
            public String getInitParameter(String name) { return null; }
            @Override
            public java.util.Enumeration<String> getInitParameterNames() { return java.util.Collections.emptyEnumeration(); }
        });

        servlet.doGet(request, response);

        verify(response).setStatus(500);
        Map<?, ?> result = mapper.readValue(responseWriter.toString(), Map.class);
        assertEquals(false, result.get("success"));
        assertEquals("Khong the kiem tra lead trung", result.get("message"));
    }

    private static class DelegatingServletInputStream extends ServletInputStream {
        private final InputStream sourceStream;
        public DelegatingServletInputStream(InputStream sourceStream) {
            this.sourceStream = sourceStream;
        }
        @Override
        public int read() throws IOException {
            return sourceStream.read();
        }
        @Override
        public boolean isFinished() {
            try {
                return sourceStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }
        @Override
        public boolean isReady() {
            return true;
        }
        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {}
    }
}
