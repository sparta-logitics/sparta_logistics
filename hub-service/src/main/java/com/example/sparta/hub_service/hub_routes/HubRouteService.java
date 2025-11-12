package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.hub_service.core.domain.HubConnection;
import com.example.sparta.hub_service.core.domain.HubRoute;
import com.example.sparta.hub_service.core.domain.HubRouteSegment;
import com.example.sparta.hub_service.core.enums.RouteMetric;
import com.example.sparta.hub_service.core.vo.Distance;
import com.example.sparta.hub_service.core.vo.Duration;
import com.example.sparta.hub_service.core.vo.HubId;
import com.example.sparta.hub_service.hub_routes.dto.HubRouteResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubRouteService {

    private final HubConnectionRepository hubConnectionRepository;
    private final HubRouteRepository hubRouteRepository;
    private final HubRoutePathFinder pathFinder;

    @Transactional
    @CacheEvict(cacheNames = "hubRoute", key = "#departureHubId + #arrivalHubId")
    public HubRouteResult computeAndSaveRoute(
        HubId departureHubId,
        HubId arrivalHubId
    ) {
        // 1. 허브 간 연결 로드
        List<HubConnection> connections =
            hubConnectionRepository.findAllByDeletedAtIsNull();

        // 2. 그래프 구성
        Map<HubId, List<HubConnection>> graph = buildGraph(connections);

        // 3. 다익스트라 실행 (거리 기준)
        List<HubConnection> shortestPath = pathFinder.findShortestPath(
            departureHubId,
            arrivalHubId,
            graph,
            RouteMetric.DISTANCE
        );

        if (shortestPath.isEmpty()) {
            throw new IllegalStateException("유효한 경로를 찾을 수 없습니다.");
        }

        // 4. 총 거리/시간 계산
        Distance totalDistance = shortestPath
            .stream()
            .map(HubConnection::getDistanceKm)
            .reduce(Distance::add)
            .orElseThrow();

        Duration totalDuration = shortestPath
            .stream()
            .map(HubConnection::getEstimatedMinutes)
            .reduce(Duration::add)
            .orElseThrow();

        // 5. 세그먼트 생성
        List<HubRouteSegment> segments = new ArrayList<>();
        int seq = 1;
        for (HubConnection conn : shortestPath) {
            segments.add(HubRouteSegment.create(conn, seq++));
        }

        // 6. HubRoute 생성 및 저장
        HubRoute hubRoute = HubRoute.create(
            departureHubId,
            arrivalHubId,
            segments,
            totalDistance,
            totalDuration
        );

        return HubRouteResult.from(hubRouteRepository.save(hubRoute));
    }

    @Transactional
    @Cacheable(cacheNames = "hubRoute", key = "#departureHubId + #arrivalHubId")
    public HubRouteResult getOrComputeRoute(
        HubId departureHubId,
        HubId arrivalHubId
    ) {
        return hubRouteRepository
            .findDetailedRouteBetween(departureHubId, arrivalHubId)
            .orElseGet(() -> computeAndSaveRoute(departureHubId, arrivalHubId));
    }

    private Map<HubId, List<HubConnection>> buildGraph(
        List<HubConnection> connections
    ) {
        Map<HubId, List<HubConnection>> graph = new HashMap<>();
        for (HubConnection conn : connections) {
            graph
                .computeIfAbsent(conn.getDepartureHubId(), k ->
                    new ArrayList<>()
                )
                .add(conn);
        }
        return graph;
    }
}
