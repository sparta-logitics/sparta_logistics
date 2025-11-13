package com.example.sparta.hub_service.hub.domain.entity;

import com.example.sparta.common.model.BaseEntity;
import com.example.sparta.hub_service.hub.domain.vo.HubAddress;
import com.example.sparta.hub_service.hub.domain.vo.HubCode;
import com.example.sparta.hub_service.hub.domain.vo.HubStatus;
import com.example.sparta.hub_service.hub.domain.vo.Location;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_hubs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "hub_code", nullable = false)
    private HubCode code;

    @Column(name = "hub_name", nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "address", column = @Column(name = "HubAddress"))
    })
    private HubAddress address;

    @Enumerated(EnumType.STRING)
    private HubStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "hub_latitude", precision = 10, scale = 8, nullable = false)),
        @AttributeOverride(name = "longitude", column = @Column(name = "hub_longitude", precision = 11, scale = 8, nullable = false)
        )
    })
    private Location hubLocation;


    public static Hub create(
        HubCode code,
        String name,
        HubAddress address,
        Location hubLocation
    ) {
        Hub hub = new Hub();

        hub.code = code;
        hub.name = name;
        hub.address = address;
        hub.status = HubStatus.ACTIVE;
        hub.hubLocation = hubLocation;

        return hub;
    }

    public void update(
        HubCode hubCode,
        String name,
        HubStatus hubstatus,
        HubAddress hubAddress,
        Location hubLocation
    ) {
        this.code = hubCode;
        this.name = name;
        this.status = hubstatus;
        this.address = hubAddress;
        this.hubLocation = hubLocation;
    }
}