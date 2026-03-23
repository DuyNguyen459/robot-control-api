package com.example.robotcontrolsystembackend.application.service.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.RobotControlCommandRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.RobotControlDispatchResponse;

public interface RobotControlCommandService {
    RobotControlDispatchResponse dispatchCommand(RobotControlCommandRequest request, String source);
}
