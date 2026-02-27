package com.subtracker.SubTracker.refreshtoken;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {


    @Value("${app.jwtRefreshExpirationMs}")
    private Long jwtRefreshExpirationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenEntity generateRefreshToken(String email){

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setEmail(email);
        refreshTokenEntity.setExpiryDate(Instant.now().plusMillis(jwtRefreshExpirationMs));
        refreshTokenEntity.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshTokenEntity);
    }

    public RefreshTokenEntity verifyRefreshToken(RefreshTokenEntity refreshTokenEntity){

        if(refreshTokenEntity.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new RuntimeException("Refresh token expired");
        }
        return refreshTokenEntity;
    }

    public Optional<RefreshTokenEntity> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByEmail(String email){
         refreshTokenRepository.deleteByEmail(email);

    }
}
