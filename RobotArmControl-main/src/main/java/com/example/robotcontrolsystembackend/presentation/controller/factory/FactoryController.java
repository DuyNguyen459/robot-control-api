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

    // GET /api/factories?search=keyword
    @GetMapping
    public ApiResponse<List<FactoryResponse>> getFactories(
            @RequestParam(required = false, defaultValue = "") String search) {
        List<FactoryResponse> results = factoryService.search(search);
        return ApiResponse.ok("Lấy danh sách factory thành công", results);
    }

    // GET /api/factories/{factoryId}
    @GetMapping("/{factoryId}")
    public ApiResponse<FactoryResponse> getFactoryById(@PathVariable Long factoryId) {
        // TODO: Implement factoryService.getFactoryById(factoryId)
        return ApiResponse.ok("Lấy factory thành công", null);
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
    // Lấy tất cả factory (không search)
@GetMapping("/all")
public ApiResponse<List<FactoryResponse>> getAllFactories() {
    List<FactoryResponse> results = factoryService.findAll();
    return ApiResponse.ok("Lấy tất cả factory thành công", results);
}
}
