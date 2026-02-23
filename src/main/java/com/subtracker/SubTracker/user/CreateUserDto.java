package com.subtracker.SubTracker.user;

import com.subtracker.SubTracker.enums.PlanType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateUserDto {

    private String name;
    private String email;
    private PlanType planType;
    private String password;

}
