package vn.edu.ictu.qlkh.service;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.ImportLeadResult;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ImportLeadService {

    /*
     * Thu tu 10 cot trong file Excel:
     *
     * 0 - Ten doanh nghiep
     * 1 - Ho ten lien he
     * 2 - Email
     * 3 - So dien thoai
     * 4 - Chuc danh
     * 5 - Ma so thue
     * 6 - Nganh nghe
     * 7 - Quy mo
     * 8 - Website
     * 9 - Dia chi
     */
    private static final int COLUMN_COUNT = 10;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "^\\+?[0-9][0-9 .()-]{6,28}$"
            );

    private static final Pattern TAX_CODE_PATTERN =
            Pattern.compile(
                    "\\d{10}(-\\d{3})?"
            );

    private final CustomerService customerService;
    private final ContactService contactService;

    public ImportLeadService() {
        this(
                new CustomerService(),
                new ContactService()
        );
    }

    ImportLeadService(
            CustomerService customerService,
            ContactService contactService) {

        this.customerService = customerService;
        this.contactService = contactService;
    }

    /*
     * Preview:
     * - doc Excel
     * - validate
     * - kiem tra trung
     * - KHONG ghi database
     */
    public ImportLeadResult previewLeads(
            InputStream inputStream,
            long userId,
            String role) throws IOException {

        return process(
                inputStream,
                userId,
                role,
                true
        );
    }

    /*
     * Import that:
     * - doc Excel
     * - validate
     * - kiem tra trung
     * - ghi Customer(POTENTIAL) + Contact
     */
    public ImportLeadResult importLeads(
            InputStream inputStream,
            long userId,
            String role) throws IOException {

        return process(
                inputStream,
                userId,
                role,
                false
        );
    }

    private ImportLeadResult process(
            InputStream inputStream,
            long userId,
            String role,
            boolean preview) throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "Tep Excel khong hop le"
            );
        }

        if (userId <= 0) {
            throw new SecurityException(
                    "Tai khoan khong hop le"
            );
        }

        ImportLeadResult result =
                new ImportLeadResult();

        /*
         * Tap email/phone da ton tai trong database
         * hoac da gap trong chinh file Excel.
         */
        Set<String> usedEmails =
                new HashSet<>();

        Set<String> usedPhones =
                new HashSet<>();

        /*
         * MST khong phai quy tac duplicate Lead.
         * Tap nay chi dung de tranh 2 dong Excel
         * cung tao Customer voi cung ma so thue.
         */
        Set<String> usedTaxCodes =
                new HashSet<>();

        loadExistingLeadKeys(
                userId,
                role,
                usedEmails,
                usedPhones
        );

        DataFormatter formatter =
                new DataFormatter(Locale.ROOT);

        try (Workbook workbook =
                     WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Tep Excel khong co sheet du lieu"
                );
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            /*
             * Dong 0 la header.
             * Bat dau doc du lieu tu dong 1.
             */
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

                LeadRow lead = null;

                try {

                    lead =
                            readLeadRow(
                                    row,
                                    formatter
                            );

                    validateLeadRow(lead);

                    String normalizedTaxCode =
                            normalizeTaxCode(
                                    lead.taxCode
                            );

                    if (!normalizedTaxCode.isEmpty()
                            && usedTaxCodes.contains(
                                    normalizedTaxCode)) {

                        throw new IllegalArgumentException(
                                "Ma so thue bi trung trong tep Excel"
                        );
                    }

                    /*
                     * Lead khong bat buoc co MST.
                     *
                     * Neu co MST thi dung validation hien co
                     * cua CustomerService de Preview cung
                     * phat hien MST da ton tai trong database.
                     */
                    if (lead.taxCode != null) {

                        Customer customerToValidate =
                                buildCustomer(
                                        lead,
                                        userId
                                );

                        customerService.validateForImport(
                                customerToValidate,
                                userId,
                                role
                        );
                    }

                    String normalizedEmail =
                            normalizeEmail(
                                    lead.email
                            );

                    String normalizedPhone =
                            normalizePhone(
                                    lead.phone
                            );

                    boolean duplicate =
                            (!normalizedEmail.isEmpty()
                                    && usedEmails.contains(
                                            normalizedEmail
                                    ))
                            ||
                            (!normalizedPhone.isEmpty()
                                    && usedPhones.contains(
                                            normalizedPhone
                                    ));

                    if (duplicate) {

                        String message =
                                buildDuplicateMessage(
                                        normalizedEmail,
                                        normalizedPhone,
                                        usedEmails,
                                        usedPhones
                                );

                        result.addPreviewRow(
                                excelRow,
                                lead.companyName,
                                lead.contactName,
                                lead.email,
                                lead.phone,
                                false,
                                true,
                                message
                        );

                        result.addError(
                                excelRow,
                                lead.email,
                                lead.phone,
                                message
                        );

                        continue;
                    }

                    /*
                     * Preview chi validate.
                     * Import that moi ghi database.
                     */
                    if (!preview) {
                        saveLead(
                                lead,
                                userId,
                                role
                        );
                    }

                    /*
                     * Chi danh dau email/phone da dung
                     * sau khi dong hien tai hop le.
                     */
                    if (!normalizedEmail.isEmpty()) {
                        usedEmails.add(
                                normalizedEmail
                        );
                    }

                    if (!normalizedPhone.isEmpty()) {
                        usedPhones.add(
                                normalizedPhone
                        );
                    }

                    if (!normalizedTaxCode.isEmpty()) {
                        usedTaxCodes.add(
                                normalizedTaxCode
                        );
                    }

                    result.incrementSuccessCount();

                    result.addPreviewRow(
                            excelRow,
                            lead.companyName,
                            lead.contactName,
                            lead.email,
                            lead.phone,
                            true,
                            false,
                            null
                    );

                } catch (IllegalArgumentException |
                         SecurityException |
                         SQLException exception) {

                    String companyName =
                            lead == null
                                    ? null
                                    : lead.companyName;

                    String contactName =
                            lead == null
                                    ? null
                                    : lead.contactName;

                    String email =
                            lead == null
                                    ? null
                                    : lead.email;

                    String phone =
                            lead == null
                                    ? null
                                    : lead.phone;

                    String message =
                            safeMessage(exception);

                    result.addPreviewRow(
                            excelRow,
                            companyName,
                            contactName,
                            email,
                            phone,
                            false,
                            false,
                            message
                    );

                    result.addError(
                            excelRow,
                            email,
                            phone,
                            message
                    );
                }
            }

        } catch (IllegalArgumentException exception) {
            throw exception;

        } catch (IOException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new IOException(
                    "Khong the doc tep Excel",
                    exception
            );
        }

        return result;
    }

    /*
     * Lay tat ca Lead ma user hien tai duoc phep nhin thay.
     *
     * Theo Sprint 4 hien tai:
     * Lead = Customer co status POTENTIAL
     *        + Contact thuoc Customer do.
     */
    private void loadExistingLeadKeys(
            long userId,
            String role,
            Set<String> emails,
            Set<String> phones) throws IOException {

        try {

            List<Customer> customers =
                    customerService.list(
                            userId,
                            role
                    );

            for (Customer customer : customers) {

                if (!"POTENTIAL".equalsIgnoreCase(
                        customer.getStatus())) {
                    continue;
                }

                if (customer.getId() == null ||
                        customer.getId() <= 0) {
                    continue;
                }

                List<Contact> contacts =
                        contactService.list(
                                customer.getId(),
                                userId,
                                role
                        );

                if (contacts == null) {
                    continue;
                }

                for (Contact contact : contacts) {

                    String email =
                            normalizeEmail(
                                    contact.getEmail()
                            );

                    String phone =
                            normalizePhone(
                                    contact.getPhone()
                            );

                    if (!email.isEmpty()) {
                        emails.add(email);
                    }

                    if (!phone.isEmpty()) {
                        phones.add(phone);
                    }
                }
            }

        } catch (SQLException exception) {
            throw new IOException(
                    "Khong the kiem tra Lead trung lap",
                    exception
            );
        }
    }

    /*
     * Chuyen mot dong Excel thanh LeadRow.
     */
    private LeadRow readLeadRow(
            Row row,
            DataFormatter formatter) {

        LeadRow lead =
                new LeadRow();

        lead.companyName =
                nullable(
                        getCellValue(
                                row,
                                0,
                                formatter
                        )
                );

        lead.contactName =
                nullable(
                        getCellValue(
                                row,
                                1,
                                formatter
                        )
                );

        lead.email =
                nullable(
                        getCellValue(
                                row,
                                2,
                                formatter
                        )
                );

        lead.phone =
                nullable(
                        getCellValue(
                                row,
                                3,
                                formatter
                        )
                );

        lead.jobTitle =
                nullable(
                        getCellValue(
                                row,
                                4,
                                formatter
                        )
                );

        lead.taxCode =
                nullable(
                        getCellValue(
                                row,
                                5,
                                formatter
                        )
                );

        lead.industry =
                nullable(
                        getCellValue(
                                row,
                                6,
                                formatter
                        )
                );

        lead.companySize =
                nullable(
                        getCellValue(
                                row,
                                7,
                                formatter
                        )
                );

        lead.website =
                nullable(
                        getCellValue(
                                row,
                                8,
                                formatter
                        )
                );

        lead.address =
                nullable(
                        getCellValue(
                                row,
                                9,
                                formatter
                        )
                );

        return lead;
    }

    /*
     * Validate rieng cho Lead Import.
     */
    private void validateLeadRow(
            LeadRow lead) {

        if (lead.companyName == null ||
                lead.companyName.isBlank()) {

            throw new IllegalArgumentException(
                    "Ten doanh nghiep la bat buoc"
            );
        }

        if (lead.companyName.length() > 255) {
            throw new IllegalArgumentException(
                    "Ten doanh nghiep vuot qua 255 ky tu"
            );
        }

        if (lead.contactName == null ||
                lead.contactName.isBlank()) {

            throw new IllegalArgumentException(
                    "Ho ten lien he la bat buoc"
            );
        }

        if (lead.contactName.length() > 255) {
            throw new IllegalArgumentException(
                    "Ho ten lien he vuot qua 255 ky tu"
            );
        }

        if ((lead.email == null ||
                lead.email.isBlank())
                &&
                (lead.phone == null ||
                        lead.phone.isBlank())) {

            throw new IllegalArgumentException(
                    "Can cung cap email hoac so dien thoai"
            );
        }

        if (lead.email != null &&
                !EMAIL_PATTERN.matcher(
                        lead.email
                ).matches()) {

            throw new IllegalArgumentException(
                    "Email khong hop le"
            );
        }

        if (lead.email != null &&
                lead.email.length() > 255) {

            throw new IllegalArgumentException(
                    "Email vuot qua 255 ky tu"
            );
        }

        if (lead.phone != null &&
                !PHONE_PATTERN.matcher(
                        lead.phone
                ).matches()) {

            throw new IllegalArgumentException(
                    "So dien thoai khong hop le"
            );
        }

        if (lead.phone != null &&
                lead.phone.length() > 30) {

            throw new IllegalArgumentException(
                    "So dien thoai vuot qua 30 ky tu"
            );
        }

        if (lead.jobTitle != null &&
                lead.jobTitle.length() > 150) {

            throw new IllegalArgumentException(
                    "Chuc danh vuot qua 150 ky tu"
            );
        }

        if (lead.taxCode != null &&
                !TAX_CODE_PATTERN.matcher(
                        lead.taxCode
                ).matches()) {

            throw new IllegalArgumentException(
                    "Ma so thue khong hop le"
            );
        }

        checkLength(
                lead.industry,
                150,
                "Nganh nghe"
        );

        checkLength(
                lead.companySize,
                100,
                "Quy mo"
        );

        checkLength(
                lead.website,
                500,
                "Website"
        );

        checkLength(
                lead.address,
                500,
                "Dia chi"
        );
    }

    /*
     * Tao object Customer tu mot dong Lead.
     *
     * Ham nay CHUA ghi database.
     */
    private Customer buildCustomer(
            LeadRow lead,
            long userId) {

        Customer customer =
                new Customer();

        customer.setCompanyName(
                lead.companyName
        );

        customer.setTaxCode(
                lead.taxCode
        );

        customer.setIndustry(
                lead.industry
        );

        customer.setCompanySize(
                lead.companySize
        );

        customer.setWebsite(
                lead.website
        );

        customer.setAddress(
                lead.address
        );

        customer.setOwnerId(
                userId
        );

        customer.setStatus(
                "POTENTIAL"
        );

        return customer;
    }

    /*
     * Import that mot Lead.
     *
     * Buoc 1: tao Customer status POTENTIAL.
     * Buoc 2: lay customerId vua tao.
     * Buoc 3: tao Contact cho Customer do.
     */
    private void saveLead(
            LeadRow lead,
            long userId,
            String role) throws SQLException {

        Customer customer =
                buildCustomer(
                        lead,
                        userId
                );

        long customerId =
                customerService.create(
                        customer,
                        userId,
                        role
                );

        Contact contact =
                new Contact();

        contact.setCustomerId(
                customerId
        );

        contact.setFullName(
                lead.contactName
        );

        contact.setJobTitle(
                lead.jobTitle
        );

        contact.setEmail(
                lead.email
        );

        contact.setPhone(
                lead.phone
        );

        contact.setPrimary(true);

        contactService.create(
                contact,
                userId,
                role
        );
    }

    private String buildDuplicateMessage(
            String email,
            String phone,
            Set<String> usedEmails,
            Set<String> usedPhones) {

        boolean emailDuplicate =
                !email.isEmpty()
                        && usedEmails.contains(email);

        boolean phoneDuplicate =
                !phone.isEmpty()
                        && usedPhones.contains(phone);

        if (emailDuplicate &&
                phoneDuplicate) {

            return "Email va so dien thoai da ton tai";
        }

        if (emailDuplicate) {
            return "Email da ton tai";
        }

        return "So dien thoai da ton tai";
    }

    private String normalizeTaxCode(
            String taxCode) {

        if (taxCode == null) {
            return "";
        }

        return taxCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    /*
     * Giong quy tac S4-04:
     * trim + lowercase.
     */
    private String normalizeEmail(
            String email) {

        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    /*
     * Giong quy tac S4-04:
     * chi giu lai chu so.
     */
    private String normalizePhone(
            String phone) {

        if (phone == null) {
            return "";
        }

        return phone.replaceAll(
                "[^0-9]",
                ""
        );
    }

    private String getCellValue(
            Row row,
            int column,
            DataFormatter formatter) {

        if (row.getCell(column) == null) {
            return "";
        }

        return formatter.formatCellValue(
                row.getCell(column)
        ).trim();
    }

    private String nullable(
            String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
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
                    formatter
            ).isBlank()) {

                return false;
            }
        }

        return true;
    }

    private void checkLength(
            String value,
            int max,
            String field) {

        if (value != null &&
                value.length() > max) {

            throw new IllegalArgumentException(
                    field +
                    " vuot qua " +
                    max +
                    " ky tu"
            );
        }
    }

    private String safeMessage(
            Exception exception) {

        String message =
                exception.getMessage();

        if (message == null ||
                message.isBlank()) {

            return "Du lieu Lead khong hop le";
        }

        return message;
    }

    /*
     * Object tam dai dien cho mot dong Excel.
     * Khong phai bang database.
     */
    private static class LeadRow {

        private String companyName;
        private String contactName;
        private String email;
        private String phone;
        private String jobTitle;
        private String taxCode;
        private String industry;
        private String companySize;
        private String website;
        private String address;
    }
}