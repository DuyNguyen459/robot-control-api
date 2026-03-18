package com.example.robotcontrolsystembackend.common.exception;

import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode ec = ex.getErrorCode();
        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(ApiResponse.fail(ec.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        // đừng expose chi tiết lỗi production, demo thì ok
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail("INTERNAL_ERROR", ex.getMessage()));
    }
}
