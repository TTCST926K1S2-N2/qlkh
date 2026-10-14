package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeadCaptureServletTest {

    private final LeadCaptureServlet servlet = new LeadCaptureServlet();

    private HttpServletResponse response(StringWriter output) throws Exception {
        HttpServletResponse res = mock(HttpServletResponse.class);
        when(res.getWriter()).thenReturn(new PrintWriter(output, true));
        return res;
    }

    private ServletInputStream input(String body) {
        ByteArrayInputStream stream = new ByteArrayInputStream(
                body.getBytes(StandardCharsets.UTF_8));

        return new ServletInputStream() {
            @Override
            public int read() {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                return stream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }
        };
    }

    @Test
    void oversizedContentLengthReturns413() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        StringWriter output = new StringWriter();
        HttpServletResponse res = response(output);

        when(req.getContentType()).thenReturn(
                "application/x-www-form-urlencoded");
        when(req.getContentLengthLong()).thenReturn(20000L);

        servlet.doPost(req, res);

        verify(res).setStatus(413);
        assertTrue(output.toString().contains("Request too large"));
        verify(req, never()).getInputStream();
    }

    @Test
    void oversizedChunkedRequestReturns413() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        StringWriter output = new StringWriter();
        HttpServletResponse res = response(output);

        when(req.getContentType()).thenReturn(
                "application/x-www-form-urlencoded");
        when(req.getContentLengthLong()).thenReturn(-1L);
        when(req.getInputStream()).thenReturn(input("A".repeat(20000)));

        servlet.doPost(req, res);

        verify(res).setStatus(413);
        assertTrue(output.toString().contains("Request too large"));
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        StringWriter output = new StringWriter();
        HttpServletResponse res = response(output);

        when(req.getContentType()).thenReturn("application/json");

        servlet.doPost(req, res);

        verify(res).setStatus(415);
    }

    @Test
    void duplicateParameterReturns400() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        StringWriter output = new StringWriter();
        HttpServletResponse res = response(output);

        when(req.getContentType()).thenReturn(
                "application/x-www-form-urlencoded");
        when(req.getContentLengthLong()).thenReturn(-1L);
        when(req.getInputStream()).thenReturn(
                input("fullName=Test&fullName=Duplicate"));

        servlet.doPost(req, res);

        verify(res).setStatus(400);
        assertTrue(output.toString().contains("Invalid form encoding"));
    }

    @Test
    void invalidUrlEncodingReturns400() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        StringWriter output = new StringWriter();
        HttpServletResponse res = response(output);

        when(req.getContentType()).thenReturn(
                "application/x-www-form-urlencoded");
        when(req.getContentLengthLong()).thenReturn(-1L);
        when(req.getInputStream()).thenReturn(
                input("fullName=%ZZ"));

        servlet.doPost(req, res);

        verify(res).setStatus(400);
    }

    @Test
    @SuppressWarnings("unchecked")
    void formDecodesVietnameseUtf8() throws Exception {
        Method method = LeadCaptureServlet.class.getDeclaredMethod(
                "parseForm", String.class);

        method.setAccessible(true);

        Map<String, String> params = (Map<String, String>) method.invoke(
                servlet,
                "fullName=Nguy%E1%BB%85n+V%C4%83n+A&company=C%C3%B4ng+ty"
        );

        assertEquals("Nguyễn Văn A", params.get("fullName"));
        assertEquals("Công ty", params.get("company"));
    }
}