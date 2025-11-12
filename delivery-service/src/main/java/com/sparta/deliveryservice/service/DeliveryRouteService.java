package com.sparta.deliveryservice.service;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.DeliveryRouteHistory;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import com.sparta.deliveryservice.domain.enums.RouteStatus;
import com.sparta.deliveryservice.exception.EntityNotFoundException;
import com.sparta.deliveryservice.repository.DeliveryRouteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryRouteService {

    private final DeliveryRouteHistoryRepository routeHistoryRepository;

    /**
     * TDD Flow 2-1: 담당자 배정
     */
    @Transactional // 쓰기 작업이므로 readOnly=false 적용
    public void assignDriver(UUID routeHistoryId, UUID driverId) {
        // 이 로직을 TDD로 채워나갈 예정

        // 1. 검증1 findyById
        // routeHistoryRepository.findById가 호출되었는지 검증(verify)
        DeliveryRouteHistory route = routeHistoryRepository.findById(routeHistoryId)
                .orElseThrow(() -> new EntityNotFoundException("배송 경로", routeHistoryId));
                // 나중에 리팩토링 단꼐에서 Custom Exception 으로 변경

        // 2. 검증2,3 도메인 로직assignDriver을 호출하여 driverId를 변경한다.
        route.assignDriver(driverId);

        // 3. 검증4 save
        //    TDD 테스트가 save호출을 검증(verify)하므로
        //    JPA Dirty Checking에 의존하지 않고 명시적으로 save를 호출한다.
        routeHistoryRepository.save(route);
    }

    /**
     * TDD Flow 2-2: 배송 시작
     */
    @Transactional
    public void startRoute(UUID routeHistoryId) {
        // TDD 검증. findById 호출 (및 'ID없음' 예외 처리)
        DeliveryRouteHistory route = routeHistoryRepository.findById(routeHistoryId)
                .orElseThrow(() -> new EntityNotFoundException("배송 경로", routeHistoryId));

        // 2. TDD 검증 도메인 로직 호출
        // 이 메서드가 담당자없음 또는 상태이상 예외를 던짐
        route.startRoute();

        Delivery delivery = route.getDelivery();
        delivery.startRoute();

        // 3. TDD 검증. save 호출(성공 시에만)
        routeHistoryRepository.save(route);
    }

    /**
     * TDD Flow 2-3: 허브 도착
     */
    @Transactional
    public void completeRoute(UUID routeHistoryId, Double actualDistance, Integer actualDuration) {

        // 1. [TDD 검증] findById 호출 (및 'ID 없음' 예외 처리)
        DeliveryRouteHistory route = routeHistoryRepository.findById(routeHistoryId)
                .orElseThrow(() -> new EntityNotFoundException("배송 경로", routeHistoryId));

        // 2. [TDD 검증] 도메인 로직 호출
        // 이 메서드가 '상태 불일치' 예외를 던짐
        route.completeRoute(actualDistance, actualDuration);

        Delivery delivery = route.getDelivery();
        boolean completeHubDelivery = delivery.getRouteHistories().stream()
                .allMatch(r -> {
                    return r.getStatus() == RouteStatus.ARRIVED_AT_HUB;
                });
        if (completeHubDelivery) delivery.completeHubDelivery();

        // 3. [TDD 검증] save 호출 (성공 시에만)
        routeHistoryRepository.save(route);
    }

}

















