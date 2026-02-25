package com.subtracker.SubTracker.auth;


import com.subtracker.SubTracker.enums.Role;
import com.subtracker.SubTracker.security.JwtService;
import com.subtracker.SubTracker.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    public UserResponseDto registerUser(RegisterRequest registerRequest) {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName(registerRequest.getName());
        createUserDto.setEmail(registerRequest.getEmail());
        createUserDto.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        createUserDto.setPlanType(registerRequest.getPlanType());
        UserEntity userEntity = userMapper.dtoToEntity(createUserDto);
        userEntity.setRole(Role.USER);
        userRepository.save(userEntity);
        return userMapper.entityToResponse(userEntity);
    }

    public String login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        System.out.println("Successfully logged in");
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println(userDetails.getUsername());
        System.out.println(userDetails.getPassword());
        System.out.println(passwordEncoder.encode(loginRequest.getPassword()));
        return jwtService.generateToken(userDetails);
    }
}
