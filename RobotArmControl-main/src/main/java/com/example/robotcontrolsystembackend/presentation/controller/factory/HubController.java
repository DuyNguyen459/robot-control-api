package com.example.robotcontrolsystembackend.presentation.controller.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateHubRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateHubRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.HubResponse;
import com.example.robotcontrolsystembackend.application.service.factory.HubService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")  // Fixed: was "/api/hubs" causing path duplication
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    // POST /api/areas/{areaId}/hubs
    @PostMapping("/areas/{areaId}/hubs")
    public ApiResponse<HubResponse> createHub(@PathVariable Long areaId,
                                              @RequestBody CreateHubRequest request) {
        return ApiResponse.ok("Tạo Hub thành công", hubService.createHub(areaId, request));
    }

    // GET /api/areas/{areaId}/hubs?search=&status=ACTIVE|INACTIVE|ALL
    @GetMapping("/areas/{areaId}/hubs")
    public ApiResponse<List<HubResponse>> getHubsByArea(
            @PathVariable Long areaId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatusFilter status) {
        if (search != null && !search.isEmpty()) {
            return ApiResponse.ok("Lấy danh sách Hub thành công", hubService.searchHubs(areaId, search, status));
        }
        if (status == StatusFilter.ACTIVE) {
            return ApiResponse.ok("Lấy danh sách Hub thành công", hubService.getHubsByAreaActive(areaId));
        }
        if (status == StatusFilter.INACTIVE) {
            return ApiResponse.ok("Lấy danh sách Hub thành công", hubService.getHubsByAreaInactive(areaId));
        }
        return ApiResponse.ok("Lấy danh sách Hub thành công", hubService.getHubsByArea(areaId));
    }

    // PUT /api/hubs/{hubId}
    @PutMapping("/hubs/{hubId}")
    public ApiResponse<HubResponse> updateHub(@PathVariable Long hubId,
                                              @RequestBody UpdateHubRequest request) {
        return ApiResponse.ok("Cập nhật Hub thành công", hubService.updateHub(hubId, request));
    }

    // DELETE /api/hubs/{hubId}
    @DeleteMapping("/hubs/{hubId}")
    public ApiResponse<Void> deleteHub(@PathVariable Long hubId) {
        hubService.deleteHub(hubId);
        return ApiResponse.ok("Xoá Hub thành công", null);
    }

    // PATCH /api/hubs/{hubId}/status  — replaces /activate verb
    @PatchMapping("/hubs/{hubId}/status")
    public ApiResponse<HubResponse> updateHubStatus(@PathVariable Long hubId) {
        return ApiResponse.ok("Kích hoạt lại Hub thành công", hubService.activateHub(hubId));
    }
}
