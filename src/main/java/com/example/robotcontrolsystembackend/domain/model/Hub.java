package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.HubStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "hub")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hub_id")
    private Long hubId;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "hub_name", nullable = false, length = 150)
    private String hubName;

    @Column(name = "hub_description", length = 500)
    private String hubDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "hub_status", nullable = false, columnDefinition = "hub_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private HubStatus hubStatus;
}