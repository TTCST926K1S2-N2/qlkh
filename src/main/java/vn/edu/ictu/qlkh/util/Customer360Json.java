package vn.edu.ictu.qlkh.util;

import vn.edu.ictu.qlkh.dto.Customer360DTO;
import vn.edu.ictu.qlkh.model.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

/** Dependency-free JSON output matching S3-01's existing field names. */
public final class Customer360Json {
    private Customer360Json() {}

    public static String toJson(Customer360DTO dto) {
        StringBuilder b = new StringBuilder(512);
        b.append("{\"customer\":");
        customer(b, dto.customer());
        b.append(",\"contacts\":");
        array(b, dto.contacts(), Customer360Json::contact);
        b.append(",\"opportunities\":");
        array(b, dto.opportunities(), Customer360Json::opportunity);
        b.append(",\"activities\":");
        array(b, dto.activities(), Customer360Json::activity);
        b.append(",\"attachments\":");
        array(b, dto.attachments(), Customer360Json::attachment);
        var a = dto.availability();
        b.append(",\"availability\":{\"contacts\":").append(a.contacts())
                .append(",\"opportunities\":").append(a.opportunities())
                .append(",\"activities\":").append(a.activities())
                .append(",\"attachments\":").append(a.attachments())
                .append("}}");
        return b.toString();
    }

    private static void customer(StringBuilder b, Customer c) {
        b.append("{\"id\":").append(c.getId())
                .append(",\"companyName\":").append(quote(c.getCompanyName()))
                .append(",\"taxCode\":").append(quote(c.getTaxCode()))
                .append(",\"industry\":").append(quote(c.getIndustry()))
                .append(",\"companySize\":").append(quote(c.getCompanySize()))
                .append(",\"website\":").append(quote(c.getWebsite()))
                .append(",\"address\":").append(quote(c.getAddress()))
                .append(",\"ownerId\":").append(c.getOwnerId())
                .append(",\"status\":").append(quote(c.getStatus()))
                .append(",\"createdAt\":").append(date(c.getCreatedAt()))
                .append(",\"updatedAt\":").append(date(c.getUpdatedAt()))
                .append('}');
    }

    private static void contact(StringBuilder b, Customer360DTO.ContactItem c) {
        b.append("{\"id\":").append(c.id())
                .append(",\"customerId\":").append(c.customerId())
                .append(",\"fullName\":").append(quote(c.fullName()))
                .append(",\"jobTitle\":").append(quote(c.jobTitle()))
                .append(",\"email\":").append(quote(c.email()))
                .append(",\"phone\":").append(quote(c.phone()))
                .append(",\"decisionRole\":").append(quote(c.decisionRole()))
                .append(",\"isPrimary\":").append(c.isPrimary())
                .append(",\"createdAt\":").append(date(c.createdAt()))
                .append(",\"updatedAt\":").append(date(c.updatedAt()))
                .append('}');
    }

    private static void opportunity(StringBuilder b,
                                    Customer360DTO.OpportunityItem o) {
        b.append("{\"id\":").append(o.id())
                .append(",\"name\":").append(quote(o.name()))
                .append(",\"stage\":").append(quote(o.stage()))
                .append(",\"expectedValue\":").append(number(o.expectedValue()))
                .append(",\"updatedAt\":").append(date(o.updatedAt()))
                .append('}');
    }

    private static void activity(StringBuilder b,
                                 Customer360DTO.ActivityItem a) {
        b.append("{\"id\":").append(a.id())
                .append(",\"type\":").append(quote(a.type()))
                .append(",\"title\":").append(quote(a.title()))
                .append(",\"occurredAt\":").append(date(a.occurredAt()))
                .append('}');
    }

    private static void attachment(StringBuilder b,
                                   Customer360DTO.AttachmentItem a) {
        b.append("{\"id\":").append(a.id())
                .append(",\"fileName\":").append(quote(a.fileName()))
                .append(",\"downloadUrl\":").append(quote(a.downloadUrl()))
                .append(",\"uploadedAt\":").append(date(a.uploadedAt()))
                .append('}');
    }

    private static <T> void array(StringBuilder b, List<T> values,
                                  BiConsumer<StringBuilder, T> writer) {
        b.append('[');
        boolean first = true;
        for (T value : values) {
            if (!first) b.append(',');
            writer.accept(b, value);
            first = false;
        }
        b.append(']');
    }

    private static String date(LocalDateTime value) {
        return value == null ? "null" : quote(value.toString());
    }

    private static String number(BigDecimal value) {
        return value == null ? "null" : value.toPlainString();
    }

    public static String quote(String value) {
        if (value == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                case '\b' -> b.append("\\b");
                case '\f' -> b.append("\\f");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }
}
