package com.example.sparta.order_service.presentation.controller;

import com.example.sparta.order_service.application.service.OrderQueryService;
import com.example.sparta.order_service.application.service.OrderCommandService;
import com.example.sparta.order_service.presentation.dto.request.OrderRequest;
import com.example.sparta.order_service.presentation.dto.request.OrderUpdateRequest;
import com.example.sparta.order_service.presentation.dto.request.SearchCondition;
import com.example.sparta.order_service.presentation.dto.response.OrderCreateResponse;
import com.example.sparta.order_service.presentation.dto.response.OrderDetailResponse;
import com.example.sparta.order_service.presentation.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
// TODO GlobalExceptionHandler 구현
public class OrderController {
    private final OrderCommandService commandService;
    private final OrderQueryService queryService;

    // TODO Principal 객체를 받아서 userEmail 할당해주기
    @PostMapping
    public ResponseEntity<OrderCreateResponse> create(@RequestBody @Valid OrderRequest request) {
        String userEmail = "tempUserEmail";
        return ResponseEntity.created(URI.create("temp"))
                .body(commandService.create(request, userEmail));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> search(SearchCondition condition, Pageable pageable) {
        return ResponseEntity.ok(queryService.search(condition, "tempEmail", pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> update(@PathVariable UUID id, @RequestBody @Valid OrderUpdateRequest request) {
        return ResponseEntity.ok(commandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Long tempUserId = 0L;
        commandService.delete(id, tempUserId);
        return ResponseEntity.noContent()
                .location(URI.create("delete-temp-url"))
                .build();
    }

}
