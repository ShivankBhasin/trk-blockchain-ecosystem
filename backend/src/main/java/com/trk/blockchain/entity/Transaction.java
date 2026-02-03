package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WalletType walletType;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String description;

    private String txHash;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, GAME_BET, GAME_WIN, GAME_LOSS, TRANSFER, REFERRAL_INCOME, CASHBACK, LUCKY_DRAW_ENTRY, LUCKY_DRAW_WIN
    }

    public enum WalletType {
        PRACTICE, CASH, DIRECT, LUCKY_DRAW
    }
}
