package com.example.robotcontrolsystembackend.application.dto.request.factory;

import com.example.robotcontrolsystembackend.domain.enumtype.HubStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateHubRequest {
    private String hubName;
    private String hubDescription;
}
