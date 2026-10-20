package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.LeadDAO;
import vn.edu.ictu.qlkh.model.ImportLeadResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImportLeadServiceTest {

    private LeadDAO leadDAO;

    private ImportLeadService service;

    @BeforeEach
    void setUp() {
        leadDAO = mock(LeadDAO.class);
        service = new ImportLeadService(leadDAO);
    }

    /*
     * CASE 1:
     * File hop le + Preview.
     *
     * Ket qua mong doi:
     * - 1 dong
     * - thanh cong 1
     * - loi 0
     * - KHONG ghi database
     */
    @Test
    void previewValidFileDoesNotWriteDatabase()
            throws Exception {

        InputStream excel =
                createExcel(
                        new String[][]{
                                {
                                        "Cong ty ABC",
                                        "Nguyen Van A",
                                        "a@example.com",
                                        "0901234567",
                                        "Truong phong",
                                        "",
                                        "Cong nghe thong tin",
                                        "50-100",
                                        "https://example.vn",
                                        "Ha Noi"
                                }
                        }
                );

        ImportLeadResult result =
                service.previewLeads(
                        excel,
                        1L,
                        "SALES"
                );

        assertEquals(
                1,
                result.getTotalRows()
        );

        assertEquals(
                1,
                result.getSuccessCount()
        );

        assertEquals(
                0,
                result.getFailedCount()
        );

        assertEquals(
                1,
                result.getRows().size()
        );

        assertTrue(
                result.getRows()
                        .get(0)
                        .isValid()
        );

        /*
         * Preview tuyet doi khong INSERT Lead.
         */
        verify(
                leadDAO,
                never()
        ).createImportedLead(
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class),
                nullable(String.class)
        );
    }

    /*
     * CASE 2:
     * Du lieu Email sai.
     */
    @Test
    void invalidEmailCreatesRowError()
            throws Exception {

        InputStream excel =
                createExcel(
                        new String[][]{
                                {
                                        "Cong ty ABC",
                                        "Nguyen Van A",
                                        "email-sai",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""
                                }
                        }
                );

        ImportLeadResult result =
                service.previewLeads(
                        excel,
                        1L,
                        "SALES"
                );

        assertEquals(
                1,
                result.getTotalRows()
        );

        assertEquals(
                0,
                result.getSuccessCount()
        );

        assertEquals(
                1,
                result.getFailedCount()
        );

        assertEquals(
                "Email khong hop le",
                result.getErrors()
                        .get(0)
                        .getMessage()
        );

        assertFalse(
                result.getRows()
                        .get(0)
                        .isValid()
        );
    }

    /*
     * CASE 3:
     * Hai dong trong chinh Excel
     * dung cung mot Email.
     */
    @Test
    void duplicateEmailInsideExcelIsDetected()
            throws Exception {

        InputStream excel =
                createExcel(
                        new String[][]{
                                {
                                        "Cong ty A",
                                        "Nguyen Van A",
                                        "same@example.com",
                                        "0901111111",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""
                                },
                                {
                                        "Cong ty B",
                                        "Nguyen Van B",
                                        "same@example.com",
                                        "0902222222",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""
                                }
                        }
                );

        ImportLeadResult result =
                service.previewLeads(
                        excel,
                        1L,
                        "SALES"
                );

        assertEquals(
                2,
                result.getTotalRows()
        );

        assertEquals(
                1,
                result.getSuccessCount()
        );

        assertEquals(
                1,
                result.getFailedCount()
        );

        assertTrue(
                result.getRows()
                        .get(1)
                        .isDuplicate()
        );

        assertEquals(
                "Email da ton tai",
                result.getErrors()
                        .get(0)
                        .getMessage()
        );
    }

    /*
     * CASE 4:
     * Email da ton tai trong bang leads.
     */
    @Test
    void duplicateEmailFromExistingLeadIsDetected()
            throws Exception {

        when(leadDAO.existsByEmailOrPhone(
                eq("existing@example.com"),
                isNull()
        )).thenReturn(true);

        InputStream excel =
                createExcel(
                        new String[][]{
                                {
                                        "Cong ty Moi",
                                        "Nguyen Van A",
                                        "EXISTING@example.com",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""
                                }
                        }
                );

        ImportLeadResult result =
                service.previewLeads(
                        excel,
                        1L,
                        "SALES"
                );

        assertEquals(
                0,
                result.getSuccessCount()
        );

        assertEquals(
                1,
                result.getFailedCount()
        );

        assertTrue(
                result.getRows()
                        .get(0)
                        .isDuplicate()
        );

        assertEquals(
                "Email da ton tai",
                result.getErrors()
                        .get(0)
                        .getMessage()
        );
    }

    /*
     * CASE 5:
     * Import that phai tao mot ban ghi trong bang leads.
     *
     * source = EXCEL va status = NEW
     * duoc co dinh ben trong LeadDAO.
     */
    @Test
    void importValidLeadCreatesLeadRecord()
            throws Exception {

        InputStream excel =
                createExcel(
                        new String[][]{
                                {
                                        "Cong ty ABC",
                                        "Nguyen Van A",
                                        "a@example.com",
                                        "0901234567",
                                        "Truong phong",
                                        "",
                                        "Cong nghe thong tin",
                                        "50-100",
                                        "",
                                        "Ha Noi"
                                }
                        }
                );

        ImportLeadResult result =
                service.importLeads(
                        excel,
                        1L,
                        "SALES"
                );

        assertEquals(
                1,
                result.getSuccessCount()
        );

        assertEquals(
                0,
                result.getFailedCount()
        );

        verify(
                leadDAO,
                times(1)
        ).createImportedLead(
                eq("Nguyen Van A"),
                eq("a@example.com"),
                eq("0901234567"),
                eq("Cong ty ABC"),
                eq("Truong phong"),
                isNull(),
                eq("Cong nghe thong tin"),
                eq("50-100"),
                isNull(),
                eq("Ha Noi")
        );
    }

    /*
     * CASE 6:
     * Du lieu dau vao khong phai Excel.
     */
    @Test
    void badExcelFormatThrowsIOException() {

        InputStream invalid =
                new ByteArrayInputStream(
                        "khong-phai-excel"
                                .getBytes()
                );

        assertThrows(
                IOException.class,
                () -> service.previewLeads(
                        invalid,
                        1L,
                        "SALES"
                )
        );
    }

    /*
     * Tao workbook Excel trong RAM.
     *
     * Moi phan tu String[] la mot dong Lead.
     */
    private InputStream createExcel(
            String[][] data)
            throws IOException {

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook();

             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            Sheet sheet =
                    workbook.createSheet("Leads");

            Row header =
                    sheet.createRow(0);

            String[] columns = {
                    "Ten doanh nghiep",
                    "Ho ten lien he",
                    "Email",
                    "So dien thoai",
                    "Chuc danh",
                    "Ma so thue",
                    "Nganh nghe",
                    "Quy mo",
                    "Website",
                    "Dia chi"
            };

            for (int column = 0;
                 column < columns.length;
                 column++) {

                header.createCell(column)
                        .setCellValue(
                                columns[column]
                        );
            }

            for (int rowIndex = 0;
                 rowIndex < data.length;
                 rowIndex++) {

                Row row =
                        sheet.createRow(
                                rowIndex + 1
                        );

                String[] values =
                        data[rowIndex];

                for (int column = 0;
                     column < values.length;
                     column++) {

                    row.createCell(column)
                            .setCellValue(
                                    values[column]
                            );
                }
            }

            workbook.write(output);

            return new ByteArrayInputStream(
                    output.toByteArray()
            );
        }
    }
}