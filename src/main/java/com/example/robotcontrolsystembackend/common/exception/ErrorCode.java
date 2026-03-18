package com.example.robotcontrolsystembackend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR"),
    FACTORY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "FACTORY_NAME_REQUIRED"),
    FACTORY_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "FACTORY_NAME_ALREADY_EXISTS"),
    FACTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "FACTORY_NOT_FOUND"),
    AREA_NOT_FOUND(org.springframework.http.HttpStatus.NOT_FOUND, "AREA_NOT_FOUND"),
    HUB_NOT_FOUND(org.springframework.http.HttpStatus.NOT_FOUND, "HUB_NOT_FOUND"),

    AREA_NAME_REQUIRED(org.springframework.http.HttpStatus.BAD_REQUEST, "AREA_NAME_REQUIRED"),
    HUB_NAME_REQUIRED(org.springframework.http.HttpStatus.BAD_REQUEST, "HUB_NAME_REQUIRED"),

    AREA_HAS_HUBS(org.springframework.http.HttpStatus.CONFLICT, "AREA_HAS_HUBS"),
    HUB_HAS_DEVICES(org.springframework.http.HttpStatus.CONFLICT, "HUB_HAS_DEVICES");

    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }
}
