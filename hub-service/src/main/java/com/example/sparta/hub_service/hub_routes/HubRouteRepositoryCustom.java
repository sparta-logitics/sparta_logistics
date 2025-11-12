package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.hub_service.core.vo.HubId;
import com.example.sparta.hub_service.hub_routes.dto.HubRouteResult;
import java.util.Optional;

public interface HubRouteRepositoryCustom {
    Optional<HubRouteResult> findDetailedRouteBetween(HubId departureHubId, HubId arrivalHubId);
}