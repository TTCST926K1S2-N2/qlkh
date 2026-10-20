package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.service.AssignmentRuleService;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignmentRuleControllerTest {

    private AssignmentRuleService service;
    private AssignmentRuleController controller;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter output;

    @Test
    void putRejectsMalformedUrlEncoding() throws Exception {
        put("id=1&name=%ZZ&conditionType=SOURCE"
                + "&conditionValue=Website&assignToUserId=1"
                + "&priority=1&active=true&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void deleteRejectsMalformedUrlEncoding() throws Exception {
        when(request.getReader()).thenReturn(
                new java.io.BufferedReader(
                        new StringReader("id=%ZZ")
                )
        );

        controller.doDelete(request, response);

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }
    @BeforeEach
    void setUp() throws Exception {
        service = mock(AssignmentRuleService.class);
        controller = new AssignmentRuleController(service);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        output = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(output));
    }

    private void post(Map<String, String> values) throws Exception {
        when(request.getParameter(anyString()))
                .thenAnswer(invocation -> values.get(
                        invocation.getArgument(0, String.class)
                ));
        controller.doPost(request, response);
    }

    private void put(String body) throws Exception {
        when(request.getReader()).thenReturn(
                new java.io.BufferedReader(new StringReader(body))
        );
        controller.doPut(request, response);
    }

    @Test
    void postRejectsInvalidActive() throws Exception {
        post(Map.of(
                "name", "Test",
                "conditionType", "SOURCE",
                "conditionValue", "Website",
                "assignToUserId", "1",
                "priority", "1",
                "active", "invalid",
                "assignmentMethod", "DIRECT"
        ));

        verify(response).setStatus(400);
        assertTrue(output.toString().contains("Invalid active value"));
        verifyNoInteractions(service);
    }

    @Test
    void postRejectsRoundRobin() throws Exception {
        post(Map.of(
                "name", "Test",
                "conditionType", "SOURCE",
                "conditionValue", "Website",
                "assignToUserId", "1",
                "priority", "1",
                "active", "true",
                "assignmentMethod", "ROUND_ROBIN"
        ));

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void postRejectsInvalidPriority() throws Exception {
        post(Map.of(
                "name", "Test",
                "conditionType", "SOURCE",
                "conditionValue", "Website",
                "assignToUserId", "1",
                "priority", "0",
                "active", "true",
                "assignmentMethod", "DIRECT"
        ));

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsInvalidActive() throws Exception {
        put("id=1&name=Test&conditionType=SOURCE"
                + "&conditionValue=Website&assignToUserId=1"
                + "&priority=1&active=invalid"
                + "&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        assertTrue(output.toString().contains("Invalid active value"));
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsRoundRobin() throws Exception {
        put("id=1&name=Test&conditionType=SOURCE"
                + "&conditionValue=Website&assignToUserId=1"
                + "&priority=1&active=true"
                + "&assignmentMethod=ROUND_ROBIN");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsEmptyName() throws Exception {
        put("id=1&name=+&conditionType=SOURCE"
                + "&conditionValue=Website&assignToUserId=1"
                + "&priority=1&active=true&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsInvalidConditionType() throws Exception {
        put("id=1&name=Test&conditionType=INVALID"
                + "&conditionValue=Website&assignToUserId=1"
                + "&priority=1&active=true&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsEmptyConditionValue() throws Exception {
        put("id=1&name=Test&conditionType=SOURCE"
                + "&conditionValue=+&assignToUserId=1"
                + "&priority=1&active=true&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsInvalidUserId() throws Exception {
        put("id=1&name=Test&conditionType=SOURCE"
                + "&conditionValue=Website&assignToUserId=0"
                + "&priority=1&active=true&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void putRejectsInvalidPriority() throws Exception {
        put("id=1&name=Test&conditionType=SOURCE"
                + "&conditionValue=Website&assignToUserId=1"
                + "&priority=0&active=true&assignmentMethod=DIRECT");

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }
    @Test
    void deleteRejectsInvalidId() throws Exception {
        when(request.getReader()).thenReturn(
                new java.io.BufferedReader(new StringReader("id=0"))
        );

        controller.doDelete(request, response);

        verify(response).setStatus(400);
        verifyNoInteractions(service);
    }
}