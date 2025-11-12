package com.sparta.deliveryservice.domain;

import com.sparta.deliveryservice.client.dto.RouteInfoResponse;
import com.sparta.deliveryservice.domain.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import com.sparta.deliveryservice.domain.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sparta.deliveryservice.domain.enums.DeliveryStatus.ARRIVED_AT_DEST_HUB;

@Entity
@Getter
@Table(name = "p_deliveries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE p_deliveries SET deleted_at = NOW() WHERE delivery_id = ?")
@Where(clause = "deleted_at IS NULL")
@Builder // 테스트를 위하 Builder를 클래스 레벨로 이동
@AllArgsConstructor // Builder가 모든 필드를 사용하도록 추가
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", columnDefinition = "uuid") // 2. name 추가
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false, unique = true, columnDefinition = "uuid") // 2. name 추가
    private UUID orderId;

    // 수령인 정보
    @Column(name = "destination_address", nullable = false) // 2. name 추가
    private String destinationAddress;

    @Column(name = "recipient_name", nullable = false) // 2. name 추가
    private String recipientName;

    @Column(name = "recipient_slack_id", nullable = false) // 2. name 추가
    private String recipientSlackId;

    // 허브 및 담당자 정보
    @Column(name = "origin_hub_id", nullable = false, columnDefinition = "uuid") // 2. name 추가
    private UUID originHubId;

    @Column(name = "destination_hub_id", nullable = false, columnDefinition = "uuid") // 2. name 추가
    private UUID destinationHubId;

    @Column(name = "company_driver_id", nullable = true, columnDefinition = "uuid") // 2. name 추가
    private UUID companyDriverId;

    // AI 예측 시간
    @Column(name = "estimated_arrival_time", nullable = false) // 2. name 추가
    private LocalDateTime estimatedArrivalTime;

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30) // 2. name 추가
    private DeliveryStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryRouteHistory> routeHistories = new ArrayList<>();

    // 실제 배송 완료 시간을 기록하기 위한 필드 추가
    @Column(name = "actual_delivery_time", nullable = true)
    private LocalDateTime actualDeliveryTime;

    // REFACTOR 생성 로직을 엔티티 내부로 이동
    public static Delivery createDelivery(DeliveryCreateRequest request,
                                          List<RouteInfoResponse> routes,
                                          LocalDateTime estimatedArrivalTime) {
        // 1. 부모(Delivery) 생성
        Delivery newDelivery = Delivery.builder()
                .orderId(request.getOrderId())
                .destinationAddress(request.getDestinationAddress())
                .recipientName(request.getRecipientName())
                .recipientSlackId(request.getRecipientSlackId())
                .originHubId(request.getOriginHubId())
                .destinationHubId(request.getDestinationHubId())
                .estimatedArrivalTime(estimatedArrivalTime)
                .status(DeliveryStatus.WAITING_AT_HUB)
                .build();

        // 2. 자식(RouteHistory) 생성 및 연결
        int sequence = 1;
        for (RouteInfoResponse routeInfo : routes) {
            DeliveryRouteHistory routeHistory = DeliveryRouteHistory.builder()
                    .sequence(sequence++)
                    .originHubId(routeInfo.getOriginHubId())
                    .destinationHubId(routeInfo.getDestinationHubId())
                    .estimatedDistance(routeInfo.getEstimatedDistance())
                    .estimatedDuration(routeInfo.getEstimatedDuration())
                    .status(RouteStatus.WAITING_FOR_TRANSIT)
                    .build();

            newDelivery.addRouteHistory(routeHistory);
        }

        return newDelivery;
    }

    public void addRouteHistory(DeliveryRouteHistory routeHistory) {
        this.routeHistories.add(routeHistory);
        routeHistory.setDelivery(this);
    }

    /**
     * [GREEN] Flow 3-1: 최종 배송 담당자를 배정합니다.
     * TDD 테스트 케이스 2개의 요구사항을 충족시킵니다.
     */
    public void assignCompanyDriver(UUID companyDriverId) {

        // 1. [TDD 검증] '경로 미도착 실패' 테스트를 통과시키기 위한 규칙
        // 'routeHistories' 리스트의 모든 항목을 순회(stream) 하며,
        // '모든(allMatch)' 경로의 상태가 'ARRIVED_AT_HUB'인지 검사합니다.
        boolean allRoutesArrived = this.routeHistories.stream()
                .allMatch(route -> route.getStatus() == RouteStatus.ARRIVED_AT_HUB);

        if (!allRoutesArrived) {
            throw new IllegalStateException("모든 허브 경로가 도착 완료 상태여야 담당자를 배정할 수 있습니다.");
        }

        // (TDD 이후 리팩토링: 이미 배정되었는지, delivery의 status가 ARRIVED_AT_DEST_HUB인지 등 추가 검증)

        // delivery의 status가 ARRIVED_AT_DEST_HUB인가?
        if (this.status != ARRIVED_AT_DEST_HUB) {
            throw new IllegalStateException("배달이 도착 완료 상태여야 담당자를 배정할 수 있습니다.");
        }


        // 2. [TDD 검증] '성공' 테스트를 통과시키기 위한 담당자 배정
        this.companyDriverId = companyDriverId;
    }

    /**
     * [GREEN] Flow 3-2: 최종 배송을 시작합니다.
     * TDD 테스트 케이스 2개의 요구사항을 충족시킵니다.
     */
    public void startCompanyDelivery() {

        // 1. [TDD 검증] '담당자 미지정 실패' 테스트를 통과시키기 위한 규칙
        if (this.companyDriverId == null) {
            throw new IllegalStateException("최종 배송 담당자가 배정되지 않아 배송을 시작할 수 없습니다.");
        }

        // 2. [TDD 검증] '성공' 테스트를 위한 상태 규칙
        // 이 규칙을 검증하는 [RED] 테스트는 다음에 추가해야 함
        if (this.status != ARRIVED_AT_DEST_HUB) {
            throw new IllegalStateException("최종 허브에 도착한 상태여야 배송을 시작할 수 있습니다.");
        }

        // 3. [TDD 검증] '성공' 테스트를 통과시키기 위한 상태 변경
        this.status = DeliveryStatus.COMPANY_DELIVERING;
    }

    /**
     * [GREEN] Flow 3-3: 최종 배송을 완료합니다.
     */
    public void completeDelivery() {

        // 1. TDD 검증. '상태 불 일치 실패' 테스트를 통과시키기 위한 규칙
        if (this.status != DeliveryStatus.COMPANY_DELIVERING) {
            throw new IllegalStateException("현재 '업체 이동중(COMPANY_DELIVERING)' 상태인 배송만 완료할 수 있습니다.");
        }

        // 2. TDD 검증. '성공' 테스트를 통과시키기 위한 상태 변경
        this.status = DeliveryStatus.COMPLETED;
        this.actualDeliveryTime = LocalDateTime.now(); // 실제 완료 시간 기록
    }

    public void startRoute() {
        this.status = DeliveryStatus.HUB_TO_HUB;
    }

    public void completeHubDelivery() {
        this.status = DeliveryStatus.ARRIVED_AT_DEST_HUB;
    }
}














