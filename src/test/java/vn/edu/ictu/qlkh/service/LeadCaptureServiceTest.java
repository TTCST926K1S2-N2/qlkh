package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LeadCaptureServiceTest {

    private final LeadCaptureService service = new LeadCaptureService();

    private void expect400(
            String name,
            String email,
            String phone,
            String company,
            String interest,
            String form,
            String honeypot
    ) {
        LeadCaptureService.ValidationException ex =
                assertThrows(
                    LeadCaptureService.ValidationException.class,
                    () -> service.submit(
                        name, email, phone, company,
                        interest, form, honeypot,
                        "192.0.2.100"
                    )
                );

        assertEquals(400, ex.getStatus());
    }

    @Test
    void shouldRejectHoneypot() {
        expect400(
            "Test User", "test@example.com",
            "0912345678", "Test Company",
            "Interested", "website-default",
            "spam-bot"
        );
    }

    @Test
    void shouldRejectEmptyName() {
        expect400(
            "", "test@example.com",
            "0912345678", "Test Company",
            "Interested", "website-default",
            ""
        );
    }

    @Test
    void shouldRejectInvalidEmail() {
        expect400(
            "Test User", "invalid-email",
            "0912345678", "Test Company",
            "Interested", "website-default",
            ""
        );
    }

    @Test
    void shouldRejectInvalidPhone() {
        expect400(
            "Test User", "test@example.com",
            "123", "Test Company",
            "Interested", "website-default",
            ""
        );
    }

    @Test
    void shouldRejectUnknownForm() {
        expect400(
            "Test User", "test@example.com",
            "0912345678", "Test Company",
            "Interested", "unknown-form",
            ""
        );
    }

    @Test
    void shouldRejectLongName() {
        expect400(
            "A".repeat(256), "test@example.com",
            "0912345678", "Test Company",
            "Interested", "website-default",
            ""
        );
    }

    @Test
    void shouldRejectLongInterest() {
        expect400(
            "Test User", "test@example.com",
            "0912345678", "Test Company",
            "A".repeat(5001), "website-default",
            ""
        );
    }
}