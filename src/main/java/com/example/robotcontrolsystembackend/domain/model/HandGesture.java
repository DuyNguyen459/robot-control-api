package com.example.robotcontrolsystembackend.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hand_gesture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandGesture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hand_gesture_id")
    private Long handGestureId;

    @Column(name = "hand_gesture_name", nullable = false, unique = true, length = 100)
    private String handGestureName;

    @Column(name = "description", length = 500)
    private String description;
}