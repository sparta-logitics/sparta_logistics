package com.example.sparta.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 시스템 전체에서 사용하는 에러 코드 정의
 * 
 * enum을 사용하여 에러 코드를 중앙 집중식으로 관리하며,
 * HTTP 상태 코드와 메시지를 함께 정의하여 일관성을 보장합니다.
 * 단일 책임 원칙(SRP)에 따라 에러 정의만 담당합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // Common 에러 (1000번대)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 타입의 값입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "필수 요청 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "지원하지 않는 HTTP 메소드입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근이 거부되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C006", "서버 내부 오류가 발생했습니다."),
    
    // Company 관련 에러 (2000번대)
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COM001", "업체를 찾을 수 없습니다."),
    COMPANY_ALREADY_EXISTS(HttpStatus.CONFLICT, "COM002", "이미 존재하는 업체입니다."),
    COMPANY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COM003", "이미 삭제된 업체입니다."),
    INVALID_COMPANY_STATUS(HttpStatus.BAD_REQUEST, "COM004", "유효하지 않은 업체 상태입니다."),
    
    // Product 관련 에러 (3000번대)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD001", "상품을 찾을 수 없습니다."),
    PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PRD002", "이미 존재하는 상품입니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRD003", "재고가 부족합니다."),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PRD004", "유효하지 않은 상품 상태입니다."),
    
    // Hub 관련 에러 (4000번대)
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "HUB001", "허브를 찾을 수 없습니다."),
    HUB_ALREADY_EXISTS(HttpStatus.CONFLICT, "HUB-002", "이미 존재하는 허브입니다."),
    HUB_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "HUB-003", "이미 존재하는 허브 코드입니다."),
    HUB_CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "HUB-101", "허브 연결을 찾을 수 없습니다."),

    // Order 관련 에러 (5000번대)
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD001", "주문을 찾을 수 없습니다."),
    ORDER_MODIFICATION_NOT_ALLOWED(HttpStatus.CONFLICT, "ORD002", "주문 수정이 불가능한 상태입니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORD003", "해당 주문에 접근할 권한이 없습니다."),
    INVALID_SORT_PARAMETER(HttpStatus.BAD_REQUEST, "ORD004", "유효하지 않은 정렬 필드명입니다."),
    ORDER_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ORD005", "주문 처리 중 알 수 없는 오류가 발생했습니다."),
    ORDER_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORD006", "주문 조회 중 쿼리 실행에 실패했습니다."),

    
    // User 관련 에러 (6000번대)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR001", "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USR002", "인증이 필요합니다."),
    DUPLICATED_USER(HttpStatus.CONFLICT, "USR003", "이미 존재하는 사용자입니다."),
    INVALID_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "USR004", "잘못된 상태 변경 요청입니다"),
    INVALID_ROLE_CHANGE(HttpStatus.BAD_REQUEST, "USR005", "잘못된 역할 변경 요청입니다"),
    ALREADY_DELETED_USER(HttpStatus.BAD_REQUEST, "USR006", "이미 삭제된 사용자입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USR007", "아이디 또는 비밀번호가 잘못되었습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "USR008", "사용자 서비스가 현재 이용 불가 상태입니다."),
    REQUEST_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "USR009", "요청 처리 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."),

    // AI 관련 에러 (7000번대)
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI001", "AI 서비스를 사용할 수 없습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    /**
     * 에러 코드가 4xx 클라이언트 에러인지 확인
     * 
     * @return 4xx 에러면 true, 아니면 false
     */
    public boolean isClientError() {
        return httpStatus.is4xxClientError();
    }
    
    /**
     * 에러 코드가 5xx 서버 에러인지 확인
     * 
     * @return 5xx 에러면 true, 아니면 false
     */
    public boolean isServerError() {
        return httpStatus.is5xxServerError();
    }
}