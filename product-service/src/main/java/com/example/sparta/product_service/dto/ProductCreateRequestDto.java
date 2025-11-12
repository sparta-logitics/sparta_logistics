package com.example.sparta.product_service.dto;

import com.example.sparta.product_service.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 상품 생성 요청 DTO
 * 
 * 클라이언트로부터 상품 생성 정보를 받기 위한 데이터 전송 객체입니다.
 * 입력값 검증 로직을 포함하며, Entity 변환 기능을 제공합니다.
 * 단일 책임 원칙(SRP)에 따라 요청 데이터 처리만 담당합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequestDto {
    
    /**
     * 상품명 (필수)
     */
    private String name;
    
    /**
     * 소속 업체 ID (필수)
     */
    private UUID company_id;
    
    /**
     * 소속 허브 ID (필수)
     */
    private UUID hub_id;
    
    /**
     * 입력값 검증
     * 
     * 비즈니스 규칙에 따른 필수값 및 형식 검증을 수행합니다.
     * 
     * @throws IllegalArgumentException 검증 실패 시
     */
    public void validate() {
        // 상품명 검증
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("상품명은 100자를 초과할 수 없습니다.");
        }
        
        // 업체 ID 검증
        if (company_id == null) {
            throw new IllegalArgumentException("업체 ID는 필수입니다.");
        }
        
        // 허브 ID 검증
        if (hub_id == null) {
            throw new IllegalArgumentException("허브 ID는 필수입니다.");
        }
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
     * DTO를 Entity로 변환
     * 
     * 개방-폐쇄 원칙(OCP)에 따라 새로운 필드 추가 시 확장 가능하도록 설계되었습니다.
     * 상태는 기본값인 ACTIVE로 설정되며, audit 정보는 BaseEntity에서 자동 처리됩니다.
     * 
     * @return 변환된 Product Entity
     */
    public Product toEntity() {
        return Product.builder()
                .name(getNormalizedName())
                .companyId(company_id)
                .hubId(hub_id)
                .status(Product.ProductStatus.ACTIVE) // 기본값으로 ACTIVE 설정
                .build();
    }
}