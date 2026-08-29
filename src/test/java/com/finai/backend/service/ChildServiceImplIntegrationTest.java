package com.finai.backend.service;

import com.finai.backend.dto.request.ChildProfileRequest;
import com.finai.backend.dto.response.ChildProfileResponse;
import com.finai.backend.entity.Role;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.repository.ChildProfileRepository;
import com.finai.backend.repository.RoleRepository;
import com.finai.backend.repository.UserRepository;
import com.finai.backend.service.interfaces.ChildService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChildServiceImplIntegrationTest {

    @Autowired
    private ChildService childService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildProfileRepository childProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createChildProfilePersistsChildUserAndLinkedProfile() {
        String suffix = UUID.randomUUID().toString();
        Role parentRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_USER).build()));

        User parent = User.builder()
                .firstName("Parent")
                .lastName("Account")
                .email("parent-" + suffix + "@example.com")
                .password(passwordEncoder.encode("parent-password"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .profileComplete(true)
                .build();
        parent.addRole(parentRole);
        parent = userRepository.save(parent);

        String childEmail = "child-" + suffix + "@example.com";
        String childPassword = "child-password";
        ChildProfileRequest request = ChildProfileRequest.builder()
                .firstName("Ava")
                .lastName("Account")
                .age(10)
                .usernameOrEmail(childEmail)
                .password(childPassword)
                .initialSavings(new BigDecimal("250.00"))
                .build();

        ChildProfileResponse response = childService.createChildProfile(request, parent);

        User childUser = userRepository.findByEmail(childEmail).orElseThrow();
        var childProfile = childProfileRepository.findById(response.getId()).orElseThrow();

        assertNotNull(response.getChildUser());
        assertEquals(childUser.getId(), response.getChildUser().getId());
        assertEquals(parent.getId(), childProfile.getParentUser().getId());
        assertEquals(childUser.getId(), childProfile.getChildUser().getId());
        assertTrue(passwordEncoder.matches(childPassword, childUser.getPassword()));
        assertTrue(childUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ROLE_CHILD));
    }
}
