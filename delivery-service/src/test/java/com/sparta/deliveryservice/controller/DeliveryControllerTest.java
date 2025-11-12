package com.sparta.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.controller.dto.request.CompanyDriverAssignRequest;
import com.sparta.deliveryservice.domain.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.exception.EntityNotFoundException;
import com.sparta.deliveryservice.exception.GlobalExceptionHandler;
import com.sparta.deliveryservice.service.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [TDD] API 계층 슬라이스 테스트
 * @WebMvcTest: 'DeliveryController'와 'GlobalExceptionHandler'만 로드합니다.
 */
@Import(GlobalExceptionHandler.class)
@WebMvcTest(DeliveryController.class)
@ContextConfiguration(classes = DeliveryControllerTestConfig.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청 시뮬레이터

    @Autowired
    private ObjectMapper objectMapper; // Java <-> JSON 변환기

    @MockitoBean // @Service를 '가짜 Bean'으로 대체
    private DeliveryService deliveryService;

    // (DeliveryRouteService는 이 컨트롤러와 무관하므로 MockBean으로 만들지 않음)

    // -----------------------------------------------------------------
    // [TDD] API 테스트: Flow 1 (createDelivery)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/deliveries (배송 생성)")
    class CreateDeliveryTests {

        @Test
        @DisplayName("[API 테스트/성공] 정상적인 배송 생성 요청 시 201 Created 반환")
        void createDelivery_Success_Returns201Created() throws Exception {

            // --- Given (준비) ---
            DeliveryCreateRequest requestDto = new DeliveryCreateRequest(
                    UUID.randomUUID(), "주소", "수령인", "slack123",
                    UUID.randomUUID(), UUID.randomUUID(), Collections.emptyList()
            );
            String requestBodyJson = objectMapper.writeValueAsString(requestDto);

            // 1. [Mocking] '가짜' 서비스가 정상 동작하도록 설정
            doNothing().when(deliveryService).createDelivery(any(DeliveryCreateRequest.class));

            // --- When (실행) ---
            // 2. MockMvc로 'POST /api/v1/deliveries' 요청
            mockMvc.perform(post("/api/v1/deliveries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyJson))

                    // --- Then (검증) ---
                    .andExpect(status().isCreated()) // 3. HTTP 201 (Created) 응답을 기대
                    .andDo(print());

            // 4. [검증] '가짜' 서비스의 'createDelivery'가 1번 호출되었는지 확인
            verify(deliveryService, times(1)).createDelivery(any(DeliveryCreateRequest.class));
        }
    }

    // -----------------------------------------------------------------
    // [TDD] API 테스트: Flow 3-1 (assignCompanyDriver)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/deliveries/{id}/assign-company-driver (최종 담당자 배정)")
    class AssignCompanyDriverTests {

        @Test
        @DisplayName("[API 테스트/성공] 정상적인 최종 담당자 배정 요청 시 200 OK 반환")
        void assignCompanyDriver_Success_Returns200Ok() throws Exception {

            // --- Given (준비) ---
            UUID deliveryId = UUID.randomUUID();
            CompanyDriverAssignRequest requestDto = new CompanyDriverAssignRequest(UUID.randomUUID());
            String requestBodyJson = objectMapper.writeValueAsString(requestDto);

            // 1. [Mocking] '가짜' 서비스가 정상 동작하도록 설정
            doNothing().when(deliveryService).assignCompanyDriver(
                    eq(deliveryId),
                    any(UUID.class)
            );

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/assign-company-driver", deliveryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyJson))

                    // --- Then (검증) ---
                    .andExpect(status().isOk())
                    .andDo(print());

            // 2. [검증] 서비스가 정확한 ID로 호출되었는지 확인
            verify(deliveryService, times(1)).assignCompanyDriver(
                    eq(deliveryId),
                    any(UUID.class)
            );
        }

        @Test
        @DisplayName("[API 테스트/실패] 경로가 미도착 상태일 때 배정 요청 시 409 Conflict 반환")
        void assignCompanyDriver_FailsWhen_RoutesNotArrived_Returns409Conflict() throws Exception {

            // --- Given (준비) ---
            UUID deliveryId = UUID.randomUUID();
            CompanyDriverAssignRequest requestDto = new CompanyDriverAssignRequest(UUID.randomUUID());
            String requestBodyJson = objectMapper.writeValueAsString(requestDto);
            String errorMessage = "모든 허브 경로가 도착 완료 상태여야 담당자를 배정할 수 있습니다.";

            // 1. [Mocking] '가짜' 서비스가 'IllegalStateException'을 던지도록 설정
            doThrow(new IllegalStateException(errorMessage))
                    .when(deliveryService)
                    .assignCompanyDriver(eq(deliveryId), any(UUID.class));

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/assign-company-driver", deliveryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyJson))

                    // --- Then (검증) ---
                    // 2. [핵심] 'GlobalExceptionHandler'가 409 (Conflict)를 반환하는지 검증
                    .andExpect(status().isConflict())
                    // 3. 응답 Body(JSON)에 정확한 에러 메시지가 포함되었는지 검증
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andDo(print());
        }

        @Test
        @DisplayName("[API 테스트/실패] 존재하지 않는 배송 ID로 배정 요청 시 404 Not Found 반환")
        void assignCompanyDriver_FailsWhen_DeliveryIdNotFound_Returns404() throws Exception {

            // --- Given (준비) ---
            UUID nonExistentDeliveryId = UUID.randomUUID();
            CompanyDriverAssignRequest requestDto = new CompanyDriverAssignRequest(UUID.randomUUID());
            String requestBodyJson = objectMapper.writeValueAsString(requestDto);
            String errorMessage = "배송을(를) 찾을 수 없습니다.";

            // 1. [Mocking] '가짜' 서비스가 'EntityNotFoundException'을 던지도록 설정
            doThrow(new EntityNotFoundException(errorMessage))
                    .when(deliveryService)
                    .assignCompanyDriver(eq(nonExistentDeliveryId), any(UUID.class));

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/assign-company-driver", nonExistentDeliveryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyJson))

                    // --- Then (검증) ---
                    // 2. [핵심] 'GlobalExceptionHandler'가 404 (Not Found)를 반환하는지 검증
                    .andExpect(status().isNotFound())
                    // 3. 응답 Body(JSON)에 정확한 에러 메시지가 포함되었는지 검증
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andDo(print());
        }
    } // End of AssignCompanyDriverTests

    // -----------------------------------------------------------------
    // [TDD] API 테스트: Flow 3-2 (startCompanyDelivery)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/deliveries/{id}/start-company-delivery (최종 배송 시작)")
    class StartCompanyDeliveryTests {

        @Test
        @DisplayName("[API 테스트/성공] 정상적인 최종 배송 '시작' 요청 시 200 OK 반환")
        void startCompanyDelivery_Success_Returns200Ok() throws Exception {

            // --- Given (준비) ---
            UUID deliveryId = UUID.randomUUID();
            doNothing().when(deliveryService).startCompanyDelivery(deliveryId);

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/start-company-delivery", deliveryId)
                            .contentType(MediaType.APPLICATION_JSON))

                    // --- Then (검증) ---
                    .andExpect(status().isOk())
                    .andDo(print());

            verify(deliveryService, times(1)).startCompanyDelivery(deliveryId);
        }

        @Test
        @DisplayName("[API 테스트/실패] 담당자가 배정되지 않았는데 '시작' 요청 시 409 Conflict 반환")
        void startCompanyDelivery_FailsWhen_DriverNotAssigned_Returns409Conflict() throws Exception {

            // --- Given (준비) ---
            UUID deliveryId = UUID.randomUUID();
            String errorMessage = "최종 배송 담당자가 배정되지 않아 배송을 시작할 수 없습니다.";

            doThrow(new IllegalStateException(errorMessage))
                    .when(deliveryService)
                    .startCompanyDelivery(deliveryId);

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/start-company-delivery", deliveryId)
                            .contentType(MediaType.APPLICATION_JSON))

                    // --- Then (검증) ---
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andDo(print());
        }
    } // End of StartCompanyDeliveryTests

    // -----------------------------------------------------------------
    // [TDD] API 테스트: Flow 3-3 (completeDelivery)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/deliveries/{id}/complete-delivery (최종 배송 완료)")
    class CompleteDeliveryTests {

        @Test
        @DisplayName("[API 테스트/성공] 정상적인 최종 배송 '완료' 요청 시 200 OK 반환")
        void completeDelivery_Success_Returns200Ok() throws Exception {

            // --- Given (준비) ---
            UUID deliveryId = UUID.randomUUID();
            doNothing().when(deliveryService).completeDelivery(deliveryId);

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/complete-delivery", deliveryId)
                            .contentType(MediaType.APPLICATION_JSON))

                    // --- Then (검증) ---
                    .andExpect(status().isOk())
                    .andDo(print());

            verify(deliveryService, times(1)).completeDelivery(deliveryId);
        }

        @Test
        @DisplayName("[API 테스트/실패] '이동중' 상태가 아닌데 '완료' 요청 시 409 Conflict 반환")
        void completeDelivery_FailsWhen_StatusNotDelivering_Returns409Conflict() throws Exception {

            // --- Given (준비) ---
            UUID deliveryId = UUID.randomUUID();
            String errorMessage = "현재 '업체 이동중(COMPANY_DELIVERING)' 상태인 배송만 완료할 수 있습니다.";

            doThrow(new IllegalStateException(errorMessage))
                    .when(deliveryService)
                    .completeDelivery(deliveryId);

            // --- When (실행) ---
            mockMvc.perform(put("/api/v1/deliveries/{id}/complete-delivery", deliveryId)
                            .contentType(MediaType.APPLICATION_JSON))

                    // --- Then (검증) ---
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andDo(print());
        }
    } // End of CompleteDeliveryTests
}
