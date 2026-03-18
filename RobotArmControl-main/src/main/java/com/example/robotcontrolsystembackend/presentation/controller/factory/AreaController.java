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
@RequestMapping("/api")  // Fixed: was "/api/areas" causing path duplication
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    // POST /api/factories/{factoryId}/areas
    @PostMapping("/factories/{factoryId}/areas")
    public ApiResponse<AreaResponse> createArea(@PathVariable Long factoryId,
                                                @RequestBody CreateAreaRequest request) {
        return ApiResponse.ok("Tạo Area thành công", areaService.createArea(factoryId, request));
    }

    // GET /api/factories/{factoryId}/areas?search=&status=ACTIVE|INACTIVE|ALL
    @GetMapping("/factories/{factoryId}/areas")
    public ApiResponse<List<AreaResponse>> getAreasByFactory(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatusFilter status) {
        if (search != null && !search.isEmpty()) {
            return ApiResponse.ok("Lấy danh sách Area thành công", areaService.searchAreas(factoryId, search, status));
        }
        if (status == StatusFilter.ACTIVE) {
            return ApiResponse.ok("Lấy danh sách Area thành công", areaService.getAreasByFactoryActive(factoryId));
        }
        if (status == StatusFilter.INACTIVE) {
            return ApiResponse.ok("Lấy danh sách Area thành công", areaService.getAreasByFactoryInactive(factoryId));
        }
        return ApiResponse.ok("Lấy danh sách Area thành công", areaService.getAreasByFactory(factoryId));
    }

    // PUT /api/areas/{areaId}
    @PutMapping("/areas/{areaId}")
    public ApiResponse<AreaResponse> updateArea(@PathVariable Long areaId,
                                                @RequestBody UpdateAreaRequest request) {
        return ApiResponse.ok("Cập nhật Area thành công", areaService.updateArea(areaId, request));
    }

    // DELETE /api/areas/{areaId}
    @DeleteMapping("/areas/{areaId}")
    public ApiResponse<Void> deleteArea(@PathVariable Long areaId) {
        areaService.deleteArea(areaId);
        return ApiResponse.ok("Xoá Area thành công", null);
    }

    // PATCH /api/areas/{areaId}/status  — replaces /activate verb
    @PatchMapping("/areas/{areaId}/status")
    public ApiResponse<AreaResponse> updateAreaStatus(@PathVariable Long areaId) {
        return ApiResponse.ok("Kích hoạt lại Area thành công", areaService.activateArea(areaId));
    }
}
