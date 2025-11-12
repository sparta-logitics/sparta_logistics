package com.example.sparta.hub_service.core.domain;

import com.example.sparta.common.model.BaseEntity;
import com.example.sparta.hub_service.core.vo.Distance;
import com.example.sparta.hub_service.core.vo.Duration;
import com.example.sparta.hub_service.core.vo.HubId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_hub_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_connection_id", nullable = false, updatable = false)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "departure_hub_id", nullable = false))
    private HubId departureHubId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "arrival_hub_id", nullable = false))
    private HubId arrivalHubId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "distance", column = @Column(name = "distance_km", nullable = false))
    })
    private Distance distanceKm;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "duration", column = @Column(name = "estimated_minutes", nullable = false))
    })
    private Duration estimatedMinutes;

    public static HubConnection create(
        HubId departureHubId,
        HubId arrivalHubId,
        Distance distance,
        Duration duration
    ) {
        HubConnection connection = new HubConnection();
        connection.departureHubId = departureHubId;
        connection.arrivalHubId = arrivalHubId;
        connection.distanceKm = distance;
        connection.estimatedMinutes = duration;
        return connection;
    }

    public void update(
        HubId departureHubId,
        HubId arrivalHubId,
        Distance distance,
        Duration duration
    ) {
        this.departureHubId = departureHubId;
        this.arrivalHubId = arrivalHubId;
        this.distanceKm = distance;
        this.estimatedMinutes = duration;
    }
}
