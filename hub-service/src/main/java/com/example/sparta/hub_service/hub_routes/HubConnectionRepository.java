package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.hub_service.core.domain.HubConnection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubConnectionRepository extends JpaRepository<HubConnection, UUID> {

    List<HubConnection> findAllByDeletedAtIsNull();
}
