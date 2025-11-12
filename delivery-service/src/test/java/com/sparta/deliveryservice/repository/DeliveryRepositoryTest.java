package com.sparta.deliveryservice.repository;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [TDD] JPA 리포지토리 (통합 테스트)
 * @DataJpaTest: JPA 관련 컴포넌트(Repository, EntityManager)만 로드하고
 * H2 인메모리 DB를 자동으로 설정하여 실제 SQL 동작을 테스트합니다.
 */
@DataJpaTest
@ActiveProfiles("test")
public class DeliveryRepositoryTest {

    @Autowired
    private DeliveryRepository deliveryRepository; // 테스트 대상 Repository

    @Autowired
    private TestEntityManager entityManager; // 테스트용 DB 조작 도구

    private Delivery delivery1;
    private Delivery delivery2;
    private UUID delivery1Id;

    /**
     * TDD [Given]: 각 테스트 실행 전, H2 DB에 2개의 데이터를 미리 저장
     */
    @BeforeEach
    void setUp() {
        // 1. 테스트 데이터 2개 생성
        delivery1 = Delivery.builder()
                .orderId(UUID.randomUUID()).status(DeliveryStatus.COMPLETED)
                .estimatedArrivalTime(LocalDateTime.now())
                .recipientName("김삭제")
                .destinationAddress("서울 강남구") // 필수값
                .recipientSlackId("ll") // 필수값
                .originHubId(UUID.randomUUID()).destinationHubId(UUID.randomUUID())
                .build();

        delivery2 = Delivery.builder()
                .orderId(UUID.randomUUID()).status(DeliveryStatus.HUB_TO_HUB)
                .estimatedArrivalTime(LocalDateTime.now())
                .recipientName("이조회")
                .destinationAddress("서울 강남구") // 필수값
                .recipientSlackId("ll") // 필수값
                .originHubId(UUID.randomUUID()).destinationHubId(UUID.randomUUID())
                .build();

        // 2. DB에 '실제' 저장 (H2)
        deliveryRepository.saveAll(List.of(delivery1, delivery2));

        // 3. 테스트에서 사용할 ID 저장
        delivery1Id = delivery1.getDeliveryId();

        // 영속성 컨텍스트 초기화 - 다음 조회가 캐시가 아닌 DB에서 오도록 보장
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("[RED] @SQLDelete 검증: deliveryRepository.delete() 호출 시, 데이터가 H2에서 삭제(DELETE)되지 않고 UPDATE(deleted_at)되어야 한다")
    void delete_ShouldExecuteUpdate_NotDelete() {

        // -- Given (준비) --
        // setUp에서 delivery1, delivery2가 저장됨
        // DB에는 2건이 존재
        assertEquals(2, deliveryRepository.count(), "테스트 시작 시 2건이 존재해야 합니다.");

        // -- When (실행) --
        // 1. 'delivery1'을 삭제(논리 삭제)하도록 요청
        deliveryRepository.delete(delivery1);

        // 2. 영속성 컨텍스트 비우기
        entityManager.flush();
        entityManager.clear();

        // -- Then (검증) --
        // 3. [핵심] @SQLDelete가 실행되어 'deleted_at'이 UPDATE 되었으므로,
        //    'count()' 쿼리(JPA가 실행)는 @Where(deleted_at=null)의 영향을 받습니다.
        //    따라서 2건이 아닌 1건('delivery2')만 조회되어야 합니다.
        assertEquals(1, deliveryRepository.count(), "@SQLDelete와 @Where가 적용되어 1건만 조회되어야 합니다.");

        // DB에서 직접 조회 (SLQ딜리트가 진짜 실행되었는가?) -> NativeQuery는 Where 어노테이션이 들지 않아, 직접 DB에서 조회하여 SQLDelete가 적용되었는지 알 수 있음.
        Object deletedAt = entityManager.getEntityManager().createNativeQuery(
                "SELECT deleted_at FROM p_deliveries WHERE delivery_id = ?1")
                .setParameter(1, delivery1Id)
                .getSingleResult();
        assertNotNull(deletedAt, "SQLDelete가 실행됐다면 deleted_at이 null이 아니어야 합니다.");

        // 4. [검증] 'delivery2'는 여전히 조회되어야 함
        assertTrue(deliveryRepository.findById(delivery2.getDeliveryId()).isPresent());

        // 5. [RED] 테스트 실패 지점
        //    만약 @Where가 작동하지 않는다면, findById(delivery1Id)는 'delivery1'을 반환할 것입니다.
        //    하지만 @Where가 올바르게 작동한다면, 'delivery1'은 조회되면 안 됩니다.
        Optional<Delivery> deletedResult = deliveryRepository.findById(delivery1Id);

        assertFalse(deletedResult.isPresent(), "@Where(deleted_at=null)에 의해 삭제된 데이터는 findById로 조회되면 안 됩니다.");

    }
}
