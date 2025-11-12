package com.sparta.deliveryservice.service;

import com.sparta.deliveryservice.client.AiServiceClient;
import com.sparta.deliveryservice.client.HubRouteServiceClient;
import com.sparta.deliveryservice.client.dto.EtaPredictRequest;
import com.sparta.deliveryservice.client.dto.EtaPredictResponse;
import com.sparta.deliveryservice.client.dto.RouteInfoResponse;
import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.DeliveryRouteHistory;
import com.sparta.deliveryservice.domain.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import com.sparta.deliveryservice.domain.enums.RouteStatus;
import com.sparta.deliveryservice.dto.request.DeliverySearchCriteria;
import com.sparta.deliveryservice.dto.response.DeliveryDetailResponse;
import com.sparta.deliveryservice.dto.response.DeliverySummaryResponse;
import com.sparta.deliveryservice.exception.EntityNotFoundException;
import com.sparta.deliveryservice.producer.RabbitMQProducer;
import com.sparta.deliveryservice.producer.dto.DeliveryCompletedEvent;
import com.sparta.deliveryservice.repository.DeliveryRepository;
import com.sparta.deliveryservice.repository.DeliveryRouteHistoryRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * [TDD] E. 배송/물류 서비스
 * 테스트 대상 : DeliveryService
 * @ExtendWith: Mockito (가짜 객체) 사용 설정
 */
@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    // @InjdectMocks: 테스트 대상 클래스. @Mock 객체들이 이 클래스에 주입된디ㅏ.
    @InjectMocks
    private DeliveryService deliveryService; // 아직 존재하지 않음

    // Mock
    // 실제 DB나 외부 API가 아닌, 가짜 객체를 만듭니다.

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryRouteHistoryRepository deliveryRouteHistoryRepository; // 아직 존재하지 않음

    @Mock
    private HubRouteServiceClient hubRouteServiceClient; // 아직 존재하지 않음

    @Mock
    private AiServiceClient aiServiceClient; // 아직 존재하지 않음

