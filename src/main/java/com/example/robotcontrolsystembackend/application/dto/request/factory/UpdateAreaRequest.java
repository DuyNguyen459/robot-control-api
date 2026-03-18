package com.example.robotcontrolsystembackend.application.dto.request.factory;

import com.example.robotcontrolsystembackend.domain.enumtype.AreaStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateAreaRequest {
    private String areaName;
    private String areaDescription;
}