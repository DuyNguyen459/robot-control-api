package com.example.robotcontrolsystembackend.application.dto.response.factory;

import com.example.robotcontrolsystembackend.domain.enumtype.AreaStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AreaResponse {
    private Long areaId;
    private Long factoryId;
    private String areaName;
    private String areaDescription;
    private AreaStatus areaStatus;
}
