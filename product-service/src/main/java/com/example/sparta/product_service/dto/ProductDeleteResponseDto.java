package com.example.sparta.product_service.dto;

import com.example.sparta.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 논리 삭제 응답 DTO
 * 
 * 상품 삭제 성공 시 반환하는 데이터 전송 객체입니다.
 * API 명세에 정의된 삭제 응답 형식에 맞추어 설계되었으며,
 * 삭제 시점의 특정 정보만 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDeleteResponseDto {
    
    /**
     * 삭제된 상품 고유 식별자
     */
    private UUID product_id;
    
    /**
     * 삭제 후 상품 상태 (INACTIVE)
     */
    private String status;
    
    /**
     * 삭제일시
     */
    private LocalDateTime deleted_at;
    
    /**
     * 삭제자 ID
     */
    private Long deleted_by;
    
    /**
     * Entity를 삭제 응답 DTO로 변환하는 정적 팩토리 메서드
     * 
     * 삭제된 상품 Entity를 API 명세에 맞는 응답 형식으로 변환합니다.
     * 단일 책임 원칙(SRP)에 따라 변환 로직만 담당합니다.
     * 
     * @param product 변환할 Product Entity
     * @return 변환된 ProductDeleteResponseDto
     */
    public static ProductDeleteResponseDto from(Product product) {
        return ProductDeleteResponseDto.builder()
                .product_id(product.getProductId())
                .status(product.getStatus().name())
                .deleted_at(product.getDeletedAt())
                .deleted_by(product.getDeletedBy())
                .build();
    }
}