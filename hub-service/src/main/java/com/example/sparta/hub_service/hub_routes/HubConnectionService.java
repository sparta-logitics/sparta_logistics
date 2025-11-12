package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.example.sparta.hub_service.core.domain.HubConnection;
import com.example.sparta.hub_service.core.vo.Distance;
import com.example.sparta.hub_service.core.vo.Duration;
import com.example.sparta.hub_service.core.vo.HubId;
import com.example.sparta.hub_service.hub_routes.dto.HubConnectionCommand;
import com.example.sparta.hub_service.hub_routes.dto.HubConnectionResult;
import com.example.sparta.hub_service.hub_routes.dto.UpdateHubConnectionCommand;
import com.example.sparta.hub_service.hubs.HubRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HubConnectionService {

    // TODO 허브 클라이언트 구현
    private final HubRepository hubRepository;
    private final HubConnectionRepository hubConnectionRepository;

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(cacheNames = "hubConnections", allEntries = true),
            @CacheEvict(cacheNames = "hubRoute", allEntries = true), // 경로 캐시도 무효화
        }
    )
    public UUID createConnection(HubConnectionCommand command) {
        validateHubsExist(command.departureHubId(), command.arrivalHubId());

        HubConnection connection = HubConnection.create(
            HubId.of(command.departureHubId()),
            HubId.of(command.arrivalHubId()),
            Distance.of(command.distanceKm()),
            Duration.of(command.estimatedMinutes())
        );

        HubConnection savedHubConnection = hubConnectionRepository.save(
            connection
        );

        return savedHubConnection.getId();
    }

    @Cacheable(cacheNames = "hubConnections", key = "methodName")
    public List<HubConnectionResult> getConnections() {
        return hubConnectionRepository
            .findAllByDeletedAtIsNull()
            .stream()
            .map(HubConnectionResult::from)
            .toList();
    }

    @Cacheable(cacheNames = "hubConnection", key = "#hubConnectionId")
    public HubConnectionResult getConnection(UUID hubConnectionId) {
        return HubConnectionResult.from(getHubConnectionById(hubConnectionId));
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(cacheNames = "hubConnection", key = "#hubConnectionId"),
            @CacheEvict(cacheNames = "hubConnections", allEntries = true),
            @CacheEvict(cacheNames = "hubRoute", allEntries = true),
        }
    )
    public void updateConnection(
        UUID hubConnectionId,
        UpdateHubConnectionCommand command
    ) {
        HubConnection hubConnection = getHubConnectionById(hubConnectionId);

        validateHubsExist(command.departureHubId(), command.arrivalHubId());

        hubConnection.update(
            HubId.of(command.departureHubId()),
            HubId.of(command.arrivalHubId()),
            Distance.of(command.distanceKm()),
            Duration.of(command.estimatedMinutes())
        );
    }

    @Transactional
    @Caching(
        evict = {
            @CacheEvict(cacheNames = "hubConnection", key = "#hubConnectionId"),
            @CacheEvict(cacheNames = "hubConnections", allEntries = true),
            @CacheEvict(cacheNames = "hubRoute", allEntries = true),
        }
    )
    public void deleteHubConnection(UUID hubConnectionId, Long userId) {
        HubConnection hubConnection = getHubConnectionById(hubConnectionId);

        hubConnection.delete(userId);
    }

    private void validateHubsExist(UUID departureId, UUID arrivalId) {
        if (!hubRepository.existsById(departureId)) {
            throw new BusinessException(ErrorCode.HUB_NOT_FOUND);
        }
        if (!hubRepository.existsById(arrivalId)) {
            throw new BusinessException(ErrorCode.HUB_NOT_FOUND);
        }
    }

    private HubConnection getHubConnectionById(UUID hubConnectionId) {
        return hubConnectionRepository
            .findById(hubConnectionId)
            .orElseThrow(() ->
                new BusinessException(ErrorCode.HUB_CONNECTION_NOT_FOUND)
            );
    }
}
