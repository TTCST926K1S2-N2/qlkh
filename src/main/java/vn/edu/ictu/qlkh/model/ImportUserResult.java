package vn.edu.ictu.qlkh.model;

import java.util.ArrayList;
import java.util.List;

public class ImportUserResult {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private final List<RowError> errors = new ArrayList<>();

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void incrementSuccessCount() {
        this.successCount++;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void incrementFailedCount() {
        this.failedCount++;
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public void addError(int row, String email, String message) {
        errors.add(new RowError(row, email, message));
        incrementFailedCount();
    }

    public static class RowError {

        private final int row;
        private final String email;
        private final String message;

        public RowError(int row, String email, String message) {
            this.row = row;
            this.email = email;
            this.message = message;
        }

        public int getRow() {
            return row;
        }

        public String getEmail() {
            return email;
        }

        public String getMessage() {
            return message;
        }
    }
}
