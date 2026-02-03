package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "incomes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private IncomeType type;

    private BigDecimal amount;

    private Long sourceUserId;

    private Integer level;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String description;

    public enum IncomeType {
        DIRECT_LEVEL, WINNER_LEVEL, CASHBACK, ROI_ON_ROI, CLUB, LUCKY_DRAW, PRACTICE_REFERRAL
    }
}
