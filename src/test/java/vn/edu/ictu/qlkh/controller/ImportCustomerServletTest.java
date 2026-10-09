package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.model.ImportCustomerResult;
import vn.edu.ictu.qlkh.service.ImportCustomerService;
import vn.edu.ictu.qlkh.service.PermissionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImportCustomerServletTest {

    @Test
    void post_shouldReturn401WhenNotLoggedIn()
            throws Exception {

        ImportCustomerService service =
                mock(ImportCustomerService.class);

        ImportCustomerServlet servlet =
                new ImportCustomerServlet(
                        service,
                        new PermissionService()
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        ByteArrayOutputStream body =
                new ByteArrayOutputStream();

        PrintWriter writer =
                new PrintWriter(body, true);

        when(request.getSession(false))
                .thenReturn(null);

        when(response.getWriter())
                .thenReturn(writer);

        servlet.doPost(request, response);

        verify(response).setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        writer.flush();

        assertTrue(
                body.toString()
                        .contains("Chua dang nhap")
        );
    }

    @Test
    void post_shouldReturn403WhenRoleHasNoPermission()
            throws Exception {

        ImportCustomerService service =
                mock(ImportCustomerService.class);

        ImportCustomerServlet servlet =
                new ImportCustomerServlet(
                        service,
                        new PermissionService()
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        HttpSession session =
                mock(HttpSession.class);

        ByteArrayOutputStream body =
                new ByteArrayOutputStream();

        PrintWriter writer =
                new PrintWriter(body, true);

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute("userId"))
                .thenReturn(7L);

        when(session.getAttribute("userRole"))
                .thenReturn("VIEWER");

        when(response.getWriter())
                .thenReturn(writer);

        servlet.doPost(request, response);

        verify(response).setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        writer.flush();

        assertTrue(
                body.toString()
                        .contains("khong co quyen")
        );
    }

    @Test
    void post_shouldRejectNonExcelFile()
            throws Exception {

        ImportCustomerService service =
                mock(ImportCustomerService.class);

        ImportCustomerServlet servlet =
                new ImportCustomerServlet(
                        service,
                        new PermissionService()
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        HttpSession session =
                mock(HttpSession.class);

        Part part =
                mock(Part.class);

        ByteArrayOutputStream body =
                new ByteArrayOutputStream();

        PrintWriter writer =
                new PrintWriter(body, true);

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute("userId"))
                .thenReturn(7L);

        when(session.getAttribute("userRole"))
                .thenReturn("ADMIN");

        when(request.getPart("file"))
                .thenReturn(part);

        when(part.getSize())
                .thenReturn(100L);

        when(part.getSubmittedFileName())
                .thenReturn("customers.txt");

        when(response.getWriter())
                .thenReturn(writer);

        servlet.doPost(request, response);

        verify(response).setStatus(
                HttpServletResponse.SC_BAD_REQUEST
        );

        writer.flush();

        assertTrue(
                body.toString()
                        .contains(
                                "Chi ho tro .xlsx hoac .xls"
                        )
        );
    }

    @Test
    void postPreview_shouldReturnImportResult()
            throws Exception {

        ImportCustomerService service =
                mock(ImportCustomerService.class);

        ImportCustomerServlet servlet =
                new ImportCustomerServlet(
                        service,
                        new PermissionService()
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        HttpSession session =
                mock(HttpSession.class);

        Part part =
                mock(Part.class);

        ByteArrayOutputStream body =
                new ByteArrayOutputStream();

        PrintWriter writer =
                new PrintWriter(body, true);

        ImportCustomerResult result =
                new ImportCustomerResult();

        result.setTotalRows(2);
        result.incrementSuccessCount();

        result.addError(
                3,
                "0109999999",
                "Ma so thue da ton tai"
        );

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute("userId"))
                .thenReturn(7L);

        when(session.getAttribute("userRole"))
                .thenReturn("ADMIN");

        when(request.getPart("file"))
                .thenReturn(part);

        when(request.getParameter("preview"))
                .thenReturn("true");

        when(part.getSize())
                .thenReturn(100L);

        when(part.getSubmittedFileName())
                .thenReturn("customers.xlsx");

        when(part.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                new byte[]{1}
                        )
                );

        when(
                service.previewCustomers(
                        any(InputStream.class),
                        eq(7L),
                        eq("ADMIN")
                )
        ).thenReturn(result);

        when(response.getWriter())
                .thenReturn(writer);

        servlet.doPost(request, response);

        writer.flush();

        String json =
                body.toString();

        assertTrue(json.contains(
                "\"preview\":true"
        ));

        assertTrue(json.contains(
                "\"totalRows\":2"
        ));

        assertTrue(json.contains(
                "\"successCount\":1"
        ));

        assertTrue(json.contains(
                "\"failedCount\":1"
        ));

        assertTrue(json.contains(
                "\"row\":3"
        ));

        assertTrue(json.contains(
                "\"taxCode\":\"0109999999\""
        ));
    }

    @Test
    void getTemplate_shouldReturnXlsxFile()
            throws Exception {

        ImportCustomerService service =
                mock(ImportCustomerService.class);

        ImportCustomerServlet servlet =
                new ImportCustomerServlet(
                        service,
                        new PermissionService()
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        HttpSession session =
                mock(HttpSession.class);

        ByteArrayOutputStream body =
                new ByteArrayOutputStream();

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute("userId"))
                .thenReturn(7L);

        when(session.getAttribute("userRole"))
                .thenReturn("ADMIN");

        when(request.getPathInfo())
                .thenReturn("/template");

        when(response.getOutputStream())
                .thenReturn(
                        new ServletOutputStream() {

                            @Override
                            public boolean isReady() {
                                return true;
                            }

                            @Override
                            public void setWriteListener(
                                    WriteListener listener) {
                            }

                            @Override
                            public void write(int b) {
                                body.write(b);
                            }
                        }
                );

        servlet.doGet(request, response);

        verify(response).setStatus(
                HttpServletResponse.SC_OK
        );

        verify(response).setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        verify(response).setHeader(
                eq("Content-Disposition"),
                contains(
                        "customer-import-template.xlsx"
                )
        );

        assertTrue(body.size() > 100);

        byte[] bytes =
                body.toByteArray();

        assertEquals(
                'P',
                (char) bytes[0]
        );

        assertEquals(
                'K',
                (char) bytes[1]
        );
    }
}