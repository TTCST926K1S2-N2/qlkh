package vn.edu.ictu.qlkh.dto;

import java.math.BigDecimal;

public record CustomerCareDTO(
        long customerId,
        String companyName,
        long ownerId,
        String status,
        BigDecimal contractValue,
        long daysInactive,
        boolean extraWarning
) {}