package com.trk.blockchain.dto;

import com.trk.blockchain.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class GameDTO {

    public GameDTO(Long id1, String gameType1, BigDecimal betAmount1, String result1, BigDecimal multiplier1, BigDecimal payout1, BigDecimal directPayout1, BigDecimal compoundPayout1, Integer selectedNumber1, Integer winningNumber1, LocalDateTime timestamp1) {
    }

    private Long id;
    private String gameType;
    private BigDecimal betAmount;
    private String result;
    private BigDecimal multiplier;
    private BigDecimal payout;
    private BigDecimal directPayout;
    private BigDecimal compoundPayout;
    private Integer selectedNumber;
    private Integer winningNumber;
    private LocalDateTime timestamp;

    // ---------- MANUAL BUILDER ----------
    public static class Builder {
        private Long id;
        private String gameType;
        private BigDecimal betAmount;
        private String result;
        private BigDecimal multiplier;
        private BigDecimal payout;
        private BigDecimal directPayout;
        private BigDecimal compoundPayout;
        private Integer selectedNumber;
        private Integer winningNumber;
        private LocalDateTime timestamp;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder gameType(String gameType) {
            this.gameType = gameType;
            return this;
        }

        public Builder betAmount(BigDecimal betAmount) {
            this.betAmount = betAmount;
            return this;
        }

        public Builder result(String result) {
            this.result = result;
            return this;
        }

        public Builder multiplier(BigDecimal multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public Builder payout(BigDecimal payout) {
            this.payout = payout;
            return this;
        }

        public Builder directPayout(BigDecimal directPayout) {
            this.directPayout = directPayout;
            return this;
        }

        public Builder compoundPayout(BigDecimal compoundPayout) {
            this.compoundPayout = compoundPayout;
            return this;
        }

        public Builder selectedNumber(Integer selectedNumber) {
            this.selectedNumber = selectedNumber;
            return this;
        }

        public Builder winningNumber(Integer winningNumber) {
            this.winningNumber = winningNumber;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public GameDTO build() {
            return new GameDTO(
                    id,
                    gameType,
                    betAmount,
                    result,
                    multiplier,
                    payout,
                    directPayout,
                    compoundPayout,
                    selectedNumber,
                    winningNumber,
                    timestamp
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GameDTO fromEntity(Game game) {

        return GameDTO.builder()
                .id(game.id)
                .gameType(game.gameType != null ? game.gameType.name() : null)
                .betAmount(game.betAmount)
                .result(game.result != null ? game.result.name() : null)
                .multiplier(game.multiplier)
                .payout(game.payout)
                .directPayout(game.directPayout)
                .compoundPayout(game.compoundPayout)
                .selectedNumber(game.selectedNumber)
                .winningNumber(game.winningNumber)
                .timestamp(game.timestamp)
                .build();
    }
}