package com.sparta.deliveryservice.repository.specification;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.DeliveryRouteHistory;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import com.sparta.deliveryservice.domain.enums.RouteStatus;
import com.sparta.deliveryservice.dto.request.DeliverySearchCriteria;
import com.sparta.deliveryservice.repository.DeliveryRepository;
import com.sparta.deliveryservice.repository.DeliveryRouteHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * [TDD] Specification (동적 쿼리 테스트)
 * @DataJpaTest: JPA 관련 Bean(Repository, EntityManager)만 로드하고
 * H2 (In-memory DB)를 사용하여 실제 SQL 쿼리를 테스트합니다.
 */
@DataJpaTest
@ActiveProfiles("test")
public class DeliverySpecificationTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryRouteHistoryRepository routeHistoryRepository; // 경로 저장을 위해

    private UUID testDriverId1;
    private UUID testDriverId2;

    private Delivery delivery1_Driver1; // companyDriverId = testDriverId1
    private Delivery delivery2_Driver1; // routeHistories.driverId = testDriverId1
    private Delivery delivery3_Driver2; // companyDriverId = testDriverId2

    /**
     * 테스트 실행 전, H2 DB에 3개의 배송 데이터를 미리 생성
     */
    @BeforeEach
    void setUp() {
        testDriverId1 = UUID.randomUUID();
        testDriverId2 = UUID.randomUUID();

        // [데이터 1] 최종 담당자가 testDriverId1인 배송
        // [REFACTOR] 수령인: "김배송"
        delivery1_Driver1 = Delivery.builder()
                .orderId(UUID.randomUUID()).status(DeliveryStatus.COMPLETED)
                .estimatedArrivalTime(LocalDateTime.now())
                .destinationAddress("서울 강남구") // 필수값
                .recipientName("김배송") // 필수값
                .recipientSlackId("ll") // 필수값
                .companyDriverId(testDriverId1) // 1번 드라이버
                .originHubId(UUID.randomUUID()).destinationHubId(UUID.randomUUID())
                .build();

        // [데이터 2] 허브 담당자가 testDriverId1인 배송
        // [REFACTOR] 수령인: "이수령"
        delivery2_Driver1 = Delivery.builder()
                .orderId(UUID.randomUUID()).status(DeliveryStatus.HUB_TO_HUB)
                .estimatedArrivalTime(LocalDateTime.now())
                .destinationAddress("서울 강남구") // 필수값
                .recipientName("이수령") // 필수값
                .recipientSlackId("ll") // 필수값
                .originHubId(UUID.randomUUID()).destinationHubId(UUID.randomUUID())
                .build();
        DeliveryRouteHistory route = DeliveryRouteHistory.builder()
                .sequence(1).status(RouteStatus.IN_TRANSIT)
                .driverId(testDriverId1) // 1번 드라이버
                .originHubId(UUID.randomUUID()).destinationHubId(UUID.randomUUID())
                .build();
        delivery2_Driver1.addRouteHistory(route);
        // 참고: delivery2_Driver1을 저장하면 route도 Cascade로 함께 저장됨

        // [데이터 3] 2번 드라이버가 담당하는 배송
        // [REFACTOR] 수령인: "김운반"
        delivery3_Driver2 = Delivery.builder()
                .orderId(UUID.randomUUID()).status(DeliveryStatus.COMPLETED)
                .estimatedArrivalTime(LocalDateTime.now())
                .destinationAddress("서울 강남구") // 필수값
                .recipientName("김운반") // 필수값
                .recipientSlackId("ll") // 필수값
                .originHubId(UUID.randomUUID()) // 필수값
                .destinationHubId(UUID.randomUUID()) // 필수값
                .companyDriverId(testDriverId2) // 2번 드라이버
                .build();

        // DB 에 저장
        deliveryRepository.saveAll(List.of(delivery1_Driver1, delivery2_Driver1, delivery3_Driver2));
    }

    @Test
    @DisplayName("[RED] driverId로 검색 시, companyDriverId 또는 route.driverId가 일치하는 모든 배송이 조회되어야 한다")
    void build_WithDriverIdCriteria_ShouldReturnMatchingDeliveries() {

        // --- Given (준비) ---
        // 1. 'testDriverId1'로 검색하는 조건 생성
        DeliverySearchCriteria criteria = new DeliverySearchCriteria();
        criteria.setDriverId(testDriverId1);

        // 2. [TDD] Specification 빌드
        // 아직 build 메서드는 driverId 로직이 없음
        Specification<Delivery> spec = DeliverySpecification.build(criteria);

        // --- When (실행) ---
        // 3. H2 DB에 'spec'을 사용하여 실제 쿼리 실행
        List<Delivery> results = deliveryRepository.findAll(spec);
        results.sort((a,b) -> a.getEstimatedArrivalTime().compareTo(b.getEstimatedArrivalTime()));

        // --- Then (검증) ---
        // 4. [RED] 테스트 실패 지점
        // 'build' 메서드가 'driverId'를 무시하고 빈 'spec'을 반환했기 때문에,
        // findAll(spec)은 DB의 모든 데이터(3개)를 반환할 것입니다.
        // 하지만 우리는 'testDriverId1'과 일치하는 2개만 기대합니다.
        assertEquals(2, results.size(), "testDriverId1이 담당하는 2건(최종배송1, 허브배송1)만 조회되어야 합니다.");
        // 결과가 3이므로 테스트는 여기서 실패

        assertEquals(testDriverId1, results.get(0).getCompanyDriverId());
    }

    @Test
    @DisplayName("[RED] recipientName(수령인)으로 'LIKE' 검색 시, '김'으로 시작하는 2건이 조회되어야 한다")
    void build_WithRecipientNameCriteria_ShouldReturnMatchingDeliveries() {

        // -- Given (준비) --
        // 1. "김"으로 검색하는 조건 생성
        DeliverySearchCriteria criteria = new DeliverySearchCriteria();
        criteria.setRecipientName("김"); // "김%" 검색을 의도

        // 2. [TDD] Specification 빌드
        // 아직 'build' 메서드는 'recipientName' 로직이 없음
        Specification<Delivery> spec = DeliverySpecification.build(criteria);

        // -- When (실행) --
        // 3. H2 DB에 'spec'을 사용하여 실제 쿼리 실행
        List<Delivery> results = deliveryRepository.findAll(spec);

        // -- Then (검증) --
        // 4. [RED] 테스트 실패 지점
        //    'build' 메서드가 'recipientName'을 무시하고 빈 'spec'을 반환했기 때문에,
        //    findAll(spec)은 DB의 모든 데이터(3개)를 반환할 것입니다.
        //     하지만 우리는 "김"으로 시작하는 2개만 기대합니다.
        assertEquals(2, results.size(), "수령인 이름이 '김'으로 시작하는 2건(김배송, 김운반)만 조회되어야 합니다.");
        // 결과가 3이므로 테스트는 여기서 실패

        assertTrue(results.stream().allMatch(d -> d.getRecipientName().startsWith("김")));
    }
}
