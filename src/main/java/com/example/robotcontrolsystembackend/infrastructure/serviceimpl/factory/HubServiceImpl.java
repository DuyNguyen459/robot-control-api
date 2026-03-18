package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.factory;

import com.example.robotcontrolsystembackend.application.service.factory.HubService;
import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateHubRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateHubRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.HubResponse;
import com.example.robotcontrolsystembackend.common.exception.AppException;
import com.example.robotcontrolsystembackend.common.exception.ErrorCode;
import com.example.robotcontrolsystembackend.domain.enumtype.HubStatus;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;
import com.example.robotcontrolsystembackend.domain.model.Area;
import com.example.robotcontrolsystembackend.domain.model.Hub;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.AreaRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.DeviceRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {

    private final AreaRepository areaRepository;
    private final HubRepository hubRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public HubResponse createHub(Long areaId, CreateHubRequest request) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }
        if (request == null || request.getHubName() == null || request.getHubName().trim().isEmpty()) {
            throw new AppException(ErrorCode.HUB_NAME_REQUIRED, "hub_name không được rỗng");
        }

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId));

        Hub hub = Hub.builder()
                .areaId(areaId)
                .hubName(request.getHubName().trim())
                .hubDescription(request.getHubDescription() == null ? null : request.getHubDescription().trim())
                .hubStatus(HubStatus.Active) // default theo DB
                .build();

        Hub saved = hubRepository.save(hub);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public HubResponse updateHub(Long hubId, UpdateHubRequest request) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }
        if (request == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "request không được null");
        }
        if (request.getHubName() == null || request.getHubName().trim().isEmpty()) {
            throw new AppException(ErrorCode.HUB_NAME_REQUIRED, "hub_name không được rỗng");
        }

        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId));

        hub.setHubName(request.getHubName().trim());
        hub.setHubDescription(request.getHubDescription() == null ? null : request.getHubDescription().trim());

        // KHÔNG sửa hubStatus ở đây

        Hub saved = hubRepository.save(hub);
        return toResponse(saved);
    }


    @Override
    @Transactional
    public void deleteHub(Long hubId) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }

        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId));

        // Rule: chỉ cho "xóa mềm" khi không còn Device trực thuộc
        if (deviceRepository.existsByHubId(hubId)) {
            throw new AppException(ErrorCode.HUB_HAS_DEVICES, "Không thể xoá Hub vì vẫn còn Device trực thuộc");
        }

        // Soft delete
        hub.setHubStatus(HubStatus.Inactive);
        hubRepository.save(hub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubResponse> getHubsByArea(Long areaId) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }

        // Optional: check area tồn tại để trả 404 rõ ràng
        if (!areaRepository.existsById(areaId)) {
            throw new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId);
        }

        return hubRepository.findByAreaIdOrderByHubIdAsc(areaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<HubResponse> getHubsByAreaActive(Long areaId) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }
        if (!areaRepository.existsById(areaId)) {
            throw new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId);
        }

        return hubRepository
                .findByAreaIdAndHubStatusOrderByHubIdAsc(areaId, HubStatus.Active)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubResponse> getHubsByAreaInactive(Long areaId) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }
        if (!areaRepository.existsById(areaId)) {
            throw new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId);
        }

        return hubRepository
                .findByAreaIdAndHubStatusOrderByHubIdAsc(areaId, HubStatus.Inactive)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public HubResponse activateHub(Long hubId) {
        if (hubId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "hub_id không được null");
        }

        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND, "Không tìm thấy Hub: " + hubId));

        hub.setHubStatus(HubStatus.Active);

        Hub saved = hubRepository.save(hub);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubResponse> searchHubs(Long areaId, String keyword, StatusFilter status) {

        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "keyword không được rỗng");
        }
        if (!areaRepository.existsById(areaId)) {
            throw new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId);
        }

        String kw = keyword.trim();
        StatusFilter filter = (status == null) ? StatusFilter.ALL : status;

        List<Hub> hubs = switch (filter) {
            case ALL -> hubRepository.searchByAreaIdAndName(areaId, kw);
            case ACTIVE -> hubRepository.searchByAreaIdAndStatusAndName(areaId, HubStatus.Active, kw);
            case INACTIVE -> hubRepository.searchByAreaIdAndStatusAndName(areaId, HubStatus.Inactive, kw);
        };

        return hubs.stream().map(this::toResponse).toList();
    }



    private HubResponse toResponse(Hub hub) {
        return HubResponse.builder()
                .hubId(hub.getHubId())
                .areaId(hub.getAreaId())
                .hubName(hub.getHubName())
                .hubDescription(hub.getHubDescription())
                .hubStatus(hub.getHubStatus())
                .build();
    }
}
