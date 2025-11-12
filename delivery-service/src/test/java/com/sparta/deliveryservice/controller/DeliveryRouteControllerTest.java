package com.sparta.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.deliveryservice.controller.dto.request.DriverAssignRequest;
import com.sparta.deliveryservice.controller.dto.request.RouteCompleteRequest;
import com.sparta.deliveryservice.exception.EntityNotFoundException;
import com.sparta.deliveryservice.exception.GlobalExceptionHandler;
import com.sparta.deliveryservice.service.DeliveryRouteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [TDD] API 계층 슬라이스 테스트
 * @WebMvcTest: 'DeliveryRouteController'만 로드합니다.
 * (@Service, @Repository 등을 로드하지 않습니다)
 */
@Import(GlobalExceptionHandler.class)
@WebMvcTest(controllers = DeliveryRouteController.class)
@ContextConfiguration(classes = DeliveryRouteControllerTestConfig.class)
public class DeliveryRouteControllerTest {

    @Autowired
    private MockMvc mockMvc; // 1. HTTP 요청을 흉내 내는 객체

    @Autowired
    private ObjectMapper objectMapper; // 2. Java DTO <-> JSON 문자열 변환기

    // 3. [핵심] @MockBean: 컨트롤러가 의존하는 '서비스'를 "가짜 Bean"으로 등록
    // 이것이 없으면 @WebMvcTest는 @Autowired할 DeliveryRouteService를 찾지 못해 실패합니다.
    @MockitoBean
    private DeliveryRouteService deliveryRouteService;

    @Test
    @DisplayName("[API 테스트/성공] Flow 2-1: 담당자 배정 API 호출 시 200 OK 반환")
    void assignDriver_Success_Returns200Ok() throws Exception {

        // --- Given (준비) ---
        // 1. HTTP 요청에 필요한 PathVariable과 RequestBody 준비
        UUID routeHistoryId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        DriverAssignRequest requestDto = new DriverAssignRequest(driverId);
        String requestBodyJson = objectMapper.writeValueAsString(requestDto);

        // 2. [Mocking] '가짜' 서비스가 정상적으로 동작하도록 설정
        // (void 메서드이므로 doNoting() 사용)
        doNothing().when(deliveryRouteService).assignDriver(routeHistoryId, driverId);

        // --- When (실행) ---
        // 3. MockMvc로 'PUT /api/v1/delivery-routes/{id}/assign-driver' 요청을 보냄
        mockMvc.perform(put("/api/v1/delivery-routes/{id}/assign-driver", routeHistoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJson))

        // --- Then (검증) ---
                .andExpect(status().isOk()) // 4. HTTP 200 (OK) 응답을 기대
                .andDo(print()); // 5. (디버깅용) 요청/응답 내용 출력

        // 6. [검증] '가짜' 서비스의 'assignDriver' 메서드가 정확히 1번 호출되었는지 확인
        verify(deliveryRouteService, times(1)).assignDriver(routeHistoryId, driverId);
    }

    @Test
    @DisplayName("[API 테스트/실패] Flow 2-1: 존재하지 않는 경로 ID로 배정 시 404 Not Found 반환 (ExceptionHandler 테스트)")
    void assignDriver_FailsWhen_RouteIdNotFound_Returns404() throws Exception {

        // --- Given (준비) ---
        // 1. 존재하지 않는 ID와 RequestBody 준비
        UUID nonExistentRouteId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        DriverAssignRequest requestDto = new DriverAssignRequest(driverId);
        String requestBodyJson = objectMapper.writeValueAsString(requestDto);
        String errorMessage = "배송 경로를 찾을 수 없습니다.";

        // 2, [Mocking] '가짜' 서비스가 'EntityNotFoundException'을 던지도록 설정
        doThrow(new EntityNotFoundException(errorMessage))
                .when(deliveryRouteService)
                .assignDriver(nonExistentRouteId, driverId);

        // 3. MockMvc로 실패할 요청을 보냄
        mockMvc.perform(put("/api/v1/delivery-routes/{id}/assign-driver", nonExistentRouteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyJson))

        // --- Then (검증) ---
        // 4. [핵심] 'GlobalExceptionHandler'가 예외를 가로채
        // HTTP 404 (Not Found)를 반환하는지 검증
                .andExpect(status().isNotFound())
                // 5. [핵심] 응답 Body(JSON)에 정확한 에러 메시지가 포함되었는지 검증
                .andExpect(jsonPath("$.message").value(errorMessage))
                        .andDo(print());

        // 6. [검증] '가짜' 서비스가 1번 호출되었는지 확인
        verify(deliveryRouteService, times(1)).assignDriver(nonExistentRouteId, driverId);
    }

    // -----------------------------------------------------------------
    // [TDD] API 테스트: Flow 2-2 (startRoute)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/delivery-routes/{id}/start (배송 시작)")
    class StartRouteTests {

