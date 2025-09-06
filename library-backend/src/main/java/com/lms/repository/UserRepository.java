package com.lms.repository;

import com.lms.model.User;
import com.lms.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find all users by role
    List<User> findByRole(UserRole role);
    
    // Find users by first name or last name containing the given string
    List<User> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
        
    // Find users by name containing and specific role
    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND u.role = :role")
    List<User> findByFirstNameContainingOrLastNameContainingAndRole(
            @Param("name") String name, 
            @Param("role") UserRole role);
    
    // Find active users by role (membership not expired)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.membershipExpiry > CURRENT_DATE")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    // Find users with expired membership
    @Query("SELECT u FROM User u WHERE u.membershipExpiry <= CURRENT_DATE")
    List<User> findUsersWithExpiredMembership();
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find users by enabled status
    List<User> findByEnabled(boolean enabled);
    
    // Find users by role and enabled status
    List<User> findByRoleAndEnabled(UserRole role, boolean enabled);
    
    // Count users by role
    long countByRole(UserRole role);
}