package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.FactoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "factory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "factory_name", nullable = false, length = 160)
    private String factoryName;

    // DB hiện tại là location (không có description)
    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "factory_status", nullable = false, columnDefinition = "factory_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private FactoryStatus factoryStatus;
}
