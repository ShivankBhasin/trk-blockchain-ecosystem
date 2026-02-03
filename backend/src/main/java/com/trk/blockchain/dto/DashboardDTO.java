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
    public UserDTO user;
    public WalletDTO wallet;
    public BigDecimal totalIncome;
    public Integer totalGamesPlayed;
    public Integer gamesWon;
    public Integer gamesLost;
    public BigDecimal winRate;
    public Integer directReferrals;
    public Integer totalTeamSize;
    public Integer daysUntilExpiry;
    public Boolean isPracticeExpiring;
    public CashbackInfo cashback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashbackInfo {
        public Boolean active;
        public BigDecimal totalLosses;
        public BigDecimal dailyRate;
        public BigDecimal totalReceived;
        public BigDecimal maxCapping;
        public BigDecimal remaining;
    }
}
