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

    private LuckyDrawRepository luckyDrawRepository;
    private LuckyDrawTicketRepository ticketRepository;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private IncomeRepository incomeRepository;

    private static final BigDecimal TICKET_PRICE = new BigDecimal("10");
    private static final int TOTAL_TICKETS = 10000;
    private static final int TOTAL_WINNERS = 1000;

    public LuckyDrawDTO getCurrentDraw(User user) {
        LuckyDraw draw = getOrCreateActiveDraw();
        List<LuckyDrawTicket> userTickets = ticketRepository.findByUserIdAndDrawId(user.id, draw.id);

        List<LuckyDrawDTO.TicketInfo> ticketInfos = userTickets.stream()
                .map(t -> {
                    LuckyDrawDTO.TicketInfo ticketInfo = new LuckyDrawDTO.TicketInfo();
                    ticketInfo.ticketId = t.id;
                    ticketInfo.ticketNumber = t.ticketNumber;
                    ticketInfo.purchaseDate = t.purchaseDate;
                    ticketInfo.isWinner = t.isWinner;
                    ticketInfo.prizeAmount = t.prizeAmount;
                    return ticketInfo;
                })
                .collect(Collectors.toList());

        List<LuckyDrawDTO.WinnerInfo> winners = new ArrayList<>();
        if (draw.status == LuckyDraw.DrawStatus.COMPLETED) {
            List<LuckyDrawTicket> winningTickets = ticketRepository.findByDrawIdAndIsWinnerTrue(draw.id);
            winners = winningTickets.stream()
                    .map(t -> {
                        User winner = userRepository.findById(t.userId).orElse(null);
                        LuckyDrawDTO.WinnerInfo winnerInfo = new LuckyDrawDTO.WinnerInfo();
                        winnerInfo.rank = t.prizeRank;
                        winnerInfo.username = winner != null ? winner.username : "Unknown";
                        winnerInfo.ticketNumber = t.ticketNumber;
                        winnerInfo.prize = t.prizeAmount;
                        return winnerInfo;
                    })
                    .sorted(Comparator.comparing(w -> w.rank))
                    .collect(Collectors.toList());
        }

        LuckyDrawDTO luckyDrawDTO = new LuckyDrawDTO();
        luckyDrawDTO.drawId = draw.id;
        luckyDrawDTO.totalTickets = draw.totalTickets;
        luckyDrawDTO.soldTickets = draw.soldTickets;
        luckyDrawDTO.remainingTickets = draw.totalTickets - draw.soldTickets;
        luckyDrawDTO.status = draw.status.name();
        luckyDrawDTO.prizePool = draw.prizePool;
        luckyDrawDTO.ticketPrice = draw.ticketPrice;
        luckyDrawDTO.drawDate = draw.drawDate;
        luckyDrawDTO.myTickets = ticketInfos;
        luckyDrawDTO.winners = winners;

        return luckyDrawDTO;
    }

    @Transactional
    public LuckyDrawDTO buyTicket(User user, int quantity) {
        if (quantity < 1 || quantity > 100) {
            throw new BadRequestException("You can buy 1-100 tickets at a time");
        }

        BigDecimal totalCost = TICKET_PRICE.multiply(new BigDecimal(quantity));

        User freshUser = userRepository.findById(user.id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        BigDecimal availableBalance = freshUser.luckyDrawWallet.add(freshUser.directWallet);
        if (availableBalance.compareTo(totalCost) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Need " + totalCost + " USDT");
        }

        LuckyDraw draw = luckyDrawRepository.findFirstByStatusWithLock(LuckyDraw.DrawStatus.ACTIVE)
                .orElseGet(this::createNewDraw);

        int remaining_tickets = draw.totalTickets - draw.soldTickets;
        if (remaining_tickets < quantity) {
            throw new BadRequestException("Not enough tickets available. Only " + remaining_tickets + " remaining");
        }

        BigDecimal remaining = totalCost;
        if (freshUser.luckyDrawWallet.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fromLuckyDraw = freshUser.luckyDrawWallet.min(remaining);
            freshUser.luckyDrawWallet = freshUser.luckyDrawWallet.subtract(fromLuckyDraw);
            remaining = remaining.subtract(fromLuckyDraw);
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            freshUser.directWallet = freshUser.directWallet.subtract(remaining);
        }

        draw.soldTickets = draw.soldTickets + quantity;
        luckyDrawRepository.save(draw);

        Integer lastTicketNumber = ticketRepository.findMaxTicketNumberByDrawId(draw.id);
        int nextTicketNumber = (lastTicketNumber != null ? lastTicketNumber : 0) + 1;

        for (int i = 0; i < quantity; i++) {
            LuckyDrawTicket ticket = new LuckyDrawTicket();
            ticket.userId = freshUser.id;
            ticket.drawId = draw.id;
            ticket.ticketNumber = nextTicketNumber + i;
            ticketRepository.save(ticket);
        }

        userRepository.save(freshUser);

        Transaction transaction = new Transaction();
        transaction.userId = freshUser.id;
        transaction.type = Transaction.TransactionType.LUCKY_DRAW_ENTRY;
        transaction.amount = totalCost;
        transaction.walletType = Transaction.WalletType.LUCKY_DRAW;
        transaction.description = "Purchased " + quantity + " lucky draw ticket(s)";
        transactionRepository.save(transaction);

        boolean shouldExecuteDraw = draw.soldTickets >= draw.totalTickets
                && draw.status == LuckyDraw.DrawStatus.ACTIVE;
        if (shouldExecuteDraw) {
            draw.status = LuckyDraw.DrawStatus.PENDING;
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
                    LuckyDraw newDraw = new LuckyDraw();
                    newDraw.totalTickets = TOTAL_TICKETS;
                    newDraw.soldTickets = 0;
                    newDraw.status = LuckyDraw.DrawStatus.ACTIVE;
                    newDraw.prizePool = new BigDecimal("70000");
                    newDraw.ticketPrice = TICKET_PRICE;
                    return luckyDrawRepository.save(newDraw);
                });
    }

    @Transactional
    public void executeDraw(LuckyDraw draw) {
        List<LuckyDrawTicket> allTickets = ticketRepository.findByDrawId(draw.id);
        Collections.shuffle(allTickets);

        Map<Integer, BigDecimal> prizeStructure = getPrizeStructure();

        for (int i = 0; i < Math.min(TOTAL_WINNERS, allTickets.size()); i++) {
            LuckyDrawTicket ticket = allTickets.get(i);
            ticket.isWinner = true;
            ticket.prizeRank = i + 1;
            ticket.prizeAmount = getPrizeForRank(i + 1);
            ticketRepository.save(ticket);

            User winner = userRepository.findById(ticket.userId).orElse(null);
            if (winner != null) {
                BigDecimal prize = ticket.prizeAmount;
                winner.directWallet = winner.directWallet.add(prize);
                userRepository.save(winner);

                Income income = new Income();
                income.userId = winner.id;
                income.type = Income.IncomeType.LUCKY_DRAW;
                income.amount = prize;
                income.level = ticket.prizeRank;
                income.description = "Lucky Draw Rank #" + ticket.prizeRank + " Prize";
                incomeRepository.save(income);

                Transaction transaction = new Transaction();
                transaction.userId = winner.id;
                transaction.type = Transaction.TransactionType.LUCKY_DRAW_WIN;
                transaction.amount = prize;
                transaction.walletType = Transaction.WalletType.DIRECT;
                transaction.description = "Lucky Draw Win - Rank #" + ticket.prizeRank;
                transactionRepository.save(transaction);
            }
        }

        draw.status = LuckyDraw.DrawStatus.COMPLETED;
        draw.drawDate = LocalDateTime.now();
        luckyDrawRepository.save(draw);

        LuckyDraw newDraw = new LuckyDraw();
        newDraw.totalTickets = TOTAL_TICKETS;
        newDraw.soldTickets = 0;
        newDraw.status = LuckyDraw.DrawStatus.ACTIVE;
        newDraw.prizePool = new BigDecimal("70000");
        newDraw.ticketPrice = TICKET_PRICE;
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