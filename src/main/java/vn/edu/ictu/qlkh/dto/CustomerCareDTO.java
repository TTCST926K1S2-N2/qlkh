package vn.edu.ictu.qlkh.dto;

public record CustomerCareDTO(
        long customerId,
        String companyName,
        long ownerId,
        String status
) {}