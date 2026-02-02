package com.trk.blockchain.dto;

import com.trk.blockchain.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
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

    public static GameDTO fromEntity(Game game) {
        return GameDTO.builder()
                .id(game.getId())
                .gameType(game.getGameType() != null ? game.getGameType().name() : null)
                .betAmount(game.getBetAmount())
                .result(game.getResult() != null ? game.getResult().name() : null)
                .multiplier(game.getMultiplier())
                .payout(game.getPayout())
                .directPayout(game.getDirectPayout())
                .compoundPayout(game.getCompoundPayout())
                .selectedNumber(game.getSelectedNumber())
                .winningNumber(game.getWinningNumber())
                .timestamp(game.getTimestamp())
                .build();
    }
}
