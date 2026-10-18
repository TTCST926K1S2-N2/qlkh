package vn.edu.ictu.qlkh.service;

import java.util.Locale;
import java.util.Set;

public final class LeadScoringRules {

    private LeadScoringRules() {
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }

    public static double industryScore(
            String industry,
            Set<String> targetIndustries) {

        String value = normalize(industry);

        if (value.isEmpty()) {
            return 0;
        }

        if (targetIndustries == null || targetIndustries.isEmpty()) {
            return 0;
        }

        for (String target : targetIndustries) {
            if (value.equals(normalize(target))) {
                return 100;
            }
        }

        return 50;
    }

    public static double companySizeScore(String companySize) {
        String value = normalize(companySize);

        if (value.isEmpty()) {
            return 0;
        }

        value = value.replace('\u2013', '-')
                     .replace('\u2014', '-');

        if (!value.matches("\\d+(\\s*-\\s*\\d+)?")) {
            return 0;
        }

        String first = value.split("-")[0].trim();

        try {
            long employees = Long.parseLong(first);

            if (employees >= 50) {
                return 100;
            }

            if (employees >= 10) {
                return 70;
            }

            return 40;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double sourceScore(String source) {
        return switch (normalize(source)) {
            case "WEBSITE" -> 80;
            case "MANUAL" -> 70;
            case "EXCEL" -> 50;
            default -> 0;
        };
    }

    public static double interestScore(String interestLevel) {
        return switch (normalize(interestLevel)) {
            case "HIGH", "CAO" -> 100;
            case "MEDIUM", "TRUNG BINH" -> 60;
            case "LOW", "THAP" -> 20;
            default -> 0;
        };
    }
}