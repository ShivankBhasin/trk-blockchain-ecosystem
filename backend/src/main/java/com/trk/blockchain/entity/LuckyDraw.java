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
    private Long id;

    @Version
    private Long version;

    @Builder.Default
    private Integer totalTickets = 10000;

    @Builder.Default
    private Integer soldTickets = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DrawStatus status = DrawStatus.ACTIVE;

    private LocalDateTime drawDate;

    @Builder.Default
    private BigDecimal prizePool = new BigDecimal("70000");

    @Builder.Default
    private BigDecimal ticketPrice = new BigDecimal("10");

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DrawStatus {
        ACTIVE, COMPLETED, PENDING
    }
}