        @Test
        @DisplayName("[API 테스트/성공] 정상적인 경로 '시작' 요청 시 200 OK 반환")
        void startRoute_Success_Returns200Ok() throws Exception {

            // --- Given (준비) ---
            UUID routeHistoryId = UUID.randomUUID();

            // 1. [Mocking] '가짜' 서비스가 정상적으로 동작하도록 설정
            doNothing().when(deliveryRouteService).startRoute(routeHistoryId);


            // --- When (실행) ---
            // 2. MockMvc로 'PUT /api/v1/delivery-routes/{id}/start' 요청을 보냄
            mockMvc.perform(put("/api/v1/delivery-routes/{id}/start", routeHistoryId)
                            .contentType(MediaType.APPLICATION_JSON))

                    // --- Then (검증) ---
                    .andExpect(status().isOk()) // 3. HTTP 200 (OK) 응답을 기대
                    .andDo(print());

            // 4. [검증] '가짜' 서비스의 'startRoute' 메서드가 1번 호출되었는지 확인
            verify(deliveryRouteService, times(1)).startRoute(routeHistoryId);
        }

        @Test
        @DisplayName("[API 테스트/실패] 담당자가 배정되지 않았는데 '시작' 요청 시 409 Conflict 반환")
        void startRoute_FailsWhen_DriverNotAssigned_Returns409Conflict() throws Exception {

            // --- Given (준비) ---
            UUID routeHistoryId = UUID.randomUUID();
            String errorMessage = "담당자가 배정되지 않은 경로는 시작할 수 없습니다.";

            // 1. [Mocking] '가짜' 서비스가 'IllegalStateException'을 던지도록 설정
            doThrow(new IllegalStateException(errorMessage))
                    .when(deliveryRouteService)
                    .startRoute(routeHistoryId);


            // --- When (실행) ---
            // 2. MockMvc로 실패할 요청을 보냄
            mockMvc.perform(put("/api/v1/delivery-routes/{id}/start", routeHistoryId)
                            .contentType(MediaType.APPLICATION_JSON))

                    // --- Then (검증) ---
                    // 3. [핵심] 'GlobalExceptionHandler'가 409 (Conflict)를 반환하는지 검증
                    .andExpect(status().isConflict())
                    // 4. 응답 Body(JSON)에 정확한 에러 메시지가 포함되었는지 검증
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andDo(print());
        }
    } // End of StartRouteTests

    // -----------------------------------------------------------------
    // [TDD] API 테스트: Flow 2-3 (completeRoute)
    // -----------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/delivery-routes/{id}/complete (허브 도착)")
    class CompleteRouteTests {

        @Test
        @DisplayName("[API 테스트/성공] 정상적인 경로 '완료' 요청 시 200 OK 반환")
        void completeRoute_Success_Returns200Ok() throws Exception {

            // --- Given (준비) ---
            UUID routeHistoryId = UUID.randomUUID();
            // 1. HTTP Request Body 준비
            RouteCompleteRequest requestDto = new RouteCompleteRequest(150.5, 180);
            String requestBodyJson = objectMapper.writeValueAsString(requestDto);

            // 2. [Mocking] '가짜' 서비스가 정상적으로 동작하도록 설정
            doNothing().when(deliveryRouteService).completeRoute(
                    routeHistoryId,
                    requestDto.getActualDistance(),
                    requestDto.getActualDuration()
            );


            // --- When (실행) ---
            // 3. MockMvc로 'PUT /api/v1/delivery-routes/{id}/complete' 요청을 보냄
            mockMvc.perform(put("/api/v1/delivery-routes/{id}/complete", routeHistoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyJson))

                    // --- Then (검증) ---
                    .andExpect(status().isOk()) // 4. HTTP 200 (OK) 응답을 기대
                    .andDo(print());

            // 5. [검증] '가짜' 서비스의 'completeRoute'가 1번 호출되었는지 확인
            verify(deliveryRouteService, times(1)).completeRoute(
                    routeHistoryId,
                    requestDto.getActualDistance(),
                    requestDto.getActualDuration()
            );
        }

        @Test
        @DisplayName("[API 테스트/실패] 아직 '이동 중'이 아닌 경로를 '완료' 요청 시 409 Conflict 반환")
        void completeRoute_FailsWhen_StatusNotValid_Returns409Conflict() throws Exception {

            // --- Given (준비) ---
            UUID routeHistoryId = UUID.randomUUID();
            RouteCompleteRequest requestDto = new RouteCompleteRequest(150.5, 180);
            String requestBodyJson = objectMapper.writeValueAsString(requestDto);
            String errorMessage = "현재 '이동 중(IN_TRANSIT)' 상태인 경로만 완료할 수 있습니다.";

            // 1. [Mocking] '가짜' 서비스가 'IllegalStateException'을 던지도록 설정
            doThrow(new IllegalStateException(errorMessage))
                    .when(deliveryRouteService)
                    .completeRoute(
                            routeHistoryId,
                            requestDto.getActualDistance(),
                            requestDto.getActualDuration()
                    );


            // --- When (실행) ---
            // 2. MockMvc로 실패할 요청을 보냄
            mockMvc.perform(put("/api/v1/delivery-routes/{id}/complete", routeHistoryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBodyJson))

                    // --- Then (검증) ---
                    // 3. 'GlobalExceptionHandler'가 409 (Conflict)를 반환하는지 검증
                    .andExpect(status().isConflict())
                    // 4. 응답 Body(JSON)에 정확한 에러 메시지가 포함되었는지 검증
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andDo(print());
        }
    } // End of CompleteRouteTests

} // End of DeliveryRouteControllerTest