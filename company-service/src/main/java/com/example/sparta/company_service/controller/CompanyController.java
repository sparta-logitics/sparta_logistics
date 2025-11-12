package com.example.sparta.company_service.controller;

import com.example.sparta.company_service.dto.CompanyCreateRequestDto;
import com.example.sparta.company_service.dto.CompanyCreateResponseDto;
import com.example.sparta.company_service.dto.CompanyDeleteResponseDto;
import com.example.sparta.company_service.dto.CompanyResponseDto;
import com.example.sparta.company_service.dto.CompanyUpdateRequestDto;
import com.example.sparta.company_service.dto.CompanyUpdateResponseDto;
import com.example.sparta.company_service.service.CompanyService;
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
 * 업체 관리 컨트롤러
 * 
 * 업체와 관련된 HTTP 요청을 처리하며, 마스터 관리자와 허브 관리자가
 * 업체 정보를 조회하고 관리할 수 있는 REST API를 제공합니다.
 */
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 업체 목록 조회 및 검색 API
     * 
     * 사용자가 업체 조회/검색 화면에서 조건(이름, 허브 등)을 입력하면,
     * 시스템이 권한과 조건을 확인하고 페이지네이션된 결과를 반환합니다.
     * 검색 조건 없이 호출하면 모든 활성 업체를 반환합니다.
     * 
     * @param name 업체명 검색 키워드 (부분 일치, 대소문자 무관)
     * @param hubId 특정 허브에 속한 업체 필터링
     * @param status 업체 상태 필터링 (ACTIVE, INACTIVE, DELETED)
     * @param pageable 페이지네이션 정보 (기본: 10개씩, 생성일 역순)
     * @return 조건에 맞는 업체 목록과 페이지네이션 정보
     */
    @GetMapping
    public ResponseEntity<Page<CompanyResponseDto>> getCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(name = "hub_id", required = false) UUID hubId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole
    ) {
        // 페이지 크기 검증
        if (pageable.getPageSize() > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    "페이지 크기는 100을 초과할 수 없습니다. 요청된 크기: " + pageable.getPageSize());
        }
        
        // 페이지 번호 검증
        if (pageable.getPageNumber() < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    "페이지 번호는 0 이상이어야 합니다. 요청된 번호: " + pageable.getPageNumber());
        }
        
        Page<CompanyResponseDto> companies = companyService.getCompanies(name, hubId, status, pageable);
        return ResponseEntity.ok(companies);
    }

    /**
     * 특정 업체 상세 조회 API
     * 
     * 단일 업체의 상세 정보를 조회합니다.
     * 마스터 관리자와 허브 관리자가 업체의 모든 상세 정보를 확인할 수 있습니다.
     * 논리적으로 삭제된 업체는 조회되지 않습니다.
     * 
     * @param companyId 조회할 업체의 UUID
     * @return 업체 상세 정보
     * @throws com.example.sparta.company_service.exception.CompanyNotFoundException 존재하지 않는 업체 ID인 경우 404 Not Found
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponseDto> getCompany(
            @PathVariable UUID companyId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        CompanyResponseDto company = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(company);
    }

    /**
     * 신규 업체 생성 API
     * 
     * 새로운 업체를 등록합니다.
     * 관리 허브 ID가 존재해야 하며, 중복 이름은 허용하지 않습니다.
     * 성공 시 생성된 업체 정보를 반환합니다.
     * 
     * @param requestDto 업체 생성 요청 정보 (name, type, hub_id, address)
     * @return 생성된 업체 정보
     * @throws BusinessException 
     *   - 400 Bad Request: 존재하지 않는 허브 ID 입력 시
     *   - 400 Bad Request: 업체명이 중복되는 경우
     *   - 403 Forbidden: 권한 없는 사용자가 요청 시
     */
    @PostMapping
    public ResponseEntity<CompanyCreateResponseDto> createCompany(
            @RequestBody CompanyCreateRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        // userId, username, userRole 헤더 정보로 권한 확인 가능
        
        CompanyCreateResponseDto response = companyService.createCompany(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 업체 정보 수정 API
     * 
     * 특정 업체의 이름, 주소 등을 수정합니다.
     * 부분 업데이트를 지원하며, 업체명이 변경되는 경우 중복을 검증합니다.
     * 
     * @param companyId 수정할 업체의 UUID
     * @param requestDto 업체 수정 요청 정보 (name, address)
     * @return 수정된 업체 정보
     * @throws com.example.sparta.company_service.exception.CompanyNotFoundException 존재하지 않는 업체 ID인 경우 404 Not Found
     * @throws BusinessException 400 Bad Request: 업체명이 중복되는 경우
     */
    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyUpdateResponseDto> updateCompany(
            @PathVariable UUID companyId,
            @RequestBody CompanyUpdateRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        // userId, username, userRole 헤더 정보로 권한 확인 가능
        
        CompanyUpdateResponseDto response = companyService.updateCompany(companyId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 업체 논리 삭제 API
     * 
     * 업체를 논리 삭제 처리합니다.
     * 실제 데이터는 유지하며, 상태만 INACTIVE로 변경하고 deleted_at, deleted_by를 기록합니다.
     * 
     * @param companyId 삭제할 업체의 UUID
     * @return 삭제된 업체 정보
     * @throws com.example.sparta.company_service.exception.CompanyNotFoundException 존재하지 않는 업체 ID인 경우 404 Not Found
     * @throws BusinessException 403 Forbidden: 권한 없는 사용자가 요청 시
     */
    @DeleteMapping("/{companyId}")
    public ResponseEntity<CompanyDeleteResponseDto> deleteCompany(
            @PathVariable UUID companyId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole) {
        // Gateway 인증 필터에서 전달받은 사용자 정보 활용
        // userId, username, userRole 헤더 정보로 권한 확인 가능
        
        CompanyDeleteResponseDto response = companyService.deleteCompany(companyId);
        return ResponseEntity.ok(response);
    }
}