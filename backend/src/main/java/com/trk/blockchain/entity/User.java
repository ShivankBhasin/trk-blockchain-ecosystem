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
    public Long id;

    @Version
    private Long version;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String password;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(unique = true, nullable = false)
    public String referralCode;

    public String referredBy;

    @Builder.Default
    public BigDecimal practiceBalance = new BigDecimal("100.00");

    @Builder.Default
    public BigDecimal cashBalance = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal directWallet = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal luckyDrawWallet = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal totalDeposits = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal totalLosses = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal cashbackReceived = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal totalWinnings = BigDecimal.ZERO;

    @Builder.Default
    public LocalDateTime registrationDate = LocalDateTime.now();

    @Builder.Default
    public Boolean activated = false;

    public LocalDateTime activationDate;

    @Builder.Default
    public Integer directReferrals = 0;

    @Builder.Default
    private String role = "USER";

    public Long getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRole() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getDirectWallet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getCashbackReceived() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getPracticeBalance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
