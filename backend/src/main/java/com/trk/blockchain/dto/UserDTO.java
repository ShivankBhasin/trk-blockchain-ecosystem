package com.trk.blockchain.dto;

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
public class UserDTO {
    public Long id;
    public String email;
    public String username;
    public String referralCode;
    public String referredBy;
    public BigDecimal practiceBalance;
    public BigDecimal cashBalance;
    public BigDecimal directWallet;
    public BigDecimal luckyDrawWallet;
    public BigDecimal totalDeposits;
    public BigDecimal totalLosses;
    public BigDecimal cashbackReceived;
    public BigDecimal totalWinnings;
    public LocalDateTime registrationDate;
    public Boolean activated;
    public LocalDateTime activationDate;
    public Integer directReferrals;
}
