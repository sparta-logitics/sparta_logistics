package com.example.sparta.hub_service.hub.domain.vo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubAddress implements Serializable {

    private String address;

    private HubAddress(String address) {
        validateAddress(address);
        this.address = address;
    }

    public static HubAddress of(String address) {
        return new HubAddress(address);
    }

    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }
        if (address.length() < 5) {
            throw new IllegalArgumentException("주소는 5자 이상이어야 합니다");
        }
        if (address.length() > 200) {
            throw new IllegalArgumentException("주소는 200자 이하여야 합니다");
        }
    }
}
