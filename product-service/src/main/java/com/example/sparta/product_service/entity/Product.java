package com.example.sparta.product_service.entity;

import com.example.sparta.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

/**
 * 상품 Entity
 * 
 * 상품 정보를 관리하는 엔티티입니다.
 * BaseEntity를 상속받아 생성자, 수정자, 생성일시, 수정일시, 삭제일시, 삭제자 정보를 자동 관리합니다.
 * p_products 테이블과 매핑되며, 업체 소속과 허브 관계를 관리합니다.
 */
@Entity
@Table(name = "p_products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    @Comment("상품 ID")
    private UUID productId;

    @Column(nullable = false, length = 100)
    @Comment("상품명")
    private String name;

    @Column(name = "company_id", nullable = false)
    @Comment("업체 ID")
    private UUID companyId;

    @Column(name = "hub_id", nullable = false)
    @Comment("허브 ID")
    private UUID hubId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("활성/비활성 상태")
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * 상품 상태 enum
     * ACTIVE: 활성 상품
     * INACTIVE: 비활성 상품 (논리 삭제된 상품)
     */
    public enum ProductStatus {
        ACTIVE, INACTIVE
    }
    
    /**
     * 상품명을 수정합니다.
     * 
     * @param name 새로운 상품명
     */
    public void updateName(String name) {
        this.name = name;
    }
    
    /**
     * 상품 소속 업체를 변경합니다.
     * 
     * @param companyId 새로운 업체 ID
     */
    public void updateCompany(UUID companyId) {
        this.companyId = companyId;
    }
    
    /**
     * 상품 소속 허브를 변경합니다.
     * 
     * @param hubId 새로운 허브 ID
     */
    public void updateHub(UUID hubId) {
        this.hubId = hubId;
    }
    
    /**
     * 상품 상태를 변경합니다.
     * 
     * @param status 새로운 상품 상태
     */
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }
    
    /**
     * 상품을 논리적으로 삭제합니다.
     * 
     * 실제 데이터는 유지하며 상태를 INACTIVE로 변경합니다.
     * BaseEntity의 deleted_at, deleted_by 필드가 자동으로 설정됩니다.
     */
    public void softDelete() {
        this.status = ProductStatus.INACTIVE;
    }
}