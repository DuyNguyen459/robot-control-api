package com.example.robotcontrolsystembackend.presentation.controller.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateAreaRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateAreaRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.AreaResponse;
import com.example.robotcontrolsystembackend.application.service.factory.AreaService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @PostMapping("/factories/{factoryId}/areas")
    public ApiResponse<AreaResponse> createArea(@PathVariable Long factoryId,
                                                @RequestBody CreateAreaRequest request) {
        return ApiResponse.ok("Tạo Area thành công", areaService.createArea(factoryId, request));
    }

    @GetMapping("/factories/{factoryId}/areas")
    public ApiResponse<List<AreaResponse>> getAreasByFactory(@PathVariable Long factoryId) {
        return ApiResponse.ok("Lấy danh sách Area thành công", areaService.getAreasByFactory(factoryId));
    }

    @PutMapping("/areas/{areaId}")
    public ApiResponse<AreaResponse> updateArea(@PathVariable Long areaId,
                                                @RequestBody UpdateAreaRequest request) {
        return ApiResponse.ok("Cập nhật Area thành công", areaService.updateArea(areaId, request));
    }

    @DeleteMapping("/areas/{areaId}")
    public ApiResponse<Void> deleteArea(@PathVariable Long areaId) {
        areaService.deleteArea(areaId);
        return ApiResponse.ok("Xoá Area thành công", null);
    }

    // LIST ACTIVE
    @GetMapping("/factories/{factoryId}/areas/active")
    public ApiResponse<List<AreaResponse>> getAreasActive(@PathVariable Long factoryId) {
        return ApiResponse.ok("Lấy danh sách Area Active", areaService.getAreasByFactoryActive(factoryId));
    }

    // LIST INACTIVE
    @GetMapping("/factories/{factoryId}/areas/inactive")
    public ApiResponse<List<AreaResponse>> getAreasInactive(@PathVariable Long factoryId) {
        return ApiResponse.ok("Lấy danh sách Area Inactive", areaService.getAreasByFactoryInactive(factoryId));
    }

    // RESTORE
    @PatchMapping("/areas/{areaId}/activate")
    public ApiResponse<AreaResponse> activateArea(@PathVariable Long areaId) {
        return ApiResponse.ok("Kích hoạt lại Area thành công", areaService.activateArea(areaId));
    }

    @GetMapping("/factories/{factoryId}/areas/search")
    public ApiResponse<List<AreaResponse>> searchAreas(
            @PathVariable Long factoryId,
            @RequestParam String keyword,
            @RequestParam(required = false) StatusFilter status
    ) {
        return ApiResponse.ok("Search Area", areaService.searchAreas(factoryId, keyword, status));
    }


}
