package com.example.robotcontrolsystembackend.application.service.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateFactoryRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.FactoryResponse;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateFactoryRequest;
import java.util.List;
public interface FactoryService {
    FactoryResponse createFactory(CreateFactoryRequest request);
    List<FactoryResponse> search(String keyword);
    FactoryResponse updateFactory(Long factoryId, UpdateFactoryRequest request);
    void deleteFactory(Long factoryId);
}


