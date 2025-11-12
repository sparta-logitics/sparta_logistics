package com.example.sparta.hub_service.hub_routes;

import com.example.sparta.hub_service.core.vo.HubId;
import com.example.sparta.hub_service.hub_routes.dto.HubRouteResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {

    private final HubRouteService hubRouteService;

    @GetMapping
    public ResponseEntity<HubRouteResponse> getRoute(
        @RequestParam UUID departureHubId,
        @RequestParam UUID arrivalHubId
    ) {
        HubRouteResponse response = HubRouteResponse.from(
            hubRouteService.getOrComputeRoute(
                HubId.of(departureHubId),
                HubId.of(arrivalHubId)
            ));

        return ResponseEntity.ok(response);
    }
}
