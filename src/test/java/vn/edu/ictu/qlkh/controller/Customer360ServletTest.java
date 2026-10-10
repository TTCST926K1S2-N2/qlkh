package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dto.Customer360DTO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.service.Customer360Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Customer360ServletTest {
    private static class ServiceStub extends Customer360Service {
        Customer360DTO result;
        int calls;
        @Override
        public Customer360DTO get(long customerId, long userId, String role)
                throws SQLException {
            calls++;
            return result;
        }
    }

    private static class Response {
        int status;
        String contentType;
        StringWriter content = new StringWriter();
        PrintWriter writer = new PrintWriter(content);
        HttpServletResponse proxy = (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                (obj, method, args) -> {
                    switch (method.getName()) {
                        case "setStatus" -> status = (Integer) args[0];
                        case "setContentType" -> contentType = (String) args[0];
                        case "getWriter" -> { return writer; }
                    }
                    return null;
                });
    }

    private HttpServletRequest request(String path, Map<String, Object> attrs) {
        HttpSession session = attrs == null ? null :
                (HttpSession) Proxy.newProxyInstance(
                        getClass().getClassLoader(), new Class<?>[]{HttpSession.class},
                        (obj, method, args) ->
                                "getAttribute".equals(method.getName())
                                        ? attrs.get((String) args[0]) : null);
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (obj, method, args) -> switch (method.getName()) {
                    case "getPathInfo" -> path;
                    case "getSession" -> session;
                    default -> null;
                });
    }

    @Test
    void getReturnsJsonAndNoStore() throws Exception {
        ServiceStub service = new ServiceStub();
        Customer customer = new Customer();
        customer.setId(42L);
        customer.setOwnerId(7L);
        customer.setCompanyName("ABC");
        service.result = new Customer360DTO(customer, List.of(), List.of(),
                List.of(), List.of(),
                new Customer360DTO.Availability(false, false, false, false));
        Response response = new Response();
        new Customer360Servlet(service).doGet(
                request("/42", Map.of("userId", 7L, "userRole", "SALES")),
                response.proxy);
        assertEquals(200, response.status);
        assertEquals("application/json;charset=UTF-8", response.contentType);
        assertTrue(response.content.toString().contains("\"companyName\":\"ABC\""));
        assertEquals(1, service.calls);
    }

    @Test
    void noSessionIsUnauthorized() throws Exception {
        ServiceStub service = new ServiceStub();
        Response response = new Response();
        new Customer360Servlet(service).doGet(request("/42", null), response.proxy);
        assertEquals(401, response.status);
        assertEquals(0, service.calls);
    }

    @Test
    void unknownRoleIsForbidden() throws Exception {
        ServiceStub service = new ServiceStub();
        Response response = new Response();
        new Customer360Servlet(service).doGet(
                request("/42", Map.of("userId", 7L, "userRole", "GUEST")),
                response.proxy);
        assertEquals(403, response.status);
        assertEquals(0, service.calls);
    }

    @Test
    void malformedPathIsBadRequest() throws Exception {
        ServiceStub service = new ServiceStub();
        Response response = new Response();
        new Customer360Servlet(service).doGet(
                request("/42/other", Map.of("userId", 7L, "userRole", "ADMIN")),
                response.proxy);
        assertEquals(400, response.status);
        assertEquals(0, service.calls);
    }

    @Test
    void missingOrOutOfScopeCustomerIsNotFound() throws Exception {
        ServiceStub service = new ServiceStub();
        Response response = new Response();
        new Customer360Servlet(service).doGet(
                request("/42", Map.of("userId", 7L, "userRole", "SALES")),
                response.proxy);
        assertEquals(404, response.status);
        assertEquals(1, service.calls);
    }
}
