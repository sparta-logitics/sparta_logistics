package com.example.sparta.hub_service.hub_route.domain.entity;

import com.example.sparta.common.model.BaseEntity;
import com.example.sparta.hub_service.hub_route.domain.vo.Distance;
import com.example.sparta.hub_service.hub_route.domain.vo.Duration;
import com.example.sparta.hub_service.hub_route.domain.vo.HubId;
import com.example.sparta.hub_service.hub_route.domain.vo.HubRouteStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_hub_routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_route_id", nullable = false, updatable = false)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "departure_hub_id", nullable = false))
    private HubId departureHubId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "arrival_hub_id", nullable = false))
    private HubId arrivalHubId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hub_route_id")
    private List<HubRouteSegment> hubRouteSegments = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "distance", column = @Column(name = "total_distance_km", nullable = false))
    })
    private Distance totalDistanceKm;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "duration", column = @Column(name = "total_estimated_minutes", nullable = false))
    })
    private Duration totalEstimatedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_status", nullable = false)
    private HubRouteStatus status;


    public static HubRoute create(
        HubId departureHubId,
        HubId arrivalHubId,
        List<HubRouteSegment> hubRouteSegments,
        Distance totalDistanceKm,
        Duration totalEstimatedMinutes
    ) {
        HubRoute hubRoute = new HubRoute();

        validateHubRouteSegments(hubRouteSegments);

        hubRoute.departureHubId = departureHubId;
        hubRoute.arrivalHubId = arrivalHubId;
        hubRoute.hubRouteSegments = hubRouteSegments;
        hubRoute.totalDistanceKm = totalDistanceKm;
        hubRoute.totalEstimatedMinutes = totalEstimatedMinutes;
        hubRoute.status = HubRouteStatus.ACTIVE;

        return hubRoute;
    }

    private static void validateHubRouteSegments(List<HubRouteSegment> hubRouteSegments) {
        if (hubRouteSegments == null || hubRouteSegments.isEmpty()) {
            throw new IllegalArgumentException(
                "허브 루트는 최소 1개 이상이어야 합니다"
            );
        }
    }
}
