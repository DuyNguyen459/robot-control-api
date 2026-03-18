package com.example.robotcontrolsystembackend.presentation.controller.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateDeviceRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateDeviceRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.DeviceResponse;
import com.example.robotcontrolsystembackend.application.service.factory.DeviceService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")  // Fixed: was "/api/devices" causing path duplication
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // POST /api/hubs/{hubId}/devices
    @PostMapping("/hubs/{hubId}/devices")
    public ApiResponse<DeviceResponse> createDevice(@PathVariable Long hubId,
                                                    @RequestBody CreateDeviceRequest request) {
        return ApiResponse.ok("Tạo Device thành công", deviceService.createDevice(hubId, request));
    }

    // GET /api/hubs/{hubId}/devices?search=&status=ACTIVE|INACTIVE|ALL
    @GetMapping("/hubs/{hubId}/devices")
    public ApiResponse<List<DeviceResponse>> getDevicesByHub(
            @PathVariable Long hubId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatusFilter status) {
        if (search != null && !search.isEmpty()) {
            return ApiResponse.ok("Lấy danh sách Device thành công", deviceService.searchDevices(hubId, search, status));
        }
        if (status == StatusFilter.ACTIVE) {
            return ApiResponse.ok("Lấy danh sách Device thành công", deviceService.getDevicesByHubActive(hubId));
        }
        if (status == StatusFilter.INACTIVE) {
            return ApiResponse.ok("Lấy danh sách Device thành công", deviceService.getDevicesByHubNotActive(hubId));
        }
        return ApiResponse.ok("Lấy danh sách Device thành công", deviceService.getDevicesByHub(hubId));
    }

    // PUT /api/devices/{deviceId}
    @PutMapping("/devices/{deviceId}")
    public ApiResponse<DeviceResponse> updateDevice(@PathVariable Long deviceId,
                                                    @RequestBody UpdateDeviceRequest request) {
        return ApiResponse.ok("Cập nhật Device thành công", deviceService.updateDevice(deviceId, request));
    }

    // DELETE /api/devices/{deviceId}
    @DeleteMapping("/devices/{deviceId}")
    public ApiResponse<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ApiResponse.ok("Xoá Device thành công", null);
    }

    // PATCH /api/devices/{deviceId}/status  — replaces /activate verb
    @PatchMapping("/devices/{deviceId}/status")
    public ApiResponse<DeviceResponse> updateDeviceStatus(@PathVariable Long deviceId) {
        return ApiResponse.ok("Kích hoạt lại Device thành công", deviceService.activateDevice(deviceId));
    }
}
