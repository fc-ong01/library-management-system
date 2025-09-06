package com.lms.service;

import com.lms.model.User;
import com.lms.model.UserRole;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user with the specified role
     */
    public User registerUser(User user, UserRole role) {
        // Validate email uniqueness
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Encode password
        user.setRole(role);
        user.setRegistrationDate(LocalDate.now());
        user.setMembershipExpiry(LocalDate.now().plusYears(1)); // 1-year membership
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find all users with a specific role
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Search users by name (partial match)
     */
    public List<User> findByNameContaining(String name, UserRole role) {
        if (role != null) {
            return userRepository.findByFirstNameContainingOrLastNameContainingAndRole(name, role);
        }
        return userRepository.findByFirstNameContainingOrLastNameContaining(name, name);
    }

    /**
     * Update user information
     */
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update fields if they are provided
        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }
        if (userDetails.getPhoneNumber() != null) {
            user.setPhoneNumber(userDetails.getPhoneNumber());
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            // Check if new email is already taken
            if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }

        return userRepository.save(user);
    }

    /**
     * Update member information (specific for librarian operations)
     */
    public User updateMember(Long id, User memberDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));

        // Ensure the user is a member
        if (!user.getRole().equals(UserRole.MEMBER)) {
            throw new RuntimeException("User is not a member: " + id);
        }

        return updateUser(id, memberDetails);
    }

    /**
     * Delete a user by ID
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if user has active borrowings (placeholder implementation)
        if (hasActiveBorrowings(id)) {
            throw new RuntimeException("Cannot delete user with active borrowings");
        }

        userRepository.delete(user);
    }

    /**
     * Delete a member by ID (specific for librarian operations)
     */
    public void deleteMember(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));

        // Ensure the user is a member
        if (!user.getRole().equals(UserRole.MEMBER)) {
            throw new RuntimeException("User is not a member: " + id);
        }

        deleteUser(id);
    }

    /**
     * Reset user password
     */
//    public void resetPassword(Long id, String newPassword) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
//
//        user.setPassword(passwordEncoder.encode(newPassword));
//        userRepository.save(user);
//    }

    /**
     * Check if user has active borrowings (placeholder implementation)
     */
    private boolean hasActiveBorrowings(Long userId) {
        // This would typically call a BorrowingRecordRepository
        // For now, we'll return false as a placeholder
        return false;
    }

    /**
     * Extend membership for a user
     */
    public User extendMembership(Long id, int years) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        LocalDate currentExpiry = user.getMembershipExpiry();
        LocalDate newExpiry = currentExpiry != null && currentExpiry.isAfter(LocalDate.now()) 
                ? currentExpiry.plusYears(years) 
                : LocalDate.now().plusYears(years);
        
        user.setMembershipExpiry(newExpiry);
        return userRepository.save(user);
    }

    /**
     * Check if a user's membership is valid
     */
    public boolean isMembershipValid(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return user.getMembershipExpiry() != null && 
               user.getMembershipExpiry().isAfter(LocalDate.now()) &&
               user.isEnabled();
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all members
     */
    public List<User> getAllMembers() {
        return userRepository.findByRole(UserRole.MEMBER);
    }

    /**
     * Get all librarians
     */
    public List<User> getAllLibrarians() {
        return userRepository.findByRole(UserRole.LIBRARIAN);
    }

    /**
     * Disable a user account
     */
    public User disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setEnabled(false);
        return userRepository.save(user);
    }

    /**
     * Enable a user account
     */
    public User enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setEnabled(true);
        return userRepository.save(user);
    }
}