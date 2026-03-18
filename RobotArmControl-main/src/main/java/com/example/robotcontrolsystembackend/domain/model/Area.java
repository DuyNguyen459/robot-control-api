package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.AreaStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "area")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long areaId;

    @Column(name = "factory_id", nullable = false)
    private Long factoryId;

    @Column(name = "area_name", nullable = false, length = 150)
    private String areaName;

    @Column(name = "area_description", length = 500)
    private String areaDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_status", nullable = false, columnDefinition = "area_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AreaStatus areaStatus;
}