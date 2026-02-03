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
    public Long id;

    public Long userId;

    @Enumerated(EnumType.STRING)
    public GameType gameType;

    public BigDecimal betAmount;

    @Enumerated(EnumType.STRING)
    public GameResult result;

    @Builder.Default
    public BigDecimal multiplier = new BigDecimal("8");

    public BigDecimal payout;

    public BigDecimal directPayout;

    public BigDecimal compoundPayout;

    @Builder.Default
    public LocalDateTime timestamp = LocalDateTime.now();

    public Integer selectedNumber;

    public Integer winningNumber;

    public void setResult(GameResult gameResult) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum GameType {
        PRACTICE, CASH
    }

    public enum GameResult {
        WIN, LOSS
    }
}
