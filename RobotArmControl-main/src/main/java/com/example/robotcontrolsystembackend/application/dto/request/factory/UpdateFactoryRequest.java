package com.example.robotcontrolsystembackend.application.dto.request.factory;

import com.example.robotcontrolsystembackend.domain.enumtype.FactoryStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFactoryRequest {
    private String factoryName;
    private String location;
    private FactoryStatus factoryStatus;
}
