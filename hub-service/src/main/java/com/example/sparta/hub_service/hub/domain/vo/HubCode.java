package com.example.sparta.hub_service.hub.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HubCode {
    SEOUL("서울특별시 센터"),
    GYEONGGI_NORTH("경기 북부 센터"),
    GYEONGGI_SOUTH("경기 남부 센터"),
    BUSAN("부산광역시 센터"),
    DAEGU("대구광역시 센터"),
    INCHEON("인천광역시 센터"),
    GWANGJU("광주광역시 센터"),
    DAEJEON("대전광역시 센터"),
    ULSAN("울산광역시 센터"),
    SEJONG("세종특별자치시 센터"),
    GANGWON("강원특별자치도 센터"),
    CHUNGBUK("충청북도 센터"),
    CHUNGNAM("충청남도 센터"),
    JEONBUK("전북특별자치도 센터"),
    JEONNAM("전라남도 센터"),
    GYEONGBUK("경상북도 센터"),
    GYEONGNAM("경상남도 센터");

    private final String description;

    public static HubCode of(String code) {
        try {
            return HubCode.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "유효하지 않은 허브 코드: " + code
            );
        }
    }
}
