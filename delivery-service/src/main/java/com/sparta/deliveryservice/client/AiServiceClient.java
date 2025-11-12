package com.sparta.deliveryservice.client;

import com.sparta.deliveryservice.client.dto.EtaPredictRequest;
import com.sparta.deliveryservice.client.dto.EtaPredictResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", url = "${AI-SERVICE_RIBBON_LISTOFSERVERS}") // (F. AI 서비스의 유레카 이름)
public interface AiServiceClient {

    // (테스트의 'calculateEta(EtaPredictRequest)' 호출을 만족시키는 메서드)
    @PostMapping("/internal/predict/eta")
    EtaPredictResponse calculateEta(@RequestBody EtaPredictRequest request);
}
