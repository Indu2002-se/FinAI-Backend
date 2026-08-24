package com.finai.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildProfileRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
    private Integer age;
    private String avatar;
    private BigDecimal initialSavings;

    // Optional credentials if creating child user login
    private String usernameOrEmail;
    private String password;
}
