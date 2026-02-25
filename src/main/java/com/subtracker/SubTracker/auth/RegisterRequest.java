package com.subtracker.SubTracker.auth;

import com.subtracker.SubTracker.enums.PlanType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {

    private String name;
    private String email;
    private PlanType planType;
    private String password;
}
