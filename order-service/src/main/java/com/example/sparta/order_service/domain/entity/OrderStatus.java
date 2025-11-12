package com.example.sparta.order_service.domain.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus {
    PAYMENT_PENDING("결제 대기 중"),
    PREPARING_FOR_SHIPMENT("출고 대기 중"),
    SHIPPED("운송 중"),
    DELIVERED("상품 배송 중"),
    COMPLETED("배송 완료"),
    CANCELED("주문 취소"),
    FAIL("주문 실패"),
    RETURNED("반품");

    private final String value;

    public static OrderStatus of(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "상태 코드 불일치 " + status
            );
        }
    }
}
