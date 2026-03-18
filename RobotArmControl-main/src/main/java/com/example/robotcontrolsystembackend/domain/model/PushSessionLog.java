package com.example.robotcontrolsystembackend.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "push_session_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_item_id")
    private Long pushItemId;

    @Column(name = "push_session_id", nullable = false)
    private UUID pushSessionId;

    @Column(name = "log_id", nullable = false)
    private Long logId;
}