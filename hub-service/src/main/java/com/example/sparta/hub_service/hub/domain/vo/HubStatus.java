package com.example.sparta.hub_service.hub.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HubStatus {
    ACTIVE("정상 운영"),
    CLOSED("운영 종료");

    private final String description;

    public static HubStatus from(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("허브 상태는 비어 있을 수 없습니다");
        }

        for (HubStatus value : HubStatus.values()) {
            if (value.name().equalsIgnoreCase(status.trim())) {
                return value;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 허브 상태입니다: " + status);
    }
}