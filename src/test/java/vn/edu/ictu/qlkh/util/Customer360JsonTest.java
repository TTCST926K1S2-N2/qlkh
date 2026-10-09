package vn.edu.ictu.qlkh.util;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dto.Customer360DTO;
import vn.edu.ictu.qlkh.model.Customer;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Customer360JsonTest {
    private Customer customer() {
        Customer c = new Customer();
        c.setId(1L);
        c.setOwnerId(2L);
        c.setCompanyName("Tên \"A\"\\B\nC");
        c.setStatus("CUSTOMER");
        return c;
    }

    @Test
    void encodesNullUnicodeAndEscapesAndModuleFlags() {
        var dto = new Customer360DTO(customer(), List.of(), List.of(),
                List.of(), List.of(),
                new Customer360DTO.Availability(false, false, false, false));
        String json = Customer360Json.toJson(dto);
        assertTrue(json.contains("Tên \\\"A\\\"\\\\B\\nC"));
        assertTrue(json.contains("\"taxCode\":null"));
        assertTrue(json.contains("\"contacts\":[]"));
        assertTrue(json.contains("\"contacts\":false"));
        assertTrue(json.contains("\"activities\":[]"));
    }

    @Test
    void sortsActivitiesNewestFirstEvenWithNullDate() {
        var old = new Customer360DTO.ActivityItem(1, "CALL", "old",
                LocalDateTime.parse("2026-01-01T08:00:00"));
        var recent = new Customer360DTO.ActivityItem(2, "MEETING", "new",
                LocalDateTime.parse("2026-02-01T08:00:00"));
        var unknown = new Customer360DTO.ActivityItem(3, "NOTE", "none", null);
        var dto = new Customer360DTO(customer(), List.of(), List.of(),
                List.of(old, unknown, recent), List.of(),
                new Customer360DTO.Availability(false, false, true, false));
        assertEquals(List.of(2L, 1L, 3L),
                dto.activities().stream().map(Customer360DTO.ActivityItem::id).toList());
        String json = Customer360Json.toJson(dto);
        assertTrue(json.indexOf("\"title\":\"new\"") <
                json.indexOf("\"title\":\"old\""));
    }

    @Test
    void handles500ActivitiesUnder1500ms() {
        assertTimeoutPreemptively(
                java.time.Duration.ofMillis(1500),
                () -> {
                    var activities = new java.util.ArrayList<Customer360DTO.ActivityItem>();

                    LocalDateTime start =
                            LocalDateTime.parse("2026-01-01T00:00:00");

                    for (int i = 0; i < 500; i++) {
                        activities.add(
                                new Customer360DTO.ActivityItem(
                                        i + 1L,
                                        "TEST",
                                        "Activity " + (i + 1),
                                        start.plusMinutes(i)
                                )
                        );
                    }

                    var dto = new Customer360DTO(
                            customer(),
                            List.of(),
                            List.of(),
                            activities,
                            List.of(),
                            new Customer360DTO.Availability(
                                    false,
                                    false,
                                    true,
                                    false
                            )
                    );

                    assertEquals(500, dto.activities().size());

                    // Newest activity must appear first.
                    assertEquals(
                            500L,
                            dto.activities().get(0).id()
                    );

                    assertEquals(
                            1L,
                            dto.activities().get(499).id()
                    );

                    String json = Customer360Json.toJson(dto);

                    assertTrue(json.contains("\"activities\":["));
                    assertTrue(json.contains("\"Activity 500\""));
                }
        );
    }
}
