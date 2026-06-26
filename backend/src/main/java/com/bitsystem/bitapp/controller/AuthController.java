package com.bitsystem.bitapp.controller;

import com.bitsystem.bitapp.dto.AuthDto;
import com.bitsystem.bitapp.dto.StandardApiResponse;
import com.bitsystem.bitapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<StandardApiResponse<AuthDto.Response>> register(
            @RequestBody @Valid AuthDto.RegisterRequest request) {
        AuthDto.Response response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardApiResponse.ok(response));
    }

    @PostMapping("/login")
    public ResponseEntity<StandardApiResponse<AuthDto.Response>> login(
            @RequestBody @Valid AuthDto.LoginRequest request) {
        AuthDto.Response response = authService.login(request);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<StandardApiResponse<AuthDto.UserResponse>> me(Authentication authentication) {
        AuthDto.UserResponse response = authService.me(authentication.getName());
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<StandardApiResponse<AuthDto.UserResponse>> updateProfile(
            Authentication authentication,
            @RequestBody @Valid AuthDto.ProfileUpdateRequest request) {
        AuthDto.UserResponse response = authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(StandardApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(StandardApiResponse.ok(null));
    }
}
