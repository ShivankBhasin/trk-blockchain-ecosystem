package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuckyDrawDTO {
    private Long drawId;
    private Integer totalTickets;
    private Integer soldTickets;
    private Integer remainingTickets;
    private String status;
    private BigDecimal prizePool;
    private BigDecimal ticketPrice;
    private LocalDateTime drawDate;
    private List<TicketInfo> myTickets;
    private List<WinnerInfo> winners;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfo {
        private Long ticketId;
        private Integer ticketNumber;
        private LocalDateTime purchaseDate;
        private Boolean isWinner;
        private BigDecimal prizeAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinnerInfo {
        private Integer rank;
        private String username;
        private Integer ticketNumber;
        private BigDecimal prize;
    }
}
