package com.example.sparta.order_service.domain.entity;

import com.example.sparta.common.model.BaseEntity;
import com.example.sparta.order_service.application.event.OrderCreateEvent;
import com.example.sparta.order_service.presentation.dto.request.OrderLineRequest;
import com.example.sparta.order_service.presentation.dto.request.OrderUpdateRequest;
import com.example.sparta.order_service.presentation.dto.response.OrderCreateResponse;
import com.example.sparta.order_service.presentation.dto.response.OrderDetailResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;
    private UUID deliveryId;
    private UUID hubId;
    @Column(nullable = false)
    private String userEmail;
    @Column(nullable = false)
    private Long totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String deliveryMessage;
    private Integer deliveryFee;
    @Column(nullable = false)
    private LocalDateTime dueDate;
    private String representativeProductName;
    private int orderLineCount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "companyName", column = @Column(name = "origin_company_name", nullable = false, length = 100)),
            @AttributeOverride(name = "name", column = @Column(name = "origin_name", nullable = false, length = 10)),
            @AttributeOverride(name = "phone", column = @Column(name = "origin_phone", nullable = false, length = 13)),
            @AttributeOverride(name = "address", column = @Column(name = "origin_address", nullable = false, length = 50)),
            @AttributeOverride(name = "addressDetail", column = @Column(name = "origin_address_detail", nullable = false, length = 100)),
            @AttributeOverride(name = "zipCode", column = @Column(name = "origin_zip_code", nullable = false, length = 5))
    })
    private ShippingInfo originInfo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "companyName", column = @Column(name = "recipient_company_name", nullable = false, length = 100)),
            @AttributeOverride(name = "name", column = @Column(name = "recipient_name", nullable = false, length = 10)),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_phone", nullable = false, length = 13)),
            @AttributeOverride(name = "address", column = @Column(name = "recipient_address", nullable = false, length = 50)),
            @AttributeOverride(name = "addressDetail", column = @Column(name = "recipient_address_detail", nullable = false, length = 100)),
            @AttributeOverride(name = "zipCode", column = @Column(name = "recipient_zip_code", nullable = false, length = 5))
    })
    private ShippingInfo recipientInfo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "p_order_histories",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @OrderBy("createdAt DESC")
    private List<OrderHistory> orderHistories = new ArrayList<>();

    @Builder
    public Order(UUID orderId, String userEmail, Long totalAmount, OrderStatus status, String deliveryMessage, Integer deliveryFee, LocalDateTime dueDate, String representativeProductName, int orderLineCount, ShippingInfo originInfo, ShippingInfo recipientInfo, List<OrderLine> orderLines, List<OrderHistory> orderHistories) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
        this.status = status;
        this.deliveryMessage = deliveryMessage;
        this.deliveryFee = deliveryFee;
        this.dueDate = dueDate;
        this.representativeProductName = representativeProductName;
        this.orderLineCount = orderLineCount;
        this.originInfo = originInfo;
        this.recipientInfo = recipientInfo;
        this.orderLines = orderLines;
        this.orderHistories = orderHistories;
    }


    public OrderCreateResponse toCreateResponse() {
        return OrderCreateResponse.builder()
                .orderId(orderId)
                .deliveryMessage(deliveryMessage)
                .totalAmount(totalAmount)
                .orderDate(getCreatedAt())
                .orderedBy(userEmail)
                .state(status)
                .originInfo(originInfo.toResponse())
                .recipientInfo(recipientInfo.toResponse())
                .orderLines(orderLines.stream().map(OrderLine::toResponse).toList())
                .build();
    }

    public void setUserEmailToCreate(String email) {
        userEmail = email;
    }

    public void update(OrderUpdateRequest request) {
        userEmail = request.userEmail();
        status = request.status();
        deliveryMessage = request.deliveryMessage();
        dueDate = request.dueDate();
        originInfo = request.originInfo().toEntity();
        recipientInfo = request.recipientInfo().toEntity();
        orderLines.clear();
        orderLines.addAll(request.orderLines().stream()
                .map(OrderLineRequest::toEntity)
                .toList());
        representativeProductName = orderLines.get(0).toResponse().productName();
        orderLineCount = orderLines.size();
    }

    public boolean isPreparing() {
        return status == OrderStatus.PAYMENT_PENDING || status == OrderStatus.PREPARING_FOR_SHIPMENT;
    }

    public void assignDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void changeOrderStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderDetailResponse toDetailResponse() {
        return OrderDetailResponse.builder()
                .deliveryMessage(deliveryMessage)
                .totalAmount(totalAmount)
                .orderDate(getCreatedAt())
                .orderedBy(userEmail)
                .state(status)
                .originInfo(originInfo.toResponse())
                .recipientInfo(recipientInfo.toResponse())
                .orderLines(orderLines.stream().map(OrderLine::toResponse).toList())
                .build();
    }

    public OrderCreateEvent toEvent() {
        return OrderCreateEvent.builder()
                .orderId(orderId)
                .destinationAddress(recipientInfo.toResponse().address())
                .recipientName(recipientInfo.toResponse().name())
                // TODO Order Entity에 slackId도 넣어야할지 고려
                .recipientSlackId("tempSlackId")
                // TODO 배송 생성 request에 hubId가 필요한지 논의
                .originHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .orderLines(orderLines.stream().map(OrderLine::toResponse).toList())
                .build();
    }
}
