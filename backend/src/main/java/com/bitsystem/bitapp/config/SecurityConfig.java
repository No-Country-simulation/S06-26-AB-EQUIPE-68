package com.bitsystem.bitapp.config;

import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.repository.UserSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtUtil jwtUtil, UserSessionRepository sessionRepository, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/*.html", "/*.css", "/css/**", "/js/**", "/img/**", "/assets/**", "/favicon.ico", "/manifest.json", "/sw.js").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/orientar").permitAll()
                .requestMatchers("/api/saude/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/rota").permitAll()
                .requestMatchers("/api/network-status/**").permitAll()
                .requestMatchers("/api/assessment/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/vagas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/vagas/*/match").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cursos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/roadmaps/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/lazer/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/mentores/**").permitAll()
                .requestMatchers("/api/mentorias/**").permitAll()
                .requestMatchers("/api/sugestoes/**").permitAll()
                .anyRequest().authenticated()
            )
            // Sem entry point o Spring responde 403 a quem não mandou token; o certo é 401,
            // que o frontend usa para levar o usuário de volta ao login.
            .exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, ex) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write(objectMapper.writeValueAsString(
                        StandardApiResponse.error("Autenticação necessária.", "NAO_AUTENTICADO")));
            }))
            .headers(headers -> headers.frameOptions(fo -> fo.disable()))
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        if (!jwtUtil.isTokenExpired(token)) {
                            var session = sessionRepository.findByToken(token);
                            if (session.isPresent() && !session.get().getRevoked()) {
                                var auth = new org.springframework.security.authentication
                                    .UsernamePasswordAuthenticationToken(
                                        jwtUtil.getEmailFromToken(token), null,
                                        java.util.Collections.emptyList());
                                org.springframework.security.core.context.SecurityContextHolder
                                    .getContext().setAuthentication(auth);
                            }
                        }
                    } catch (Exception ex) {
                        log.warn("JWT authentication failed: {}", ex.getMessage());
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
