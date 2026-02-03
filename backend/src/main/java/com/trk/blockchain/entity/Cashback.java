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

    public Long userId;

    @Builder.Default
    public BigDecimal totalLosses = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal dailyRate = new BigDecimal("0.005");

    @Builder.Default
    public Integer cappingMultiplier = 1;

    @Builder.Default
    public BigDecimal totalReceived = BigDecimal.ZERO;

    @Builder.Default
    public BigDecimal maxCapping = BigDecimal.ZERO;

    public LocalDate lastCreditDate;

    @Builder.Default
    public Boolean active = false;

    public Object getLastCreditDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getTotalReceived() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getTotalLosses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigDecimal getDailyRate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
