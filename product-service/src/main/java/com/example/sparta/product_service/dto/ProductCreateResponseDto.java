package com.example.sparta.product_service.dto;

import com.example.sparta.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 생성 응답 DTO
 * 
 * 상품 생성 성공 시 반환하는 데이터 전송 객체입니다.
 * API 명세에 정의된 생성 응답 형식에 맞추어 설계되었으며,
 * 생성된 상품의 모든 필요한 정보를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateResponseDto {
    
    /**
     * 생성된 상품 고유 식별자
     */
    private UUID product_id;
    
    /**
     * 상품명
     */
    private String name;
    
    /**
     * 소속 업체 ID
     */
    private UUID company_id;
    
    /**
     * 소속 허브 ID
     */
    private UUID hub_id;
    
    /**
     * 상품 상태 (ACTIVE)
     */
    private String status;
    
    /**
     * 생성일시
     */
    private LocalDateTime created_at;
    
    /**
     * 생성자 ID
     */
    private Long created_by;
    
    /**
     * Entity를 생성 응답 DTO로 변환하는 정적 팩토리 메서드
     * 
     * 생성된 상품 Entity를 API 명세에 맞는 응답 형식으로 변환합니다.
     * 단일 책임 원칙(SRP)에 따라 변환 로직만 담당합니다.
     * 
     * @param product 변환할 Product Entity
     * @return 변환된 ProductCreateResponseDto
     */
    public static ProductCreateResponseDto from(Product product) {
        return ProductCreateResponseDto.builder()
                .product_id(product.getProductId())
                .name(product.getName())
                .company_id(product.getCompanyId())
                .hub_id(product.getHubId())
                .status(product.getStatus().name())
                .created_at(product.getCreatedAt())
                .created_by(product.getCreatedBy())
                .build();
    }
}