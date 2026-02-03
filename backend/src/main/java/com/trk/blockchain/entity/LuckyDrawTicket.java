package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lucky_draw_tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuckyDrawTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long drawId;

    private Integer ticketNumber;

    @Builder.Default
    private LocalDateTime purchaseDate = LocalDateTime.now();

    @Builder.Default
    private Boolean isWinner = false;

    private Integer prizeRank;

    private BigDecimal prizeAmount;
}
