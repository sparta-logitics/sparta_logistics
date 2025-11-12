package com.sparta.deliveryservice.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDriverAssignRequest {
    private UUID companyDriverId;
}
