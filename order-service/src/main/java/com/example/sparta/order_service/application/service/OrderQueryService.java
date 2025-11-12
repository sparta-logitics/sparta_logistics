package com.example.sparta.order_service.application.service;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.example.sparta.order_service.domain.repository.OrderQueryRepository;
import com.example.sparta.order_service.domain.repository.OrderRepository;
import com.example.sparta.order_service.presentation.dto.request.SearchCondition;
import com.example.sparta.order_service.presentation.dto.response.OrderDetailResponse;
import com.example.sparta.order_service.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderRepository orderRepository;

    // TODO 사용자 권한에 따른 verify 절차 필요
    public Page<OrderResponse> search(SearchCondition condition, String userEmail, Pageable pageable) {
        if (pageable.getPageSize() < 0 || pageable.getPageNumber() < 0)
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "페이지 사이즈와 번호는 0 이상이어야합니다.");
        return orderRepository.search(condition, userEmail, pageable);
    }

    public OrderDetailResponse findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND))
                .toDetailResponse();
    }
}
