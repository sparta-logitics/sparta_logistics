package com.example.sparta.hub_service.hub_route.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Distance {

    private Double distance;

    private Distance(Double distance) {
        this.distance = validateDistance(distance);
    }

    private Double validateDistance(Double distance) {
        if (distance == null) {
            throw new IllegalArgumentException("거리는 필수입니다");
        }
        if (distance < 0) {
            throw new IllegalArgumentException("거리는 0 이상이어야 합니다");
        }
        return distance;
    }

    public static Distance of(double kilometers) {
        return new Distance(kilometers);
    }

    public static Distance ofMeters(int meters) {
        if (meters < 0) {
            throw new IllegalArgumentException("거리는 0 이상이어야 합니다");
        }
        return new Distance(meters / 1000.0);
    }

    public Distance add(Distance other) {
        if (other == null) {
            return this;
        }
        return new Distance(this.distance + other.distance);
    }
}
