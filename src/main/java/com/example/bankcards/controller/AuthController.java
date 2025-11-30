package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "Эндпоинты для регистрации и входа пользователей")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserService userService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создание нового пользователя с ролью USER. Доступно без авторизации."
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Parameter(description = "Данные для регистрации нового пользователя")
            @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(
            summary = "Вход пользователя",
            description = "Аутентификация по логину и паролю. Возвращает JWT токен для дальнейших запросов."
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Parameter(description = "Данные для входа: логин и пароль")
            @RequestBody LoginRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
        return ResponseEntity.ok(token);
    }
}


