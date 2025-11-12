package com.example.sparta.order_service.presentation.dto.request;

import com.example.sparta.order_service.domain.entity.ShippingInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ShippingInfoRequest(
        @NotBlank(message = "회사 이름은 빈값일 수 없습니다.")
        @Size(min = 2, max = 20)
        String companyName,
        @NotNull
        @Size(min = 2, max = 20)
        String name,
        @NotNull
        @Size(min = 10, max = 13)
        String phone,
        @NotNull
        @Size(min = 2, max = 50)
        String address,
        @NotNull
        @Size(min = 2, max = 100)
        String addressDetail,
        @NotNull
        @Size(min = 2, max = 5)
        String zipCode) {

    public ShippingInfo toEntity() {
        return ShippingInfo.builder()
                .companyName(companyName)
                .name(name)
                .phone(phone)
                .address(address)
                .addressDetail(addressDetail)
                .zipCode(zipCode)
                .build();
    }
}
