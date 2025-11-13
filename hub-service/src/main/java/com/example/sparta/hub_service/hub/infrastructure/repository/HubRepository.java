package com.example.sparta.hub_service.hub.infrastructure.repository;

import com.example.sparta.hub_service.hub.domain.entity.Hub;
import com.example.sparta.hub_service.hub.domain.vo.HubCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<Hub, UUID>, HubRepositoryCustom {

    boolean existsByCode(HubCode hubCode);

    List<Hub> findAllByDeletedAtIsNull();
}
