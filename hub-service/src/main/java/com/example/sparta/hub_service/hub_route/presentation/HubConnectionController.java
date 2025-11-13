package com.example.sparta.hub_service.hub_route.presentation;

import com.example.sparta.hub_service.hub_route.application.HubConnectionService;
import com.example.sparta.hub_service.hub_route.application.command.HubConnectionCommand;
import com.example.sparta.hub_service.hub_route.application.command.UpdateHubConnectionCommand;
import com.example.sparta.hub_service.hub_route.application.dto.HubConnectionResult;
import com.example.sparta.hub_service.hub_route.presentation.request.HubConnectionRequest;
import com.example.sparta.hub_service.hub_route.presentation.request.UpdateHubConnectionRequest;
import com.example.sparta.hub_service.hub_route.presentation.response.HubConnectionResponse;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hub-connections")
@RequiredArgsConstructor
public class HubConnectionController {

    private final HubConnectionService hubConnectionService;

    @GetMapping
    public ResponseEntity<List<HubConnectionResponse>> getConnections() {

        List<HubConnectionResponse> response = hubConnectionService.getConnections().stream()
            .map(HubConnectionResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hubConnectionId}")
    public ResponseEntity<HubConnectionResponse> getConnection(
        @PathVariable UUID hubConnectionId
    ) {
        HubConnectionResult result = hubConnectionService.getConnection(hubConnectionId);
        return ResponseEntity.ok(HubConnectionResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<UUID> createConnection(@RequestBody HubConnectionRequest request) {

        HubConnectionCommand command = HubConnectionCommand.from(request);

        UUID hubConnectionId = hubConnectionService.createConnection(command);

        URI location = URI.create("/hub-connections/" + hubConnectionId);

        return ResponseEntity.created(location).body(hubConnectionId);
    }

    @PutMapping("/{hubConnectionId}")
    public ResponseEntity<Void> updateConnection(@PathVariable UUID hubConnectionId, @RequestBody UpdateHubConnectionRequest request) {

        UpdateHubConnectionCommand command = UpdateHubConnectionCommand.from(request);

        hubConnectionService.updateConnection(hubConnectionId, command);

        return ResponseEntity.ok().build();
    }

    /// TODO 유저 ID 받아오기
    @DeleteMapping("/{hubConnectionId}")
    public ResponseEntity<Void> deleteHubConnection(
        @PathVariable UUID hubConnectionId,
        Long userId
    ) {
        userId = 1L;

        hubConnectionService.deleteHubConnection(hubConnectionId, userId);

        return ResponseEntity.noContent().build();
    }

}
