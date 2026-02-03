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
    public BigDecimal practiceBalance;
    public BigDecimal cashBalance;
    public BigDecimal directWallet;
    public BigDecimal luckyDrawWallet;
    public BigDecimal totalBalance;
}
