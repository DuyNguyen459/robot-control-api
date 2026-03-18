package com.example.robotcontrolsystembackend.application.dto.request.factory;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFactoryRequest {
    private String factoryName;
    private String location; // optional
}

