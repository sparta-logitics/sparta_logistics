package com.example.sparta.order_service;

import com.example.sparta.order_service.application.service.OrderCommandService;
import com.example.sparta.order_service.domain.entity.Order;
import com.example.sparta.order_service.domain.repository.OrderRepository;
import com.example.sparta.order_service.presentation.dto.request.OrderLineRequest;
import com.example.sparta.order_service.presentation.dto.request.OrderRequest;
import com.example.sparta.order_service.presentation.dto.request.ShippingInfoRequest;
import com.example.sparta.order_service.presentation.dto.response.OrderCreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderUnitTests {
    @InjectMocks
    private OrderCommandService orderCommandService;
    @Mock
    private OrderRepository orderRepository;

    private OrderRequest orderRequest;

    @BeforeEach
    void setup() {
        ShippingInfoRequest originInfo = new ShippingInfoRequest(
                "출발지회사", "김보냄", "010-1111-1111",
                "서울시 강남구", "A빌딩 101호", "12345"
        );
        ShippingInfoRequest recipientInfo = new ShippingInfoRequest(
                "거래처", "이받음", "010-2222-2222",
                "서울시 종로구", "B아파트 202호", "67890"
        );

        List<OrderLineRequest> orderLines = List.of(
                new OrderLineRequest("상품A", 10000L, 2, UUID.randomUUID()), // 20000원
                new OrderLineRequest("상품B", 5000L, 3, UUID.randomUUID())  // 15000원
        );

        orderRequest = new OrderRequest(
                "배송메시지 테스트",
                "slackId",
                LocalDateTime.of(LocalDate.of(2999, 12, 31), LocalTime.now()),
                originInfo,
                recipientInfo,
                orderLines
        );
    }

    @DisplayName("주문 생성 성공")
    @Test
    void createOrder_Success() {
        Order order = orderRequest.toEntity();
        String userEmail = "tempUserEmail";

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);

        OrderCreateResponse createResponse = orderCommandService.create(orderRequest, userEmail);

        assertThat(createResponse).isNotNull();
        assertThat(createResponse.totalAmount()).isEqualTo(35000L);
        assertThat(createResponse.deliveryMessage().equals("배송메시지 테스트"));

        verify(orderRepository, times(1)).save(orderArgumentCaptor.capture());
    }

    @DisplayName("주문 생성 실패: DB 저장 중 에러 발생")
    @Test
    void createOrder_Fail_DB() {
        String userEmail = "tempUserEmail";
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new DataAccessException("Test DB Error") {});

        assertThatThrownBy(() -> orderCommandService.create(orderRequest, userEmail))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Test DB Error");

        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
