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
public class Duration {

    private Integer duration;

    private Duration(Integer duration) {
        this.duration = validateDuration(duration);
    }

    private Integer validateDuration(Integer duration) {
        if (duration == null) {
            throw new IllegalArgumentException("소요 시간은 필수입니다");
        }
        if (duration < 0) {
            throw new IllegalArgumentException(
                "소요 시간은 0 이상이어야 합니다"
            );
        }
        return duration;
    }

    public static Duration of(Integer minutes) {
        return new Duration(minutes);
    }

    public static Duration ofHours(double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("시간은 0 이상이어야 합니다");
        }
        return new Duration((int) Math.round(hours * 60));
    }

    public Duration add(Duration other) {
        if (other == null) {
            return this;
        }
        return new Duration(this.duration + other.duration);
    }
}
