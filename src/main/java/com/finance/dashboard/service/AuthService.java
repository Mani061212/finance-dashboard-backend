package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.RegisterRequest;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.exception.AppException;
import com.finance.dashboard.model.RefreshToken;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.RefreshTokenRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtProperties;
import com.finance.dashboard.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider       jwtTokenProvider;
    private final UserDetailsService     userDetailsService;
    private final AuthenticationManager  authenticationManager;
    private final PasswordEncoder        passwordEncoder;
    private final JwtProperties          jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw AppException.conflict("Email already registered: " + request.email());
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.VIEWER)
                .isActive(true)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> AppException.notFound("User not found"));
        refreshTokenRepository.revokeAllUserTokens(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String tokenValue) {
        RefreshToken rt = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> AppException.unauthorized("Refresh token not found"));
        if (!rt.isValid()) {
            throw AppException.unauthorized("Refresh token is expired or revoked");
        }
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        return buildAuthResponse(rt.getUser());
    }

    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(ud);
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiryMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
        return AuthResponse.of(accessToken, rt.getToken(),
                jwtProperties.getAccessTokenExpiryMs(), UserResponse.from(user));
    }
}
