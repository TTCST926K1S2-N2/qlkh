package vn.edu.ictu.qlkh.model;

import java.util.ArrayList;
import java.util.List;

public class CustomerDuplicate {

    private Customer customer;
    private List<String> matchedFields;

    public CustomerDuplicate() {
        this.matchedFields = new ArrayList<>();
    }

    public CustomerDuplicate(
            Customer customer,
            List<String> matchedFields) {

        this.customer = customer;
        this.matchedFields = matchedFields == null
                ? new ArrayList<>()
                : new ArrayList<>(matchedFields);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<String> getMatchedFields() {
        return matchedFields;
    }

    public void setMatchedFields(List<String> matchedFields) {
        this.matchedFields = matchedFields == null
                ? new ArrayList<>()
                : new ArrayList<>(matchedFields);
    }
}