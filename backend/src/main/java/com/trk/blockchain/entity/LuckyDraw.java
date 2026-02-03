package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lucky_draws")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuckyDraw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Version
    private Long version;

    @Builder.Default
    public Integer totalTickets = 10000;

    @Builder.Default
    public Integer soldTickets = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    public DrawStatus status = DrawStatus.ACTIVE;

    public LocalDateTime drawDate;

    @Builder.Default
    public BigDecimal prizePool = new BigDecimal("70000");

    @Builder.Default
    public BigDecimal ticketPrice = new BigDecimal("10");

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DrawStatus {
        ACTIVE, COMPLETED, PENDING
    }
}
