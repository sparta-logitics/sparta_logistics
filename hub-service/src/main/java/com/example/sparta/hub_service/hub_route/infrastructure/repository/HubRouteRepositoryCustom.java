package com.example.sparta.hub_service.hub_route.infrastructure.repository;


import com.example.sparta.hub_service.hub_route.application.dto.HubRouteResult;
import com.example.sparta.hub_service.hub_route.domain.vo.HubId;
import java.util.Optional;

public interface HubRouteRepositoryCustom {
    Optional<HubRouteResult> findDetailedRouteBetween(HubId departureHubId, HubId arrivalHubId);
}