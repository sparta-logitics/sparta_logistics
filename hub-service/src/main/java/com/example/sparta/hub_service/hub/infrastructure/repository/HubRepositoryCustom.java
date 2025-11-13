package com.example.sparta.hub_service.hub.infrastructure.repository;

import com.example.sparta.hub_service.hub.application.dto.HubResult;
import com.example.sparta.hub_service.hub.application.dto.HubSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepositoryCustom {
    Page<HubResult> search(HubSearchCondition condition, Pageable pageable);
}
