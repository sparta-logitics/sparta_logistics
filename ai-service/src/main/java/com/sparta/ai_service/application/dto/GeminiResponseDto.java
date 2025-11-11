package com.sparta.ai_service.application.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeminiResponseDto {
    private LocalDateTime finalShippingTime; // 최종 발송 시한
    private boolean success; // AI 예측 성공 여부
}