//    @Mock
//    private DeliveryEventProducer deliveryEventProducer; // 이벤트 발행기 Mock
    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @Test
    @DisplayName("[RED] Flow 1: 배송 생성 시 'AI 서비스'가 실패하면, DB 저장은 절대 일어나지 않아야 한다 (롤백)")
    void createDelivery_FailsWhen_AiServiceThrowsExceptions() {

        // --- Given (준비) ---
        // 1. D. 주문 서비스로부터 받은 요청 데이터 (가상)
        UUID originHubId = UUID.randomUUID();
        UUID destinationHubId = UUID.randomUUID();

        DeliveryCreateRequest request = new DeliveryCreateRequest(
                UUID.randomUUID(), "서울시 강남구", "김수령", "U123ABC",
                originHubId, destinationHubId, Collections.emptyList());

        // 2. 'B. 허브/경로 서비스'는 성공적으로 응답한다고 가정
        // 👇 [수정] '...' 부분을 실제 (가짜) 객체로 채워 넣습니다.
        RouteInfoResponse fakeRoute = new RouteInfoResponse(
                originHubId,
                destinationHubId
        );

        when(hubRouteServiceClient.getRoutes(any(UUID.class), any(UUID.class)))
                .thenReturn(Collections.singletonList(fakeRoute)); // 👈 수정됨

        // 3. [핵심] 'F. AI 서비스'는 FeignException을 던진다고 가정
        when(aiServiceClient.calculateEta(any()))
                .thenThrow(FeignException.InternalServerError.class);
        // When 실행
        // 4. 'createDlivery' 메서드 실행 시, FeignException이 발생할 것을 기대함
        assertThrows(FeignException.class, () -> {
            deliveryService.createDelivery(request);
        });

        // Then 검증
        // 5. [가장 중요] AI 서비스 호출이 실패했으므로, DB 트랜잭션이 롤백되어
        // 'save'나 'saveAll'이 절대 호출되지 않았는지 검증
        verify(deliveryRepository, never()).save(any(Delivery.class));
        verify(deliveryRouteHistoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("[RED] Flow 1: 배송 생성 시 모든 서비스가 성공하면, DB에 배송(Delivery)과 경로(Routes)가 저장되어야 한다")
    void createDelivery_SuccessScenario_ShouldSaveDeliveryAndHistories() {
        // Given (준비)
        // 1. D. 주문 서비스로부터 받은 요청 데이터
        UUID orderId = UUID.randomUUID();
        UUID originHubId = UUID.randomUUID();
        UUID destHubId = UUID.randomUUID();
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                orderId , "서울시 강남구", "김수령", "U123ABC",
                originHubId, destHubId, Collections.emptyList());

        // 2. 'B. 허브/경로 서비스'는 성공하고, (가짜) 경로 2개를 반환하도록 설정
        RouteInfoResponse fakeRoute1 = new RouteInfoResponse(originHubId, UUID.randomUUID(), 100.0, 120);
        RouteInfoResponse fakeRoute2 = new RouteInfoResponse(fakeRoute1.getDestinationHubId(), destHubId, 50.0, 60);
        List<RouteInfoResponse> fakeRoutes = Arrays.asList(fakeRoute1, fakeRoute2);

        when(hubRouteServiceClient.getRoutes(any(UUID.class), any(UUID.class)))
                .thenReturn(fakeRoutes);

        // 3, 'F. AI 서비스'도 성공하고, (가짜) 예쌍 도착 시간을 반환하도록 설정
        LocalDateTime fakeEta = LocalDateTime.now().plusDays(2); // 가짜 예상 시간 : 2일 뒤
        EtaPredictResponse fakeEtaResponse = new EtaPredictResponse(fakeEta);

        when(aiServiceClient.calculateEta(any(EtaPredictRequest.class)))
                .thenReturn(fakeEtaResponse);

        // 4. [핵심] DB 저장을 캡처할 ArgumentCaptor 준비
        // "deliveryRepository.save()"가 호출될 때, 어떤 'Delivery' 객체가 전달되었는지 붙잡기 위함
        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);

        // When (실행)
        // 5. 'createDelivery' 메서드 실행 (아직 실제 구현 코드가 없음)
        deliveryService.createDelivery(request);

        // Then 검증
        // 6. deliveryRepository.save()가 '정확히 1번' 호출되었는지 검증
        verify(deliveryRepository, times(1)).save(deliveryCaptor.capture());

        // 7. CascadeType.ALL 이므로 routeHistoryRepository.saveAll()은 호출되지 않아야 함
        verify(deliveryRouteHistoryRepository, never()).saveAll(any());

        // 8. DB에 저장된 'Delivery' 객체를 캡처해서 검증
        Delivery savedDelivery = deliveryCaptor.getValue();

        assertEquals(orderId, savedDelivery.getOrderId(), "주문 ID가 일치해야 합니다.");
        assertEquals(fakeEta, savedDelivery.getEstimatedArrivalTime(), "AI가 예측한 시간이 저장되어야 합니다.");
        assertEquals(DeliveryStatus.WAITING_AT_HUB, savedDelivery.getStatus(), "초기 상태는 '허브 대기중'이어야 합니다.");

        // 9. 'Delivery' 객체 안에 'DeliveryRouteHistory' 자식 객체들이 생성되었는지 검증
        List<DeliveryRouteHistory> savedRoutes = savedDelivery.getRouteHistories();
        assertNotNull(savedRoutes);
        assertEquals(2, savedRoutes.size(), "B. 허브 서비스가 반환한 경로 수(2개)만큼 생성되어야 합니다.");

        // 10. 첫 번째 경로의 세부 정보 검증
        DeliveryRouteHistory savedRoute1 = savedRoutes.get(0);
        assertEquals(1, savedRoute1.getSequence(), "첫 번째 경로의 순번(sequence)은 1이어야 합니다.");
        assertEquals(fakeRoute1.getOriginHubId(), savedRoute1.getOriginHubId(), "첫 번째 경로의 출발지가 일치해야 합니다.");
        assertEquals(RouteStatus.WAITING_FOR_TRANSIT, savedRoute1.getStatus(), "경로의 초기 상태는 '허브 이동 대기중'이어야 합니다.");

    }

    // -----------------------------------------------------------------
    // [TDD] Flow 3-1: assignCompanyDriver (최종 담당자 배정)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("[RED] Flow 3-1 (성공): 모든 허브 경로가 '도착' 상태일 때, 최종 담당자를 배정하면 성공한다")
    void assignCompanyDriver_SuccessScenario_ShouldAssignDriver() {

        // given 준비
        // 1. 테스트할 ID
        UUID deliveryId = UUID.randomUUID();
        UUID companyDriverId = UUID.randomUUID();

        // 2, [핵심] '가짜' 경로 데이터 (모두 'ARRIVED_AT_HUB' 상태)
        DeliveryRouteHistory route1 = DeliveryRouteHistory.builder()
                .status(RouteStatus.ARRIVED_AT_HUB)
                .build();
        DeliveryRouteHistory route2 = DeliveryRouteHistory.builder()
                .status(RouteStatus.ARRIVED_AT_HUB)
                .build();

        // 3. '가짜' 배송(Delivery) 데이터 (위의 경로 2개를 포함)
        Delivery fakeDelivery = Delivery.builder()
                .status(DeliveryStatus.ARRIVED_AT_DEST_HUB) // 최종 허브 도착 상태
                .build();
        // 실제 엔티티가 아닌 테스트 객체이므로 setter나 list.add로 주입
        fakeDelivery.getRouteHistories().add(route1);
        fakeDelivery.getRouteHistories().add(route2);

        // 4. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeDelivery));

        // 5. 저장될 객체를 캡처할 Captor 준비
        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);

        // When 실행
        // 6. 'assignCompanyDriver' 메서드 실행 (아직 존재하지 않음)
        deliveryService.assignCompanyDriver(deliveryId, companyDriverId);

        // Then 검증
        // 7. save가 1번 호출되었는지 검증
        verify(deliveryRepository, times(1)).save(deliveryCaptor.capture());

        // 8. 저장된 객체의 필드 검증
        Delivery savedDelivery = deliveryCaptor.getValue();

        assertEquals(companyDriverId, savedDelivery.getCompanyDriverId(), "최종 담당자 ID가 저장되어야 합니다.");
    }

    @Test
    @DisplayName("[RED] Flow 3-1 (실패): 허브 경로가 '이동 중'일 때 최종 담당자를 배정하려 하면, IllegalStateException이 발생한다")
    void assignCompanyDriver_FailsWhen_RoutesAreNotInTransit() {

        // --- Given 준비 ---
        // 1. 테스트할 ID
        UUID deliveryId = UUID.randomUUID();
        UUID companyDriverId = UUID.randomUUID();

        // 2. [핵심] '가짜' 경로 데이터 (하나가 'IN_TRANSIT' 상태)
        DeliveryRouteHistory route1 = DeliveryRouteHistory.builder()
                .status(RouteStatus.ARRIVED_AT_HUB) // 1번 경로는 도착
                .build();
        DeliveryRouteHistory route2 = DeliveryRouteHistory.builder()
                .status(RouteStatus.IN_TRANSIT) // 2번 경로가 아직 이동중
                .build();

        // 3, '가짜' 배송(Delivery) 데이터
        Delivery fakeDelivery = Delivery.builder()
                .status(DeliveryStatus.HUB_TO_HUB) // 아직 최종 허브 도착 전
                .build();
        fakeDelivery.getRouteHistories().add(route1);
        fakeDelivery.getRouteHistories().add(route2);

        // 4. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeDelivery));

        // --- When (실행) & Then (검증) ---
        // 5. 'assignCompanyDriver' 메서드 실행 시 'IllegalStateException'이 발생하는지 검증
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            deliveryService.assignCompanyDriver(deliveryId, companyDriverId);
        });

        // 6. 예외 메시지 검증
        assertEquals("모든 허브 경로가 도착 완료 상태여야 담당자를 배정할 수 있습니다.", exception.getMessage());

        // 7. [중요] 예외가 발생했으므로, 'save'는 절대 호출되지 않았어야 함
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    // -----------------------------------------------------------------
    // [TDD] Flow 3-2: startCompanyDelivery (최종 배송 시작)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("[RED] Flow 3-2 (성공): 최종 담당자가 배정된 배송을 '시작'하면, 상태가 COMPANY_DELIVERING으로 변경된다")
    void startCompanyDelivery_SuccessScenario_ShouldChangeStatusToCompanyDelivering() {

        // Given (준비)
        // 1. 테스트할 ID
        UUID deliveryId = UUID.randomUUID();

        // 2. [핵심] '가짜' 배송(Delivery) 데이터
        Delivery fakeDelivery = Delivery.builder()
                .status(DeliveryStatus.ARRIVED_AT_DEST_HUB) // 최종 허브 도착 상태
                .build();
        // (companyDriverId는 private이므로 직접 설정 대신, 이전 단계인 assignCompanyDriver가 호출되었다고 가정합니다)
        // 테스트 편의를 위해 setter 대신 assignCompanyDriver를 모킹/호출하거나, Builder에 companyDriverId를 추가해야 할 수도 있습니다.
        // 우선은 'startCompanyDelivery' 로직 내에서 companyDriverId 필드를 직접 사용할 것이므로, 리플렉션이나 @Builder 수정이 필요할 수 있으나,
        // 여기서는 테스트용 '가짜' 객체를 만듭니다.

        // 테스트용 객체 생성을 위해 @Builder에 companyDriverId 필드 추가가 필요함 -> [REFACTOR]
        // 임시로 assign을 먼저 호출하여 상태를 만듭니다.
        UUID companyDriverId = UUID.randomUUID();
        fakeDelivery.assignCompanyDriver(companyDriverId); // 테스트를 위해 상태를 강제 설정, 이 테스트는 assignCompanyDriver의 성공을 전제로 함
        // 참고 : fakeDelivery.assignCompanyDriver()가 routeHistories를 검사하므로,
        // 테스트가 복잡해집니다. 이럴 땐 테스트용 객체를 스텁하는 것이 좋습니다.

        // --- [Given] (더 나은 방법: @Spy 또는 실제 객체 생성) ---
        // (테스트의 고립을 위해, 'start'로직에만 집중하도록
        // @Builder를 수정하여 필요한 상태의 객체를 직접 만듭니다)

        // (Delivery.java의 @Builder에 companyDriverId가 추가되었다고 가정하고
        // 아래와 같이 Given을 다시 작성합니다.)

        Delivery fakeReadyDelivery = Delivery.builder()
                .orderId(UUID.randomUUID())
                .status(DeliveryStatus.ARRIVED_AT_DEST_HUB) // 최종 허브 도착 상태
                .companyDriverId(UUID.randomUUID()) // 최종 담당자 배정됨
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeReadyDelivery));

        // 4. 저장될 객체를 캡처할 Captor 준비
        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);

        // --- When(실행) ---
        // 5. 'startCompanyDelivery' 메서드 실행 (아직 존재하지 않음)
        deliveryService.startCompanyDelivery(deliveryId);

        // --- Then(검증) ---
        // 6. 'save'가 1번 호출되었는지 검증
        verify(deliveryRepository, times(1)).save(deliveryCaptor.capture());

        // 7. 저장된 객체의 필드 검증
        Delivery savedDelivery = deliveryCaptor.getValue();

        assertEquals(DeliveryStatus.COMPANY_DELIVERING, savedDelivery.getStatus(), "상태가 'COMPANY_DELIVERING'(업체 이동중)으로 변경되어야 합니다.");

    }

    @Test
    @DisplayName("[RED] Flow 3-2 (실패): 최종 담당자가 '배정되지 않은(null)' 배송을 '시작'하려 하면, IllegalStateException이 발생한다")
    void startCompanyDelivery_FailsWhen_CompanyDriverNotAssigned() {

        // --- Given (준비) ---
        // 1. 테스트할 ID
        UUID deliveryId = UUID.randomUUID();

        // 2. [핵심] '가짜' 배송(Delivery) 데이터
        Delivery fakeNotReadyDelivery = Delivery.builder()
                .status(DeliveryStatus.ARRIVED_AT_DEST_HUB) // 최종 허브 도착 상태
                .companyDriverId(null) // 최종 담당자 배정 안됨
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeNotReadyDelivery));

        // --- When (실행) & Then (검증) ---
        // 4. 'startCompanyDelivery' 메서드 실행 시 'IllegalStateException'이 발생하는지 검증
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            deliveryService.startCompanyDelivery(deliveryId);
        });

        // 5. 에외 메시지 검증
        assertEquals("최종 배송 담당자가 배정되지 않아 배송을 시작할 수 없습니다.", exception.getMessage());

        // 6. [중요] 예외가 발생했으므로, 'save'는 절대 호출되지 않았어야 함
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    // -----------------------------------------------------------------
    // [TDD] Flow 3-3: completeDelivery (최종 배송 완료)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("[RED] Flow 3-3 (성공): '업체 이동중'인 배송을 '완료'하면, 상태가 COMPLETED로 변경되고 이벤트가 발행된다")
    void completeDelivery_SuccessScenario_ShouldChangeStatusToCompletedAndPublishEvent() {

        // --- Given (준비) ---
        UUID deliveryId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        // 2. [핵심] '가짜' 배송(Delivery) 데이터
        Delivery fakeDeliveryingDelivery = Delivery.builder()
                .orderId(orderId)
                .status(DeliveryStatus.COMPANY_DELIVERING) // '업체 이동중' 상태
                .companyDriverId(UUID.randomUUID()) // 담당자 배정됨
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeDeliveryingDelivery));

        // 4. 저장될 객체(Delivery)를 갭처할 Captor 준비
        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);
        // 5. 발행될 이벤트(Event)를 캡처할 Captor 준비
        ArgumentCaptor<DeliveryCompletedEvent> eventCaptor = ArgumentCaptor.forClass(DeliveryCompletedEvent.class);

        // --- When (실행) ---
        // 6. 'completeDelivery' 메서드 실행
        deliveryService.completeDelivery(deliveryId);

        // --- Then (검증) ---
        // 7. 'save'가 1번 호출되었는지 검증
        verify(deliveryRepository, times(1)).save(deliveryCaptor.capture());

        // 8. 'sendDeliveryCompletedEvent'가 1번 호출되었는지 검증
        verify(rabbitMQProducer, times(1)).sendDeliveryCompletedEvent(eventCaptor.capture());

        // 9. 저장된 Delivery 객체의 상태 검증
        Delivery savedDelivery = deliveryCaptor.getValue();
        assertEquals(DeliveryStatus.COMPLETED, savedDelivery.getStatus(), "상태가 'COMPLETE'(배송 완료)로 변경되어야 합니다.");

        // 10. 발행된 Event 객체의 데이터 검증
        DeliveryCompletedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(orderId, publishedEvent.getOrderId(), "이벤트에 올바른 orderId가 포함되어야 합니다.");
    }

    @Test
    @DisplayName("[RED] Flow 3-3 (실패): '최종 허브 도착' 상태인 배송을 (시작도 안하고) '완료'하려 하면, IllegalStateException이 발생한다")
    void completeDelivery_FailsWhen_StatusIsNotCompanyDelivering() {

        // --- Given (준비) ---
        UUID deliveryId = UUID.randomUUID();

        // 2. [핵심] '가짜' 배송(Delivery) 데이터
        Delivery fakeArrivedDelivery = Delivery.builder()
                .status(DeliveryStatus.ARRIVED_AT_DEST_HUB) // 아직 '업체 이동중'이 아님
                .companyDriverId(UUID.randomUUID())
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeArrivedDelivery));

        // --- When (실행) & Then (검증) ---
        // 4. 'completeDelivery' 메서드 실행 시 'IllegalStateException'이 발생하는지 검증
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            deliveryService.completeDelivery(deliveryId);
        });

        // 5. 예외 메시지 검증
        assertEquals("현재 '업체 이동중(COMPANY_DELIVERING)' 상태인 배송만 완료할 수 있습니다.", exception.getMessage());

        // 6. [중요] 예외가 발생했으므로 'save'와 'sendEvent'는 절대 호출되지 않았어야 함
        verify(deliveryRepository, never()).save(any(Delivery.class));
        verify(rabbitMQProducer, never()).sendDeliveryCompletedEvent(any(DeliveryCompletedEvent.class));


    }

    // -----------------------------------------------------------------
    // [TDD] GET /deliveries/{id} (상세 조회)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("[REFACTOR/GREEN] GET (성공): Fetch Join으로, 존재하는 ID로 배송 상세 조회 시, DTO(경로 포함)를 반환한다")
    void getDeliveryDetails_Success_ShouldReturnDetailDto() {

        // --- Given (준비) ---
        // 1. 테스트할 ID
        UUID deliveryId = UUID.randomUUID();

        // 2. [핵심] '가짜' 배송(Delivery) 데이터 (자식 경로 2개를 포함)
        Delivery fakeDelivery = Delivery.builder()
                .orderId(UUID.randomUUID())
                .status(DeliveryStatus.COMPLETED)
                .build();

        DeliveryRouteHistory fakeRoute1 = DeliveryRouteHistory.builder().sequence(1).build();
        DeliveryRouteHistory fakeRoute2 = DeliveryRouteHistory.builder().sequence(2).build();

        fakeDelivery.addRouteHistory(fakeRoute1);
        fakeDelivery.addRouteHistory(fakeRoute2);

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
//        when(deliveryRepository.findById(deliveryId))
        when(deliveryRepository.findDeliveryWithHistoriesById(deliveryId))
                .thenReturn(Optional.of(fakeDelivery));

        // --- When (실행) ---
        // 4. 'getDeliveryDetails' 메서드 실행 (아직 내용이 null)
        DeliveryDetailResponse response = deliveryService.getDeliveryDetails(deliveryId);

        // --- Then (검증) ---
        // 5. 'findById'가 1번 호출되었는지 검증
//        verify(deliveryRepository, times(1)).findById(deliveryId);
        verify(deliveryRepository, times(1)).findDeliveryWithHistoriesById(deliveryId);

        // 6. 반환된 DTO가 null이 아니고, 핵심 정보가 일치하는지 검증
        assertNotNull(response);
        assertEquals(fakeDelivery.getOrderId(), response.getOrderId(), "주문 ID가 일치해야 합니다.");

        // 7. [핵심] 자식(경로) 정보도 DTO에 포함되었는지 검증
        assertNotNull(response.getRouteHistories());
        assertEquals(2, response.getRouteHistories().size(), "경로 2개가 모두 포함되어야 합니다.");
    }

    @Test
    @DisplayName("[RED] GET (실패): 존재하지 않는 ID로 배송 상세 조회 시, EntityNotFoundException이 발생한다")
    void getDeliveryDetails_FailsWhen_IdNotFound() {

        // --- Given (준비) ---
        // 1. 존재하지 않는 ID
        UUID nonExistentDeliveryId = UUID.randomUUID();

        // 2. [핵심] Repository가 '빈 Optional'을 반환하도록 설정
//        when(deliveryRepository.findById(nonExistentDeliveryId))
        when(deliveryRepository.findDeliveryWithHistoriesById(nonExistentDeliveryId))
                .thenReturn(Optional.empty());

        // --- When (실행) & Then (검증) ---
        // 3. 'getDeliveryDetails' 메서드 실행 시 'EntityNotFoundException'이 발생하는지 검증
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            deliveryService.getDeliveryDetails(nonExistentDeliveryId);
        });

        // 4. 예외 메시지 검증
        String expectedMessage = String.format("배송을(를) 찾을 수 없습니다. (ID: %s)", nonExistentDeliveryId.toString());
        assertEquals(expectedMessage, exception.getMessage());
    }

    // -----------------------------------------------------------------
    // [TDD] GET /deliveries (목록 조회/페이지네이션) - [REFACTOR]
    // -----------------------------------------------------------------

    @Test
