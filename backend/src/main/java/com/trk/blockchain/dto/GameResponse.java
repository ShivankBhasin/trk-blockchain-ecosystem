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
    public Long gameId;
    public String gameType;
    public BigDecimal betAmount;
    public String result;
    public Integer selectedNumber;
    public Integer winningNumber;
    public BigDecimal payout;
    public BigDecimal directPayout;
    public BigDecimal compoundPayout;
    public BigDecimal newBalance;
    public String message;
}
