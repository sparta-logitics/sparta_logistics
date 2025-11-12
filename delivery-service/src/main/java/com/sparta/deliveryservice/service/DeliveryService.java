package com.sparta.deliveryservice.service;

import com.sparta.deliveryservice.client.AiServiceClient;
import com.sparta.deliveryservice.client.HubRouteServiceClient;
import com.sparta.deliveryservice.client.dto.EtaPredictRequest;
import com.sparta.deliveryservice.client.dto.EtaPredictResponse;
import com.sparta.deliveryservice.client.dto.RouteInfoResponse;
import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.dto.request.DeliverySearchCriteria;
import com.sparta.deliveryservice.dto.response.DeliveryDetailResponse;
import com.sparta.deliveryservice.dto.response.DeliverySummaryResponse;
import com.sparta.deliveryservice.exception.EntityNotFoundException;
import com.sparta.deliveryservice.producer.RabbitMQProducer;
import com.sparta.deliveryservice.producer.dto.DeliveryCompletedEvent;
import com.sparta.deliveryservice.repository.DeliveryRepository;
import com.sparta.deliveryservice.repository.DeliveryRouteHistoryRepository;
import com.sparta.deliveryservice.repository.specification.DeliverySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor // @Mock이 주입될 생성자
@Transactional(readOnly = true)
public class DeliveryService {


    // [RED] 테스트가 @Mock으로 주입할 의존성들
    // 의존성 주입은 @RequiredArgsConstructor가 처리
    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteHistoryRepository deliveryRouteHistoryRepository;
    private final HubRouteServiceClient hubRouteServiceClient;
    private final AiServiceClient aiServiceClient;
//    private final DeliveryEventProducer deliveryEventProducer; // 이벤트 발행기 의존성
    private final RabbitMQProducer rabbitMQProducer;

    /**
     * [TDD] Flow 1: 배송 생성 (Flow 1)
     * TDD 성공 시나리오를 통과하기 위한 실제 구현
     */
    @Transactional
    public void createDelivery(DeliveryCreateRequest request) {

        // 1. B. 허브/경로 서비스 호출
        List<RouteInfoResponse> routes = hubRouteServiceClient.getRoutes(
                request.getOriginHubId(), request.getDestinationHubId()
        );

        // 방어 코드
        if (routes == null || routes.isEmpty()) {
             // 나중에 custom Exception 을 만들어주면 더 좋음
            throw new IllegalArgumentException("유효한 배송 경로를 찾을 수 없습니다.");
        }

        // 2. F. AI 서비스 호출
        EtaPredictRequest aiRequest = new EtaPredictRequest(routes);
        EtaPredictResponse aiResponse = aiServiceClient.calculateEta(aiRequest);

        // 3. 도메인 객체에게 생성을 위임
        Delivery newDelivery = Delivery.createDelivery(request,
                routes,
                aiResponse.getEstimatedArrivalTime());

        // 4. db 저장
         deliveryRepository.save(newDelivery);
    }

    /**
     * [GREEN] Flow 3-1: 최종 담당자 배정
     */
    @Transactional
    public void assignCompanyDriver(UUID deliveryId, UUID companyDriverId) {

        // 1. [TDD 검증] findById 호출 (및 'ID 없음' 예외 처리)
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("배송", deliveryId));

        // 2. [TDD 검증] 도메인 로직 호출
        // (이 메서드가 '경로 미도착' 예외를 던짐)
        delivery.assignCompanyDriver(companyDriverId);

