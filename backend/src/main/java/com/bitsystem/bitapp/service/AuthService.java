package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.config.JwtUtil;
import com.bitsystem.bitapp.domain.User;
import com.bitsystem.bitapp.domain.UserSession;
import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.exception.BusinessException;
import com.bitsystem.bitapp.repository.UserRepository;
import com.bitsystem.bitapp.repository.UserSessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FallbackStorage fallbackStorage;

    public AuthService(UserRepository userRepository, UserSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, FallbackStorage fallbackStorage) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.fallbackStorage = fallbackStorage;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REGISTER
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthDto.Response register(AuthDto.RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());

        // ── Tentar banco de dados ──────────────────────────────────────────
        try {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException("EMAIL_EXISTS", "Email já cadastrado");
            }

            User user = new User(request.nome(), request.email(), encodedPassword);
            applyProfile(user, request.cidade(), request.whatsapp(), request.nivelProfissional(),
                    request.areaTecnologia(), request.competenciasAtuais());
            user = userRepository.save(user);

            log.info("[AuthService] Registro salvo no banco: email={}", request.email());
            return buildAuthResponse(user);

        } catch (BusinessException ex) {
            // Business exception (email duplicado) relança normalmente
            throw ex;

        } catch (Exception ex) {
            // ── Fallback: salvar em memória ────────────────────────────────
            log.warn("[AuthService] Banco indisponível, usando fallback em memória: {}", ex.getMessage());

            if (fallbackStorage.existsByEmail(request.email())) {
                throw new BusinessException("EMAIL_EXISTS", "Email já cadastrado");
            }

            FallbackStorage.UserRecord record = fallbackStorage.saveUser(
                request.nome(), request.email(), encodedPassword,
                request.cidade(), request.whatsapp(), request.nivelProfissional(),
                request.areaTecnologia(), request.competenciasAtuais()
            );

            return buildAuthResponseFromFallback(record);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGIN
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthDto.Response login(AuthDto.LoginRequest request) {
        // ── Tentar banco de dados ──────────────────────────────────────────
        try {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Email ou senha inválidos"));

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new BusinessException("INVALID_CREDENTIALS", "Email ou senha inválidos");
            }

            sessionRepository.deleteByUsuarioId(user.getId());

            log.info("[AuthService] Login via banco: email={}", request.email());
            return buildAuthResponse(user);

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            // ── Fallback: verificar em memória ─────────────────────────────
            log.warn("[AuthService] Banco indisponível, login via fallback: {}", ex.getMessage());

            return fallbackStorage.findUserByEmail(request.email())
                .filter(record -> passwordEncoder.matches(request.password(), record.passwordHash()))
                .map(record -> {
                    fallbackStorage.deleteSessionsByUserId(record.id());
                    log.info("[AuthService] Login via fallback: email={}", request.email());
                    return buildAuthResponseFromFallback(record);
                })
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Email ou senha inválidos"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ME
    // ════════════════════════════════════════════════════════════════════════

    public AuthDto.UserResponse me(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuário não encontrado"));
            return toUserResponse(user);

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            log.warn("[AuthService] Banco indisponível, me() via fallback: {}", ex.getMessage());

            return fallbackStorage.findUserByEmail(email)
                .map(this::toUserResponseFromFallback)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuário não encontrado"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPDATE PROFILE
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public AuthDto.UserResponse updateProfile(String email, AuthDto.ProfileUpdateRequest request) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuário não encontrado"));

            if (request.nome() != null && !request.nome().isBlank()) {
                user.setNome(request.nome());
            }
            applyProfile(user, request.cidade(), request.whatsapp(), request.nivelProfissional(),
                    request.areaTecnologia(), request.competenciasAtuais());

            return toUserResponse(userRepository.save(user));

        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception ex) {
            log.warn("[AuthService] Banco indisponível, updateProfile via fallback: {}", ex.getMessage());

            return fallbackStorage.findUserByEmail(email)
                .map(record -> {
                    FallbackStorage.UserRecord updated = fallbackStorage.updateUser(
                        record.id(), request.nome(), request.cidade(), request.whatsapp(),
                        request.nivelProfissional(), request.areaTecnologia(), request.competenciasAtuais()
                    );
                    return toUserResponseFromFallback(updated);
                })
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuário não encontrado"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LIST USERS
    // ════════════════════════════════════════════════════════════════════════

    public List<AuthDto.UserResponse> listUsers() {
        try {
            return userRepository.findAll().stream()
                    .map(this::toUserResponse)
                    .toList();

        } catch (Exception ex) {
            log.warn("[AuthService] Banco indisponível, listUsers via fallback: {}", ex.getMessage());

            return fallbackStorage.findAllUsers().stream()
                .map(this::toUserResponseFromFallback)
                .toList();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGOUT
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public void logout(String token) {
        try {
            sessionRepository.findByToken(token).ifPresent(session -> {
                session.setRevoked(true);
                sessionRepository.save(session);
            });
        } catch (Exception ex) {
            log.warn("[AuthService] Banco indisponível, logout via fallback: {}", ex.getMessage());
            fallbackStorage.revokeSession(token);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS (Banco → resposta)
    // ════════════════════════════════════════════════════════════════════════

    private AuthDto.Response buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        saveSession(user.getId(), token, refreshToken);
        return toAuthResponse(user, token, refreshToken);
    }

    private void applyProfile(User user, String cidade, String whatsapp, String nivelProfissional,
            String areaTecnologia, String competenciasAtuais) {
        user.setCidade(cidade);
        user.setWhatsapp(whatsapp);
        user.setNivelProfissional(nivelProfissional);
        user.setAreaTecnologia(areaTecnologia);
        user.setCompetenciasAtuais(competenciasAtuais);
    }

    private AuthDto.Response toAuthResponse(User user, String token, String refreshToken) {
        return new AuthDto.Response(
                token, refreshToken, user.getId(), user.getNome(), user.getEmail(),
                user.getCidade(), user.getWhatsapp(), user.getNivelProfissional(),
                user.getAreaTecnologia(), user.getCompetenciasAtuais());
    }

    private AuthDto.UserResponse toUserResponse(User user) {
        return new AuthDto.UserResponse(
                user.getId(), user.getNome(), user.getEmail(),
                user.getCidade(), user.getWhatsapp(), user.getNivelProfissional(),
                user.getAreaTecnologia(), user.getCompetenciasAtuais(),
                user.getCreatedAt(), user.getUpdatedAt());
    }

    private void saveSession(Long userId, String token, String refreshToken) {
        try {
            UserSession session = new UserSession();
            session.setUsuarioId(userId);
            session.setToken(token);
            session.setRefreshToken(refreshToken);
            session.setExpiresAt(LocalDateTime.now().plusDays(7));
            sessionRepository.save(session);
        } catch (Exception ex) {
            log.warn("[AuthService] Não foi possível salvar sessão no banco, usando fallback");
            fallbackStorage.saveSession(userId, token, refreshToken, LocalDateTime.now().plusDays(7));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS (Fallback → resposta)
    // ════════════════════════════════════════════════════════════════════════

    private AuthDto.Response buildAuthResponseFromFallback(FallbackStorage.UserRecord record) {
        String token = jwtUtil.generateToken(record.id(), record.email());
        String refreshToken = jwtUtil.generateRefreshToken(record.id(), record.email());
        fallbackStorage.saveSession(record.id(), token, refreshToken, LocalDateTime.now().plusDays(7));
        return new AuthDto.Response(
                token, refreshToken, record.id(), record.nome(), record.email(),
                record.cidade(), record.whatsapp(), record.nivelProfissional(),
                record.areaTecnologia(), record.competenciasAtuais());
    }

    private AuthDto.UserResponse toUserResponseFromFallback(FallbackStorage.UserRecord record) {
        return new AuthDto.UserResponse(
                record.id(), record.nome(), record.email(),
                record.cidade(), record.whatsapp(), record.nivelProfissional(),
                record.areaTecnologia(), record.competenciasAtuais(),
                record.createdAt(), record.updatedAt());
    }
}
