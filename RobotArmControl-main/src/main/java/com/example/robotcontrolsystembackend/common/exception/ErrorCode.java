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
    HUB_HAS_DEVICES(org.springframework.http.HttpStatus.CONFLICT, "HUB_HAS_DEVICES"),

    DEVICE_NOT_FOUND(org.springframework.http.HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND"),
    DEVICE_NAME_REQUIRED(org.springframework.http.HttpStatus.BAD_REQUEST, "DEVICE_NAME_REQUIRED"),
    DEVICE_TYPE_REQUIRED(org.springframework.http.HttpStatus.BAD_REQUEST, "DEVICE_TYPE_REQUIRED"),
    CONNECTION_TYPE_REQUIRED(org.springframework.http.HttpStatus.BAD_REQUEST, "CONNECTION_TYPE_REQUIRED"),
    SERIAL_NUMBER_ALREADY_EXISTS(org.springframework.http.HttpStatus.CONFLICT, "SERIAL_NUMBER_ALREADY_EXISTS"),

    // Authentication & Authorization
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
    USER_EXISTED(HttpStatus.CONFLICT, "USER_EXISTED"),
    EMAIL_EXISTED(HttpStatus.CONFLICT, "EMAIL_EXISTED"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED"),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "USER_INACTIVE");

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
