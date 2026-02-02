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
    private Long id;
    private String email;
    private String username;
    private String referralCode;
    private String referredBy;
    private BigDecimal practiceBalance;
    private BigDecimal cashBalance;
    private BigDecimal directWallet;
    private BigDecimal luckyDrawWallet;
    private BigDecimal totalDeposits;
    private BigDecimal totalLosses;
    private BigDecimal cashbackReceived;
    private BigDecimal totalWinnings;
    private LocalDateTime registrationDate;
    private Boolean activated;
    private LocalDateTime activationDate;
    private Integer directReferrals;
}
