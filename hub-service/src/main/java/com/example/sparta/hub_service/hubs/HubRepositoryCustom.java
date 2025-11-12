package com.example.sparta.hub_service.hubs;

import com.example.sparta.hub_service.hubs.dto.HubSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepositoryCustom {
    Page<HubResult> search(HubSearchCondition condition, Pageable pageable);
}
