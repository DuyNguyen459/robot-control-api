package com.example.robotcontrolsystembackend.application.dto.request.factory;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFactoryRequest {
    private String factoryName;
    private String location;
}
