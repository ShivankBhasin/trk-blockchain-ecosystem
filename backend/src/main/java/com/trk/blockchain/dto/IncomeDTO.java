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
    public BigDecimal totalIncome;
    public BigDecimal winnersIncome;
    public BigDecimal directLevelIncome;
    public BigDecimal winnerLevelIncome;
    public BigDecimal cashbackIncome;
    public BigDecimal roiOnRoiIncome;
    public BigDecimal clubIncome;
    public BigDecimal luckyDrawIncome;
    public List<IncomeHistory> recentIncomes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncomeHistory {
        public String type;
        public BigDecimal amount;
        public String source;
        public String timestamp;
    }
}
