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

    @Transactional
    public WalletDTO deposit(User user, DepositRequest request) {
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(new BigDecimal("10")) < 0) {
            throw new BadRequestException("Minimum deposit is 10 USDT");
        }

        user.setCashBalance(user.getCashBalance().add(amount));
        user.setTotalDeposits(user.getTotalDeposits().add(amount));

        if (!user.getActivated() && user.getTotalDeposits().compareTo(new BigDecimal("10")) >= 0) {
            user.setActivated(true);
            user.setActivationDate(LocalDateTime.now());
        }

        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .userId(user.getId())
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(amount)
                .walletType(Transaction.WalletType.CASH)
                .txHash(request.getTxHash())
                .description("USDT Deposit")
                .build();
        transactionRepository.save(transaction);

        processDirectLevelIncome(user, amount);

        return getWalletDTO(user);
    }

    private void processDirectLevelIncome(User depositor, BigDecimal depositAmount) {
        if (depositor.getReferredBy() == null) return;

        User referrer = userRepository.findByReferralCode(depositor.getReferredBy()).orElse(null);
        if (referrer == null || !referrer.getActivated()) return;

        processDirectLevelIncomeChain(referrer, depositor, depositAmount, 1);
    }

    private void processDirectLevelIncomeChain(User referrer, User depositor, BigDecimal depositAmount, int level) {
        if (level > 15 || referrer == null) return;

        if (referrer.getActivated() && referrer.getDirectReferrals() >= level) {
            BigDecimal commissionRate = getDirectLevelCommissionRate(level);
            BigDecimal commission = depositAmount.multiply(commissionRate);

            referrer.setDirectWallet(referrer.getDirectWallet().add(commission));
            userRepository.save(referrer);

            Income income = Income.builder()
                    .userId(referrer.getId())
                    .type(Income.IncomeType.DIRECT_LEVEL)
                    .amount(commission)
                    .sourceUserId(depositor.getId())
                    .level(level)
                    .description("Level " + level + " direct income from " + depositor.getUsername())
                    .build();
            incomeRepository.save(income);

            Transaction transaction = Transaction.builder()
                    .userId(referrer.getId())
                    .type(Transaction.TransactionType.REFERRAL_INCOME)
                    .amount(commission)
                    .walletType(Transaction.WalletType.DIRECT)
                    .description("Direct level " + level + " income")
                    .build();
            transactionRepository.save(transaction);
        }

        if (referrer.getReferredBy() != null) {
            User nextReferrer = userRepository.findByReferralCode(referrer.getReferredBy()).orElse(null);
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
        BigDecimal amount = request.getAmount();
        BigDecimal withdrawalFee = new BigDecimal("2");

        if (amount.compareTo(new BigDecimal("10")) < 0) {
            throw new BadRequestException("Minimum withdrawal is 10 USDT");
        }

        if (amount.compareTo(new BigDecimal("5000")) > 0) {
            throw new BadRequestException("Maximum withdrawal is 5000 USDT per day");
        }

        if (!user.getActivated()) {
            throw new BadRequestException("Please activate your account to withdraw");
        }

        BigDecimal totalRequired = amount.add(withdrawalFee);
        if (user.getDirectWallet().compareTo(totalRequired) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in direct wallet (amount + $2 fee required)");
        }

        BigDecimal netAmount = amount;

        user.setDirectWallet(user.getDirectWallet().subtract(totalRequired));
        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .userId(user.getId())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(netAmount)
                .walletType(Transaction.WalletType.DIRECT)
                .description("Withdrawal to " + request.getWalletAddress() + " (Fee: " + withdrawalFee + " USDT)")
                .build();
        transactionRepository.save(transaction);

        return getWalletDTO(user);
    }

    @Transactional
    public WalletDTO transfer(User user, TransferRequest request) {
        BigDecimal amount = request.getAmount();
        String from = request.getFromWallet().toUpperCase();
        String to = request.getToWallet().toUpperCase();

        if (from.equals(to)) {
            throw new BadRequestException("Cannot transfer to the same wallet");
        }

        BigDecimal sourceBalance = getWalletBalance(user, from);
        if (sourceBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in " + from + " wallet");
        }

        if (from.equals("PRACTICE")) {
            if (user.getTotalDeposits().compareTo(new BigDecimal("100")) < 0) {
                throw new BadRequestException("Deposit 100+ USDT to transfer practice balance");
            }
        }

        deductFromWallet(user, from, amount);
        addToWallet(user, to, amount);

        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .userId(user.getId())
                .type(Transaction.TransactionType.TRANSFER)
                .amount(amount)
                .walletType(Transaction.WalletType.valueOf(to))
                .description("Transfer from " + from + " to " + to)
                .build();
        transactionRepository.save(transaction);

        return getWalletDTO(user);
    }

    private BigDecimal getWalletBalance(User user, String walletType) {
        switch (walletType) {
            case "PRACTICE": return user.getPracticeBalance();
            case "CASH": return user.getCashBalance();
            case "DIRECT": return user.getDirectWallet();
            case "LUCKY_DRAW": return user.getLuckyDrawWallet();
            default: throw new BadRequestException("Invalid wallet type");
        }
    }

    private void deductFromWallet(User user, String walletType, BigDecimal amount) {
        switch (walletType) {
            case "PRACTICE":
                user.setPracticeBalance(user.getPracticeBalance().subtract(amount));
                break;
            case "CASH":
                user.setCashBalance(user.getCashBalance().subtract(amount));
                break;
            case "DIRECT":
                user.setDirectWallet(user.getDirectWallet().subtract(amount));
                break;
            case "LUCKY_DRAW":
                user.setLuckyDrawWallet(user.getLuckyDrawWallet().subtract(amount));
                break;
        }
    }

    private void addToWallet(User user, String walletType, BigDecimal amount) {
        switch (walletType) {
            case "PRACTICE":
                user.setPracticeBalance(user.getPracticeBalance().add(amount));
                break;
            case "CASH":
                user.setCashBalance(user.getCashBalance().add(amount));
                break;
            case "DIRECT":
                user.setDirectWallet(user.getDirectWallet().add(amount));
                break;
            case "LUCKY_DRAW":
                user.setLuckyDrawWallet(user.getLuckyDrawWallet().add(amount));
                break;
        }
    }

    public WalletDTO getWalletDTO(User user) {
        BigDecimal totalBalance = user.getPracticeBalance()
                .add(user.getCashBalance())
                .add(user.getDirectWallet())
                .add(user.getLuckyDrawWallet());

        return WalletDTO.builder()
                .practiceBalance(user.getPracticeBalance())
                .cashBalance(user.getCashBalance())
                .directWallet(user.getDirectWallet())
                .luckyDrawWallet(user.getLuckyDrawWallet())
                .totalBalance(totalBalance)
                .build();
    }

    public List<Transaction> getTransactionHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
