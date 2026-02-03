package com.trk.blockchain.service;

import com.trk.blockchain.dto.GameRequest;
import com.trk.blockchain.dto.GameResponse;
import com.trk.blockchain.entity.*;
import com.trk.blockchain.exception.BadRequestException;
import com.trk.blockchain.exception.InsufficientBalanceException;
import com.trk.blockchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final TransactionRepository transactionRepository;
    private final ReferralRepository referralRepository;
    private final IncomeRepository incomeRepository;
    private final CashbackRepository cashbackRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    private static final BigDecimal MULTIPLIER = new BigDecimal("8");
    private static final double WIN_PROBABILITY = 0.125;

    @Transactional
    public GameResponse playGame(User user, GameRequest request) {
        Game.GameType gameType = Game.GameType.valueOf(request.getGameType().toUpperCase());
        BigDecimal betAmount = request.getBetAmount();

        validateBet(user, gameType, betAmount, request.getSelectedNumber());

        boolean isWin = secureRandom.nextDouble() < WIN_PROBABILITY;
        int winningNumber = isWin ? request.getSelectedNumber() : generateLosingNumber(request.getSelectedNumber());

        Game game = Game.builder()
                .userId(user.getId())
                .gameType(gameType)
                .betAmount(betAmount)
                .selectedNumber(request.getSelectedNumber())
                .winningNumber(winningNumber)
                .multiplier(MULTIPLIER)
                .build();

        BigDecimal newBalance;
        BigDecimal payout = BigDecimal.ZERO;
        BigDecimal directPayout = BigDecimal.ZERO;
        BigDecimal compoundPayout = BigDecimal.ZERO;
        String message;

        if (isWin) {
            game.setResult(Game.GameResult.WIN);
            payout = betAmount.multiply(MULTIPLIER);
            game.setPayout(payout);

            if (gameType == Game.GameType.PRACTICE) {
                user.setPracticeBalance(user.getPracticeBalance().add(payout).subtract(betAmount));
                newBalance = user.getPracticeBalance();
                message = "Congratulations! You won " + payout + " USDT!";
            } else {
                directPayout = payout.multiply(new BigDecimal("0.25"));
                compoundPayout = payout.multiply(new BigDecimal("0.75"));
                game.setDirectPayout(directPayout);
                game.setCompoundPayout(compoundPayout);

                user.setCashBalance(user.getCashBalance().subtract(betAmount).add(compoundPayout));
                user.setDirectWallet(user.getDirectWallet().add(directPayout));
                user.setTotalWinnings(user.getTotalWinnings().add(payout));
                newBalance = user.getCashBalance();
                message = "Congratulations! You won " + payout + " USDT! (2X to wallet, 6X compounded)";

                processWinnerLevelIncome(user, payout);
            }

            createTransaction(user.getId(), Transaction.TransactionType.GAME_WIN, payout,
                    gameType == Game.GameType.PRACTICE ? Transaction.WalletType.PRACTICE : Transaction.WalletType.CASH,
                    "Game win - " + MULTIPLIER + "x multiplier");

        } else {
            game.setResult(Game.GameResult.LOSS);
            game.setPayout(BigDecimal.ZERO);

            if (gameType == Game.GameType.PRACTICE) {
                user.setPracticeBalance(user.getPracticeBalance().subtract(betAmount));
                newBalance = user.getPracticeBalance();
            } else {
                user.setCashBalance(user.getCashBalance().subtract(betAmount));
                user.setTotalLosses(user.getTotalLosses().add(betAmount));
                newBalance = user.getCashBalance();

                updateCashback(user, betAmount);
            }

            message = "Better luck next time! The winning number was " + winningNumber;

            createTransaction(user.getId(), Transaction.TransactionType.GAME_LOSS, betAmount,
                    gameType == Game.GameType.PRACTICE ? Transaction.WalletType.PRACTICE : Transaction.WalletType.CASH,
                    "Game loss");
        }

        userRepository.save(user);
        gameRepository.save(game);

        return GameResponse.builder()
                .gameId(game.getId())
                .gameType(gameType.name())
                .betAmount(betAmount)
                .result(game.getResult().name())
                .selectedNumber(request.getSelectedNumber())
                .winningNumber(winningNumber)
                .payout(payout)
                .directPayout(directPayout)
                .compoundPayout(compoundPayout)
                .newBalance(newBalance)
                .message(message)
                .build();
    }

    private void validateBet(User user, Game.GameType gameType, BigDecimal betAmount, Integer selectedNumber) {
        if (betAmount.compareTo(BigDecimal.ONE) < 0) {
            throw new BadRequestException("Minimum bet amount is 1 USDT");
        }

        if (selectedNumber < 1 || selectedNumber > 8) {
            throw new BadRequestException("Selected number must be between 1 and 8");
        }

        BigDecimal balance = gameType == Game.GameType.PRACTICE ? user.getPracticeBalance() : user.getCashBalance();

        if (balance.compareTo(betAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this bet");
        }

        if (gameType == Game.GameType.CASH && !user.getActivated()) {
            throw new BadRequestException("Please activate your account with a minimum 10 USDT deposit to play cash games");
        }
    }

    private int generateLosingNumber(int selectedNumber) {
        int losingNumber;
        do {
            losingNumber = secureRandom.nextInt(8) + 1;
        } while (losingNumber == selectedNumber);
        return losingNumber;
    }

    private void processWinnerLevelIncome(User winner, BigDecimal winAmount) {
        if (winner.getReferredBy() == null) return;

        User referrer = userRepository.findByReferralCode(winner.getReferredBy()).orElse(null);
        if (referrer == null || !referrer.getActivated()) return;

        processWinnerIncomeChain(referrer, winner, winAmount, 1);
    }

    private void processWinnerIncomeChain(User referrer, User winner, BigDecimal winAmount, int level) {
        if (level > 15 || referrer == null) return;

        if (referrer.getActivated() && referrer.getDirectReferrals() >= level) {
            BigDecimal commissionRate = getWinnerLevelCommissionRate(level);
            BigDecimal commission = winAmount.multiply(commissionRate);

            referrer.setDirectWallet(referrer.getDirectWallet().add(commission));
            userRepository.save(referrer);

            Income income = Income.builder()
                    .userId(referrer.getId())
                    .type(Income.IncomeType.WINNER_LEVEL)
                    .amount(commission)
                    .sourceUserId(winner.getId())
                    .level(level)
                    .description("Level " + level + " winner income from " + winner.getUsername())
                    .build();
            incomeRepository.save(income);

            createTransaction(referrer.getId(), Transaction.TransactionType.REFERRAL_INCOME, commission,
                    Transaction.WalletType.DIRECT, "Winner level " + level + " income");
        }

        if (referrer.getReferredBy() != null) {
            User nextReferrer = userRepository.findByReferralCode(referrer.getReferredBy()).orElse(null);
            processWinnerIncomeChain(nextReferrer, winner, winAmount, level + 1);
        }
    }

    private BigDecimal getWinnerLevelCommissionRate(int level) {
        if (level == 1) return new BigDecimal("0.05");
        if (level == 2) return new BigDecimal("0.02");
        if (level >= 3 && level <= 5) return new BigDecimal("0.01");
        if (level >= 6 && level <= 15) return new BigDecimal("0.005");
        return BigDecimal.ZERO;
    }

    private void updateCashback(User user, BigDecimal lossAmount) {
        Cashback cashback = cashbackRepository.findByUserId(user.getId()).orElse(null);
        if (cashback == null) {
            cashback = Cashback.builder().userId(user.getId()).build();
        }

        cashback.setTotalLosses(cashback.getTotalLosses().add(lossAmount));

        if (cashback.getTotalLosses().compareTo(new BigDecimal("100")) >= 0 && !cashback.getActive()) {
            cashback.setActive(true);
            int directRefs = user.getDirectReferrals();
            int multiplier = 1;
            if (directRefs >= 20) multiplier = 8;
            else if (directRefs >= 10) multiplier = 4;
            else if (directRefs >= 5) multiplier = 2;

            cashback.setCappingMultiplier(multiplier);
            cashback.setMaxCapping(cashback.getTotalLosses().multiply(new BigDecimal(multiplier)));
        }

        cashbackRepository.save(cashback);
    }

    private void createTransaction(Long userId, Transaction.TransactionType type, BigDecimal amount,
                                   Transaction.WalletType walletType, String description) {
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .type(type)
                .amount(amount)
                .walletType(walletType)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }

    public List<Game> getGameHistory(Long userId) {
        return gameRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
