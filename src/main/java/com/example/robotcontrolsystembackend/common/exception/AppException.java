package com.example.robotcontrolsystembackend.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final com.example.robotcontrolsystembackend.common.exception.ErrorCode errorCode;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
