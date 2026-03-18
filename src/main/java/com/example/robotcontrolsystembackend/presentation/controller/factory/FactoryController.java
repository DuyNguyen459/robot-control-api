package com.example.robotcontrolsystembackend.presentation.controller.factory;

import com.example.robotcontrolsystembackend.application.dto.request.factory.CreateFactoryRequest;
import com.example.robotcontrolsystembackend.application.dto.request.factory.UpdateFactoryRequest;
import com.example.robotcontrolsystembackend.application.dto.response.factory.FactoryResponse;
import com.example.robotcontrolsystembackend.application.service.factory.FactoryService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.* ;

import java.util.List;


@RestController
@RequestMapping("/api/factories")
public class FactoryController {
    private final FactoryService factoryService;

    public FactoryController(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    @PostMapping
    public ApiResponse<FactoryResponse> create(@RequestBody CreateFactoryRequest request) {
        FactoryResponse res = factoryService.createFactory(request);
        return ApiResponse.ok("Tạo factory thành công", res);
    }

    @GetMapping("/search")
    public ApiResponse<List<FactoryResponse>> search(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        List<FactoryResponse> results = factoryService.search(keyword);
        return ApiResponse.ok("Tìm kiếm thành công", results);
    }
    @PutMapping("/{factoryId}")
    public ApiResponse<FactoryResponse> update(
            @PathVariable Long factoryId,
            @RequestBody UpdateFactoryRequest request) {
        FactoryResponse res = factoryService.updateFactory(factoryId, request);
        return ApiResponse.ok("Cập nhật factory thành công", res);
    }

    @DeleteMapping("/{factoryId}")
    public ApiResponse<Void> delete(@PathVariable Long factoryId) {
        factoryService.deleteFactory(factoryId);
        return ApiResponse.ok("Xóa factory thành công", null);
    }
}
