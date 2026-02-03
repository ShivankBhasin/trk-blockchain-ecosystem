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
    public Long id;

    public Long userId;

    public Long drawId;

    public Integer ticketNumber;

    @Builder.Default
    public LocalDateTime purchaseDate = LocalDateTime.now();

    @Builder.Default
    public Boolean isWinner = false;

    public Integer prizeRank;

    public BigDecimal prizeAmount;
}
