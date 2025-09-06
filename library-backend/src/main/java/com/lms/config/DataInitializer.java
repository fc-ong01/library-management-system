package com.lms.config;

import com.lms.model.User;
import com.lms.model.UserRole;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Component to initialize some default data when application starts (optional)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default librarian if not exists
        if (userRepository.findByEmail("librarian@library.com").isEmpty()) {
            User librarian = new User();
            librarian.setEmail("librarian@library.com");
            librarian.setPassword(passwordEncoder.encode("librarian123"));
            librarian.setFirstName("System");
            librarian.setLastName("Librarian");
            librarian.setRole(UserRole.LIBRARIAN);
            librarian.setRegistrationDate(LocalDate.now());
            librarian.setMembershipExpiry(LocalDate.now().plusYears(1));
            librarian.setEnabled(true);
            
            userRepository.save(librarian);
            System.out.println("Default librarian created: librarian@library.com / librarian123");
        }

        // Create default member if not exists
        if (userRepository.findByEmail("member@library.com").isEmpty()) {
            User member = new User();
            member.setEmail("member@library.com");
            member.setPassword(passwordEncoder.encode("member123"));
            member.setFirstName("John");
            member.setLastName("Member");
            member.setRole(UserRole.MEMBER);
            member.setRegistrationDate(LocalDate.now());
            member.setMembershipExpiry(LocalDate.now().plusYears(1));
            member.setEnabled(true);
            
            userRepository.save(member);
            System.out.println("Default member created: member@library.com / member123");
        }
    }
}