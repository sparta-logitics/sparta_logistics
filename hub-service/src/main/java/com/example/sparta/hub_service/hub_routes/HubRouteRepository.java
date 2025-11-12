package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.hub_service.core.domain.HubRoute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRouteRepository extends JpaRepository<HubRoute, UUID>, HubRouteRepositoryCustom {}
