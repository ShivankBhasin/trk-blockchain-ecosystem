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
public class DashboardDTO {
    private UserDTO user;
    private WalletDTO wallet;
    private BigDecimal totalIncome;
    private Integer totalGamesPlayed;
    private Integer gamesWon;
    private Integer gamesLost;
    private BigDecimal winRate;
    private Integer directReferrals;
    private Integer totalTeamSize;
    private Integer daysUntilExpiry;
    private Boolean isPracticeExpiring;
    private CashbackInfo cashback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashbackInfo {
        private Boolean active;
        private BigDecimal totalLosses;
        private BigDecimal dailyRate;
        private BigDecimal totalReceived;
        private BigDecimal maxCapping;
        private BigDecimal remaining;
    }
}
