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

    private UserRepository userRepository;
    private GameRepository gameRepository;
    private TransactionRepository transactionRepository;
    private ReferralRepository referralRepository;
    private IncomeRepository incomeRepository;
    private CashbackRepository cashbackRepository;

    private SecureRandom secureRandom = new SecureRandom(); 
    private static final BigDecimal MULTIPLIER = new BigDecimal("8");
    private static final double WIN_PROBABILITY = 0.125;

    @Transactional
    public GameResponse playGame(User user, GameRequest request) {
        Game.GameType gameType = Game.GameType.valueOf(request.gameType.toUpperCase());
        BigDecimal betAmount = request.betAmount;

        validateBet(user, gameType, betAmount, request.selectedNumber);

        boolean isWin = secureRandom.nextDouble() < WIN_PROBABILITY;
        int winningNumber = isWin ? request.selectedNumber : generateLosingNumber(request.selectedNumber);

        Game game = new Game();
        game.userId = user.id;
        game.gameType = gameType;
        game.betAmount = betAmount;
        game.selectedNumber = request.selectedNumber;
        game.winningNumber = winningNumber;
        game.multiplier = MULTIPLIER;

        BigDecimal newBalance;
        BigDecimal payout = BigDecimal.ZERO;
        BigDecimal directPayout = BigDecimal.ZERO;
        BigDecimal compoundPayout = BigDecimal.ZERO;
        String message;

        if (isWin) {
            game.result = Game.GameResult.WIN;
            payout = betAmount.multiply(MULTIPLIER);
            game.payout = payout;

            if (gameType == Game.GameType.PRACTICE) {
                user.practiceBalance = user.practiceBalance.add(payout).subtract(betAmount);
                newBalance = user.practiceBalance;
                message = "Congratulations! You won " + payout + " USDT!";
            } else {
                directPayout = payout.multiply(new BigDecimal("0.25"));
                compoundPayout = payout.multiply(new BigDecimal("0.75"));
                game.directPayout = directPayout;
                game.compoundPayout = compoundPayout;

                user.cashBalance = user.cashBalance.subtract(betAmount).add(compoundPayout);
                user.directWallet = user.directWallet.add(directPayout);
                user.totalWinnings = user.totalWinnings.add(payout);
                newBalance = user.cashBalance;
                message = "Congratulations! You won " + payout + " USDT! (2X to wallet, 6X compounded)";

                processWinnerLevelIncome(user, payout);
            }

            createTransaction(user.id, Transaction.TransactionType.GAME_WIN, payout,
                    gameType == Game.GameType.PRACTICE ? Transaction.WalletType.PRACTICE : Transaction.WalletType.CASH,
                    "Game win - " + MULTIPLIER + "x multiplier");

        } else {
            game.result = Game.GameResult.LOSS;
            game.payout = BigDecimal.ZERO;

            if (gameType == Game.GameType.PRACTICE) {
                user.practiceBalance = user.practiceBalance.subtract(betAmount);
                newBalance = user.practiceBalance;
            } else {
                user.cashBalance = user.cashBalance.subtract(betAmount);
                user.totalLosses = user.totalLosses.add(betAmount);
                newBalance = user.cashBalance;

                updateCashback(user, betAmount);
            }

            message = "Better luck next time! The winning number was " + winningNumber;

            createTransaction(user.id, Transaction.TransactionType.GAME_LOSS, betAmount,
                    gameType == Game.GameType.PRACTICE ? Transaction.WalletType.PRACTICE : Transaction.WalletType.CASH,
                    "Game loss");
        }

        userRepository.save(user);
        gameRepository.save(game);

        GameResponse response = new GameResponse();
        response.gameId = game.id;
        response.gameType = gameType.name();
        response.betAmount = betAmount;
        response.result = game.result.name();
        response.selectedNumber = request.selectedNumber;
        response.winningNumber = winningNumber;
        response.payout = payout;
        response.directPayout = directPayout;
        response.compoundPayout = compoundPayout;
        response.newBalance = newBalance;
        response.message = message;

        return response;
    }

    private void validateBet(User user, Game.GameType gameType, BigDecimal betAmount, Integer selectedNumber) {
        if (betAmount.compareTo(BigDecimal.ONE) < 0) {
            throw new BadRequestException("Minimum bet amount is 1 USDT");
        }

        if (selectedNumber < 1 || selectedNumber > 8) {
            throw new BadRequestException("Selected number must be between 1 and 8");
        }

        BigDecimal balance = gameType == Game.GameType.PRACTICE ? user.practiceBalance : user.cashBalance;

        if (balance.compareTo(betAmount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this bet");
        }

        if (gameType == Game.GameType.CASH && !user.activated) {
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
        if (winner.referredBy == null) return;

        User referrer = userRepository.findByReferralCode(winner.referredBy).orElse(null);
        if (referrer == null || !referrer.activated) return;

        processWinnerIncomeChain(referrer, winner, winAmount, 1);
    }

    private void processWinnerIncomeChain(User referrer, User winner, BigDecimal winAmount, int level) {
        if (level > 15 || referrer == null) return;

        if (referrer.activated && referrer.directReferrals >= level) {
            BigDecimal commissionRate = getWinnerLevelCommissionRate(level);
            BigDecimal commission = winAmount.multiply(commissionRate);

            referrer.directWallet = referrer.directWallet.add(commission);
            userRepository.save(referrer);

            Income income = new Income();
            income.userId = referrer.id;
            income.type = Income.IncomeType.WINNER_LEVEL;
            income.amount = commission;
            income.sourceUserId = winner.id;
            income.level = level;
            income.description = "Level " + level + " winner income from " + winner.username;
            incomeRepository.save(income);

            createTransaction(referrer.id, Transaction.TransactionType.REFERRAL_INCOME, commission,
                    Transaction.WalletType.DIRECT, "Winner level " + level + " income");
        }

        if (referrer.referredBy != null) {
            User nextReferrer = userRepository.findByReferralCode(referrer.referredBy).orElse(null);
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
        Cashback cashback = cashbackRepository.findByUserId(user.id).orElse(null);
        if (cashback == null) {
            cashback = new Cashback();
            cashback.userId = user.id;
        }

        cashback.totalLosses = cashback.totalLosses.add(lossAmount);

        if (cashback.totalLosses.compareTo(new BigDecimal("100")) >= 0 && !cashback.active) {
            cashback.active = true;
            int directRefs = user.directReferrals;
            int multiplier = 1;
            if (directRefs >= 20) multiplier = 8;
            else if (directRefs >= 10) multiplier = 4;
            else if (directRefs >= 5) multiplier = 2;

            cashback.cappingMultiplier = multiplier;
            cashback.maxCapping = cashback.totalLosses.multiply(new BigDecimal(multiplier));
        }

        cashbackRepository.save(cashback);
    }

    private void createTransaction(Long userId, Transaction.TransactionType type, BigDecimal amount,
                                   Transaction.WalletType walletType, String description) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.type = type;
        transaction.amount = amount;
        transaction.walletType = walletType;
        transaction.description = description;
        transactionRepository.save(transaction);
    }

    public List<Game> getGameHistory(Long userId) {
        return gameRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}