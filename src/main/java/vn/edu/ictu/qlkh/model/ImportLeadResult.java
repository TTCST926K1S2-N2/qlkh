package vn.edu.ictu.qlkh.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImportLeadResult {

    private int totalRows;
    private int successCount;
    private int failedCount;

    private final List<PreviewRow> rows =
            new ArrayList<>();

    private final List<RowError> errors =
            new ArrayList<>();

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<PreviewRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public List<RowError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void incrementSuccessCount() {
        successCount++;
    }

    public void addPreviewRow(
            int row,
            String companyName,
            String contactName,
            String email,
            String phone,
            boolean valid,
            boolean duplicate,
            String message) {

        rows.add(
                new PreviewRow(
                        row,
                        companyName,
                        contactName,
                        email,
                        phone,
                        valid,
                        duplicate,
                        message
                )
        );
    }

    public void addError(
            int row,
            String email,
            String phone,
            String message) {

        errors.add(
                new RowError(
                        row,
                        email,
                        phone,
                        message
                )
        );

        failedCount++;
    }

    public static class PreviewRow {

        private final int row;
        private final String companyName;
        private final String contactName;
        private final String email;
        private final String phone;
        private final boolean valid;
        private final boolean duplicate;
        private final String message;

        public PreviewRow(
                int row,
                String companyName,
                String contactName,
                String email,
                String phone,
                boolean valid,
                boolean duplicate,
                String message) {

            this.row = row;
            this.companyName = companyName;
            this.contactName = contactName;
            this.email = email;
            this.phone = phone;
            this.valid = valid;
            this.duplicate = duplicate;
            this.message = message;
        }

        public int getRow() {
            return row;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getContactName() {
            return contactName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class RowError {

        private final int row;
        private final String email;
        private final String phone;
        private final String message;

        public RowError(
                int row,
                String email,
                String phone,
                String message) {

            this.row = row;
            this.email = email;
            this.phone = phone;
            this.message = message;
        }

        public int getRow() {
            return row;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getMessage() {
            return message;
        }
    }
}
