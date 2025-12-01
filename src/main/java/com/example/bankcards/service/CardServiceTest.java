package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        cardService = new CardService(cardRepository, userRepository);
    }

    @Test
    void createCard_assignsOwnerAndActiveStatus() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.USER);

        Card card = new Card();
        card.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        Card created = cardService.createCard(card, 1L);

        assertEquals(100L, created.getId());
        assertEquals(user, created.getOwner());
        assertEquals(CardStatus.ACTIVE, created.getStatus());
        verify(cardRepository).save(created);
    }

    @Test
    void blockCard_changesStatusToBlocked() {
        Card card = new Card();
        card.setId(10L);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        cardService.blockCard(10L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    @Test
    void activateCard_changesStatusToActive() {
        Card card = new Card();
        card.setId(20L);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(20L)).thenReturn(Optional.of(card));

        cardService.activateCard(20L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    void deleteCard_invokesRepositoryDelete() {
        cardService.deleteCard(30L);
        verify(cardRepository).deleteById(30L);
    }

    @Test
    void requestBlock_setsStatusBlocked() {
        Card card = new Card();
        card.setId(40L);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(40L)).thenReturn(Optional.of(card));

        cardService.requestBlock(40L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    @Test
    void transfer_movesFundsBetweenCards() {
        Card from = new Card();
        from.setId(50L);
        from.setBalance(new BigDecimal("100.00"));

        Card to = new Card();
        to.setId(60L);
        to.setBalance(new BigDecimal("50.00"));

        when(cardRepository.findById(50L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(60L)).thenReturn(Optional.of(to));

        cardService.transfer(50L, 60L, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("80.00"), to.getBalance());
    }

    @Test
    void transfer_throwsIfInsufficientFunds() {
        Card from = new Card();
        from.setId(70L);
        from.setBalance(new BigDecimal("10.00"));

        Card to = new Card();
        to.setId(80L);
        to.setBalance(new BigDecimal("20.00"));

        when(cardRepository.findById(70L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(80L)).thenReturn(Optional.of(to));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cardService.transfer(70L, 80L, new BigDecimal("50.00")));

        assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

    @Test
    void getCardsForCurrentUser_adminGetsAll_userGetsOwn() {
        // --- подготовка данных ---
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        User user = new User();
        user.setId(2L);
        user.setUsername("user");
        user.setRole(Role.USER);

        Card card1 = new Card();
        card1.setId(100L);
        card1.setOwner(user);

        Card card2 = new Card();
        card2.setId(200L);
        card2.setOwner(admin);

        // --- тест для ADMIN ---
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cardRepository.findAll()).thenReturn(List.of(card1, card2));

        List<Card> adminCards = cardService.getCardsForCurrentUser();

        assertEquals(2, adminCards.size()); // админ видит все карты

        // --- тест для USER ---
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null));

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(cardRepository.findByOwner(user)).thenReturn(List.of(card1));

        List<Card> userCards = cardService.getCardsForCurrentUser();

        assertEquals(1, userCards.size()); // пользователь видит только свои карты
        assertEquals(user, userCards.get(0).getOwner());
    }

}

