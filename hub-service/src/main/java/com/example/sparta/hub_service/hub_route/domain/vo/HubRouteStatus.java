package com.example.sparta.hub_service.hub_route.domain.vo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum HubRouteStatus {
    ACTIVE("운영중") {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    },
    CLOSED("폐쇄됨") {

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    };

    private final String description;

    public abstract boolean isActive();

    public abstract boolean isClosed();
}
