package com.sparta.ai_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeminiRequestDto {
    private String productInfo;
    private String orderRequest;
    private String departureHub;
    private String arrivalHub;
    private String deliveryWorkerSchedule;
}