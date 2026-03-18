package com.example.robotcontrolsystembackend.application.dto.response.factory;

import com.example.robotcontrolsystembackend.domain.enumtype.HubStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HubResponse {
    private Long hubId;
    private Long areaId;
    private String hubName;
    private String hubDescription;
    private HubStatus hubStatus;
}
