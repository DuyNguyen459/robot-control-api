package com.example.robotcontrolsystembackend.application.dto.response.factory;


import com.example.robotcontrolsystembackend.domain.enumtype.FactoryStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactoryResponse {
    private Long factoryId;
    private String factoryName;
    private String location;
    private FactoryStatus factoryStatus;
}
