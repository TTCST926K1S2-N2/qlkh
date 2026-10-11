package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ContractServletTest {

    @Test
    void unauthenticatedGetReturns401() throws Exception {
        ContractServlet servlet = new ContractServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, res);

        verify(res).sendError(401);
    }

    @Test
    void unauthenticatedPutReturns401() throws Exception {
        ContractServlet servlet = new ContractServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        servlet.doPut(req, res);

        verify(res).sendError(401);
    }

    @Test
    void userRoleCannotUpdateContract() throws Exception {
        ContractServlet servlet = new ContractServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(9001L);
        when(session.getAttribute("userRole")).thenReturn("USER");

        servlet.doPut(req, res);

        verify(res).sendError(403);
    }

    @Test
    void putBodyDecodesUtf8() throws Exception {
        ContractServlet servlet = new ContractServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);

        String body = "customerId=123&contractCode=HD01&contractName=H%E1%BB%A3p+%C4%91%E1%BB%93ng&contractValue=1500000.00&status=SIGNED&signedDate=2026-10-12";

        when(req.getContentType()).thenReturn(
                "application/x-www-form-urlencoded;charset=UTF-8");

        jakarta.servlet.ServletInputStream stream =
                new jakarta.servlet.ServletInputStream() {
                    private final java.io.ByteArrayInputStream input =
                            new java.io.ByteArrayInputStream(
                                    body.getBytes(java.nio.charset.StandardCharsets.UTF_8));

                    @Override
                    public int read() {
                        return input.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return input.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(jakarta.servlet.ReadListener listener) {
                    }
                };

        when(req.getInputStream()).thenReturn(stream);

        java.lang.reflect.Method method =
                ContractServlet.class.getDeclaredMethod(
                        "parameters", HttpServletRequest.class, boolean.class);

        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.Map<String,String> params =
                (java.util.Map<String,String>) method.invoke(servlet, req, true);

        org.junit.jupiter.api.Assertions.assertEquals(
                "Hợp đồng", params.get("contractName"));

        org.junit.jupiter.api.Assertions.assertEquals(
                "1500000.00", params.get("contractValue"));

        org.junit.jupiter.api.Assertions.assertEquals(
                "SIGNED", params.get("status"));
    }
}