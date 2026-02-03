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
public class WalletDTO {
    private BigDecimal practiceBalance;
    private BigDecimal cashBalance;
    private BigDecimal directWallet;
    private BigDecimal luckyDrawWallet;
    private BigDecimal totalBalance;
}
