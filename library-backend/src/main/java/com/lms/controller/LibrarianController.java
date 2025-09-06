package com.lms.controller;

import com.lms.model.User;
import com.lms.model.UserRole;
import com.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/librarian")
@PreAuthorize("hasRole('LIBRARIAN')")
public class LibrarianController {

    private final UserService userService;

    @Autowired
    public LibrarianController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new member
     */
    @PostMapping("/members")
    public ResponseEntity<?> registerMember(@Valid @RequestBody User member) {
        try {
            User newMember = userService.registerUser(member, UserRole.MEMBER);
            return ResponseEntity.ok(newMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating member: " + e.getMessage());
        }
    }

    /**
     * Get all members
     */
    @GetMapping("/members")
    public ResponseEntity<List<User>> getAllMembers() {
        try {
            List<User> members = userService.getAllMembers();
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get member by ID
     */
    @GetMapping("/members/{id}")
    public ResponseEntity<?> getMemberById(@PathVariable Long id) {
        try {
            Optional<User> member = userService.findById(id);
            if (member.isPresent() && member.get().getRole() == UserRole.MEMBER) {
                return ResponseEntity.ok(member.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving member: " + e.getMessage());
        }
    }

    /**
     * Search members by name or ID
     */
    @GetMapping("/members/search")
    public ResponseEntity<?> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long id) {
        try {
            if (id != null) {
                Optional<User> member = userService.findById(id);
                if (member.isPresent() && member.get().getRole() == UserRole.MEMBER) {
                    return ResponseEntity.ok(List.of(member.get()));
                } else {
                    return ResponseEntity.ok(List.of());
                }
            } else if (name != null && !name.trim().isEmpty()) {
                List<User> members = userService.findByNameContaining(name, UserRole.MEMBER);
                return ResponseEntity.ok(members);
            } else {
                List<User> members = userService.getAllMembers();
                return ResponseEntity.ok(members);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error searching members: " + e.getMessage());
        }
    }

    /**
     * Update member information
     */
    @PutMapping("/members/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @Valid @RequestBody User memberDetails) {
        try {
            Optional<User> existingMember = userService.findById(id);
            if (existingMember.isEmpty() || existingMember.get().getRole() != UserRole.MEMBER) {
                return ResponseEntity.notFound().build();
            }

            memberDetails.setRole(UserRole.MEMBER);
            User updatedMember = userService.updateMember(id, memberDetails);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating member: " + e.getMessage());
        }
    }

    /**
     * Delete a member
     */
    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        try {
            Optional<User> member = userService.findById(id);
            if (member.isEmpty() || member.get().getRole() != UserRole.MEMBER) {
                return ResponseEntity.notFound().build();
            }

            userService.deleteMember(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting member: " + e.getMessage());
        }
    }

    /**
     * Extend a member's membership
     */
    @PostMapping("/members/{id}/extend-membership")
    public ResponseEntity<?> extendMembership(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int years) {
        try {
            Optional<User> member = userService.findById(id);
            if (member.isEmpty() || member.get().getRole() != UserRole.MEMBER) {
                return ResponseEntity.notFound().build();
            }

            User updatedMember = userService.extendMembership(id, years);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error extending membership: " + e.getMessage());
        }
    }

    /**
     * Check if a member's membership is valid
     */
    @GetMapping("/members/{id}/membership-status")
    public ResponseEntity<?> checkMembershipStatus(@PathVariable Long id) {
        try {
            Optional<User> member = userService.findById(id);
            if (member.isEmpty() || member.get().getRole() != UserRole.MEMBER) {
                return ResponseEntity.notFound().build();
            }

            boolean isValid = userService.isMembershipValid(id);
            return ResponseEntity.ok(new MembershipStatusResponse(isValid, member.get().getMembershipExpiry()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error checking membership status: " + e.getMessage());
        }
    }

    /**
     * Membership status response DTO
     */
    public static class MembershipStatusResponse {
        private boolean valid;
        private String expiryDate;

        public MembershipStatusResponse(boolean valid, java.time.LocalDate expiryDate) {
            this.valid = valid;
            this.expiryDate = expiryDate != null ? expiryDate.toString() : null;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    }
}