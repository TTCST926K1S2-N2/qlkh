package vn.edu.ictu.qlkh.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImportCustomerResult {

    private int totalRows;
    private int successCount;
    private int failedCount;

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

    public List<RowError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void incrementSuccessCount() {
        successCount++;
    }

    public void addError(
            int row,
            String taxCode,
            String message) {

        errors.add(
                new RowError(row, taxCode, message)
        );

        failedCount++;
    }

    public static class RowError {

        private final int row;
        private final String taxCode;
        private final String message;

        public RowError(
                int row,
                String taxCode,
                String message) {

            this.row = row;
            this.taxCode = taxCode;
            this.message = message;
        }

        public int getRow() {
            return row;
        }

        public String getTaxCode() {
            return taxCode;
        }

        public String getMessage() {
            return message;
        }
    }
}
