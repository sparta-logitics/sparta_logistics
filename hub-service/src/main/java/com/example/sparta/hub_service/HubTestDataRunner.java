package com.example.sparta.hub_service;

import com.example.sparta.hub_service.core.domain.Hub;
import com.example.sparta.hub_service.core.domain.HubConnection;
import com.example.sparta.hub_service.core.enums.HubCode;
import com.example.sparta.hub_service.core.vo.Distance;
import com.example.sparta.hub_service.core.vo.Duration;
import com.example.sparta.hub_service.core.vo.HubAddress;
import com.example.sparta.hub_service.core.vo.HubId;
import com.example.sparta.hub_service.core.vo.Location;
import com.example.sparta.hub_service.hub_routes.HubConnectionRepository;
import com.example.sparta.hub_service.hubs.HubRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubTestDataRunner implements ApplicationRunner {

    private final HubRepository hubRepository;
    private final HubConnectionRepository hubConnectionRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hubRepository.count() > 0) {
            return;
        }

        List<Hub> hubs = createHubs();
        hubRepository.saveAll(hubs);

        List<HubConnection> connections = createAllConnections(hubs);
        hubConnectionRepository.saveAll(connections);
    }

    /**
     * 허브 기본 데이터 등록
     */
    private List<Hub> createHubs() {
        return List.of(
            Hub.create(HubCode.SEOUL, "서울특별시 센터",
                HubAddress.of("서울특별시 송파구 송파대로 55"),
                Location.of(37.514575, 127.105399)),
            Hub.create(HubCode.GYEONGGI_NORTH, "경기 북부 센터",
                HubAddress.of("경기도 고양시 덕양구 권율대로 570"),
                Location.of(37.642349, 126.832020)),
            Hub.create(HubCode.GYEONGGI_SOUTH, "경기 남부 센터",
                HubAddress.of("경기도 이천시 덕평로 257-21"),
                Location.of(37.232060, 127.458671)),
            Hub.create(HubCode.BUSAN, "부산광역시 센터",
                HubAddress.of("부산 동구 중앙대로 206"),
                Location.of(35.115201, 129.041580)),
            Hub.create(HubCode.DAEGU, "대구광역시 센터",
                HubAddress.of("대구 북구 태평로 161"),
                Location.of(35.885099, 128.582720)),
            Hub.create(HubCode.INCHEON, "인천광역시 센터",
                HubAddress.of("인천 남동구 정각로 29"),
                Location.of(37.447344, 126.731604)),
            Hub.create(HubCode.GWANGJU, "광주광역시 센터",
                HubAddress.of("광주 서구 내방로 111"),
                Location.of(35.152122, 126.889018)),
            Hub.create(HubCode.DAEJEON, "대전광역시 센터",
                HubAddress.of("대전 서구 둔산로 100"),
                Location.of(36.350469, 127.384826)),
            Hub.create(HubCode.ULSAN, "울산광역시 센터",
                HubAddress.of("울산 남구 중앙로 201"),
                Location.of(35.541331, 129.335270)),
            Hub.create(HubCode.SEJONG, "세종특별자치시 센터",
                HubAddress.of("세종특별자치시 한누리대로 2130"),
                Location.of(36.480099, 127.289379)),
            Hub.create(HubCode.GANGWON, "강원특별자치도 센터",
                HubAddress.of("강원특별자치도 춘천시 중앙로 1"),
                Location.of(37.881315, 127.730197)),
            Hub.create(HubCode.CHUNGBUK, "충청북도 센터",
                HubAddress.of("충북 청주시 상당구 상당로 82"),
                Location.of(36.636101, 127.488980)),
            Hub.create(HubCode.CHUNGNAM, "충청남도 센터",
                HubAddress.of("충남 홍성군 홍북읍 충남대로 21"),
                Location.of(36.656449, 126.672900)),
            Hub.create(HubCode.JEONBUK, "전북특별자치도 센터",
                HubAddress.of("전북특별자치도 전주시 완산구 효자로 225"),
                Location.of(35.814083, 127.147938)),
            Hub.create(HubCode.JEONNAM, "전라남도 센터",
                HubAddress.of("전남 무안군 삼향읍 오룡길 1"),
                Location.of(34.808585, 126.465100)),
            Hub.create(HubCode.GYEONGBUK, "경상북도 센터",
                HubAddress.of("경북 안동시 풍천면 도청대로 455"),
                Location.of(36.568339, 128.729501)),
            Hub.create(HubCode.GYEONGNAM, "경상남도 센터",
                HubAddress.of("경남 창원시 의창구 중앙대로 300"),
                Location.of(35.227824, 128.681400))
        );
    }

    /**
     * 모든 허브 간 연결 자동 생성 (P2P 완전 연결)
     */
    private List<HubConnection> createAllConnections(List<Hub> hubs) {
        List<HubConnection> connections = new ArrayList<>();

        for (Hub from : hubs) {
            for (Hub to : hubs) {
                if (!from.equals(to)) {
                    double distance = randomDistanceBetween(from, to);
                    int duration = (int) (distance / 1.2); // 단순히 거리 대비 시간 비례 계산
                    connections.add(HubConnection.create(
                        HubId.of(from.getId()),
                        HubId.of(to.getId()),
                        Distance.of(distance),
                        Duration.of(duration)
                    ));
                }
            }
        }
        return connections;
    }

    /**
     * 단순 거리 계산 (Haversine 근사치)
     */
    private double randomDistanceBetween(Hub from, Hub to) {
        double R = 6371; // 지구 반경 (km)
        double lat1 = from.getHubLocation().getLatitude().doubleValue();
        double lon1 = from.getHubLocation().getLongitude().doubleValue();
        double lat2 = to.getHubLocation().getLatitude().doubleValue();
        double lon2 = to.getHubLocation().getLongitude().doubleValue();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        // 거리 오차 보정 (실제 경로 대비 +10~15%)
        return Math.round(distance * (1.1 + random.nextDouble() * 0.05) * 10) / 10.0;
    }
}
