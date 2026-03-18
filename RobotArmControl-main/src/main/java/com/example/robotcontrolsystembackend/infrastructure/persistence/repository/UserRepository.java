package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import com.example.robotcontrolsystembackend.domain.enumtype.UserStatus;
import com.example.robotcontrolsystembackend.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "(u.role = COALESCE(:role, u.role)) AND " +
           "(u.userStatus = COALESCE(:status, u.userStatus)) AND " +
           "(COALESCE(:search, '') = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')))")
    Page<User> findAllWithFilters(
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            @Param("search") String search,
            Pageable pageable);

    @Query(
           value = "SELECT u.* FROM \"user\" u WHERE " +
                  "(:role IS NULL OR u.role::text = :role) AND " +
                  "(:status IS NULL OR u.user_status::text = :status) AND " +
                  "(CASE WHEN COALESCE(:search, '') = '' THEN TRUE ELSE (" +
                  "convert_from(u.user_name::bytea, 'UTF8') ILIKE CONCAT('%', :search, '%') OR " +
                  "convert_from(u.email::bytea, 'UTF8') ILIKE CONCAT('%', :search, '%')" +
                  ") END) " +
                  "ORDER BY u.user_id",
           countQuery = "SELECT COUNT(*) FROM \"user\" u WHERE " +
                  "(:role IS NULL OR u.role::text = :role) AND " +
                  "(:status IS NULL OR u.user_status::text = :status) AND " +
                  "(CASE WHEN COALESCE(:search, '') = '' THEN TRUE ELSE (" +
                  "convert_from(u.user_name::bytea, 'UTF8') ILIKE CONCAT('%', :search, '%') OR " +
                  "convert_from(u.email::bytea, 'UTF8') ILIKE CONCAT('%', :search, '%')" +
                  ") END)",
           nativeQuery = true
    )
    Page<User> findAllWithFiltersBytea(
           @Param("role") String role,
           @Param("status") String status,
           @Param("search") String search,
           Pageable pageable);
}
