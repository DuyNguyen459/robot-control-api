package com.example.robotcontrolsystembackend.application.service.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateDeviceRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateDeviceRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.DeviceResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;

import java.util.List;

public interface DeviceService {
    DeviceResponse createDevice(Long hubId, CreateDeviceRequest request);
    DeviceResponse updateDevice(Long deviceId, UpdateDeviceRequest request);
    void deleteDevice(Long deviceId);
    List<DeviceResponse> getDevicesByHub(Long hubId);
    List<DeviceResponse> getDevicesByHubActive(Long hubId);
    List<DeviceResponse> getDevicesByHubNotActive(Long hubId);
    DeviceResponse activateDevice(Long deviceId);
    List<DeviceResponse> searchDevices(Long hubId, String keyword, StatusFilter status);
}
