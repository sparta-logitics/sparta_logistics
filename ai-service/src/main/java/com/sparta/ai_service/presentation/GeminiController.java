package com.sparta.ai_service.presentation;

import com.sparta.ai_service.application.GeminiServiceV1;
import com.sparta.ai_service.application.dto.GeminiRequestDto;
import com.sparta.ai_service.application.dto.GeminiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiServiceV1 geminiServiceV1;

    @PostMapping("/predict")
    public GeminiResponseDto predict(@RequestBody GeminiRequestDto request) {
        return geminiServiceV1.predictShipping(request);
    }
}