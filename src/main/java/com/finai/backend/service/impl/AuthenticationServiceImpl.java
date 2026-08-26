package com.finai.backend.service.impl;

import com.finai.backend.dto.request.LoginRequest;
import com.finai.backend.dto.request.RegisterRequest;
import com.finai.backend.dto.response.AuthenticationResponse;
import com.finai.backend.dto.response.UserResponse;
import com.finai.backend.entity.Role;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.exception.AuthenticationException;
import com.finai.backend.exception.BadRequestException;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.ChildProfileRepository;
import com.finai.backend.repository.RoleRepository;
import com.finai.backend.repository.UserRepository;
import com.finai.backend.security.JwtService;
import com.finai.backend.service.interfaces.AuthenticationService;
import com.finai.backend.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service implementation
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ChildProfileRepository childProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Get default user role
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default user role not found in database"));

        // Create user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(false)
                .build();

        // Add role to user
        user.addRole(userRole);

        // Save user
        user = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toArray(String[]::new))
                .build();

        String token = jwtService.generateToken(userDetails);

        // Map to response
        UserResponse userResponse = UserMapper.toUserResponse(user);

        // Create authentication response with userType
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .refreshToken(token)
                .type("Bearer")
                .user(userResponse)
                .userType(determineUserType(user))
                .build();

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Check if user is enabled
        if (!user.getEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        // Generate JWT token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toArray(String[]::new))
                .build();

        String token = jwtService.generateToken(userDetails);

        // Map to response
        UserResponse userResponse = UserMapper.toUserResponse(user);
        
        // Create authentication response with userType
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .refreshToken(token)
                .type("Bearer")
                .user(userResponse)
                .userType(determineUserType(user))
                .build();

        // If this is a child user, populate childProfileId
        if ("CHILD".equals(response.getUserType())) {
            response.setChildProfileId(findChildProfileId(user));
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        // Get current authenticated user email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Get user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Map to response
        return UserMapper.toUserResponse(user);
    }

    /**
     * Determine user type based on roles
     */
    private String determineUserType(User user) {
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role.getName() == RoleType.ROLE_CHILD) {
                    return "CHILD";
                }
            }
        }
        return "PARENT";
    }

    /**
     * Find child profile ID for a child user
     */
    private Long findChildProfileId(User user) {
        return childProfileRepository.findByChildUser(user)
                .map(childProfile -> childProfile.getId())
                .orElse(null);
    }
}
