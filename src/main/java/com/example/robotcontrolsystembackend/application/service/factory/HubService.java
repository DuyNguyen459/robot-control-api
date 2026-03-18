package com.example.robotcontrolsystembackend.application.service.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateHubRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateHubRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.HubResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.StatusFilter;

import java.util.List;

public interface HubService {
    HubResponse createHub(Long areaId, CreateHubRequest request);
    HubResponse updateHub(Long hubId, UpdateHubRequest request);
    void deleteHub(Long hubId);
    List<HubResponse> getHubsByArea(Long areaId);
    List<HubResponse> getHubsByAreaActive(Long areaId);
    List<HubResponse> getHubsByAreaInactive(Long areaId);
    HubResponse activateHub(Long hubId); // restore
    List<HubResponse> searchHubs(Long areaId, String keyword, StatusFilter status);
}
