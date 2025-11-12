package com.example.sparta.product_service.controller;

import com.example.sparta.product_service.dto.ProductCreateRequestDto;
import com.example.sparta.product_service.dto.ProductCreateResponseDto;
import com.example.sparta.product_service.dto.ProductDeleteResponseDto;
import com.example.sparta.product_service.dto.ProductResponseDto;
import com.example.sparta.product_service.dto.ProductUpdateRequestDto;
import com.example.sparta.product_service.dto.ProductUpdateResponseDto;
import com.example.sparta.product_service.service.ProductService;
import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 상품 관리 컨트롤러
 * 
 * 상품 관련 HTTP 요청을 처리하는 컨트롤러입니다.
 * RESTful API 설계 원칙을 따르며, 적절한 HTTP 상태 코드를 반환합니다.
 * 
 * 권한 검증은 추후 Spring Security와 JWT를 통해 구현 예정입니다.
 * 현재는 비즈니스 로직에 집중하여 구현되었습니다.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 상품 목록 조회/검색 API
     * 
     * 사용자가 상품 조회/검색 화면에서 조건을 입력하면, 
     * 시스템이 권한과 조건을 확인하고 결과를 반환합니다.
     * 조건 없이 호출하면 모든 활성 상품을 반환합니다.
     * 
     * 마스터 관리자, 허브 관리자, 업체 담당자가 호출할 수 있습니다.
     * 
     * @param name 상품명 (부분 검색, 선택사항)
     * @param company_id 업체 ID (완전 일치, 선택사항)
     * @param hub_id 허브 ID (완전 일치, 선택사항)
     * @param status 상품 상태 (ACTIVE/INACTIVE, 선택사항)
     * @param pageable 페이지네이션 정보 (페이지 크기, 페이지 번호, 정렬)
     * @return 조건에 맞는 상품 목록과 페이지네이션 정보
     * @throws BusinessException 401 Unauthorized: JWT 토큰 없거나 만료 시
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String company_id,
            @RequestParam(required = false) String hub_id,
            @RequestParam(required = false) String status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        // userId, username, userRole 헤더 정보로 권한별 조회 제한 가능
        
        Page<ProductResponseDto> products = productService.getProducts(name, company_id, hub_id, status, pageable);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 특정 상품 상세 조회 API
     * 
     * 단일 상품의 상세 정보를 조회합니다.
     * 마스터 관리자, 허브 관리자, 업체 담당자가 상품의 모든 상세 정보를 확인할 수 있습니다.
     * 논리적으로 삭제된 상품은 조회되지 않습니다.
     * 
     * @param productId 조회할 상품의 UUID
     * @return 상품 상세 정보
     * @throws com.example.sparta.product_service.exception.ProductNotFoundException 존재하지 않는 상품 ID인 경우 404 Not Found
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        ProductResponseDto product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * 신규 상품 생성 API
     * 
     * 새로운 상품을 등록합니다.
     * 소속 업체와 허브가 유효해야 하며, 생성자는 자동 기록됩니다.
     * 성공 시 생성된 상품 정보를 반환합니다.
     * 
     * @param requestDto 상품 생성 요청 정보 (name, company_id, hub_id)
     * @return 생성된 상품 정보
     * @throws BusinessException 
     *   - 400 Bad Request: 존재하지 않는 업체/허브 ID 입력 시
     *   - 400 Bad Request: 상품명이 중복되는 경우
     *   - 403 Forbidden: 권한 없는 사용자가 요청 시
     */
    @PostMapping
    public ResponseEntity<ProductCreateResponseDto> createProduct(
            @RequestBody ProductCreateRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username, 
            @RequestHeader("X-User-Role") String userRole) {
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        
        ProductCreateResponseDto response = productService.createProduct(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 상품 정보 수정 API
     * 
     * 기존 상품의 정보를 수정합니다.
     * 상품명과 상태를 부분적으로 수정할 수 있으며, 수정자 정보는 자동 기록됩니다.
     * 
     * @param productId 수정할 상품의 UUID
     * @param requestDto 수정할 상품 정보 (name, status 중 수정할 필드만 전송)
     * @return 수정된 상품 정보
     * @throws com.example.sparta.product_service.exception.ProductNotFoundException 존재하지 않는 상품 ID인 경우 404 Not Found
     * @throws BusinessException 
     *   - 400 Bad Request: 수정할 필드가 없거나 유효하지 않은 값 입력 시
     *   - 400 Bad Request: 상품명이 중복되는 경우
     *   - 403 Forbidden: 권한 없는 사용자가 요청 시
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductUpdateResponseDto> updateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductUpdateRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        // userId, username, userRole 헤더 정보로 권한 확인 가능
        // 권한별 접근 제어: userRole 헤더로 MASTER_ADMIN, HUB_MANAGER, COMPANY_MANAGER 확인 가능
        
        ProductUpdateResponseDto response = productService.updateProduct(productId, requestDto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 상품 논리 삭제 API
     * 
     * 상품을 논리적으로 삭제합니다.
     * 실제 데이터는 유지하며 deleted_at, deleted_by 필드를 설정하고 상태를 INACTIVE로 변경합니다.
     * 연관 데이터(주문 등)는 별도 비활성화 처리가 필요합니다.
     * 
     * @param productId 삭제할 상품의 UUID
     * @return 삭제된 상품 정보
     * @throws com.example.sparta.product_service.exception.ProductNotFoundException 존재하지 않는 상품 ID인 경우 404 Not Found
     * @throws BusinessException 
     *   - 403 Forbidden: 권한 없는 사용자가 요청 시 (마스터 관리자, 허브 관리자만 삭제 가능)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductDeleteResponseDto> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        // userId, username, userRole 헤더 정보로 권한 확인 가능
        // 권한별 접근 제어: userRole 헤더로 MASTER_ADMIN, HUB_MANAGER만 삭제 가능하도록 확인 가능
        
        ProductDeleteResponseDto response = productService.deleteProduct(productId);
        return ResponseEntity.ok(response);
    }
}