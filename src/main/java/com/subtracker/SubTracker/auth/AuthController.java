package com.subtracker.SubTracker.auth;

import com.subtracker.SubTracker.refreshtoken.RefreshTokenEntity;
import com.subtracker.SubTracker.refreshtoken.RefreshTokenService;
import com.subtracker.SubTracker.security.JwtService;
import com.subtracker.SubTracker.user.UserResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;


    //Register
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(authService.registerUser(registerRequest), HttpStatus.OK);
    }


    //Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println(loginRequest);
        return new ResponseEntity<>(authService.login(loginRequest),HttpStatus.OK);
    }

    //Refresh Token
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request){
        String requestToken = request.get("requestToken");
        //using this token fetch the  token from db
        RefreshTokenEntity refreshToken = refreshTokenService.findByToken(requestToken)
                .orElseThrow(()->new RuntimeException("Refresh token not found"));
        refreshTokenService.verifyRefreshToken(refreshToken);

        //To generate new access token we need the email
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        String newAccessToken = jwtService.generateToken((UserDetails)auth.getPrincipal());


        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }
}
