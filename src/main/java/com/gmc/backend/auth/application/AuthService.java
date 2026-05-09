package com.gmc.backend.auth.application;

import com.gmc.backend.auth.jwt.JwtTokenProvider;
import com.gmc.backend.auth.jwt.TokenBlacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    public void logout(String token) {
        tokenBlacklist.add(token, jwtTokenProvider.getExpiration(token));
    }
}
