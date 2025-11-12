package com.sparta.deliveryservice.repository;

import com.sparta.deliveryservice.domain.Delivery;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // 1. UUID 임포트

@Repository
// 2. JpaRepository<Entity, ID타입> -> ID타입을 UUID로 변경
public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery>{

    // [REFACTOR] Fetch Join을 사용한 커스텀 쿼리 추가
    /**
     * N+1 문제를 해결하기 위해 Fetch Join을 사용합니다.
     * 'JOIN FETCH d.routeHistories': Delivery(d)를 조회할 때,
     * 연관된 routeHistories 컬렉션을 즉시 함께 로딩합니다.
     */
    @Query("SELECT d FROM Delivery d JOIN FETCH d.routeHistories WHERE d.deliveryId = :deliveryId")
    Optional<Delivery> findDeliveryWithHistoriesById(@Param("deliveryId") UUID deliveryId);

    // 이 쿼리는 deliveryId가 없을 때 routeHistories가 없으면
    // Delivery도 조회되지 않을 수 있으나,
    // 우리는 생성(createDelivery) 시 항상 경로를 함께 생성하므로
    // JOIN FETCH가 안전합니다.
}