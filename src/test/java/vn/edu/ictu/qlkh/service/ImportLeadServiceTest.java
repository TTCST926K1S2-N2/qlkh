package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.ImportLeadResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImportLeadServiceTest {

    private CustomerService customerService;
    private ContactService contactService;

    private ImportLeadService service;

    @BeforeEach
    void setUp() throws Exception {

        customerService =
                mock(CustomerService.class);

        contactService =
                mock(ContactService.class);

        service =
                new ImportLeadService(
                        customerService,
                        contactService
                );

        /*
         * Mac dinh database khong co Lead nao.
         */
        when(customerService.list(
                anyLong(),
                anyString()
        )).thenReturn(List.of());
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
         * Preview tuyet doi khong INSERT.
         */
        verify(
                customerService,
                never()
        ).create(
                any(Customer.class),
                anyLong(),
                anyString()
        );

        verify(
                contactService,
                never()
        ).create(
                any(Contact.class),
                anyLong(),
                anyString()
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
     * Email da ton tai trong Lead
     * POTENTIAL cua database.
     */
    @Test
    void duplicateEmailFromExistingLeadIsDetected()
            throws Exception {

        Customer existing =
                new Customer();

        existing.setId(10L);
        existing.setStatus("POTENTIAL");

        Contact existingContact =
                new Contact();

        existingContact.setCustomerId(10L);
        existingContact.setEmail(
                "existing@example.com"
        );

        when(customerService.list(
                1L,
                "SALES"
        )).thenReturn(
                List.of(existing)
        );

        when(contactService.list(
                10L,
                1L,
                "SALES"
        )).thenReturn(
                List.of(existingContact)
        );

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
     * Import that phai tao:
     *
     * Customer status POTENTIAL
     *        +
     * Contact primary=true.
     */
    @Test
    void importValidLeadCreatesCustomerAndContact()
            throws Exception {

        when(customerService.create(
                any(Customer.class),
                eq(1L),
                eq("SALES")
        )).thenReturn(100L);

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

        verify(
                customerService,
                times(1)
        ).create(
                argThat(customer ->
                        "Cong ty ABC".equals(
                                customer.getCompanyName()
                        )
                        &&
                        "POTENTIAL".equals(
                                customer.getStatus()
                        )
                ),
                eq(1L),
                eq("SALES")
        );

        verify(
                contactService,
                times(1)
        ).create(
                argThat(contact ->
                        Long.valueOf(100L).equals(
                                contact.getCustomerId()
                        )
                        &&
                        "Nguyen Van A".equals(
                                contact.getFullName()
                        )
                        &&
                        contact.isPrimary()
                ),
                eq(1L),
                eq("SALES")
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