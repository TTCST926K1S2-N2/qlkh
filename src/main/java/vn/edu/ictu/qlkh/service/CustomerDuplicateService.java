package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerDuplicateDAO;
import vn.edu.ictu.qlkh.model.Customer;

import java.net.URI;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;

public class CustomerDuplicateService {

    private final CustomerDuplicateDAO dao = new CustomerDuplicateDAO();

    public record DuplicatePair(
            Customer first,
            Customer second,
            int score,
            List<String> reasons) {}

    public List<DuplicatePair> findDuplicates(long userId, String role)
            throws SQLException {

        List<Customer> customers = dao.findCandidates(userId, role);
        List<DuplicatePair> result = new ArrayList<>();

        for (int i = 0; i < customers.size(); i++) {
            for (int j = i + 1; j < customers.size(); j++) {

                Customer a = customers.get(i);
                Customer b = customers.get(j);

                List<String> reasons = new ArrayList<>();
                int score = 0;

                String taxA = normalizeTax(a.getTaxCode());
                String taxB = normalizeTax(b.getTaxCode());

                if (!taxA.isEmpty() && taxA.equals(taxB)) {
                    score += 100;
                    reasons.add("Trung ma so thue");
                }

                String webA = normalizeWebsite(a.getWebsite());
                String webB = normalizeWebsite(b.getWebsite());

                if (!webA.isEmpty() && webA.equals(webB)) {
                    score += 40;
                    reasons.add("Trung website");
                }

                String nameA = normalizeName(a.getCompanyName());
                String nameB = normalizeName(b.getCompanyName());

                if (!nameA.isEmpty() && !nameB.isEmpty()) {
                    if (nameA.equals(nameB)) {
                        score += 60;
                        reasons.add("Trung ten cong ty");
                    } else {
                        double similarity = similarity(nameA, nameB);

                        if (similarity >= 0.85) {
                            score += 30;
                            reasons.add("Ten cong ty gan giong");
                        }
                    }
                }

                if (score > 0) {
                    result.add(new DuplicatePair(
                            a, b, score, List.copyOf(reasons)));
                }
            }
        }

        result.sort(Comparator.comparingInt(
                DuplicatePair::score).reversed());

        return result;
    }

    private String normalizeTax(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeName(String value) {
        if (value == null) return "";

        String normalized = Normalizer.normalize(
                value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();

        return normalized.replaceAll("\\s+", " ");
    }

    private String normalizeWebsite(String value) {
        if (value == null || value.isBlank()) return "";

        try {
            String text = value.trim();

            if (!text.contains("://")) {
                text = "https://" + text;
            }

            String host = URI.create(text).getHost();

            if (host == null) return "";

            host = host.toLowerCase(Locale.ROOT);

            return host.startsWith("www.")
                    ? host.substring(4)
                    : host;

        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private double similarity(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        int max = Math.max(a.length(), b.length());

        return max == 0 ? 1.0
                : 1.0 - (double) dp[a.length()][b.length()] / max;
    }
}