        // 3. [TDD 검증] save 호출 (성공 시에만)
        // JPA Dirver Checking으로 자동 저장 되지만, TDD의 명시적 검증을 위해 save 호출
        deliveryRepository.save(delivery);


    }

    /**
     * [TDD] Flow 3-2: 최종 배송 시작
     * TDD [RED] 테스트 2개를 통과하기 위한 실제 구현
     */
    @Transactional
    public void startCompanyDelivery(UUID deliveryId) {

        // 1. [TDD 검증] findById 호출 (및 'ID 없음' 예외 처리)
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("배송", deliveryId));

        // 2. [TDD 검증] 도메인 로직 호출
        // 이 메서드가 '담당자 미지정' 또는 '상태 이상' 예외를 던짐
        delivery.startCompanyDelivery();

        // 3. [TDD 검증] save 호출 (성공 시에만)
        deliveryRepository.save(delivery);
    }

    /**
     * [GREEN] Flow 3-3: 최종 배송 완료
     */
    @Transactional
    public void completeDelivery(UUID deliveryId) {
        // 1. [TDD 검증] findById 호출
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("배송", deliveryId));

        // 2. [TDD 검증] 도메인 로직 호출 (상태 검증 및 변경)
        // 이 메서드가 '상태 불일치' 예외를 던짐)
        delivery.completeDelivery();

        // 3. [TDD 검증] save 호출
        deliveryRepository.save(delivery);

        // 4. [TDD 검증] 이벤트 생성 및 발행
        DeliveryCompletedEvent event = new DeliveryCompletedEvent(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getActualDeliveryTime() // 완료된 시간으로 이벤트 생성
        );
        rabbitMQProducer.sendDeliveryCompletedEvent(event);
    }

    /**
     * [GREEN] 배송 상세 정보 조회
     */
    @Transactional(readOnly = true) // 조회 전용 트랜잭션
    public DeliveryDetailResponse getDeliveryDetails(UUID deliveryId) {

        // 1. [TDD 검증] findById 호출 및 'ID 없음' 예외 처리
        // 이 로직이 '실패 테스트'를 통과시킴
//        Delivery delivery = deliveryRepository.findById(deliveryId)
        Delivery delivery = deliveryRepository.findDeliveryWithHistoriesById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("배송", deliveryId));

        // 2. [TDD 검증] 엔티티를 DTO로 변환하여 반환
        // 이 로직이 '성공 테스트'를 통과시킴
        // @Transactional 덕분에 지연 로딩(LAZY)된 routeHistories도 조회 가능
        return new DeliveryDetailResponse(delivery);

    }

    /**
     * [GREEN] 배송 목록 검색(Specification 사용)
     */
    @Transactional(readOnly = true)
    // Pageable -> criteria, pageable로 시그니처 변경
    public Page<DeliverySummaryResponse> searchDeliveries(DeliverySearchCriteria criteria, Pageable pageable) {

        // 1. [TDD 검증] Specification 객체 생성
        // criteria.status가 null이면 빈 spec, null이 아니면 status 조건이 포함된 spec이 생성됨
        Specification<Delivery> spec = DeliverySpecification.build(criteria);

        // 2. [TDD 검증] JpaSpecificationExecutor의 findAll(spec, pageable) 호출
        // BaseEntity의 @Where(deleted_at=null)도 함께 적용됨
        Page<Delivery> entityPage = deliveryRepository.findAll(spec, pageable);

        // 3. [TDD 검증] Page<Delivery> -> Page<DeliverySummaryResponse>로 변환
        return entityPage.map(DeliverySummaryResponse::new);

//        // 1. [TDD 검증] findById(pageable) 호출
//        // BaseEntity의 @Where(clause = "deleted_at IS NULL")가
//        // JPA에 의해 자동으로 적용되어, 논리 삭제된 데이터는 제외됩니다.
//        Page<Delivery> entityPage = deliveryRepository.findAll(pegeable);
//
//        // 2. [TDD 검증] Page<Delivery>를 Page<DeliverySummaryResponse>로 변환
//        // Page 객체의 .map() 메서드를 사용하면
//        // 페이지네이션 정보(총 개수, 총 페이지 등)는 그대로 유지하면서
//        // 내부의 '내용(content)'만 DTO로 변환해줍니다.
//        return entityPage.map(DeliverySummaryResponse::new);
//        // 위 코드는 entityPage.map(delivery -> new DeliverySummaryResponse(delivery)와 동일
    }

    /**
     * [GREEN] 배송 논리 삭제
     */
    @Transactional // 쓰기 작업이므로 readOnly=false 적용
    public void deleteDelivery(UUID deliveryId) {
        // 1. [TDD 검증] findById 호출 및 'ID 없음' 예외 처리
        //    이 로직이 '실패 테스트'의 assertThrows를 통과시킴
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("배송", deliveryId));

        // 2. [TDD 검증] repository.delete() 호출
        //    이 로직이 '성공 테스트'의 verify(delete)를 통과시킴
        //    JPA가 이 호출을 가로채서 @SQLDelete 쿼리를 실행
        deliveryRepository.delete(delivery);

    }
}


















