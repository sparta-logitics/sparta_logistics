package com.example.sparta.hub_service.core.vo;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("33.00000000");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("38.60000000");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("125.00000000");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("131.00000000");

    private static final int SCALE = 8;


    private BigDecimal latitude;
    private BigDecimal longitude;

    private Location(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = normalizeAndValidateLatitude(latitude);
        this.longitude = normalizeAndValidateLongitude(longitude);
    }

    public static Location of(double latitude, double longitude) {
        return new Location(
            BigDecimal.valueOf(latitude),
            BigDecimal.valueOf(longitude)
        );
    }

    public static Location of(BigDecimal latitude, BigDecimal longitude) {
        return new Location(latitude, longitude);
    }


    private BigDecimal normalizeAndValidateLatitude(BigDecimal latitude) {
        if (latitude == null) {
            throw new IllegalArgumentException("위도는 필수입니다.");
        }

        BigDecimal normalized = latitude.setScale(SCALE, RoundingMode.HALF_UP);

        if (normalized.compareTo(MIN_LATITUDE) < 0 || normalized.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("위도 범위는 -90 ~ 90 사이여야 합니다.");
        }

        return normalized;
    }

    private BigDecimal normalizeAndValidateLongitude(BigDecimal longitude) {
        if (longitude == null) {
            throw new IllegalArgumentException("경도는 필수입니다");
        }

        BigDecimal normalized = longitude.setScale(SCALE, RoundingMode.HALF_UP);

        if (normalized.compareTo(MIN_LONGITUDE) < 0 || normalized.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("경도 범위는 -180 ~ 180 사이여야 합니다.");
        }

        return normalized;
    }

    @Override
    public String toString() {
        return String.format(
            "Location(lat=%s, lon=%s)",
            latitude.toPlainString(),
            longitude.toPlainString()
        );
    }
}
