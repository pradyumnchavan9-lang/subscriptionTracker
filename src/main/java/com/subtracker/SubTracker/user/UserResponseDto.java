package com.subtracker.SubTracker.user;

import com.subtracker.SubTracker.enums.PlanType;
import com.subtracker.SubTracker.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private PlanType planType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
