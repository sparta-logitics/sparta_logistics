package com.example.sparta.product_service.dto;

import com.example.sparta.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 정보 수정 요청 DTO
 * 
 * 클라이언트로부터 상품 수정 정보를 받기 위한 데이터 전송 객체입니다.
 * 입력값 검증 로직을 포함하며, 부분 업데이트를 지원합니다.
 * 단일 책임 원칙(SRP)에 따라 요청 데이터 처리만 담당합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequestDto {
    
    /**
     * 수정할 상품명 (선택적)
     */
    private String name;
    
    /**
     * 수정할 상품 상태 (선택적)
     */
    private String status;
    
    /**
     * 입력값 검증
     * 
     * 비즈니스 규칙에 따른 필수값 및 형식 검증을 수행합니다.
     * 부분 업데이트이므로 null 값은 허용되지만, 빈 문자열은 검증합니다.
     * 
     * @throws IllegalArgumentException 검증 실패 시
     */
    public void validate() {
        // 상품명 검증 (null은 허용, 빈 문자열은 불허)
        if (name != null) {
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("상품명은 빈 값일 수 없습니다.");
            }
            
            if (name.trim().length() > 100) {
                throw new IllegalArgumentException("상품명은 100자를 초과할 수 없습니다.");
            }
        }
        
        // 상품 상태 검증 (null은 허용, 유효하지 않은 enum 값은 불허)
        if (status != null) {
            if (status.trim().isEmpty()) {
                throw new IllegalArgumentException("상품 상태는 빈 값일 수 없습니다.");
            }
            
            // 유효한 상품 상태인지 확인
            try {
                Product.ProductStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 상품 상태입니다. (ACTIVE, INACTIVE만 허용)");
            }
        }
    }
    
    /**
     * 수정할 필드가 있는지 확인
     * 
     * @return 수정할 필드가 하나라도 있으면 true, 모두 null이면 false
     */
    public boolean hasFieldsToUpdate() {
        return name != null || status != null;
    }
    
    /**
     * 상품명이 수정 대상인지 확인
     * 
     * @return 상품명 수정 여부
     */
    public boolean isNameToUpdate() {
        return name != null;
    }
    
    /**
     * 상품 상태가 수정 대상인지 확인
     * 
     * @return 상품 상태 수정 여부
     */
    public boolean isStatusToUpdate() {
        return status != null;
    }
    
    /**
     * 정규화된 상품명 반환
     * 
     * @return 공백이 제거된 상품명
     */
    public String getNormalizedName() {
        return name != null ? name.trim() : null;
    }
    
    /**
     * 파싱된 상품 상태 반환
     * 
     * @return 파싱된 ProductStatus enum
     */
    public Product.ProductStatus getParsedStatus() {
        return status != null ? Product.ProductStatus.valueOf(status.trim().toUpperCase()) : null;
    }
}