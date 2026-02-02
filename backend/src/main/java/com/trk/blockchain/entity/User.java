package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String referralCode;

    private String referredBy;

    @Builder.Default
    private BigDecimal practiceBalance = new BigDecimal("100.00");

    @Builder.Default
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal directWallet = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal luckyDrawWallet = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalDeposits = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalLosses = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal cashbackReceived = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalWinnings = BigDecimal.ZERO;

    @Builder.Default
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Builder.Default
    private Boolean activated = false;

    private LocalDateTime activationDate;

    @Builder.Default
    private Integer directReferrals = 0;

    @Builder.Default
    private String role = "USER";

    public Long getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
