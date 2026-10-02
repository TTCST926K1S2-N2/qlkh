package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.ImportUserResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportUserServiceTest {

    @Test
    void importUsers_shouldImportValidRowsAndContinueWhenOneRowFails()
            throws Exception {

        FakeUserService fakeUserService =
                new FakeUserService();

        ImportUserService service =
                new ImportUserService(fakeUserService);

        byte[] excel = createExcel(
                new String[][]{
                        {
                                "Nguyen Van A",
                                "a@gmail.com",
                                "SALES",
                                "ACTIVE"
                        },
                        {
                                "Nguyen Van B",
                                "duplicate@gmail.com",
                                "SALES",
                                "ACTIVE"
                        },
                        {
                                "Nguyen Van C",
                                "c@gmail.com",
                                "MANAGER",
                                "ACTIVE"
                        }
                }
        );

        ImportUserResult result =
                service.importUsers(
                        new ByteArrayInputStream(excel)
                );

        assertEquals(3, result.getTotalRows());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());

        assertEquals(1, result.getErrors().size());
        assertEquals(3, result.getErrors().get(0).getRow());

        assertEquals(
                "duplicate@gmail.com",
                result.getErrors().get(0).getEmail()
        );

        assertTrue(
                result.getErrors()
                        .get(0)
                        .getMessage()
                        .contains("tồn tại")
        );

        // Dam bao dong loi khong lam dung Import.
        assertEquals(3, fakeUserService.callCount);
    }

    @Test
    void importUsers_shouldIgnoreCompletelyEmptyRows()
            throws Exception {

        FakeUserService fakeUserService =
                new FakeUserService();

        ImportUserService service =
                new ImportUserService(fakeUserService);

        byte[] excel = createExcelWithEmptyRow();

        ImportUserResult result =
                service.importUsers(
                        new ByteArrayInputStream(excel)
                );

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(1, fakeUserService.callCount);
    }

    private byte[] createExcel(String[][] rows)
            throws IOException {

        try (Workbook workbook =
                     new XSSFWorkbook();

             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            Sheet sheet =
                    workbook.createSheet("Users");

            createHeader(sheet);

            for (int i = 0; i < rows.length; i++) {

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

            workbook.write(output);

            return output.toByteArray();
        }
    }

    private byte[] createExcelWithEmptyRow()
            throws IOException {

        try (Workbook workbook =
                     new XSSFWorkbook();

             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            Sheet sheet =
                    workbook.createSheet("Users");

            createHeader(sheet);

            Row validRow =
                    sheet.createRow(1);

            validRow.createCell(0)
                    .setCellValue("Nguyen Van A");

            validRow.createCell(1)
                    .setCellValue("a@gmail.com");

            validRow.createCell(2)
                    .setCellValue("SALES");

            validRow.createCell(3)
                    .setCellValue("ACTIVE");

            // Dong Excel so 3 de trong.
            sheet.createRow(2);

            workbook.write(output);

            return output.toByteArray();
        }
    }

    private void createHeader(Sheet sheet) {

        Row header =
                sheet.createRow(0);

        header.createCell(0)
                .setCellValue("Ho ten");

        header.createCell(1)
                .setCellValue("Email");

        header.createCell(2)
                .setCellValue("Vai tro");

        header.createCell(3)
                .setCellValue("Trang thai");
    }

    private static class FakeUserService
            extends UserService {

        private int callCount;

        @Override
        public CreateUserResult createUser(
                String fullName,
                String email,
                String role,
                String status)
                throws SQLException {

            callCount++;

            if ("duplicate@gmail.com"
                    .equalsIgnoreCase(email)) {

                return CreateUserResult.failed(
                        "Email đã tồn tại trong hệ thống."
                );
            }

            return CreateUserResult.success(
                    callCount,
                    "TempPassword123!"
            );
        }
    }
}