package com.example.sparta.product_service.client;

import com.example.sparta.company_service.dto.CompanyResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Company Service와 통신하는 Feign Client
 * 
 * MSA 환경에서 product-service가 company-service의 API를 호출하기 위한 클라이언트입니다.
 * Eureka를 통해 서비스 디스커버리를 수행하며, 로드 밸런싱을 지원합니다.
 * 
 * 개방-폐쇄 원칙(OCP)에 따라 새로운 API 호출이 필요할 때 확장 가능하도록 설계되었습니다.
 */
@FeignClient(
        name = "company-service",
        path = "/companies"
)
public interface CompanyClient {
    
    /**
     * 특정 업체 정보 조회
     * 
     * Company Service의 상세 조회 API를 호출하여 업체 존재 여부를 확인합니다.
     * 논리적으로 삭제된 업체는 404 에러가 반환됩니다.
     * 
     * @param companyId 조회할 업체 ID
     * @return 업체 정보 (존재하는 경우)
     * @throws feign.FeignException.NotFound 업체가 존재하지 않는 경우 (404)
     * @throws feign.FeignException 기타 통신 오류
     */
    @GetMapping("/{companyId}")
    CompanyResponseDto getCompany(@PathVariable("companyId") UUID companyId);
}