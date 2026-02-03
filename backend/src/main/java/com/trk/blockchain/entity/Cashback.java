package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cashbacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cashback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Builder.Default
    private BigDecimal totalLosses = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal dailyRate = new BigDecimal("0.005");

    @Builder.Default
    private Integer cappingMultiplier = 1;

    @Builder.Default
    private BigDecimal totalReceived = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal maxCapping = BigDecimal.ZERO;

    private LocalDate lastCreditDate;

    @Builder.Default
    private Boolean active = false;
}
