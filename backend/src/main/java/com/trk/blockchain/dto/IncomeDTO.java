package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDTO {
    private BigDecimal totalIncome;
    private BigDecimal winnersIncome;
    private BigDecimal directLevelIncome;
    private BigDecimal winnerLevelIncome;
    private BigDecimal cashbackIncome;
    private BigDecimal roiOnRoiIncome;
    private BigDecimal clubIncome;
    private BigDecimal luckyDrawIncome;
    private List<IncomeHistory> recentIncomes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncomeHistory {
        private String type;
        private BigDecimal amount;
        private String source;
        private String timestamp;
    }
}
