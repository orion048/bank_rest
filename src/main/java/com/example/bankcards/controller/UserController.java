package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@Tag(name = "Пользователи", description = "Эндпоинты для управления пользователями (только ADMIN)")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Создать пользователя", description = "Создание нового пользователя (только ADMIN)")
    @PostMapping
    public ResponseEntity<User> createUser(
            @Parameter(description = "Данные нового пользователя") @RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @Operation(summary = "Список пользователей", description = "Получение списка всех пользователей (только ADMIN)")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Удалить пользователя", description = "Удаление пользователя по ID (только ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя для удаления") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Текущий пользователь", description = "Получение информации о текущем авторизованном пользователе")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getCurrentUser(principal.getName()));
    }
}

