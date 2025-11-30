package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Карты", description = "Эндпоинты для управления картами")
@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Создать карту", description = "Создание новой карты для пользователя (только ADMIN)")
    @PostMapping
    public ResponseEntity<Card> createCard(
            @Valid @RequestBody CardRequest request,
            @Parameter(description = "ID владельца карты") @RequestParam Long userId) {
        Card card = new Card();
        card.setCardNumber(request.getCardNumber());
        card.setExpirationDate(request.getExpirationDate());
        card.setBalance(request.getBalance());
        return ResponseEntity.ok(cardService.createCard(card, userId));
    }

    @Operation(summary = "Список карт", description = "ADMIN видит все карты, USER только свои")
    @GetMapping
    public ResponseEntity<List<Card>> getCards() {
        return ResponseEntity.ok(cardService.getCardsForCurrentUser());
    }

    @Operation(summary = "Просмотр карты", description = "Получение информации о карте по её ID")
    @GetMapping("/{id}")
    public ResponseEntity<Card> getCard(@Parameter(description = "ID карты") @PathVariable Long id) {
        return ResponseEntity.of(cardService.getCardsForCurrentUser()
                .stream().filter(c -> c.getId().equals(id)).findFirst());
    }

    @Operation(summary = "Блокировка карты", description = "Блокировка карты (только ADMIN)")
    @PutMapping("/{id}/block")
    public ResponseEntity<Void> blockCard(@Parameter(description = "ID карты") @PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Активация карты", description = "Активация карты (только ADMIN)")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(@Parameter(description = "ID карты") @PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удаление карты", description = "Удаление карты (только ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@Parameter(description = "ID карты") @PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Запрос блокировки карты", description = "Пользователь может запросить блокировку своей карты (USER)")
    @PutMapping("/{id}/request-block")
    public ResponseEntity<Void> requestBlock(@Parameter(description = "ID карты") @PathVariable Long id) {
        cardService.requestBlock(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перевод между картами", description = "Перевод средств между картами (USER)")
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @Parameter(description = "ID карты отправителя") @RequestParam Long fromCardId,
            @Parameter(description = "ID карты получателя") @RequestParam Long toCardId,
            @Parameter(description = "Сумма перевода") @RequestParam BigDecimal amount) {
        cardService.transfer(fromCardId, toCardId, amount);
        return ResponseEntity.ok().build();
    }
}
