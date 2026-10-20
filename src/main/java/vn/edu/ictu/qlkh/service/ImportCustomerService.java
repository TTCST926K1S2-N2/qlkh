package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.ImportCustomerResult;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ImportCustomerService {

    private static final int COLUMN_COUNT = 8;

    private final CustomerService customerService;

    public ImportCustomerService() {
        this(new CustomerService());
    }

    ImportCustomerService(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    public ImportCustomerResult importCustomers(
            InputStream inputStream,
            long userId,
            String role)
            throws IOException {

        return importCustomers(inputStream, userId, role, "error");
    }

    public ImportCustomerResult previewCustomers(
            InputStream inputStream,
            long userId,
            String role)
            throws IOException {

        return previewCustomers(inputStream, userId, role, "error");
    }

    public ImportCustomerResult importCustomers(InputStream stream, long userId, String role,
                                                String duplicateMode) throws IOException {
        return process(stream, userId, role, false, duplicateMode);
    }

    public ImportCustomerResult previewCustomers(InputStream stream, long userId, String role,
                                                 String duplicateMode) throws IOException {
        return process(stream, userId, role, true, duplicateMode);
    }

    private ImportCustomerResult process(
            InputStream inputStream,
            long userId,
            String role,
            boolean preview,
            String duplicateMode)
            throws IOException {
        if (!Set.of("error", "skip", "update").contains(duplicateMode)) {
            throw new IllegalArgumentException("Che do xu ly trung khong hop le");
        }

        ImportCustomerResult result =
                new ImportCustomerResult();

        DataFormatter formatter =
                new DataFormatter();

        Set<String> acceptedTaxCodes =
                new HashSet<>();

        try (Workbook workbook =
                     WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                return result;
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            // Dong 1 la tieu de:
            // Ten doanh nghiep | Ma so thue | Nganh nghe
            // Quy mo | Website | Dia chi
            // Nguoi phu trach ID | Trang thai
            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row =
                        sheet.getRow(rowIndex);

                if (row == null ||
                        isEmptyRow(row, formatter)) {
                    continue;
                }

                result.setTotalRows(
                        result.getTotalRows() + 1
                );

                int excelRow =
                        rowIndex + 1;

                Customer customer = null;

                try {
                    customer =
                            readCustomer(
                                    row,
                                    formatter
                            );

                    String taxCode =
                            customer.getTaxCode();

                    String normalizedTaxCode =
                            normalizeTaxCode(taxCode);

                    validateTaxCodeFormat(taxCode);

                    if (normalizedTaxCode != null &&
                            acceptedTaxCodes.contains(
                                    normalizedTaxCode)) {

                        throw new IllegalArgumentException(
                                "Ma so thue bi trung trong tep Excel"
                        );
                    }

                    /*
                     * Preview va Import deu validate:
                     * - role / scope
                     * - truong bat buoc
                     * - owner
                     * - ma so thue
                     * - trung ma so thue trong DB
                     */
                    Customer existing = normalizedTaxCode == null ? null
                            : customerService.findVisibleByTaxCode(taxCode, userId, role);
                    if (existing != null && "skip".equals(duplicateMode)) {
                        result.addError(excelRow, taxCode, "Bo qua khach hang trung ma so thue");
                        continue;
                    }
                    if (existing != null && "update".equals(duplicateMode)) {
                        // Keep owner and existing values for blank optional Excel cells.
                        customer.setOwnerId(existing.getOwnerId());
                        if (customer.getIndustry() == null) customer.setIndustry(existing.getIndustry());
                        if (customer.getCompanySize() == null) customer.setCompanySize(existing.getCompanySize());
                        if (customer.getWebsite() == null) customer.setWebsite(existing.getWebsite());
                        if (customer.getAddress() == null) customer.setAddress(existing.getAddress());
                        if (customer.getStatus() == null) customer.setStatus(existing.getStatus());
                        if (!preview && !customerService.update(existing.getId(), customer, userId, role))
                            throw new IllegalArgumentException("Khong the cap nhat khach hang");
                    } else {
                        customerService.validateForImport(customer, userId, role);
                        if (!preview) customerService.create(customer, userId, role);
                    }

                    if (normalizedTaxCode != null) {
                        acceptedTaxCodes.add(
                                normalizedTaxCode
                        );
                    }

                    result.incrementSuccessCount();

                } catch (IllegalArgumentException |
                         SecurityException e) {

                    result.addError(
                            excelRow,
                            customer == null
                                    ? getCellValue(
                                            row,
                                            1,
                                            formatter)
                                    : customer.getTaxCode(),
                            e.getMessage()
                    );

                } catch (SQLException e) {

                    result.addError(
                            excelRow,
                            customer == null
                                    ? getCellValue(
                                            row,
                                            1,
                                            formatter)
                                    : customer.getTaxCode(),
                            "Khong the xu ly khach hang trong co so du lieu"
                    );
                }
            }
        }

        return result;
    }

    private Customer readCustomer(
            Row row,
            DataFormatter formatter) {

        Customer customer =
                new Customer();

        customer.setCompanyName(
                getCellValue(row, 0, formatter)
        );

        customer.setTaxCode(
                getCellValue(row, 1, formatter)
        );

        customer.setIndustry(
                nullable(
                        getCellValue(
                                row,
                                2,
                                formatter)
                )
        );

        customer.setCompanySize(
                nullable(
                        getCellValue(
                                row,
                                3,
                                formatter)
                )
        );

        customer.setWebsite(
                nullable(
                        getCellValue(
                                row,
                                4,
                                formatter)
                )
        );

        customer.setAddress(
                nullable(
                        getCellValue(
                                row,
                                5,
                                formatter)
                )
        );

        String owner =
                getCellValue(
                        row,
                        6,
                        formatter
                );

        if (!owner.isBlank()) {
            try {
                long ownerId =
                        Long.parseLong(owner);

                if (ownerId <= 0) {
                    throw new NumberFormatException();
                }

                customer.setOwnerId(ownerId);

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Nguoi phu trach ID khong hop le"
                );
            }
        }

        String status =
                getCellValue(
                        row,
                        7,
                        formatter
                );

        customer.setStatus(
                status.isBlank()
                        ? null
                        : status.toUpperCase(
                                Locale.ROOT)
        );

        return customer;
    }

    private void validateTaxCodeFormat(
            String taxCode) {

        if (taxCode == null ||
                taxCode.isBlank()) {
            return;
        }

        String value =
                taxCode.trim();

        if (!value.matches(
                "\\d{10}(-\\d{3})?")) {

            throw new IllegalArgumentException(
                    "Ma so thue khong hop le"
            );
        }
    }
    private String normalizeTaxCode(
            String taxCode) {

        if (taxCode == null ||
                taxCode.isBlank()) {
            return null;
        }

        return taxCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String nullable(
            String value) {

        if (value == null ||
                value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String getCellValue(
            Row row,
            int columnIndex,
            DataFormatter formatter) {

        if (row.getCell(columnIndex) == null) {
            return "";
        }

        return formatter
                .formatCellValue(
                        row.getCell(columnIndex))
                .trim();
    }

    private boolean isEmptyRow(
            Row row,
            DataFormatter formatter) {

        for (int column = 0;
             column < COLUMN_COUNT;
             column++) {

            if (!getCellValue(
                    row,
                    column,
                    formatter).isBlank()) {

                return false;
            }
        }

        return true;
    }
}
