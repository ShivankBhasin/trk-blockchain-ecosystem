package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private Long gameId;
    private String gameType;
    private BigDecimal betAmount;
    private String result;
    private Integer selectedNumber;
    private Integer winningNumber;
    private BigDecimal payout;
    private BigDecimal directPayout;
    private BigDecimal compoundPayout;
    private BigDecimal newBalance;
    private String message;
}
