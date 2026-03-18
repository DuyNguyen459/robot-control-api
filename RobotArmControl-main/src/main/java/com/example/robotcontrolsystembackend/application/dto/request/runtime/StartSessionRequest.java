package com.example.robotcontrolsystembackend.application.dto.request.runtime;

import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for starting a control session
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionRequest {
    
    /**
     * Control mode: CAMERA or BUTTON
     * - CAMERA: Activates AI Camera for hand gesture control
     * - BUTTON: Manual button control via frontend
     */
    @NotNull(message = "Control mode is required")
    private ControlMode controlMode;
    
    /**
     * Optional device ID to control
     */
    private Long deviceId;
}
