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
@RequestMapping("/api/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubService hubService;

    @PostMapping("/areas/{areaId}/hubs")
    public ApiResponse<HubResponse> createHub(@PathVariable Long areaId,
                                              @RequestBody CreateHubRequest request) {
        return ApiResponse.ok("Tạo Hub thành công", hubService.createHub(areaId, request));
    }

    @GetMapping("/areas/{areaId}/hubs")
    public ApiResponse<List<HubResponse>> getHubsByArea(@PathVariable Long areaId) {
        return ApiResponse.ok("Lấy danh sách Hub thành công", hubService.getHubsByArea(areaId));
    }

    @PutMapping("/hubs/{hubId}")
    public ApiResponse<HubResponse> updateHub(@PathVariable Long hubId,
                                              @RequestBody UpdateHubRequest request) {
        return ApiResponse.ok("Cập nhật Hub thành công", hubService.updateHub(hubId, request));
    }

    @DeleteMapping("/hubs/{hubId}")
    public ApiResponse<Void> deleteHub(@PathVariable Long hubId) {
        hubService.deleteHub(hubId);
        return ApiResponse.ok("Xoá Hub thành công", null);
    }

    // LIST ACTIVE
    @GetMapping("/areas/{areaId}/hubs/active")
    public ApiResponse<List<HubResponse>> getHubsActive(@PathVariable Long areaId) {
        return ApiResponse.ok("Lấy danh sách Hub Active", hubService.getHubsByAreaActive(areaId));
    }

    // LIST INACTIVE
    @GetMapping("/areas/{areaId}/hubs/inactive")
    public ApiResponse<List<HubResponse>> getHubsInactive(@PathVariable Long areaId) {
        return ApiResponse.ok("Lấy danh sách Hub Inactive", hubService.getHubsByAreaInactive(areaId));
    }

    // RESTORE
    @PatchMapping("/hubs/{hubId}/activate")
    public ApiResponse<HubResponse> activateHub(@PathVariable Long hubId) {
        return ApiResponse.ok("Kích hoạt lại Hub thành công", hubService.activateHub(hubId));
    }

    @GetMapping("/areas/{areaId}/hubs/search")
    public ApiResponse<List<HubResponse>> searchHubs(
            @PathVariable Long areaId,
            @RequestParam String keyword,
            @RequestParam(required = false) StatusFilter status
    ) {
        return ApiResponse.ok("Search Hub", hubService.searchHubs(areaId, keyword, status));
    }


}
