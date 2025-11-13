package com.example.sparta.hub_service.hub.presentation;

import com.example.sparta.hub_service.hub.application.HubService;
import com.example.sparta.hub_service.hub.application.command.CreateHubCommand;
import com.example.sparta.hub_service.hub.application.command.UpdateHubCommand;
import com.example.sparta.hub_service.hub.application.dto.HubSearchCondition;
import com.example.sparta.hub_service.hub.presentation.request.CreateHubRequest;
import com.example.sparta.hub_service.hub.presentation.request.UpdateHubRequest;
import com.example.sparta.hub_service.hub.presentation.response.HubCreateResponse;
import com.example.sparta.hub_service.hub.presentation.response.HubDetailResponse;
import com.example.sparta.hub_service.hub.presentation.response.HubSearchResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    @PostMapping
    public ResponseEntity<HubCreateResponse> createHub(
        @Valid @RequestBody CreateHubRequest request
    ) {
        CreateHubCommand command = new CreateHubCommand(
            request.code(),
            request.name(),
            request.address(),
            request.latitude(),
            request.longitude()
        );

        UUID hubId = hubService.createHub(command);

        HubCreateResponse response = new HubCreateResponse(
            hubId,
            "허브가 성공적으로 생성되었습니다."
        );

        URI location = URI.create("/hubs/" + hubId);

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("{hubId}")
    public ResponseEntity<HubDetailResponse> getHub(@PathVariable UUID hubId) {
        HubDetailResponse response = HubDetailResponse.from(
            hubService.getHub(hubId)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<HubDetailResponse>> getHubs() {
        List<HubDetailResponse> response = hubService
            .getHubs()
            .stream()
            .map(HubDetailResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{hubId}")
    public ResponseEntity<Void> updateHub(
        @PathVariable UUID hubId,
        @RequestBody UpdateHubRequest request
    ) {
        UpdateHubCommand command = new UpdateHubCommand(
            request.code(),
            request.name(),
            request.address(),
            request.status(),
            request.latitude(),
            request.longitude()
        );

        hubService.updateHubService(hubId, command);

        return ResponseEntity.ok().build();
    }

    /// TODO 유저 ID 받아오기
    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(
        @PathVariable UUID hubId,
        Long userId
    ) {
        userId = 1L;

        hubService.deleteHub(hubId, userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HubSearchResponse>> searchHubs(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String address,
        Pageable pageable
    ) {
        HubSearchCondition condition = HubSearchCondition.of(
            name,
            code,
            status,
            address
        );

        Page<HubSearchResponse> responses = hubService
            .searchHubs(condition, pageable)
            .map(HubSearchResponse::from);

        return ResponseEntity.ok(responses);
    }
}
