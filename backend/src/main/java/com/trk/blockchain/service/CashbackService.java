package com.trk.blockchain.service;

import com.trk.blockchain.entity.*;
import com.trk.blockchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashbackService {

    private final CashbackRepository cashbackRepository;
    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final TransactionRepository transactionRepository;
    private final ReferralRepository referralRepository;

    public CashbackService(CashbackRepository cashbackRepository, IncomeRepository incomeRepository, ReferralRepository referralRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.cashbackRepository = cashbackRepository;
        this.incomeRepository = incomeRepository;
        this.referralRepository = referralRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processDailyCashback() {
        LocalDate today = LocalDate.now();
        List<Cashback> activeCashbacks = cashbackRepository.findActiveCashbacksNotCapped();

        for (Cashback cashback : activeCashbacks) {
            if (cashback.lastCreditDate != null && cashback.lastCreditDate.isEqual(today)) {
                continue;
            }

            if (cashback.totalReceived.compareTo(cashback.maxCapping) >= 0) {
                cashback.active = false;
                cashbackRepository.save(cashback);
                continue;
            }

            BigDecimal dailyAmount = cashback.totalLosses.multiply(cashback.dailyRate);
            BigDecimal remainingCap = cashback.maxCapping.subtract(cashback.totalReceived);
            BigDecimal actualCredit = dailyAmount.min(remainingCap);

            if (actualCredit.compareTo(BigDecimal.ZERO) <= 0) continue;

            User user = userRepository.findById(cashback.userId).orElse(null);
            if (user == null) continue;

            BigDecimal luckyDrawCredit = actualCredit.multiply(new BigDecimal("0.20"));
            BigDecimal directCredit = actualCredit.subtract(luckyDrawCredit);

            user.directWallet = user.directWallet.add(directCredit);
            user.luckyDrawWallet = user.luckyDrawWallet.add(luckyDrawCredit);
            user.cashbackReceived = user.cashbackReceived.add(actualCredit);
            userRepository.save(user);

            cashback.totalReceived = cashback.totalReceived.add(actualCredit);
            cashback.lastCreditDate = today;
            cashbackRepository.save(cashback);

            Income income = new Income();
            income.userId = user.id;
            income.type = Income.IncomeType.CASHBACK;
            income.amount = actualCredit;
            income.description = "Daily cashback (80% direct, 20% lucky draw)";
            incomeRepository.save(income);

            Transaction transaction = new Transaction();
            transaction.userId = user.id;
            transaction.type = Transaction.TransactionType.CASHBACK;
            transaction.amount = actualCredit;
            transaction.walletType = Transaction.WalletType.DIRECT;
            transaction.description = "Daily cashback credit";
            transactionRepository.save(transaction);

            processRoiOnRoi(user, actualCredit);
        }
    }

    private void processRoiOnRoi(User user, BigDecimal cashbackAmount) {
        if (user.referredBy == null) return;

        BigDecimal distributableAmount = cashbackAmount.multiply(new BigDecimal("0.50"));

        User referrer = userRepository.findByReferralCode(user.referredBy).orElse(null);
        processRoiOnRoiChain(referrer, user, distributableAmount, 1);
    }

    private void processRoiOnRoiChain(User referrer, User source, BigDecimal distributableAmount, int level) {
        if (level > 15 || referrer == null || !referrer.activated) return;

        BigDecimal commissionRate = getRoiOnRoiRate(level);
        BigDecimal commission = distributableAmount.multiply(commissionRate);

        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            referrer.directWallet = referrer.directWallet.add(commission);
            userRepository.save(referrer);

            Income income = new Income();
            income.userId = referrer.id;
            income.type = Income.IncomeType.ROI_ON_ROI;
            income.amount = commission;
            income.sourceUserId = source.id;
            income.level = level;
            income.description = "ROI on ROI from " + source.username + " (Level " + level + ")";
            incomeRepository.save(income);
        }

        if (referrer.referredBy != null) {
            User nextReferrer = userRepository.findByReferralCode(referrer.referredBy).orElse(null);
            processRoiOnRoiChain(nextReferrer, source, distributableAmount, level + 1);
        }
    }

    private BigDecimal getRoiOnRoiRate(int level) {
        if (level == 1) return new BigDecimal("0.20");
        if (level >= 2 && level <= 5) return new BigDecimal("0.10");
        if (level >= 6 && level <= 10) return new BigDecimal("0.05");
        if (level >= 11 && level <= 15) return new BigDecimal("0.03");
        return BigDecimal.ZERO;
    }

    public void updateCashbackTier(User user) {
        Cashback cashback = cashbackRepository.findByUserId(user.id).orElse(null);
        if (cashback == null || !cashback.active) return;

        int directRefs = user.directReferrals;
        int newMultiplier = 1;
        if (directRefs >= 20) newMultiplier = 8;
        else if (directRefs >= 10) newMultiplier = 4;
        else if (directRefs >= 5) newMultiplier = 2;

        if (newMultiplier > cashback.cappingMultiplier) {
            cashback.cappingMultiplier = newMultiplier;
            cashback.maxCapping = cashback.totalLosses.multiply(new BigDecimal(newMultiplier));
            cashbackRepository.save(cashback);
        }
    }
}