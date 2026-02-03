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
    public Long drawId;
    public Integer totalTickets;
    public Integer soldTickets;
    public Integer remainingTickets;
    public String status;
    public BigDecimal prizePool;
    public BigDecimal ticketPrice;
    public LocalDateTime drawDate;
    public List<TicketInfo> myTickets;
    public List<WinnerInfo> winners;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfo {
        public Long ticketId;
        public Integer ticketNumber;
        public LocalDateTime purchaseDate;
        public Boolean isWinner;
        public BigDecimal prizeAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinnerInfo {
        public Integer rank;
        public String username;
        public Integer ticketNumber;
        public BigDecimal prize;
    }
}
