package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.hub_service.core.domain.HubConnection;
import com.example.sparta.hub_service.core.enums.RouteMetric;
import com.example.sparta.hub_service.core.vo.HubId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.springframework.stereotype.Component;

/**
 * 허브 간 경로 탐색(Shortest Path) 알고리즘 구현 클래스.
 *
 * - 그래프 탐색 알고리즘으로 다익스트라(Dijkstra)를 사용함.
 * - 거리(DISTANCE) 또는 시간(DURATION)을 기준으로 최적 경로를 탐색할 수 있음.
 * - 허브 간 연결 정보(p_hub_connections 테이블)를 기반으로 그래프를 구성하여 탐색.
 */
@Component
public class HubRoutePathFinder {

    private record NodeDistance(HubId hubId, double cost) {}

    public List<HubConnection> findShortestPath(
        HubId start,
        HubId target,
        Map<HubId, List<HubConnection>> graph,
        RouteMetric metric
    ) {
        // 각 허브까지의 최단 거리(또는 시간)를 저장하는 맵
        Map<HubId, Double> dist = new HashMap<>();

        // 경로 추적을 위해 "이전 허브와의 연결 간선"을 저장하는 맵
        Map<HubId, HubConnection> prevEdge = new HashMap<>();

        // 다익스트라 우선순위 큐 (비용이 낮은 순으로 탐색)
        PriorityQueue<NodeDistance> pq =
            new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.cost));

        // 1️⃣ 초기 거리 설정: 모든 허브까지의 거리를 무한대(INF)로 초기화
        for (HubId hub : graph.keySet()) {
            dist.put(hub, Double.POSITIVE_INFINITY);
        }

        // 출발 허브의 거리는 0으로 설정
        dist.put(start, 0.0);
        pq.add(new NodeDistance(start, 0.0));

        // 2️⃣ 다익스트라 탐색 시작
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();  // 현재 가장 짧은 거리의 허브 선택
            HubId currentHub = current.hubId;

            // 이미 더 짧은 거리로 방문한 경우 건너뜀
            if (current.cost > dist.get(currentHub)) continue;

            // 도착 허브에 도달한 경우 조기 종료
            if (currentHub.equals(target)) break;

            // 현재 허브와 연결된 모든 허브(간선) 탐색
            for (HubConnection edge : graph.getOrDefault(currentHub, List.of())) {
                HubId next = edge.getArrivalHubId();

                // ✅ metric 기준에 따라 거리 또는 시간을 가중치로 사용
                double weight = (metric == RouteMetric.DISTANCE)
                    ? edge.getDistanceKm().getDistance()
                    : edge.getEstimatedMinutes().getDuration();

                // 현재까지의 거리 + 새로 이동할 거리
                double alt = dist.get(currentHub) + weight;

                // 더 짧은 경로를 찾은 경우 갱신
                if (alt < dist.getOrDefault(next, Double.POSITIVE_INFINITY)) {
                    dist.put(next, alt);       // 최단 거리 갱신
                    prevEdge.put(next, edge);  // 경로 추적 정보 저장
                    pq.add(new NodeDistance(next, alt)); // 다음 탐색 후보 추가
                }
            }
        }

        // 3️⃣ 도착 허브에 도달하지 못한 경우 (경로 없음)
        if (!prevEdge.containsKey(target)) {
            return List.of();
        }

        // 4️⃣ 경로 역추적 (도착지 → 출발지 방향으로 따라가며 연결된 간선을 수집)
        List<HubConnection> path = new ArrayList<>();
        HubId curr = target;

        while (!curr.equals(start)) {
            HubConnection edge = prevEdge.get(curr);
            if (edge == null) return List.of(); // 끊긴 경로
            path.add(edge);
            curr = edge.getDepartureHubId();    // 이전 허브로 이동
        }

        // 경로는 도착지부터 거꾸로 수집되므로 순서를 뒤집음
        Collections.reverse(path);

        // 5️⃣ 완성된 최단 경로 반환
        return path;
    }
}
