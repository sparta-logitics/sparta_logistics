package com.example.sparta.hub_service.hub.application;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.example.sparta.hub_service.hub.application.command.CreateHubCommand;
import com.example.sparta.hub_service.hub.application.command.UpdateHubCommand;
import com.example.sparta.hub_service.hub.application.dto.HubResult;
import com.example.sparta.hub_service.hub.application.dto.HubSearchCondition;
import com.example.sparta.hub_service.hub.domain.entity.Hub;
import com.example.sparta.hub_service.hub.domain.vo.HubAddress;
import com.example.sparta.hub_service.hub.domain.vo.HubCode;
import com.example.sparta.hub_service.hub.domain.vo.HubStatus;
import com.example.sparta.hub_service.hub.domain.vo.Location;
import com.example.sparta.hub_service.hub.infrastructure.repository.HubRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubService {

    private final HubRepository hubRepository;

    @Transactional
    @CacheEvict(cacheNames = "hub", allEntries = true)
    public UUID createHub(CreateHubCommand command) {
        HubCode hubCode = HubCode.of(command.code());

        if (hubRepository.existsByCode(hubCode)) {
            throw new BusinessException(ErrorCode.HUB_CODE_ALREADY_EXISTS);
        }

        HubAddress hubAddress = HubAddress.of(command.address());

        Location hubLocation = Location.of(
            command.latitude(),
            command.longitude()
        );

        Hub hub = Hub.create(hubCode, command.name(), hubAddress, hubLocation);

        Hub savedHub = hubRepository.save(hub);

        return savedHub.getId();
    }

    @Cacheable(value = "hub", key = "#hubId")
    public HubResult getHub(UUID hubId) {
        Hub hub = getHubById(hubId);

        return HubResult.from(hub);
    }

    @Cacheable(value = "hubList")
    public List<HubResult> getHubs() {
        return hubRepository
            .findAllByDeletedAtIsNull()
            .stream()
            .map(HubResult::from)
            .toList();
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(cacheNames = "hub", key = "#hubId"),
            @CacheEvict(cacheNames = "hubList", allEntries = true),
        }
    )
    public void updateHubService(UUID hubId, UpdateHubCommand command) {
        Hub hub = getHubById(hubId);

        HubCode newHubCode = HubCode.of(command.code());

        if (
            !hub.getCode().equals(newHubCode) &&
            hubRepository.existsByCode(newHubCode)
        ) {
            throw new BusinessException(ErrorCode.HUB_CODE_ALREADY_EXISTS);
        }

        HubAddress hubAddress = HubAddress.of(command.address());

        HubStatus hubStatus = HubStatus.from(command.status());

        Location hubLocation = Location.of(
            command.latitude(),
            command.longitude()
        );

        hub.update(
            newHubCode,
            command.name(),
            hubStatus,
            hubAddress,
            hubLocation
        );
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(cacheNames = "hub", key = "#hubId"),
            @CacheEvict(cacheNames = "hubList", allEntries = true),
        }
    )
    public void deleteHub(UUID hubId, Long userId) {
        Hub hub = getHubById(hubId);

        hub.delete(userId);
    }

    public Page<HubResult> searchHubs(
        HubSearchCondition condition,
        Pageable pageable
    ) {
        return hubRepository.search(condition, pageable);
    }

    private Hub getHubById(UUID hubId) {
        return hubRepository
            .findById(hubId)
            .orElseThrow(() -> new BusinessException(ErrorCode.HUB_NOT_FOUND));
    }
}
