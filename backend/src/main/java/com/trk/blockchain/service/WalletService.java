package com.trk.blockchain.service;

import com.trk.blockchain.dto.DepositRequest;
import com.trk.blockchain.dto.TransferRequest;
import com.trk.blockchain.dto.WalletDTO;
import com.trk.blockchain.dto.WithdrawRequest;
import com.trk.blockchain.entity.Income;
import com.trk.blockchain.entity.Transaction;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.exception.BadRequestException;
import com.trk.blockchain.exception.InsufficientBalanceException;
import com.trk.blockchain.repository.IncomeRepository;
import com.trk.blockchain.repository.TransactionRepository;
import com.trk.blockchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncomeRepository incomeRepository;

    public WalletService(IncomeRepository incomeRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WalletDTO deposit(User user, DepositRequest request) {
        BigDecimal amount = request.amount;

        if (amount.compareTo(new BigDecimal("10")) < 0) {
            throw new BadRequestException("Minimum deposit is 10 USDT");
        }

        user.cashBalance = user.cashBalance.add(amount);
        user.totalDeposits = user.totalDeposits.add(amount);

        if (!user.activated && user.totalDeposits.compareTo(new BigDecimal("10")) >= 0) {
            user.activated = true;
            user.activationDate = LocalDateTime.now();
        }

        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.userId = user.id;
        transaction.type = Transaction.TransactionType.DEPOSIT;
        transaction.amount = amount;
        transaction.walletType = Transaction.WalletType.CASH;
        transaction.txHash = request.txHash;
        transaction.description = "USDT Deposit";
        transactionRepository.save(transaction);

        processDirectLevelIncome(user, amount);

        return getWalletDTO(user);
    }

    private void processDirectLevelIncome(User depositor, BigDecimal depositAmount) {
        if (depositor.referredBy == null) return;

        User referrer = userRepository.findByReferralCode(depositor.referredBy).orElse(null);
        if (referrer == null || !referrer.activated) return;

        processDirectLevelIncomeChain(referrer, depositor, depositAmount, 1);
    }

    private void processDirectLevelIncomeChain(User referrer, User depositor, BigDecimal depositAmount, int level) {
        if (level > 15 || referrer == null) return;

        if (referrer.activated && referrer.directReferrals >= level) {
            BigDecimal commissionRate = getDirectLevelCommissionRate(level);
            BigDecimal commission = depositAmount.multiply(commissionRate);

            referrer.directWallet = referrer.directWallet.add(commission);
            userRepository.save(referrer);

            Income income = new Income();
            income.userId = referrer.id;
            income.type = Income.IncomeType.DIRECT_LEVEL;
            income.amount = commission;
            income.sourceUserId = depositor.id;
            income.level = level;
            income.description = "Level " + level + " direct income from " + depositor.username;
            incomeRepository.save(income);

            Transaction transaction = new Transaction();
            transaction.userId = referrer.id;
            transaction.type = Transaction.TransactionType.REFERRAL_INCOME;
            transaction.amount = commission;
            transaction.walletType = Transaction.WalletType.DIRECT;
            transaction.description = "Direct level " + level + " income";
            transactionRepository.save(transaction);
        }

        if (referrer.referredBy != null) {
            User nextReferrer = userRepository.findByReferralCode(referrer.referredBy).orElse(null);
            processDirectLevelIncomeChain(nextReferrer, depositor, depositAmount, level + 1);
        }
    }

    private BigDecimal getDirectLevelCommissionRate(int level) {
        if (level == 1) return new BigDecimal("0.05");
        if (level == 2) return new BigDecimal("0.02");
        if (level >= 3 && level <= 5) return new BigDecimal("0.01");
        if (level >= 6 && level <= 15) return new BigDecimal("0.005");
        return BigDecimal.ZERO;
    }

    @Transactional
    public WalletDTO withdraw(User user, WithdrawRequest request) {
        BigDecimal amount = request.amount;
        BigDecimal withdrawalFee = new BigDecimal("2");

        if (amount.compareTo(new BigDecimal("10")) < 0) {
            throw new BadRequestException("Minimum withdrawal is 10 USDT");
        }

        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            throw new BadRequestException("Maximum withdrawal is 5000 USDT per day");
        }

        if (!user.activated) {
            throw new BadRequestException("Please activate your account to withdraw");
        }

        BigDecimal totalRequired = amount.add(withdrawalFee);
        if (user.directWallet.compareTo(totalRequired) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in direct wallet (amount + $2 fee required)");
        }

        BigDecimal netAmount = amount;

        user.directWallet = user.directWallet.subtract(totalRequired);
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.userId = user.id;
        transaction.type = Transaction.TransactionType.WITHDRAWAL;
        transaction.amount = netAmount;
        transaction.walletType = Transaction.WalletType.DIRECT;
        transaction.description = "Withdrawal to " + request.walletAddress + " (Fee: " + withdrawalFee + " USDT)";
        transactionRepository.save(transaction);

        return getWalletDTO(user);
    }

    @Transactional
    public WalletDTO transfer(User user, TransferRequest request) {
        BigDecimal amount = request.amount;
        String from = request.fromWallet.toUpperCase();
        String to = request.toWallet.toUpperCase();

        if (from.equals(to)) {
            throw new BadRequestException("Cannot transfer to the same wallet");
        }

        BigDecimal sourceBalance = getWalletBalance(user, from);
        if (sourceBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in " + from + " wallet");
        }

        if (from.equals("PRACTICE")) {
            if (user.totalDeposits.compareTo(new BigDecimal("100")) < 0) {
                throw new BadRequestException("Deposit 100+ USDT to transfer practice balance");
            }
        }

        deductFromWallet(user, from, amount);
        addToWallet(user, to, amount);

        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.userId = user.id;
        transaction.type = Transaction.TransactionType.TRANSFER;
        transaction.amount = amount;
        transaction.walletType = Transaction.WalletType.valueOf(to);
        transaction.description = "Transfer from " + from + " to " + to;
        transactionRepository.save(transaction);

        return getWalletDTO(user);
    }

    private BigDecimal getWalletBalance(User user, String walletType) {
        switch (walletType) {
            case "PRACTICE": return user.practiceBalance;
            case "CASH": return user.cashBalance;
            case "DIRECT": return user.directWallet;
            case "LUCKY_DRAW": return user.luckyDrawWallet;
            default: throw new BadRequestException("Invalid wallet type");
        }
    }

    private void deductFromWallet(User user, String walletType, BigDecimal amount) {
        switch (walletType) {
            case "PRACTICE":
                user.practiceBalance = user.practiceBalance.subtract(amount);
                break;
            case "CASH":
                user.cashBalance = user.cashBalance.subtract(amount);
                break;
            case "DIRECT":
                user.directWallet = user.directWallet.subtract(amount);
                break;
            case "LUCKY_DRAW":
                user.luckyDrawWallet = user.luckyDrawWallet.subtract(amount);
                break;
        }
    }

    private void addToWallet(User user, String walletType, BigDecimal amount) {
        switch (walletType) {
            case "PRACTICE":
                user.practiceBalance = user.practiceBalance.add(amount);
                break;
            case "CASH":
                user.cashBalance = user.cashBalance.add(amount);
                break;
            case "DIRECT":
                user.directWallet = user.directWallet.add(amount);
                break;
            case "LUCKY_DRAW":
                user.luckyDrawWallet = user.luckyDrawWallet.add(amount);
                break;
        }
    }

    public WalletDTO getWalletDTO(User user) {
        BigDecimal totalBalance = user.practiceBalance
                .add(user.cashBalance)
                .add(user.directWallet)
                .add(user.luckyDrawWallet);

        WalletDTO walletDTO = new WalletDTO();
        walletDTO.practiceBalance = user.practiceBalance;
        walletDTO.cashBalance = user.cashBalance;
        walletDTO.directWallet = user.directWallet;
        walletDTO.luckyDrawWallet = user.luckyDrawWallet;
        walletDTO.totalBalance = totalBalance;
        return walletDTO;
    }

    public List<Transaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}