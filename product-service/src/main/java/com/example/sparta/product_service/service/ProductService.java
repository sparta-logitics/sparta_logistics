package com.example.sparta.product_service.service;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.example.sparta.company_service.dto.CompanyResponseDto;
import com.example.sparta.hub_service.hub.domain.vo.HubStatus;
import com.example.sparta.hub_service.hub.presentation.response.HubDetailResponse;
import com.example.sparta.product_service.client.CompanyClient;
import com.example.sparta.product_service.client.HubClient;
import com.example.sparta.product_service.dto.ProductCreateRequestDto;
import com.example.sparta.product_service.dto.ProductCreateResponseDto;
import com.example.sparta.product_service.dto.ProductDeleteResponseDto;
import com.example.sparta.product_service.dto.ProductResponseDto;
import com.example.sparta.product_service.dto.ProductSearchCriteria;
import com.example.sparta.product_service.dto.ProductUpdateRequestDto;
import com.example.sparta.product_service.dto.ProductUpdateResponseDto;
import com.example.sparta.product_service.entity.Product;
import com.example.sparta.product_service.repository.ProductRepository;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 조회 서비스 구현체
 * 
 * 상품 조회와 관련된 비즈니스 로직을 구현합니다.
 * Repository 계층과의 의존성을 인터페이스로 관리하여 결합도를 낮췄습니다. (DIP)
 * 
 * 트랜잭션 관리를 통해 데이터 일관성을 보장하며,
 * 로깅을 통해 운영 시 추적 가능성을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService implements ProductQueryService {
    
    private final ProductRepository productRepository;
    private final CompanyClient companyClient;
    private final HubClient hubClient;
    
    /**
     * 검색 조건에 따른 상품 목록을 조회합니다.
     * 
     * 검색 조건이 없으면 모든 활성 상품을 반환하며,
     * 조건이 있으면 해당 조건에 맞는 상품만 반환합니다.
     * 논리적으로 삭제된 상품은 결과에서 제외됩니다.
     * 
     * @param searchCriteria 검색 조건 (상품명, 업체ID, 허브ID, 상태)
     * @param pageable 페이지네이션 정보
     * @return 조건에 맞는 상품 목록과 페이지 정보
     * @throws BusinessException 조회 중 오류 발생 시
     */
    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchCriteria searchCriteria, Pageable pageable) {
        log.debug("상품 목록 조회 요청 - 조건: {}, 페이지: {}", searchCriteria, pageable);
        
        try {
            // 검색 조건이 없는 경우 기본 조건 설정 (활성 상품만)
            ProductSearchCriteria effectiveCriteria = searchCriteria;
            if (searchCriteria == null || !searchCriteria.hasAnySearchCondition()) {
                effectiveCriteria = ProductSearchCriteria.builder()
                        .status(Product.ProductStatus.ACTIVE)
                        .build();
                log.debug("검색 조건이 없어 기본 조건 적용 - 활성 상품만 조회");
            }
            
            // Repository를 통한 검색 실행
            Page<Product> productsPage = productRepository.searchProducts(effectiveCriteria, pageable);
            
            // Entity를 DTO로 변환
            Page<ProductResponseDto> responsePage = productsPage.map(ProductResponseDto::from);
            
            log.info("상품 목록 조회 완료 - 총 {}개 상품 중 {}개 조회됨 (페이지 {}/{})",
                    responsePage.getTotalElements(),
                    responsePage.getNumberOfElements(),
                    responsePage.getNumber() + 1,
                    responsePage.getTotalPages());
            
            return responsePage;
            
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생 - 조건: {}, 오류: {}", searchCriteria, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 목록 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 편의 메서드: 조건 없이 모든 활성 상품 조회
     * 
     * @param pageable 페이지네이션 정보
     * @return 모든 활성 상품 목록
     */
    public Page<ProductResponseDto> getProducts(Pageable pageable) {
        return searchProducts(null, pageable);
    }
    
    /**
     * 특정 상품 상세 정보를 조회합니다.
     * 
     * 존재하지 않거나 논리적으로 삭제된 상품에 대해서는 
     * ProductNotFoundException을 발생시킵니다.
     * 
     * @param productId 조회할 상품 ID
     * @return 상품 상세 정보
     * @throws com.example.sparta.product_service.exception.ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    @Override
    public ProductResponseDto getProductById(UUID productId) {
        log.debug("상품 상세 조회 요청 - productId: {}", productId);
        
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "상품 ID는 필수입니다.");
        }
        
        try {
            Product product = productRepository.findById(productId)
                    .filter(p -> p.getDeletedAt() == null) // 논리 삭제된 상품 제외
                    .orElseThrow(() -> {
                        log.warn("조회할 상품을 찾을 수 없음 - productId: {}", productId);
                        return new com.example.sparta.product_service.exception.ProductNotFoundException(productId);
                    });
            
            ProductResponseDto response = ProductResponseDto.from(product);
            
            log.info("상품 상세 조회 완료 - productId: {}, name: {}", 
                    product.getProductId(), product.getName());
            
            return response;
            
        } catch (com.example.sparta.product_service.exception.ProductNotFoundException e) {
            // ProductNotFoundException은 그대로 던짐 (이미 적절한 에러 코드 포함)
            throw e;
        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류 발생 - productId: {}, 오류: {}", productId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 편의 메서드: 쿼리 파라미터를 받아 검색 조건 생성 후 조회
     * 
     * @param name 상품명 (선택사항)
     * @param companyId 업체 ID (선택사항)  
     * @param hubId 허브 ID (선택사항)
     * @param status 상품 상태 (선택사항)
     * @param pageable 페이지네이션 정보
     * @return 조건에 맞는 상품 목록
     */
    public Page<ProductResponseDto> getProducts(String name, String companyId, String hubId, String status, Pageable pageable) {
        log.debug("상품 목록 조회 요청 - name: {}, companyId: {}, hubId: {}, status: {}", 
                 name, companyId, hubId, status);
        
        try {
            ProductSearchCriteria.ProductSearchCriteriaBuilder builder = ProductSearchCriteria.builder();
            
            // 상품명 조건
            if (name != null && !name.trim().isEmpty()) {
                builder.name(name.trim());
            }
            
            // 업체 ID 조건
            if (companyId != null && !companyId.trim().isEmpty()) {
                try {
                    builder.companyId(java.util.UUID.fromString(companyId.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 업체 ID 형식 - companyId: {}", companyId);
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 업체 ID 형식입니다.");
                }
            }
            
            // 허브 ID 조건
            if (hubId != null && !hubId.trim().isEmpty()) {
                try {
                    builder.hubId(java.util.UUID.fromString(hubId.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 허브 ID 형식 - hubId: {}", hubId);
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 허브 ID 형식입니다.");
                }
            }
            
            // 상품 상태 조건
            if (status != null && !status.trim().isEmpty()) {
                try {
                    builder.status(Product.ProductStatus.valueOf(status.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 상품 상태 값 - status: {}", status);
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 상품 상태 값입니다.");
                }
            }
            
            ProductSearchCriteria searchCriteria = builder.build();
            return searchProducts(searchCriteria, pageable);
            
        } catch (BusinessException e) {
            // BusinessException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생 - 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 신규 상품을 생성합니다.
     * 
     * 소속 업체와 허브의 존재 여부를 검증하고 상품명 중복을 확인한 후 새로운 상품을 등록합니다.
     * 비즈니스 규칙에 따라 생성 시점에 ACTIVE 상태로 설정됩니다.
     * 
     * @param requestDto 상품 생성 요청 정보
     * @return 생성된 상품 정보
     * @throws BusinessException 업체/허브가 존재하지 않거나 상품명이 중복되는 경우
     */
    @Override
    @Transactional
    public ProductCreateResponseDto createProduct(ProductCreateRequestDto requestDto) {
        log.debug("상품 생성 요청 - name: {}, companyId: {}, hubId: {}", 
                 requestDto.getName(), requestDto.getCompany_id(), requestDto.getHub_id());
        
        // 입력값 검증
        if (requestDto == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "상품 생성 요청 정보는 필수입니다.");
        }
        
        requestDto.validate();
        
        try {
            // 1. 업체 존재 여부 검증 (Company Service 연동)
            UUID companyId = requestDto.getCompany_id();
            validateCompanyExists(companyId);
            
            // 2. 허브 존재 여부 검증 (Hub Service 연동)
            UUID hubId = requestDto.getHub_id();
            validateHubExists(hubId);
            
            // 3. 상품명 중복 검증
            String trimmedName = requestDto.getNormalizedName();
            boolean nameExists = productRepository.existsByNameAndDeletedAtIsNull(trimmedName);
            if (nameExists) {
                log.warn("상품명 중복 감지 - name: {}", trimmedName);
                throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS, 
                        String.format("이미 존재하는 상품명입니다: %s", trimmedName));
            }
            
            // 4. 상품 Entity 생성 및 저장
            Product product = requestDto.toEntity();
            Product savedProduct = productRepository.save(product);
            
            log.info("상품 생성 완료 - productId: {}, name: {}, companyId: {}, hubId: {}", 
                    savedProduct.getProductId(), savedProduct.getName(), 
                    savedProduct.getCompanyId(), savedProduct.getHubId());
            
            // 5. 응답 DTO 변환 및 반환
            return ProductCreateResponseDto.from(savedProduct);
            
        } catch (BusinessException e) {
            // BusinessException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("상품 생성 중 오류 발생 - name: {}, 오류: {}", requestDto.getName(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 업체 존재 여부를 검증합니다.
     * 
     * Company Service를 호출하여 업체가 존재하고 활성 상태인지 확인합니다.
     * MSA 환경에서 서비스 간 통신을 통해 데이터 일관성을 보장합니다.
     * 
     * @param companyId 검증할 업체 ID
     * @throws BusinessException 업체가 존재하지 않거나 비활성 상태인 경우
     */
    private void validateCompanyExists(UUID companyId) {
        if (companyId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "업체 ID는 필수입니다.");
        }

        try {
            CompanyResponseDto company = companyClient.getCompany(companyId);
            
            // 업체가 활성 상태인지 확인
            if (!"ACTIVE".equals(company.getStatus())) {
                log.warn("비활성 업체 감지 - companyId: {}, status: {}", companyId, company.getStatus());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성 상태의 업체입니다.");
            }
            
            log.debug("업체 검증 완료 - companyId: {}, name: {}", companyId, company.getName());
            
        } catch (FeignException.NotFound e) {
            log.warn("존재하지 않는 업체 - companyId: {}", companyId);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    String.format("존재하지 않는 업체입니다. ID: %s", companyId));
        } catch (FeignException e) {
            log.error("Company 서비스 호출 중 오류 발생 - companyId: {}, 상태코드: {}", 
                     companyId, e.status(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "업체 정보 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 허브 존재 여부를 검증합니다.
     * 
     * Hub Service를 호출하여 허브가 존재하고 활성 상태인지 확인합니다.
     * Hub Service의 기존 API (GET /hubs/{hubId})를 사용합니다.
     * 
     * @param hubId 검증할 허브 ID
     * @throws BusinessException 허브가 존재하지 않거나 비활성 상태인 경우
     */
    private void validateHubExists(UUID hubId) {
        if (hubId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "허브 ID는 필수입니다.");
        }

        try {
            HubDetailResponse hub = hubClient.getHub(hubId);
            
            // 허브가 활성 상태인지 확인
            if (hub.status() != HubStatus.ACTIVE) {
                log.warn("비활성 허브 감지 - hubId: {}, status: {}", hubId, hub.status());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성 상태의 허브입니다.");
            }
            
            log.debug("허브 검증 완료 - hubId: {}, name: {}", hubId, hub.name());
            
        } catch (FeignException.NotFound e) {
            log.warn("존재하지 않는 허브 - hubId: {}", hubId);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    String.format("존재하지 않는 허브입니다. ID: %s", hubId));
        } catch (FeignException e) {
            log.error("Hub 서비스 호출 중 오류 발생 - hubId: {}, 상태코드: {}", 
                     hubId, e.status(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "허브 정보 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    
    /**
     * 상품 정보를 수정합니다.
     * 
     * 기존 상품의 정보를 부분적으로 수정할 수 있으며,
     * 상품명 중복 검증과 비즈니스 규칙을 적용합니다.
     * 
     * @param productId 수정할 상품 ID
     * @param requestDto 수정할 상품 정보
     * @return 수정된 상품 정보
     * @throws com.example.sparta.product_service.exception.ProductNotFoundException 상품을 찾을 수 없는 경우
     * @throws BusinessException 유효하지 않은 수정 요청이거나 상품명이 중복되는 경우
     */
    @Override
    @Transactional
    public ProductUpdateResponseDto updateProduct(UUID productId, ProductUpdateRequestDto requestDto) {
        log.debug("상품 정보 수정 요청 - productId: {}, name: {}, status: {}", 
                 productId, requestDto.getName(), requestDto.getStatus());
        
        // 입력값 검증
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "상품 ID는 필수입니다.");
        }
        
        if (requestDto == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수정할 상품 정보는 필수입니다.");
        }
        
        requestDto.validate();
        
        // 수정할 필드가 있는지 확인
        if (!requestDto.hasFieldsToUpdate()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수정할 필드가 없습니다.");
        }
        
        try {
            // 1. 상품 존재 여부 확인
            Product product = productRepository.findById(productId)
                    .filter(p -> p.getDeletedAt() == null) // 논리 삭제된 상품 제외
                    .orElseThrow(() -> {
                        log.warn("수정할 상품을 찾을 수 없음 - productId: {}", productId);
                        return new com.example.sparta.product_service.exception.ProductNotFoundException(productId);
                    });
            
            // 2. 상품명 변경 시 중복 검증
            if (requestDto.isNameToUpdate()) {
                String newName = requestDto.getNormalizedName();
                
                // 현재 상품과 다른 이름인 경우에만 중복 검증
                if (!newName.equals(product.getName())) {
                    boolean nameExists = productRepository.existsByNameAndDeletedAtIsNull(newName);
                    if (nameExists) {
                        log.warn("상품명 중복 감지 - name: {}", newName);
                        throw new BusinessException(ErrorCode.PRODUCT_ALREADY_EXISTS, 
                                String.format("이미 존재하는 상품명입니다: %s", newName));
                    }
                }
                
                // 상품명 변경
                product.updateName(newName);
                log.debug("상품명 변경 - productId: {}, 기존: {}, 변경: {}", 
                         productId, product.getName(), newName);
            }
            
            // 3. 상품 상태 변경
            if (requestDto.isStatusToUpdate()) {
                Product.ProductStatus newStatus = requestDto.getParsedStatus();
                product.updateStatus(newStatus);
                log.debug("상품 상태 변경 - productId: {}, 변경: {}", productId, newStatus);
            }
            
            // 4. 변경사항 저장
            Product updatedProduct = productRepository.save(product);
            
            log.info("상품 정보 수정 완료 - productId: {}, name: {}, status: {}", 
                    updatedProduct.getProductId(), updatedProduct.getName(), updatedProduct.getStatus());
            
            // 5. 응답 DTO 변환 및 반환
            return ProductUpdateResponseDto.from(updatedProduct);
            
        } catch (com.example.sparta.product_service.exception.ProductNotFoundException e) {
            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("상품 정보 수정 중 오류 발생 - productId: {}, 오류: {}", productId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 정보 수정 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 상품을 논리적으로 삭제합니다.
     * 
     * 실제 데이터를 삭제하지 않고 deleted_at, deleted_by 필드를 설정하고
     * 상태를 INACTIVE로 변경하여 논리적 삭제를 수행합니다.
     * BaseEntity의 소프트 삭제 기능을 활용합니다.
     * 
     * @param productId 삭제할 상품 ID
     * @return 삭제된 상품 정보
     * @throws com.example.sparta.product_service.exception.ProductNotFoundException 상품을 찾을 수 없는 경우
     * @throws BusinessException 이미 삭제된 상품이거나 기타 오류 발생 시
     */
    @Override
    @Transactional
    public ProductDeleteResponseDto deleteProduct(UUID productId) {
        log.debug("상품 논리 삭제 요청 - productId: {}", productId);
        
        // 입력값 검증
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "상품 ID는 필수입니다.");
        }
        
        try {
            // 1. 상품 존재 여부 확인 (이미 삭제된 상품 제외)
            Product product = productRepository.findById(productId)
                    .filter(p -> p.getDeletedAt() == null) // 논리 삭제된 상품 제외
                    .orElseThrow(() -> {
                        log.warn("삭제할 상품을 찾을 수 없음 - productId: {}", productId);
                        return new com.example.sparta.product_service.exception.ProductNotFoundException(productId);
                    });
            
            // 2. 논리 삭제 수행
            // BaseEntity의 소프트 삭제 기능을 활용하여 deleted_at, deleted_by 자동 설정
            product.softDelete(); // 상태를 INACTIVE로 변경
            
            // 3. 변경사항 저장
            Product deletedProduct = productRepository.save(product);
            
            log.info("상품 논리 삭제 완료 - productId: {}, name: {}, deletedAt: {}", 
                    deletedProduct.getProductId(), deletedProduct.getName(), deletedProduct.getDeletedAt());
            
            // 4. 응답 DTO 변환 및 반환
            return ProductDeleteResponseDto.from(deletedProduct);
            
        } catch (com.example.sparta.product_service.exception.ProductNotFoundException e) {
            // ProductNotFoundException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("상품 논리 삭제 중 오류 발생 - productId: {}, 오류: {}", productId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상품 삭제 중 오류가 발생했습니다.", e);
        }
    }
}