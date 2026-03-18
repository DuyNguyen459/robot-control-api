package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.MappingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "gesture_command_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GestureCommandMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @Column(name = "hand_gesture_id", nullable = false)
    private Long handGestureId;

    @Column(name = "command_id", nullable = false)
    private Long commandId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, columnDefinition = "mapping_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MappingStatus mappingStatus;

    @Column(name = "condition", length = 255)
    private String condition;

    @Column(name = "note", length = 500)
    private String note;
}