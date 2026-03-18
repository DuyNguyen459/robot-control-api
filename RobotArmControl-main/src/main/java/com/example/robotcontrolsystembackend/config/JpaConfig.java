package com.example.robotcontrolsystembackend.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.robotcontrolsystembackend.infrastructure.persistence.repository",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.example\\.robotcontrolsystembackend\\.infrastructure\\.persistence\\.repository\\.cassandra\\..*"
    )
)
public class JpaConfig {
    // JPA manages all repositories EXCEPT the cassandra sub-package
}
