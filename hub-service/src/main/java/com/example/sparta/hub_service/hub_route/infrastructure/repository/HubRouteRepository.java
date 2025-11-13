package com.example.sparta.hub_service.hub_route.infrastructure.repository;

import com.example.sparta.hub_service.hub_route.domain.entity.HubRoute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRouteRepository extends JpaRepository<HubRoute, UUID>,
    HubRouteRepositoryCustom {}
