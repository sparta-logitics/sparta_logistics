package com.sparta.deliveryservice.dto.request;

import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DeliverySearchCriteria {

    // 1. [TDD] 첫 번째 검색 조건: 배송 상태
    private DeliveryStatus status;

    // REFACTOR TDD를 위한 driverId 필드 추가
    // API 명세서에 따라 companyDriverId, routeDriverId 등 더 명확히 나눌 수 있음
     private UUID driverId;
     private String recipientName;
}
