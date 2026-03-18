package com.example.robotcontrolsystembackend.application.service.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateAreaRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateAreaRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.AreaResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;

import java.util.List;

public interface AreaService {
    AreaResponse createArea(Long factoryId, CreateAreaRequest request);
    AreaResponse updateArea(Long areaId, UpdateAreaRequest request);
    void deleteArea(Long areaId);
    List<AreaResponse> getAreasByFactory(Long factoryId);
    List<AreaResponse> getAreasByFactoryActive(Long factoryId);
    List<AreaResponse> getAreasByFactoryInactive(Long factoryId);
    AreaResponse activateArea(Long areaId); // restore
    List<AreaResponse> searchAreas(Long factoryId, String keyword, StatusFilter status);

}
