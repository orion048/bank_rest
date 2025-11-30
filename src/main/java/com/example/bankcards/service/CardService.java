package com.example.bankcards.service;


import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    // Создание карты (ADMIN)
    public Card createCard(Card card, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        card.setOwner(owner);
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    // Блокировка карты (ADMIN)
    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
    }

    // Активация карты (ADMIN)
    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
    }

    // Удаление карты (ADMIN)
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    // Запрос блокировки (USER)
    @Transactional
    public void requestBlock(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.BLOCKED);
    }

    // Перевод между картами (USER)
    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        Card from = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card to = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Target card not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }


    // Просмотр карт (ADMIN видит все, USER только свои)
    public List<Card> getCardsForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ADMIN".equals(currentUser.getRole())) {
            return cardRepository.findAll();
        } else {
            return cardRepository.findByOwner(currentUser);
        }
    }
}

