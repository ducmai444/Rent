package com.example.demo.model.DTO.contract;

import lombok.Data;

@Data
public class ContractUpdateDTO {
    private String startDate;
    private String endDate;
    private String customerId;
    private String apartmentId;
}