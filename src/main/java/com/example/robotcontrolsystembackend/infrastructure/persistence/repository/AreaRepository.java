package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.enumtype.AreaStatus;
import com.example.robotcontrolsystembackend.domain.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findByFactoryIdOrderByAreaIdAsc(Long factoryId);
    List<Area> findByFactoryIdAndAreaStatusOrderByAreaIdAsc(Long factoryId, AreaStatus areaStatus);
    @Query("""
           SELECT a FROM Area a
           WHERE a.factoryId = :factoryId
             AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY a.areaId ASC
           """)
    List<Area> searchByFactoryIdAndName(Long factoryId, String keyword);

    @Query("""
           SELECT a FROM Area a
           WHERE a.factoryId = :factoryId
             AND a.areaStatus = :status
             AND LOWER(a.areaName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY a.areaId ASC
           """)
    List<Area> searchByFactoryIdAndStatusAndName(Long factoryId, AreaStatus status, String keyword);
}
