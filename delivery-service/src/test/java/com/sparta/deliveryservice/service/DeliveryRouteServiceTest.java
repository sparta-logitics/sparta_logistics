package com.sparta.deliveryservice.service;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.DeliveryRouteHistory;
import com.sparta.deliveryservice.domain.enums.RouteStatus;
import com.sparta.deliveryservice.exception.EntityNotFoundException;
import com.sparta.deliveryservice.repository.DeliveryRouteHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryRouteServiceTest {

    @InjectMocks
    private DeliveryRouteService deliveryRouteService;

    @Mock
    private DeliveryRouteHistoryRepository routeHistoryRepository;

    @Mock
    private Delivery delivery;

    @Test
    @DisplayName("RED Flow 2-1: 담당자 배정 성공 시, driverId가 업데이트되고 상태는 유지되어야 한다.")
    void assignDriver_SuccessScenario_ShouldUpdateDriverIdAndMaintainStatus() {

        // given 준비
        // 1. 배정할 담당자 id와 경로 Id
        UUID routeHistoryId = UUID.randomUUID();
        UUID newDriverId = UUID.randomUUID();

        // 2. db에서 찾을 가짜 경로 데이터 (아직 driverId가 null임)
        DeliveryRouteHistory fakeRoute = DeliveryRouteHistory.builder()
                .sequence(1)
                .status(RouteStatus.WAITING_FOR_TRANSIT) // 이동 대기중 상태
                .build();
        // 테스트 편의를 위해 @Builder 사용. 실제 엔티티에 추가 필요

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(routeHistoryRepository.findById(routeHistoryId))
                .thenReturn(Optional.of(fakeRoute));

        // 4. 저장될 객체를 캡처할 Captor 준비
        ArgumentCaptor<DeliveryRouteHistory> routeCaptor = ArgumentCaptor.forClass(DeliveryRouteHistory.class);

        // When 실행
        // 5. assignDriver 메서드 실행 (아직 실제 구현 코드가 없음)
        deliveryRouteService.assignDriver(routeHistoryId, newDriverId);

        // then 검증
        // 6. findById 가 1번 호출되었는지 검증
        verify(routeHistoryRepository, times(1)).findById(routeHistoryId);

        // 7. save가 1번 호출되었는지를 검증 (명시적 저장을 권장)
        verify(routeHistoryRepository, times(1)).save(routeCaptor.capture());

        // 8. 저장된 객체의 필드 검증
        DeliveryRouteHistory savedRoute = routeCaptor.getValue();

        assertNotNull(savedRoute);
        assertEquals(newDriverId, savedRoute.getDriverId(), "새로운 담당자 ID가 저장되어야 합니다.");
        assertEquals(RouteStatus.WAITING_FOR_TRANSIT, savedRoute.getStatus(), "상태는 'WAITING_FOR_TRANSIT'로 유지되어야 합니다.");
    }

    @Test
    @DisplayName("RED/GREEN Flow 2-1: 존재하지 않는 routeHistoryId로 담당자 배정 요청 시, IllegalArgumentException이 발생해야 한다")
    void assignDriver_FailsWhen_RouteIdNotFound() {
        // given 준비
        // 1. 존재하지 않는 ID 준비
        UUID nonExistentRouteId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        // 2. 핵심 repository가 빈 Potional을 반환하도록 설정
        when(routeHistoryRepository.findById(nonExistentRouteId))
                .thenReturn(Optional.empty());

        // when 실행 & then 검증
        // 3. assignDriver 메서드 실행 시 IllegalArgumentException이 발생하는지 검증
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            deliveryRouteService.assignDriver(nonExistentRouteId, driverId);
        });

        // 4. 보너스, 예외 메시지가 정확한지 검증
        String expectedMessage = String.format("배송 경로을(를) 찾을 수 없습니다. (ID: %s)", nonExistentRouteId.toString());
        assertEquals(expectedMessage, exception.getMessage());

        // 5. 중요. 예외가 발생했으므로, 'save'는 절대 호출되지 않았어야 함
        verify(routeHistoryRepository, never()).save(any(DeliveryRouteHistory.class));
    }

    // -----------------------------------------------------------------
    // [TDD] Flow 2-2: startRoute (배송 시작)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("RED Flow 2-2 성공: 담당자가 배정된 경로를 시작하면, 상태가 IN_TRANSIT로 변경되어야 한다")
    void startRoute_SuccessScenario_ShouldChangeStatusToInTransit() {

        // Given 준비
        // 1. 테스트할 경로 ID
        UUID routeHistoryId = UUID.randomUUID();
        UUID assignedDriverId = UUID.randomUUID();

        // 2. 핵심. DB에서 찾을 가짜 경로 데이터
        // 담당자가 배정되었고(not null), 상태가 대기중임
        DeliveryRouteHistory fakeRoute = DeliveryRouteHistory.builder()
                .driverId(assignedDriverId)
                .status(RouteStatus.WAITING_FOR_TRANSIT)
                .delivery(delivery)
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(routeHistoryRepository.findById(routeHistoryId))
                .thenReturn(Optional.of(fakeRoute));

        // 4. 저장될 객체를 캡처할 Captor 준비
        ArgumentCaptor<DeliveryRouteHistory> routeCaptor = ArgumentCaptor.forClass(DeliveryRouteHistory.class);

        // When 실행
        // 5. startRoute 메서드를 실행 (아직 존재하지 않거나, 내용이 비어있음)
        deliveryRouteService.startRoute(routeHistoryId);

        // Then 검증
        // 6. save가 1번 호출되었는지 검증
        verify(routeHistoryRepository, times(1)).save(routeCaptor.capture());

        // 7. 저장된 객체의 필드 검증
        DeliveryRouteHistory savedRoute = routeCaptor.getValue();

        assertEquals(RouteStatus.IN_TRANSIT, savedRoute.getStatus(), "상태가 'IN_TRANSIT'(이동중)로 변경되어야 합니다.");
        assertEquals(assignedDriverId, savedRoute.getDriverId(), "담당자 ID는 변경되지 않아야 합니다.");
    }

    @Test
    @DisplayName("[RED] Flow 2-2 실패: 담당자가 배정되지않은null 경로를 시작하려 하면, IllegalStateException이 발생해야 한다")
    void startRoute_FailsWhen_DriverNotAssigned() {
        // Given 준비
        // 1. 테스트할 경로 ID
        UUID routeHistoryId = UUID.randomUUID();

        // 2. 핵심. DB에서 찾을 가짜 경로 데이터
        // 담당자가 배정되지 않았음(null)
        DeliveryRouteHistory fakeRoute = DeliveryRouteHistory.builder()
                .driverId(null)
                .status(RouteStatus.WAITING_FOR_TRANSIT)
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(routeHistoryRepository.findById(routeHistoryId))
                .thenReturn(Optional.of(fakeRoute));

        // when 실행 & then 검증
        // 4. 'startRoute' 메서드 실행 시 'IllegalStateException'이 발생하는지 검증
        // 잘못된 상태에서 로직을 실행하려 했기 때문
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            deliveryRouteService.startRoute(routeHistoryId);
        });

        // 5. 예외 메시지 검증
        assertEquals("담당자가 배정되지 않은 경로는 시작할 수 없습니다.", exception.getMessage());

        // 6. 중요. 예외가 발생했으므로 save는 절대 호출되지 않았어야 함
        verify(routeHistoryRepository, never()).save(any(DeliveryRouteHistory.class));
    }

    // -----------------------------------------------------------------
    // [TDD] Flow 2-3: completeRoute (허브 도착)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("[RED] Flow 2-3 (성공): '이동 중'인 경로를 '완료'하면, 상태가 ARRIVED_AT_HUB로 변경되고 실제 기록이 저장된다")
    void completeRoute_SuccessScenario_ShouldChangeStatusToArrivedAndSaveActuals() {

        // --- Given (준비) ---
        // 1. 테스트할 경로 ID와 기록할 실제 데이터
        UUID routeHistoryId = UUID.randomUUID();
        Double actualDistance = 150.5;
        Integer actualDuration = 180;

        // 2. [핵심] DB에서 찾을 '가짜'경로 데이터
        // 상태가 IN_TRANSIT (이동중)임
        DeliveryRouteHistory fakeRoute = DeliveryRouteHistory.builder()
                .driverId(UUID.randomUUID()) // 이미 배정됨
                .status(RouteStatus.IN_TRANSIT) // 상태가 이동중
                .delivery(delivery)
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(routeHistoryRepository.findById(routeHistoryId))
                .thenReturn(Optional.of(fakeRoute));

        // 4. 저장될 객체를 캡처할 Captor 준비
        ArgumentCaptor<DeliveryRouteHistory> routeCaptor = ArgumentCaptor.forClass(DeliveryRouteHistory.class);

        // --- When 실행 ---
        // 5. 'completeRoute' 메서드 실행 (아직 존재하지 않거나, 내용이 비어있음)
        deliveryRouteService.completeRoute(routeHistoryId, actualDistance, actualDuration);

        // --- Then 검증 ---
        // 6. 'save'가 1번 호출되었는지 검증
        verify(routeHistoryRepository, times(1)).save(routeCaptor.capture());

        // 7. 저장된 객체의 필드 검증
        DeliveryRouteHistory savedRoute = routeCaptor.getValue();

        assertEquals(RouteStatus.ARRIVED_AT_HUB, savedRoute.getStatus(), "상태가 'ARRIVED_AT_HUB'(허브 도착)로 변경되어야 합니다.");
        assertEquals(actualDistance, savedRoute.getActualDistance(), "실제 거리가 저장되어야 합니다.");
        assertEquals(actualDuration, savedRoute.getActualDuration(), "실제 시간이 저장되어야 합니다.");
    }

    @Test
    @DisplayName("[RED] Flow 2-3 (실패): '이동 대기중'인 경로를 '완료'하려 하면, IllegalStateException이 발생해야 한다")
    void completeRoute_FailsWhen_StatusIsNotInTransit(){

        // Given 준비
        // 1. 테스트할 경로 ID
        UUID routeHistoryId = UUID.randomUUID();

        // 2. [핵심] DB에서 찾을 '가짜' 경로 데이터
        // 상태가 'WAITING_FOR_TRANSIT' (대기중)임
        DeliveryRouteHistory fakeRoute = DeliveryRouteHistory.builder()
                .driverId(UUID.randomUUID())
                .status(RouteStatus.WAITING_FOR_TRANSIT)
                .delivery(delivery)
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(routeHistoryRepository.findById(routeHistoryId))
                .thenReturn(Optional.of(fakeRoute));

        // --- When 실행 & Then 검증 ---
        // 4. 'completeRoute' 메서드 실행 시 'IllegalStateException'이 발생하는지 검증
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            deliveryRouteService.completeRoute(routeHistoryId, 150.5, 180);
        });

        // 5. 예외 메시지 검증
        assertEquals("현재 '이동 중(IN_TRANSIT)' 상태인 경로만 완료할 수 있습니다.", exception.getMessage());

        // 6. [중요] 예외가 발생했으므로, 'save'는 절대 호출되지 않았어야 함
        verify(routeHistoryRepository, never()).save(any(DeliveryRouteHistory.class));
    }

}

















