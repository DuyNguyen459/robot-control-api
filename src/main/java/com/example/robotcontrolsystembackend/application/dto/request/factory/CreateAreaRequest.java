package com.example.robotcontrolsystembackend.application.dto.request.factory;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateAreaRequest {
    private String areaName;
    private String areaDescription;
}