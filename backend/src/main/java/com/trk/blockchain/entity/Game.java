package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    private BigDecimal betAmount;

    @Enumerated(EnumType.STRING)
    private GameResult result;

    @Builder.Default
    private BigDecimal multiplier = new BigDecimal("8");

    private BigDecimal payout;

    private BigDecimal directPayout;

    private BigDecimal compoundPayout;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Integer selectedNumber;

    private Integer winningNumber;

    public enum GameType {
        PRACTICE, CASH
    }

    public enum GameResult {
        WIN, LOSS
    }
}
