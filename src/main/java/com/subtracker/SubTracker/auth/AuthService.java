package com.subtracker.SubTracker.auth;


import com.subtracker.SubTracker.enums.Role;
import com.subtracker.SubTracker.refreshtoken.RefreshTokenEntity;
import com.subtracker.SubTracker.refreshtoken.RefreshTokenService;
import com.subtracker.SubTracker.security.JwtService;
import com.subtracker.SubTracker.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    @Autowired
    private RefreshTokenService refreshTokenService;

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


    //return jwt token
    public ResponseEntity<?> login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        RefreshTokenEntity refreshToken = refreshTokenService.generateRefreshToken(loginRequest.getEmail());
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken(),
                "email", loginRequest.getEmail()
        ));
    }
}
