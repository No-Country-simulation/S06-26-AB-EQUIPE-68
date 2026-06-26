package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.dto.MentalHealthDto;
import com.bitsystem.bitapp.dto.SaudeDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FallbackStorage {

    private static final Logger log = LoggerFactory.getLogger(FallbackStorage.class);

    // ── Usuários ──────────────────────────────────────────────────────────
    private final Map<Long, UserRecord> users = new ConcurrentHashMap<>();
    private final Map<String, Long> emailToId = new ConcurrentHashMap<>();
    private final AtomicLong userIdSeq = new AtomicLong(1);

    // ── Sessões ───────────────────────────────────────────────────────────
    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    // ── Histórico de Saúde ────────────────────────────────────────────────
    private final Map<Long, List<SaudeRecord>> saudeHistory = new ConcurrentHashMap<>();

    // ── Assessments de Carreira ───────────────────────────────────────────
    private final Map<Long, List<AssessmentRecord>> assessments = new ConcurrentHashMap<>();

    // ── Registros de Saúde Mental (N8N) ──────────────────────────────────
    private final Map<Long, List<MentalHealthRecord>> mentalHealthRecords = new ConcurrentHashMap<>();

    // ════════════════════════════════════════════════════════════════════════
    //  RECORDS internos
    // ════════════════════════════════════════════════════════════════════════

    public record UserRecord(
        Long id, String nome, String email, String passwordHash,
        String cidade, String whatsapp, String nivelProfissional,
        String areaTecnologia, String competenciasAtuais,
        LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    public record SessionRecord(
        Long id, Long usuarioId, String token, String refreshToken,
        LocalDateTime expiresAt, LocalDateTime createdAt, boolean revoked
    ) {}

    public record SaudeRecord(
        Long id, Long usuarioId, String humor, Integer notaSemanal,
        String contexto, boolean derivouCvv, LocalDateTime createdAt
    ) {}

    public record AssessmentRecord(
        Long id, Long usuarioId, Integer compatibilidade, String nivel,
        List<String> pontosFortes, List<String> gaps,
        List<String> planoDesenvolvimento, LocalDateTime createdAt
    ) {}

    public record MentalHealthRecord(
        Long id, Long usuarioId, String nivel, String alerta,
        List<String> recomendacoes, List<String> acoes,
        List<String> canaisApoio, Boolean derivarCvv, Integer scoreRisco,
        LocalDateTime createdAt
    ) {}

    // ════════════════════════════════════════════════════════════════════════
    //  USER operations
    // ════════════════════════════════════════════════════════════════════════

    public UserRecord saveUser(String nome, String email, String passwordHash,
            String cidade, String whatsapp, String nivelProfissional,
            String areaTecnologia, String competenciasAtuais) {
        Long id = userIdSeq.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        UserRecord record = new UserRecord(
            id, nome, email, passwordHash,
            cidade, whatsapp, nivelProfissional,
            areaTecnologia, competenciasAtuais,
            now, now
        );
        users.put(id, record);
        emailToId.put(email.toLowerCase(), id);
        log.info("[FallbackStorage] Usuário salvo em memória: id={}, email={}", id, email);
        return record;
    }

    public Optional<UserRecord> findUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<UserRecord> findUserByEmail(String email) {
        Long id = emailToId.get(email.toLowerCase());
        return id != null ? Optional.ofNullable(users.get(id)) : Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return emailToId.containsKey(email.toLowerCase());
    }

    public List<UserRecord> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    public UserRecord updateUser(Long id, String nome, String cidade, String whatsapp,
            String nivelProfissional, String areaTecnologia, String competenciasAtuais) {
        UserRecord existing = users.get(id);
        if (existing == null) return null;
        UserRecord updated = new UserRecord(
            id,
            nome != null ? nome : existing.nome(),
            existing.email(),
            existing.passwordHash(),
            cidade != null ? cidade : existing.cidade(),
            whatsapp != null ? whatsapp : existing.whatsapp(),
            nivelProfissional != null ? nivelProfissional : existing.nivelProfissional(),
            areaTecnologia != null ? areaTecnologia : existing.areaTecnologia(),
            competenciasAtuais != null ? competenciasAtuais : existing.competenciasAtuais(),
            existing.createdAt(),
            LocalDateTime.now()
        );
        users.put(id, updated);
        return updated;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SESSION operations
    // ════════════════════════════════════════════════════════════════════════

    public void saveSession(Long usuarioId, String token, String refreshToken,
            LocalDateTime expiresAt) {
        SessionRecord record = new SessionRecord(
            (long) sessions.size() + 1, usuarioId, token, refreshToken,
            expiresAt, LocalDateTime.now(), false
        );
        sessions.put(token, record);
        log.info("[FallbackStorage] Sessão salva em memória: usuarioId={}", usuarioId);
    }

    public Optional<SessionRecord> findSessionByToken(String token) {
        return Optional.ofNullable(sessions.get(token));
    }

    public void revokeSession(String token) {
        SessionRecord existing = sessions.get(token);
        if (existing != null) {
            SessionRecord revoked = new SessionRecord(
                existing.id(), existing.usuarioId(), existing.token(),
                existing.refreshToken(), existing.expiresAt(),
                existing.createdAt(), true
            );
            sessions.put(token, revoked);
        }
    }

    public void deleteSessionsByUserId(Long userId) {
        sessions.values().removeIf(s -> s.usuarioId().equals(userId));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SAÚDE (check-in) operations
    // ════════════════════════════════════════════════════════════════════════

    public void saveSaudeRecord(Long usuarioId, String humor, Integer notaSemanal,
            String contexto, boolean derivouCvv) {
        long id = saudeHistory.values().stream().mapToLong(List::size).sum() + 1;
        SaudeRecord record = new SaudeRecord(
            id, usuarioId, humor, notaSemanal, contexto, derivouCvv, LocalDateTime.now()
        );
        saudeHistory.computeIfAbsent(usuarioId, k -> new ArrayList<>()).add(record);
        log.info("[FallbackStorage] Histórico de saúde salvo: usuarioId={}, humor={}", usuarioId, humor);
    }

    public List<SaudeRecord> findSaudeByUserId(Long usuarioId) {
        return saudeHistory.getOrDefault(usuarioId, Collections.emptyList());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ASSESSMENT operations
    // ════════════════════════════════════════════════════════════════════════

    public void saveAssessment(Long usuarioId, AssessmentDto.Response response) {
        long id = assessments.values().stream().mapToLong(List::size).sum() + 1;
        AssessmentRecord record = new AssessmentRecord(
            id, usuarioId, response.compatibilidade(), response.nivel(),
            response.pontosFortes(), response.gaps(),
            response.planoDesenvolvimento(), LocalDateTime.now()
        );
        assessments.computeIfAbsent(usuarioId, k -> new ArrayList<>()).add(record);
        log.info("[FallbackStorage] Assessment salvo: usuarioId={}", usuarioId);
    }

    public List<AssessmentRecord> findAssessmentsByUserId(Long usuarioId) {
        return assessments.getOrDefault(usuarioId, Collections.emptyList());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MENTAL HEALTH operations
    // ════════════════════════════════════════════════════════════════════════

    public void saveMentalHealthRecord(Long usuarioId, MentalHealthDto.Response response) {
        long id = mentalHealthRecords.values().stream().mapToLong(List::size).sum() + 1;
        MentalHealthRecord record = new MentalHealthRecord(
            id, usuarioId, response.nivel(), response.alerta(),
            response.recomendacoes(), response.acoes(),
            response.canaisApoio(), response.derivarCvv(),
            response.scoreRisco(), LocalDateTime.now()
        );
        mentalHealthRecords.computeIfAbsent(usuarioId, k -> new ArrayList<>()).add(record);
        log.info("[FallbackStorage] Registro de saúde mental salvo: usuarioId={}", usuarioId);
    }

    public List<MentalHealthRecord> findMentalHealthByUserId(Long usuarioId) {
        return mentalHealthRecords.getOrDefault(usuarioId, Collections.emptyList());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Stats
    // ════════════════════════════════════════════════════════════════════════

    public int getUserCount() {
        return users.size();
    }

    public Map<String, Object> getStats() {
        return Map.of(
            "users", users.size(),
            "sessions", sessions.size(),
            "saudeRecords", saudeHistory.values().stream().mapToInt(List::size).sum(),
            "assessments", assessments.values().stream().mapToInt(List::size).sum(),
            "mentalHealthRecords", mentalHealthRecords.values().stream().mapToInt(List::size).sum()
        );
    }
}