//    @DisplayName("[REFACTOR/GREEN] GET (성공): 배송 목록 조회 시, 페이지네이션된 DTO를 반환한다")
    @DisplayName("[REFACTOR/GREEN] GET (성공): 기본 배송 목록 조회(검색 조건 없음) 시, 페이지 DTO를 반환한다")
    void searchDeliveries_Success_ShouldReturnPaginatedDto() {

        // --- Given (준비) ---
        // 1. 페이지 요청 객체 (0번째 페이지, 10개)
        Pageable pageable = PageRequest.of(0,10);

        // [REFACTOR] 검색 조건이 없는 빈 DTO 생성
        DeliverySearchCriteria emptyCriteria = new DeliverySearchCriteria();

        // 2. [핵심] DB에서 반환될 '가짜' 엔티티 목록
        Delivery fakeDelivery1 = Delivery.builder()
                .recipientName("김배송1")
                .status(DeliveryStatus.COMPLETED)
                .build();
        Delivery fakeDelivery2 = Delivery.builder()
                .recipientName("김배송2")
                .status(DeliveryStatus.HUB_TO_HUB)
                .build();
        List<Delivery> deliveryList = List.of(fakeDelivery1, fakeDelivery2);

        // 3. '가짜' 페이지(Page) 객체 생성 (총 2개의 요소)
        Page<Delivery> fakePage = new PageImpl<>(deliveryList, pageable, 2L);

        // 4. Repository가 이 가짜 페이지를 반환하도록 설정
        // 참고: @Where(deleted_at=null)은 JPA가 자동 처리하므로 findAll() 호출
        // [REFACTOR] findAll(Pageable) -> findAll(Specification, Pageable)
        // Specification은 어떤 것이든 상관없다는 any() 사용
//        when(deliveryRepository.findAll(pageable))
        when(deliveryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(fakePage);

        // --- When (실행) ---
        // 5. 'searchDeliveries' 메서드 실행 (아직 내용이 Null)
        // [REFACTOR] 빈 criteria 객체를 전달
        Page<DeliverySummaryResponse> responsePage = deliveryService.searchDeliveries(emptyCriteria, pageable);

        // --- Then (검증) ---
        // 6. 'findAll'이 1번 호출되었는지 검증
        // [REFACTOR] 호출 검증 메서드 변경
        verify(deliveryRepository, times(1)).findAll(any(Specification.class), eq(pageable));

        // 7. 반환된 DTO 페이지가 null이 아니고, 핵심 정보가 일치하는지 검증
        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements(), "전체 요소 개수가 2개여야 합니다.");
        assertEquals(1, responsePage.getTotalPages(), "전체 페이지 개수가 1개여야 합니다.");

        // 8. [핵심] 엔티티(Delivery)가 DTO(DeliverySummaryResponse)로 변환되었는지 검증
        assertEquals("김배송1", responsePage.getContent().get(0).getRecipientName());
        assertEquals(DeliveryStatus.HUB_TO_HUB, responsePage.getContent().get(1).getStatus());
    }

    // 다음 RED 테스트 : '검색 조건(Specification)이 포함된 테스트
    @Test
    @DisplayName("[RED] GET (성공): 'status'로 검색 시, 필터링된 Specification으로 repository를 호출해야 한다")
    void searchDeliveries_WithStatusCriteria_ShouldCallRepositoryWithSpecification() {

        // --- Given (준비) ---
        Pageable pageable = PageRequest.of(0,10);

        // 1. [핵심] 'status'가 'COMPLETED'인 검색 조건 생성
        DeliverySearchCriteria criteria = new DeliverySearchCriteria();
        criteria.setStatus(DeliveryStatus.COMPLETED);

        // 2. DB에서 변환될 '가짜' 필터링된 목록 (COMPLETE 1건만)
        Delivery fakeCompletedDelivery = Delivery.builder()
                .recipientName("김완료")
                .status(DeliveryStatus.COMPLETED)
                .build();
        Page<Delivery> fakePage = new PageImpl<>(List.of(fakeCompletedDelivery), pageable, 1L);

        // 3. Repository Mocking
        // 어떤 Specification이든, 이 Pageable과 함께 호출되면 fakePage를 반환
        when(deliveryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(fakePage);

        // 4. [핵심] Repository에 전달될 'Specification'을 캡처할 Captor 준비
        ArgumentCaptor<Specification<Delivery>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        // --- When (실행) ---
        // 5. 'searchDeliveries' 메서드 실행 (아직 내용이 null)
        Page<DeliverySummaryResponse> responsePage = deliveryService.searchDeliveries(criteria, pageable);

        // --- Then (검증) ---
        // 6. [RED] 'findAll'이 1번 호출되었는지 검증
        // Service가 null을 반환하므로, 이 테스트는 responsePage 검증에서 실패함
        verify(deliveryRepository, times(1)).findAll(specCaptor.capture(), eq(pageable));

        // 7. [RED] 반환된 DTO 페이지 검증
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements(), "필터링된 1건만 반환되어야 합니다.");
        assertEquals("김완료", responsePage.getContent().get(0).getRecipientName());

        // 8. 전달된 Specification이 null이 아닌지 검증
        assertNotNull(specCaptor.getValue(), "Specification 객체가 생성되어 전달되어야 합니다.");
    }

    // 다음 RED 테스트 : '권한(DRIVER)'에 따라 필터링되는 테스트

    // -----------------------------------------------------------------
    // [TDD] DELETE /deliveries/{id} (논리 삭제)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("[RED] DELETE (성공): 존재하는 ID로 삭제 요청 시, repository.delete가 호출된다")
    void deleteDelivery_Success_ShouldCallRepositoryDelete() {

        // -- Given (준비) --
        // 1. 테스트할 ID
        UUID deliveryId = UUID.randomUUID();

        // 2. [핵심] '가짜' 배송(Delivery) 데이터
        Delivery fakeDelivery = Delivery.builder()
                .orderId(UUID.randomUUID())
                .status(DeliveryStatus.COMPLETED)
                .build();

        // 3. Repository가 이 가짜 데이터를 반환하도록 설정
        when(deliveryRepository.findById(deliveryId))
                .thenReturn(Optional.of(fakeDelivery));

        // 4. repository.delete()는 void를 반환하므로, 정상 동작하도록 설정
        doNothing().when(deliveryRepository).delete(fakeDelivery);

        // -- When (실행) --
        // 5. 'deleteDelivery' 메서드 실행 (아직 내용이 비어있음)
        deliveryService.deleteDelivery(deliveryId);

        // -- Then (검증) --
        // 6. 'findById'가 1번 호출되었는지 검증 (존재 확인)
        verify(deliveryRepository, times(1)).findById(deliveryId);

        // 7. [핵심] 'delete(entity)'가 1번 호출되었는지 검증
        // JPA가 이 호출을 @SQLDelete로 변환할 것임
        verify(deliveryRepository, times(1)).delete(fakeDelivery);
    }

    @Test
    @DisplayName("[RED] DELETE (실패): 존재하지 않는 ID로 삭제 요청 시, EntityNotFoundException이 발생하고 delete는 호출되지 않는다")
    void deleteDelivery_FailsWhen_IdNotFound() {

        // -- Given (준비) --
        // 1. 존재하지 않는 ID
        UUID nonExistentDeliveryId = UUID.randomUUID();

        // 2. [핵심] Repository가 '빈 Optional'을 반환하도록 설정
        when(deliveryRepository.findById(nonExistentDeliveryId))
                .thenReturn(Optional.empty());

        // -- When (실행) & Then (검증) --
        // 3. 'deleteDelivery' 메서드 실행 시 'EntityNotFoundException'이 발생하는지 검증
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            deliveryService.deleteDelivery(nonExistentDeliveryId);
        });

        // 4. [중요] 예외가 발생했으므로, 'delete'는 절대 호출되지 않았어야 함
        verify(deliveryRepository, never()).delete(any(Delivery.class));
    }
}


















