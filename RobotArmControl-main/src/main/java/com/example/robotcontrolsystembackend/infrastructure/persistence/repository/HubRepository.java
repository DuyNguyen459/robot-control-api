package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.enumtype.HubStatus;
import com.example.robotcontrolsystembackend.domain.model.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HubRepository extends JpaRepository<Hub, Long> {
    List<Hub> findByAreaIdOrderByHubIdAsc(Long areaId);
    List<Hub> findByAreaIdAndHubStatusOrderByHubIdAsc(Long areaId, HubStatus hubStatus);
    boolean existsByAreaId(Long areaId);
    @Query("""
           SELECT h FROM Hub h
           WHERE h.areaId = :areaId
             AND LOWER(h.hubName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY h.hubId ASC
           """)
    List<Hub> searchByAreaIdAndName(Long areaId, String keyword);

    @Query("""
           SELECT h FROM Hub h
           WHERE h.areaId = :areaId
             AND h.hubStatus = :status
             AND LOWER(h.hubName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY h.hubId ASC
           """)
    List<Hub> searchByAreaIdAndStatusAndName(Long areaId, HubStatus status, String keyword);
}