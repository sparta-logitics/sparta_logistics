package com.sparta.ai_service.infrastructure.client;


import com.sparta.ai_service.application.dto.GeminiRequestDto;
import com.sparta.ai_service.application.dto.GeminiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Gemini API 서버 URL은 application.yml에서 설정 가능
@FeignClient(name = "geminiClient", url = "${gemini.api.url}")
public interface GeminiClient {

    @PostMapping("/predict-shipping-time")
    GeminiResponseDto predictShippingTime(@RequestBody GeminiRequestDto request);
}