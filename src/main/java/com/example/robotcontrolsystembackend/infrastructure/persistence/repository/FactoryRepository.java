package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.model.Factory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryRepository extends JpaRepository<Factory, Long> {
    boolean existsByFactoryNameIgnoreCase(String factoryName);
    List<Factory> findByFactoryNameContainingIgnoreCaseOrLocationContainingIgnoreCase(String factoryName, String location);
}
