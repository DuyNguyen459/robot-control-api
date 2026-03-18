package com.example.robotcontrolsystembackend.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "robot_command")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "command_id")
    private Long commandId;

    @Column(name = "command_name", nullable = false, unique = true, length = 100)
    private String commandName;

    @Column(name = "command_params", columnDefinition = "TEXT")
    private String commandParams;
}