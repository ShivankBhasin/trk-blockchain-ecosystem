package com.trk.blockchain.service;

import com.trk.blockchain.dto.LuckyDrawDTO;
import com.trk.blockchain.entity.*;
import com.trk.blockchain.exception.BadRequestException;
import com.trk.blockchain.exception.InsufficientBalanceException;
import com.trk.blockchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LuckyDrawService {

    private final LuckyDrawRepository luckyDrawRepository;
    private final LuckyDrawTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncomeRepository incomeRepository;

    private static final BigDecimal TICKET_PRICE = new BigDecimal("10");
    private static final int TOTAL_TICKETS = 10000;
    private static final int TOTAL_WINNERS = 1000;

    public LuckyDrawDTO getCurrentDraw(User user) {
        LuckyDraw draw = getOrCreateActiveDraw();
        List<LuckyDrawTicket> userTickets = ticketRepository.findByUserIdAndDrawId(user.getId(), draw.getId());

        List<LuckyDrawDTO.TicketInfo> ticketInfos = userTickets.stream()
                .map(t -> LuckyDrawDTO.TicketInfo.builder()
                        .ticketId(t.getId())
                        .ticketNumber(t.getTicketNumber())
                        .purchaseDate(t.getPurchaseDate())
                        .isWinner(t.getIsWinner())
                        .prizeAmount(t.getPrizeAmount())
                        .build())
                .collect(Collectors.toList());

        List<LuckyDrawDTO.WinnerInfo> winners = new ArrayList<>();
        if (draw.getStatus() == LuckyDraw.DrawStatus.COMPLETED) {
            List<LuckyDrawTicket> winningTickets = ticketRepository.findByDrawIdAndIsWinnerTrue(draw.getId());
            winners = winningTickets.stream()
                    .map(t -> {
                        User winner = userRepository.findById(t.getUserId()).orElse(null);
                        return LuckyDrawDTO.WinnerInfo.builder()
                                .rank(t.getPrizeRank())
                                .username(winner != null ? winner.getUsername() : "Unknown")
                                .ticketNumber(t.getTicketNumber())
                                .prize(t.getPrizeAmount())
                                .build();
                    })
                    .sorted(Comparator.comparing(LuckyDrawDTO.WinnerInfo::getRank))
                    .collect(Collectors.toList());
        }

        return LuckyDrawDTO.builder()
                .drawId(draw.getId())
                .totalTickets(draw.getTotalTickets())
                .soldTickets(draw.getSoldTickets())
                .remainingTickets(draw.getTotalTickets() - draw.getSoldTickets())
                .status(draw.getStatus().name())
                .prizePool(draw.getPrizePool())
                .ticketPrice(draw.getTicketPrice())
                .drawDate(draw.getDrawDate())
                .myTickets(ticketInfos)
                .winners(winners)
                .build();
    }

    @Transactional
    public LuckyDrawDTO buyTicket(User user, int quantity) {
        if (quantity < 1 || quantity > 100) {
            throw new BadRequestException("You can buy 1-100 tickets at a time");
        }

        BigDecimal totalCost = TICKET_PRICE.multiply(new BigDecimal(quantity));

        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        BigDecimal availableBalance = freshUser.getLuckyDrawWallet().add(freshUser.getDirectWallet());
        if (availableBalance.compareTo(totalCost) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Need " + totalCost + " USDT");
        }

        LuckyDraw draw = luckyDrawRepository.findFirstByStatusWithLock(LuckyDraw.DrawStatus.ACTIVE)
                .orElseGet(this::createNewDraw);

        int remaining_tickets = draw.getTotalTickets() - draw.getSoldTickets();
        if (remaining_tickets < quantity) {
            throw new BadRequestException("Not enough tickets available. Only " + remaining_tickets + " remaining");
        }

        BigDecimal remaining = totalCost;
        if (freshUser.getLuckyDrawWallet().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fromLuckyDraw = freshUser.getLuckyDrawWallet().min(remaining);
            freshUser.setLuckyDrawWallet(freshUser.getLuckyDrawWallet().subtract(fromLuckyDraw));
            remaining = remaining.subtract(fromLuckyDraw);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            freshUser.setDirectWallet(freshUser.getDirectWallet().subtract(remaining));
        }

        draw.setSoldTickets(draw.getSoldTickets() + quantity);
        luckyDrawRepository.save(draw);

        Integer lastTicketNumber = ticketRepository.findMaxTicketNumberByDrawId(draw.getId());
        int nextTicketNumber = (lastTicketNumber != null ? lastTicketNumber : 0) + 1;

        for (int i = 0; i < quantity; i++) {
            LuckyDrawTicket ticket = LuckyDrawTicket.builder()
                    .userId(freshUser.getId())
                    .drawId(draw.getId())
                    .ticketNumber(nextTicketNumber + i)
                    .build();
            ticketRepository.save(ticket);
        }

        userRepository.save(freshUser);

        Transaction transaction = Transaction.builder()
                .userId(freshUser.getId())
                .type(Transaction.TransactionType.LUCKY_DRAW_ENTRY)
                .amount(totalCost)
                .walletType(Transaction.WalletType.LUCKY_DRAW)
                .description("Purchased " + quantity + " lucky draw ticket(s)")
                .build();
        transactionRepository.save(transaction);

        boolean shouldExecuteDraw = draw.getSoldTickets() >= draw.getTotalTickets()
                && draw.getStatus() == LuckyDraw.DrawStatus.ACTIVE;
        if (shouldExecuteDraw) {
            draw.setStatus(LuckyDraw.DrawStatus.PENDING);
            luckyDrawRepository.save(draw);
            executeDraw(draw);
        }

        return getCurrentDraw(freshUser);
    }

    private LuckyDraw getOrCreateActiveDraw() {
        return luckyDrawRepository.findFirstByStatusOrderByCreatedAtDesc(LuckyDraw.DrawStatus.ACTIVE)
                .orElseGet(this::createNewDraw);
    }

    private synchronized LuckyDraw createNewDraw() {
        return luckyDrawRepository.findFirstByStatusOrderByCreatedAtDesc(LuckyDraw.DrawStatus.ACTIVE)
                .orElseGet(() -> {
                    LuckyDraw newDraw = LuckyDraw.builder()
                            .totalTickets(TOTAL_TICKETS)
                            .soldTickets(0)
                            .status(LuckyDraw.DrawStatus.ACTIVE)
                            .prizePool(new BigDecimal("70000"))
                            .ticketPrice(TICKET_PRICE)
                            .build();
                    return luckyDrawRepository.save(newDraw);
                });
    }

    @Transactional
    public void executeDraw(LuckyDraw draw) {
        List<LuckyDrawTicket> allTickets = ticketRepository.findByDrawId(draw.getId());
        Collections.shuffle(allTickets);

        Map<Integer, BigDecimal> prizeStructure = getPrizeStructure();

        for (int i = 0; i < Math.min(TOTAL_WINNERS, allTickets.size()); i++) {
            LuckyDrawTicket ticket = allTickets.get(i);
            ticket.setIsWinner(true);
            ticket.setPrizeRank(i + 1);
            ticket.setPrizeAmount(getPrizeForRank(i + 1));
            ticketRepository.save(ticket);

            User winner = userRepository.findById(ticket.getUserId()).orElse(null);
            if (winner != null) {
                BigDecimal prize = ticket.getPrizeAmount();
                winner.setDirectWallet(winner.getDirectWallet().add(prize));
                userRepository.save(winner);

                Income income = Income.builder()
                        .userId(winner.getId())
                        .type(Income.IncomeType.LUCKY_DRAW)
                        .amount(prize)
                        .level(ticket.getPrizeRank())
                        .description("Lucky Draw Rank #" + ticket.getPrizeRank() + " Prize")
                        .build();
                incomeRepository.save(income);

                Transaction transaction = Transaction.builder()
                        .userId(winner.getId())
                        .type(Transaction.TransactionType.LUCKY_DRAW_WIN)
                        .amount(prize)
                        .walletType(Transaction.WalletType.DIRECT)
                        .description("Lucky Draw Win - Rank #" + ticket.getPrizeRank())
                        .build();
                transactionRepository.save(transaction);
            }
        }

        draw.setStatus(LuckyDraw.DrawStatus.COMPLETED);
        draw.setDrawDate(LocalDateTime.now());
        luckyDrawRepository.save(draw);

        LuckyDraw newDraw = LuckyDraw.builder()
                .totalTickets(TOTAL_TICKETS)
                .soldTickets(0)
                .status(LuckyDraw.DrawStatus.ACTIVE)
                .prizePool(new BigDecimal("70000"))
                .ticketPrice(TICKET_PRICE)
                .build();
        luckyDrawRepository.save(newDraw);
    }

    private BigDecimal getPrizeForRank(int rank) {
        if (rank == 1) return new BigDecimal("10000");
        if (rank == 2) return new BigDecimal("5000");
        if (rank == 3) return new BigDecimal("4000");
        if (rank >= 4 && rank <= 10) return new BigDecimal("1000");
        if (rank >= 11 && rank <= 50) return new BigDecimal("300");
        if (rank >= 51 && rank <= 100) return new BigDecimal("120");
        if (rank >= 101 && rank <= 500) return new BigDecimal("40");
        if (rank >= 501 && rank <= 1000) return new BigDecimal("20");
        return BigDecimal.ZERO;
    }

    private Map<Integer, BigDecimal> getPrizeStructure() {
        Map<Integer, BigDecimal> prizes = new HashMap<>();
        prizes.put(1, new BigDecimal("10000"));
        prizes.put(2, new BigDecimal("5000"));
        prizes.put(3, new BigDecimal("4000"));
        return prizes;
    }

    public List<LuckyDraw> getDrawHistory() {
        return luckyDrawRepository.findAllByOrderByCreatedAtDesc();
    }
}
