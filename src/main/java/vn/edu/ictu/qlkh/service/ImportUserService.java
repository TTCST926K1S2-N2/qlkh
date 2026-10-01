package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import vn.edu.ictu.qlkh.model.ImportUserResult;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ImportUserService {

    private final UserService userService;

    public ImportUserService() {
        this(new UserService());
    }

    ImportUserService(UserService userService) {
        this.userService = userService;
    }

    public ImportUserResult importUsers(InputStream inputStream)
            throws IOException {

        ImportUserResult result = new ImportUserResult();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook =
                     WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                return result;
            }

            Sheet sheet = workbook.getSheetAt(0);

            // Dong 1 la tieu de:
            // Ho ten | Email | Vai tro | Trang thai
            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isEmptyRow(row, formatter)) {
                    continue;
                }

                result.setTotalRows(
                        result.getTotalRows() + 1
                );

                int excelRow = rowIndex + 1;

                String fullName =
                        getCellValue(row, 0, formatter);

                String email =
                        getCellValue(row, 1, formatter);

                String role =
                        getCellValue(row, 2, formatter);

                String status =
                        getCellValue(row, 3, formatter);

                try {
                    UserService.CreateUserResult createResult =
                            userService.createUser(
                                    fullName,
                                    email,
                                    role,
                                    status
                            );

                    if (createResult.isSuccess()) {
                        result.incrementSuccessCount();
                    } else {
                        result.addError(
                                excelRow,
                                email,
                                createResult.getMessage()
                        );
                    }

                } catch (SQLException e) {
                    result.addError(
                            excelRow,
                            email,
                            "Không thể lưu người dùng vào cơ sở dữ liệu."
                    );
                }
            }
        }

        return result;
    }

    private String getCellValue(
            Row row,
            int columnIndex,
            DataFormatter formatter) {

        if (row.getCell(columnIndex) == null) {
            return "";
        }

        return formatter
                .formatCellValue(row.getCell(columnIndex))
                .trim();
    }

    private boolean isEmptyRow(
            Row row,
            DataFormatter formatter) {

        for (int columnIndex = 0;
             columnIndex < 4;
             columnIndex++) {

            if (!getCellValue(
                    row,
                    columnIndex,
                    formatter
            ).isBlank()) {

                return false;
            }
        }

        return true;
    }
}
