package vn.edu.ictu.qlkh.dto;

import vn.edu.ictu.qlkh.model.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * S3-03: Read-only view of a customer and related CRM information.
 * An empty list is not the same as an unavailable module: see availability.
 */
public record Customer360DTO(
        Customer customer,
        List<ContactItem> contacts,
        List<OpportunityItem> opportunities,
        List<ActivityItem> activities,
        List<AttachmentItem> attachments,
        Availability availability) {

    public Customer360DTO {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(availability, "availability");
        contacts = List.copyOf(contacts);
        opportunities = List.copyOf(opportunities);
        attachments = List.copyOf(attachments);
        // Once an activity source is implemented, the newest events appear first.
        activities = activities.stream()
                .sorted(Comparator.comparing(
                        ActivityItem::occurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public record Availability(boolean contacts, boolean opportunities,
                               boolean activities, boolean attachments) {}

    public record ContactItem(long id, long customerId, String fullName,
                              String jobTitle, String email, String phone,
                              String decisionRole, boolean isPrimary,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {}

    // Contract for future modules. Do not populate without real data sources.
    public record OpportunityItem(long id, String name, String stage,
                                  BigDecimal expectedValue,
                                  LocalDateTime updatedAt) {}

    public record ActivityItem(long id, String type, String title,
                               LocalDateTime occurredAt) {}

    public record AttachmentItem(long id, String fileName, String downloadUrl,
                                 LocalDateTime uploadedAt) {}
}
