package com.machinerift.machine_rift.service;

import com.machinerift.machine_rift.dto.AuthLoginRequestDto;
import com.machinerift.machine_rift.dto.AuthRegisterRequestDto;
import com.machinerift.machine_rift.dto.AuthResponseDto;
import com.machinerift.machine_rift.entity.Player;
import com.machinerift.machine_rift.entity.PlayerSession;
import com.machinerift.machine_rift.exception.AuthenticationException;
import com.machinerift.machine_rift.exception.ResourceConflictException;
import com.machinerift.machine_rift.mapper.PlayerMapper;
import com.machinerift.machine_rift.repository.PlayerRepository;
import com.machinerift.machine_rift.repository.PlayerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int TOKEN_BYTES = 32;
    private static final int SESSION_DAYS = 30;

    private final PlayerRepository playerRepository;
    private final PlayerSessionRepository playerSessionRepository;
    private final PlayerProgressService playerProgressService;
    private final PlayerMapper playerMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponseDto register(AuthRegisterRequestDto request) {
        String username = normalizeUsername(request.getUsername());
        String playerName = request.getPlayerName().trim();
        if (playerRepository.existsByUsername(username)) {
            throw new ResourceConflictException("此帳號已被使用");
        }
        if (playerRepository.existsByPlayerNameIgnoreCase(playerName)) {
            throw new ResourceConflictException("此玩家名稱已被使用");
        }

        Player player;
        try {
            player = playerRepository.saveAndFlush(Player.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .playerName(playerName)
                    .level(1)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("此帳號或玩家名稱已被使用");
        }
        playerProgressService.initializePlayer(player);
        return createSession(player);
    }

    @Transactional
    public AuthResponseDto login(AuthLoginRequestDto request) {
        Player player = playerRepository.findByUsername(normalizeUsername(request.getUsername()))
                .orElseThrow(() -> new AuthenticationException("帳號或密碼錯誤"));
        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new AuthenticationException("帳號或密碼錯誤");
        }
        return createSession(player);
    }

    @Transactional(readOnly = true)
    public Player requirePlayer(String authorizationHeader) {
        String rawToken = extractBearerToken(authorizationHeader);
        PlayerSession session = playerSessionRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new AuthenticationException("登入狀態無效，請重新登入"));
        if (!session.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new AuthenticationException("登入已過期，請重新登入");
        }
        return session.getPlayer();
    }

    @Transactional(readOnly = true)
    public Player requirePlayer(String authorizationHeader, Long expectedPlayerId) {
        Player player = requirePlayer(authorizationHeader);
        if (!player.getPlayerId().equals(expectedPlayerId)) {
            throw new AuthenticationException("無法存取其他玩家的資料");
        }
        return player;
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String rawToken = extractBearerToken(authorizationHeader);
        playerSessionRepository.deleteByTokenHash(hashToken(rawToken));
    }

    @Transactional
    public void removeExpiredSessions() {
        playerSessionRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

    private AuthResponseDto createSession(Player player) {
        removeExpiredSessions();
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(SESSION_DAYS);

        playerSessionRepository.save(PlayerSession.builder()
                .player(player)
                .tokenHash(hashToken(rawToken))
                .createdAt(now)
                .expiresAt(expiresAt)
                .build());

        return AuthResponseDto.builder()
                .accessToken(rawToken)
                .expiresAt(expiresAt)
                .player(playerMapper.toResponseDto(player))
                .build();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("請先登入");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new AuthenticationException("請先登入");
        }
        return token;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
