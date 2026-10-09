package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.ImportCustomerResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ImportCustomerServiceTest {

    @Test
    void importCustomers_shouldContinueWhenOneRowFails()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Cong ty A",
                                "0100000001",
                                "CNTT",
                                "50",
                                "https://a.vn",
                                "Ha Noi",
                                "",
                                "POTENTIAL"
                        },
                        {
                                "Cong ty B",
                                "0109999999",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        },
                        {
                                "Cong ty C",
                                "0100000003",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "CUSTOMER"
                        }
                },
                false
        );

        ImportCustomerResult result =
                service.importCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "SALES"
                );

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());

        assertEquals(1, result.getErrors().size());
        assertEquals(3, result.getErrors().get(0).getRow());
        assertEquals(
                "0109999999",
                result.getErrors().get(0).getTaxCode()
        );

        assertTrue(
                result.getErrors()
                        .get(0)
                        .getMessage()
                        .contains("ton tai")
        );

        assertEquals(2, fake.createCount);
    }

    @Test
    void previewCustomers_shouldDetectDuplicateTaxCodeInExcel()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Cong ty A",
                                "0101234567",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        },
                        {
                                "Cong ty B",
                                "0101234567",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        }
                },
                false
        );

        ImportCustomerResult result =
                service.previewCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "SALES"
                );

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());

        assertEquals(3, result.getErrors().get(0).getRow());

        assertTrue(
                result.getErrors()
                        .get(0)
                        .getMessage()
                        .contains("trung trong tep Excel")
        );

        // Preview khong duoc ghi DB.
        assertEquals(0, fake.createCount);
    }

    @Test
    void previewCustomers_shouldValidateRequiredFields()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "",
                                "0101111111",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        },
                        {
                                "Cong ty B",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        }
                },
                false
        );

        ImportCustomerResult result =
                service.previewCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "SALES"
                );

        assertEquals(2, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(2, result.getFailedCount());

        assertEquals(2, result.getErrors().get(0).getRow());
        assertEquals(3, result.getErrors().get(1).getRow());
    }

    @Test
    void previewCustomers_shouldRejectInvalidOwnerId()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Cong ty A",
                                "0102222222",
                                "",
                                "",
                                "",
                                "",
                                "abc",
                                "POTENTIAL"
                        }
                },
                false
        );

        ImportCustomerResult result =
                service.previewCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "SALES"
                );

        assertEquals(1, result.getFailedCount());

        assertTrue(
                result.getErrors()
                        .get(0)
                        .getMessage()
                        .contains("ID khong hop le")
        );
    }

    @Test
    void previewCustomers_shouldRejectUnauthorizedRole()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Cong ty A",
                                "0103333333",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        }
                },
                false
        );

        ImportCustomerResult result =
                service.previewCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "VIEWER"
                );

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());

        assertTrue(
                result.getErrors()
                        .get(0)
                        .getMessage()
                        .contains("quyen")
        );
    }

    @Test
    void previewCustomers_shouldIgnoreEmptyRows()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Cong ty A",
                                "0104444444",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        }
                },
                true
        );

        ImportCustomerResult result =
                service.previewCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "SALES"
                );

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    void previewCustomers_shouldRejectInvalidTaxCodeFormat()
            throws Exception {

        FakeCustomerService fake =
                new FakeCustomerService();

        ImportCustomerService service =
                new ImportCustomerService(fake);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Cong ty MST sai",
                                "ABC123",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "POTENTIAL"
                        }
                },
                false
        );

        ImportCustomerResult result =
                service.previewCustomers(
                        new ByteArrayInputStream(excel),
                        7L,
                        "SALES"
                );

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());

        assertEquals(
                "ABC123",
                result.getErrors()
                        .get(0)
                        .getTaxCode()
        );

        assertTrue(
                result.getErrors()
                        .get(0)
                        .getMessage()
                        .contains("khong hop le")
        );
    }
    private byte[] createExcel(
            String[][] rows,
            boolean addEmptyRow)
            throws IOException {

        try (Workbook workbook =
                     new XSSFWorkbook();

             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            Sheet sheet =
                    workbook.createSheet("Customers");

            Row header =
                    sheet.createRow(0);

            String[] headers = {
                    "Ten doanh nghiep",
                    "Ma so thue",
                    "Nganh nghe",
                    "Quy mo",
                    "Website",
                    "Dia chi",
                    "Nguoi phu trach ID",
                    "Trang thai"
            };

            for (int i = 0;
                 i < headers.length;
                 i++) {

                header.createCell(i)
                        .setCellValue(headers[i]);
            }

            for (int i = 0;
                 i < rows.length;
                 i++) {

                Row row =
                        sheet.createRow(i + 1);

                for (int column = 0;
                     column < rows[i].length;
                     column++) {

                    row.createCell(column)
                            .setCellValue(
                                    rows[i][column]
                            );
                }
            }

            if (addEmptyRow) {
                sheet.createRow(rows.length + 1);
            }

            workbook.write(output);

            return output.toByteArray();
        }
    }

    private static class FakeCustomerService
            extends CustomerService {

        private int createCount;

        @Override
        public void validateForImport(
                Customer customer,
                long userId,
                String role)
                throws SQLException {

            if (!"SALES".equalsIgnoreCase(role)
                    && !"MANAGER".equalsIgnoreCase(role)
                    && !"ADMIN".equalsIgnoreCase(role)) {

                throw new SecurityException(
                        "Khong co quyen import khach hang"
                );
            }

            if (customer.getCompanyName() == null
                    || customer.getCompanyName().isBlank()) {

                throw new IllegalArgumentException(
                        "Ten doanh nghiep khong hop le"
                );
            }

            if (customer.getTaxCode() == null
                    || customer.getTaxCode().isBlank()) {

                throw new IllegalArgumentException(
                        "Ma so thue la bat buoc"
                );
            }

            if ("0109999999"
                    .equalsIgnoreCase(
                            customer.getTaxCode())) {

                throw new IllegalArgumentException(
                        "Ma so thue da ton tai"
                );
            }
        }

        @Override
        public long create(
                Customer customer,
                long userId,
                String role)
                throws SQLException {

            createCount++;
            return createCount;
        }
    }
}