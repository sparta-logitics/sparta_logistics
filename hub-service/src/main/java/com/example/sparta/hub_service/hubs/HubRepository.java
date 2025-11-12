package com.example.sparta.hub_service.hubs;

import com.example.sparta.hub_service.core.domain.Hub;
import com.example.sparta.hub_service.core.enums.HubCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {

    boolean existsByCode(HubCode hubCode);

    List<Hub> findAllByDeletedAtIsNull();
}
