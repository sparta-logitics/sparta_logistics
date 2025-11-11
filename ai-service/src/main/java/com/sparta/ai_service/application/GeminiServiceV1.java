package com.sparta.ai_service.application;

import com.sparta.ai_service.application.dto.GeminiRequestDto;
import com.sparta.ai_service.application.dto.GeminiResponseDto;
import com.sparta.ai_service.infrastructure.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiServiceV1 {

    private final GeminiClient geminiClient;

    public GeminiResponseDto predictShipping(GeminiRequestDto request) {
        try {
            return geminiClient.predictShippingTime(request);
        } catch (Exception e) {
            return new GeminiResponseDto(null, false);
        }
    }
}