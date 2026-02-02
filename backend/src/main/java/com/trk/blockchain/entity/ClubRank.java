package com.trk.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "club_ranks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Integer rank;

    private BigDecimal targetVolume;

    private BigDecimal achievedVolume;

    private BigDecimal strongLegVolume;

    private BigDecimal otherLegsVolume;

    @Builder.Default
    private LocalDateTime achievedAt = LocalDateTime.now();

    @Builder.Default
    private Boolean qualified = false;
}
