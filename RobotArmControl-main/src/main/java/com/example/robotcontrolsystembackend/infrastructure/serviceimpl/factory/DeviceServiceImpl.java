package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateDeviceRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateDeviceRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.DeviceResponse;
import com.example.robotcontrolsystembackend.application.service.factory.DeviceService;
import com.example.robotcontrolsystembackend.common.exception.AppException;
import com.example.robotcontrolsystembackend.common.exception.ErrorCode;
import com.example.robotcontrolsystembackend.domain.enumtype.DeviceStatus;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;
import com.example.robotcontrolsystembackend.domain.model.Device;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.DeviceRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final HubRepository hubRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public DeviceResponse createDevice(Long hubId, CreateDeviceRequest request) {
        // 1) Validate hubId
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }

        // 2) Validate request
        if (request == null || request.getDeviceName() == null || request.getDeviceName().trim().isEmpty()) {
            throw new AppException(ErrorCode.DEVICE_NAME_REQUIRED, "device_name không được rỗng");
        }
        if (request.getDeviceType() == null) {
            throw new AppException(ErrorCode.DEVICE_TYPE_REQUIRED, "device_type không được null");
        }
        if (request.getConnectionType() == null) {
            throw new AppException(ErrorCode.CONNECTION_TYPE_REQUIRED, "connection_type không được null");
        }

        // 3) Check Hub exists
        if (!hubRepository.existsById(hubId)) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId);
        }

        // 4) Check serial number unique (nếu có)
        if (request.getSerialNumber() != null && !request.getSerialNumber().trim().isEmpty()) {
            if (deviceRepository.existsBySerialNumber(request.getSerialNumber().trim())) {
                throw new AppException(ErrorCode.SERIAL_NUMBER_ALREADY_EXISTS, 
                        "Serial number đã tồn tại: " + request.getSerialNumber());
            }
        }

        // 5) Build entity
        Device device = Device.builder()
                .hubId(hubId)
                .deviceName(request.getDeviceName().trim())
                .deviceType(request.getDeviceType())
                .robotType(request.getRobotType())
                .model(request.getModel() == null ? null : request.getModel().trim())
                .serialNumber(request.getSerialNumber() == null ? null : request.getSerialNumber().trim())
                .connectionType(request.getConnectionType())
                .deviceStatus(DeviceStatus.Active) // default
                .build();

        // 6) Save
        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse updateDevice(Long deviceId, UpdateDeviceRequest request) {
        if (deviceId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "device_id không được null");
        }
        if (request == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "request không được null");
        }
        if (request.getDeviceName() == null || request.getDeviceName().trim().isEmpty()) {
            throw new AppException(ErrorCode.DEVICE_NAME_REQUIRED, "device_name không được rỗng");
        }
        if (request.getDeviceType() == null) {
            throw new AppException(ErrorCode.DEVICE_TYPE_REQUIRED, "device_type không được null");
        }
        if (request.getConnectionType() == null) {
            throw new AppException(ErrorCode.CONNECTION_TYPE_REQUIRED, "connection_type không được null");
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Không tìm thấy Device: " + deviceId));

        // Check serial number unique (nếu thay đổi)
        if (request.getSerialNumber() != null && !request.getSerialNumber().trim().isEmpty()) {
            String newSerial = request.getSerialNumber().trim();
            if (!newSerial.equals(device.getSerialNumber()) && deviceRepository.existsBySerialNumber(newSerial)) {
                throw new AppException(ErrorCode.SERIAL_NUMBER_ALREADY_EXISTS, 
                        "Serial number đã tồn tại: " + newSerial);
            }
            device.setSerialNumber(newSerial);
        }

        device.setDeviceName(request.getDeviceName().trim());
        device.setDeviceType(request.getDeviceType());
        device.setRobotType(request.getRobotType());
        device.setModel(request.getModel() == null ? null : request.getModel().trim());
        device.setConnectionType(request.getConnectionType());

        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDevice(Long deviceId) {
        if (deviceId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "device_id không được null");
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Không tìm thấy Device: " + deviceId));

        // Soft delete
        device.setDeviceStatus(DeviceStatus.NotActive);
        deviceRepository.save(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByHub(Long hubId) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }
        if (!hubRepository.existsById(hubId)) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId);
        }

        return deviceRepository.findByHubIdOrderByDeviceIdAsc(hubId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByHubActive(Long hubId) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }
        if (!hubRepository.existsById(hubId)) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId);
        }

        return deviceRepository
                .findByHubIdAndDeviceStatusOrderByDeviceIdAsc(hubId, DeviceStatus.Active)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByHubNotActive(Long hubId) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }
        if (!hubRepository.existsById(hubId)) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId);
        }

        return deviceRepository
                .findByHubIdAndDeviceStatusOrderByDeviceIdAsc(hubId, DeviceStatus.NotActive)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeviceResponse activateDevice(Long deviceId) {
        if (deviceId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "device_id không được null");
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Không tìm thấy Device: " + deviceId));

        device.setDeviceStatus(DeviceStatus.Active);
        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> searchDevices(Long hubId, String keyword, StatusFilter status) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }
        if (!hubRepository.existsById(hubId)) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId);
        }

        List<Device> devices = deviceRepository.findByHubIdOrderByDeviceIdAsc(hubId);

        // Filter by keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            devices = devices.stream()
                    .filter(d -> d.getDeviceName().toLowerCase().contains(kw) ||
                            (d.getModel() != null && d.getModel().toLowerCase().contains(kw)) ||
                            (d.getSerialNumber() != null && d.getSerialNumber().toLowerCase().contains(kw)))
                    .toList();
        }

        // Filter by status
        if (status != null) {
            devices = switch (status) {
                case ACTIVE -> devices.stream()
                        .filter(d -> d.getDeviceStatus() == DeviceStatus.Active)
                        .toList();
                case INACTIVE -> devices.stream()
                        .filter(d -> d.getDeviceStatus() == DeviceStatus.NotActive)
                        .toList();
                case ALL -> devices;
            };
        }

        return devices.stream().map(this::toResponse).toList();
    }

    private DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .hubId(device.getHubId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .robotType(device.getRobotType())
                .model(device.getModel())
                .serialNumber(device.getSerialNumber())
                .connectionType(device.getConnectionType())
                .deviceStatus(device.getDeviceStatus())
                .build();
    }
}
