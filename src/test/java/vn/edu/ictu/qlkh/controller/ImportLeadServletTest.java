package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.model.ImportLeadResult;
import vn.edu.ictu.qlkh.service.ImportLeadService;
import vn.edu.ictu.qlkh.service.PermissionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImportLeadServletTest {

    private ImportLeadService importLeadService;
    private ImportLeadServlet servlet;

    @BeforeEach
    void setUp() {

        importLeadService =
                mock(ImportLeadService.class);

        servlet =
                new ImportLeadServlet(
                        importLeadService,
                        new PermissionService()
                );
    }

    /*
     * CASE 1:
     * Chua dang nhap.
     *
     * Ket qua:
     * HTTP 401
     * Service khong duoc goi.
     */
    @Test
    void unauthenticatedPostReturns401()
            throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        StringWriter body =
                new StringWriter();

        when(request.getSession(false))
                .thenReturn(null);

        when(response.getWriter())
                .thenReturn(
                        new PrintWriter(body)
                );

        servlet.doPost(
                request,
                response
        );

        verify(response)
                .setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

        verifyNoInteractions(
                importLeadService
        );

        assertTrue(
                body.toString()
                        .contains("Chua dang nhap")
        );
    }

    /*
     * CASE 2:
     * File upload sai dinh dang.
     *
     * Ket qua:
     * HTTP 400
     * Service khong duoc goi.
     */
    @Test
    void invalidFileExtensionReturns400()
            throws Exception {

        HttpServletRequest request =
                authenticatedRequest(
                        7L,
                        "SALES"
                );

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        Part part =
                mock(Part.class);

        StringWriter body =
                new StringWriter();

        when(request.getPart("file"))
                .thenReturn(part);

        when(part.getSize())
                .thenReturn(100L);

        when(part.getSubmittedFileName())
                .thenReturn("leads.txt");

        when(response.getWriter())
                .thenReturn(
                        new PrintWriter(body)
                );

        servlet.doPost(
                request,
                response
        );

        verify(response)
                .setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

        verifyNoInteractions(
                importLeadService
        );

        assertTrue(
                body.toString()
                        .contains(
                                "Chi ho tro .xlsx hoac .xls"
                        )
        );
    }

    /*
     * CASE 3:
     * POST ?preview=true
     *
     * Servlet phai:
     * - goi previewLeads()
     * - KHONG goi importLeads()
     * - tra JSON preview=true
     */
    @Test
    void previewRequestCallsPreviewService()
            throws Exception {

        HttpServletRequest request =
                authenticatedRequest(
                        7L,
                        "SALES"
                );

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        Part part =
                validExcelPart();

        StringWriter body =
                new StringWriter();

        when(request.getPart("file"))
                .thenReturn(part);

        when(request.getParameter("preview"))
                .thenReturn("true");

        when(response.getWriter())
                .thenReturn(
                        new PrintWriter(body)
                );

        ImportLeadResult result =
                new ImportLeadResult();

        result.setTotalRows(1);
        result.incrementSuccessCount();

        result.addPreviewRow(
                2,
                "Cong ty ABC",
                "Nguyen Van A",
                "a@example.com",
                "0901234567",
                true,
                false,
                null
        );

        when(importLeadService.previewLeads(
                any(),
                eq(7L),
                eq("SALES")
        )).thenReturn(result);

        servlet.doPost(
                request,
                response
        );

        verify(importLeadService)
                .previewLeads(
                        any(),
                        eq(7L),
                        eq("SALES")
                );

        verify(importLeadService, never())
                .importLeads(
                        any(),
                        anyLong(),
                        anyString()
                );

        verify(response)
                .setStatus(
                        HttpServletResponse.SC_OK
                );

        String json =
                body.toString();

        assertTrue(
                json.contains(
                        "\"preview\":true"
                )
        );

        assertTrue(
                json.contains(
                        "\"totalRows\":1"
                )
        );

        assertTrue(
                json.contains(
                        "\"successCount\":1"
                )
        );

        assertTrue(
                json.contains(
                        "\"companyName\":\"Cong ty ABC\""
                )
        );
    }

    /*
     * CASE 4:
     * POST khong co preview=true.
     *
     * Servlet phai goi importLeads().
     */
    @Test
    void importRequestCallsImportService()
            throws Exception {

        HttpServletRequest request =
                authenticatedRequest(
                        7L,
                        "SALES"
                );

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        Part part =
                validExcelPart();

        StringWriter body =
                new StringWriter();

        when(request.getPart("file"))
                .thenReturn(part);

        when(request.getParameter("preview"))
                .thenReturn(null);

        when(response.getWriter())
                .thenReturn(
                        new PrintWriter(body)
                );

        ImportLeadResult result =
                new ImportLeadResult();

        result.setTotalRows(1);
        result.incrementSuccessCount();

        when(importLeadService.importLeads(
                any(),
                eq(7L),
                eq("SALES")
        )).thenReturn(result);

        servlet.doPost(
                request,
                response
        );

        verify(importLeadService)
                .importLeads(
                        any(),
                        eq(7L),
                        eq("SALES")
                );

        verify(importLeadService, never())
                .previewLeads(
                        any(),
                        anyLong(),
                        anyString()
                );

        verify(response)
                .setStatus(
                        HttpServletResponse.SC_OK
                );

        String json =
                body.toString();

        assertTrue(
                json.contains(
                        "\"preview\":false"
                )
        );

        assertTrue(
                json.contains(
                        "\"successCount\":1"
                )
        );
    }

    /*
     * CASE 5:
     * GET /api/v1/leads/import/template
     *
     * Phai tra file Excel mau.
     */
    @Test
    void templateEndpointReturnsExcel()
            throws Exception {

        HttpServletRequest request =
                authenticatedRequest(
                        7L,
                        "SALES"
                );

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        ServletOutputStream servletOutput =
                new ServletOutputStream() {

                    @Override
                    public void write(int b) {
                        output.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setWriteListener(
                            WriteListener listener) {
                    }
                };

        when(request.getPathInfo())
                .thenReturn("/template");

        when(response.getOutputStream())
                .thenReturn(servletOutput);

        servlet.doGet(
                request,
                response
        );

        verify(response)
                .setStatus(
                        HttpServletResponse.SC_OK
                );

        verify(response)
                .setContentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );

        verify(response)
                .setHeader(
                        "Content-Disposition",
                        "attachment; filename=lead-import-template.xlsx"
                );

        assertTrue(
                output.size() > 0
        );

        /*
         * File xlsx la ZIP container,
         * thuong bat dau bang byte PK.
         */
        byte[] bytes =
                output.toByteArray();

        assertTrue(
                bytes.length >= 2
        );

        assertEquals(
                'P',
                (char) bytes[0]
        );

        assertEquals(
                'K',
                (char) bytes[1]
        );
    }

    /*
     * Tao request da dang nhap.
     *
     * Servlet cua project lay:
     * - userId
     * - userRole
     * tu HttpSession.
     */
    private HttpServletRequest authenticatedRequest(
            long userId,
            String role) {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpSession session =
                mock(HttpSession.class);

        when(request.getSession(false))
                .thenReturn(session);

        when(session.getAttribute("userId"))
                .thenReturn(userId);

        when(session.getAttribute("userRole"))
                .thenReturn(role);

        return request;
    }

    /*
     * Part Excel gia.
     *
     * Servlet test chi can file hop le
     * de truyen InputStream sang Service mock.
     */
    private Part validExcelPart()
            throws Exception {

        Part part =
                mock(Part.class);

        when(part.getSize())
                .thenReturn(100L);

        when(part.getSubmittedFileName())
                .thenReturn("leads.xlsx");

        when(part.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                new byte[]{
                                        1, 2, 3
                                }
                        )
                );

        return part;
    }
}