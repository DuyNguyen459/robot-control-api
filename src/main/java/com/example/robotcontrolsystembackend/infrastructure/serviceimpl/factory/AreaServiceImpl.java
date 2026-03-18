package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.factory;

import com.example.robotcontrolsystembackend.application.service.factory.AreaService;
import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateAreaRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateAreaRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.AreaResponse;
import com.example.robotcontrolsystembackend.common.exception.AppException;
import com.example.robotcontrolsystembackend.common.exception.ErrorCode;
import com.example.robotcontrolsystembackend.domain.enumtype.AreaStatus;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;
import com.example.robotcontrolsystembackend.domain.model.Area;
import com.example.robotcontrolsystembackend.domain.model.Factory;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.AreaRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.FactoryRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final FactoryRepository factoryRepository;
    private final AreaRepository areaRepository;
    private final HubRepository hubRepository;

    @Override
    @Transactional
    public AreaResponse createArea(Long factoryId, CreateAreaRequest request) {
        if (factoryId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "factory_id không được null");
        }
        if (request == null || request.getAreaName() == null || request.getAreaName().trim().isEmpty()) {
            throw new AppException(ErrorCode.AREA_NAME_REQUIRED, "area_name không được rỗng");
        }

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_NOT_FOUND, "Không tìm thấy Factory: " + factoryId));

        Area area = Area.builder()
                .factoryId(factoryId)
                .areaName(request.getAreaName().trim())
                .areaDescription(request.getAreaDescription() == null ? null : request.getAreaDescription().trim())
                .areaStatus(AreaStatus.Active) // default theo DB
                .build();

        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AreaResponse updateArea(Long areaId, UpdateAreaRequest request) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }
        if (request == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "request không được null");
        }
        if (request.getAreaName() == null || request.getAreaName().trim().isEmpty()) {
            throw new AppException(ErrorCode.AREA_NAME_REQUIRED, "area_name không được rỗng");
        }

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId));

        area.setAreaName(request.getAreaName().trim());
        area.setAreaDescription(request.getAreaDescription() == null ? null : request.getAreaDescription().trim());

        // KHÔNG sửa areaStatus ở đây

        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }


    @Override
    @Transactional
    public void deleteArea(Long areaId) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId));

        // Rule: chỉ cho "xóa mềm" khi không còn Hub trực thuộc
        if (hubRepository.existsByAreaId(areaId)) {
            throw new AppException(ErrorCode.AREA_HAS_HUBS, "Không thể xoá Area vì vẫn còn Hub trực thuộc");
        }

        // Soft delete
        area.setAreaStatus(AreaStatus.Inactive);
        areaRepository.save(area);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> getAreasByFactory(Long factoryId) {
        if (factoryId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "factory_id không được null");
        }

        // Optional: check factory tồn tại để trả 404 rõ ràng
        if (!factoryRepository.existsById(factoryId)) {
            throw new AppException(ErrorCode.FACTORY_NOT_FOUND, "Không tìm thấy Factory: " + factoryId);
        }

        return areaRepository.findByFactoryIdOrderByAreaIdAsc(factoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> getAreasByFactoryActive(Long factoryId) {
        if (factoryId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "factory_id không được null");
        }
        if (!factoryRepository.existsById(factoryId)) {
            throw new AppException(ErrorCode.FACTORY_NOT_FOUND, "Không tìm thấy Factory: " + factoryId);
        }

        return areaRepository
                .findByFactoryIdAndAreaStatusOrderByAreaIdAsc(factoryId, AreaStatus.Active)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> getAreasByFactoryInactive(Long factoryId) {
        if (factoryId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "factory_id không được null");
        }
        if (!factoryRepository.existsById(factoryId)) {
            throw new AppException(ErrorCode.FACTORY_NOT_FOUND, "Không tìm thấy Factory: " + factoryId);
        }

        return areaRepository
                .findByFactoryIdAndAreaStatusOrderByAreaIdAsc(factoryId, AreaStatus.Inactive)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AreaResponse activateArea(Long areaId) {
        if (areaId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "area_id không được null");
        }

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND, "Không tìm thấy Area: " + areaId));

        // nếu đang Active thì vẫn trả về OK
        area.setAreaStatus(AreaStatus.Active);

        Area saved = areaRepository.save(area);
        return toResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> searchAreas(Long factoryId, String keyword, StatusFilter status) {

        if (factoryId == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "factory_id không được null");
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "keyword không được rỗng");
        }
        if (!factoryRepository.existsById(factoryId)) {
            throw new AppException(ErrorCode.FACTORY_NOT_FOUND, "Không tìm thấy Factory: " + factoryId);
        }

        String kw = keyword.trim();
        StatusFilter filter = (status == null) ? StatusFilter.ALL : status;

        List<Area> areas = switch (filter) {
            case ALL -> areaRepository.searchByFactoryIdAndName(factoryId, kw);
            case ACTIVE -> areaRepository.searchByFactoryIdAndStatusAndName(factoryId, AreaStatus.Active, kw);
            case INACTIVE -> areaRepository.searchByFactoryIdAndStatusAndName(factoryId, AreaStatus.Inactive, kw);
        };

        return areas.stream().map(this::toResponse).toList();
    }


    private AreaResponse toResponse(Area area) {
        return AreaResponse.builder()
                .areaId(area.getAreaId())
                .factoryId(area.getFactoryId())
                .areaName(area.getAreaName())
                .areaDescription(area.getAreaDescription())
                .areaStatus(area.getAreaStatus())
                .build();
    }
}
