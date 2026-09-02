package com.zestindia.productapi.auth;

import com.zestindia.productapi.auth.AuthDtos.*;
import com.zestindia.productapi.user.AppUser;
import com.zestindia.productapi.user.Role;
import com.zestindia.productapi.user.UserRepository;
import com.zestindia.productapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshExpirationMillis;

    @Value("${app.jwt.access-token-expiration}")
    private long accessExpirationMillis;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = AppUser.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AppUser user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Revoke previous active refresh tokens for this user.
        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()) && !t.isRevoked())
                .forEach(t -> t.setRevoked(true));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String hash = sha256(request.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        stored.setRevoked(true); // rotation: old token cannot be reused
        AppUser user = stored.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        String hash = sha256(request.refreshToken());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> token.setRevoked(true));
    }

    private AuthResponse issueTokens(AppUser user) {

    String access = jwtService.generateAccessToken(user);
    String refresh = jwtService.generateRefreshToken(user);

    RefreshToken entity = RefreshToken.builder()
            .tokenHash(sha256(refresh))
            .user(user)
            .expiresAt(
                Instant.now().plusMillis(refreshExpirationMillis)
            )
            .revoked(false)
            .build();

    refreshTokenRepository.save(entity);

    return new AuthResponse(
            access,
            refresh,
            "Bearer",
            accessExpirationMillis / 1000
    );
}

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}
