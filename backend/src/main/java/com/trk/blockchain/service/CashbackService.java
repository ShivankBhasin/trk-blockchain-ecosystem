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

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processDailyCashback() {
        LocalDate today = LocalDate.now();
        List<Cashback> activeCashbacks = cashbackRepository.findActiveCashbacksNotCapped();

        for (Cashback cashback : activeCashbacks) {
            if (cashback.getLastCreditDate() != null && cashback.getLastCreditDate().isEqual(today)) {
                continue;
            }

            if (cashback.getTotalReceived().compareTo(cashback.getMaxCapping()) >= 0) {
                cashback.setActive(false);
                cashbackRepository.save(cashback);
                continue;
            }

            BigDecimal dailyAmount = cashback.getTotalLosses().multiply(cashback.getDailyRate());
            BigDecimal remainingCap = cashback.getMaxCapping().subtract(cashback.getTotalReceived());
            BigDecimal actualCredit = dailyAmount.min(remainingCap);

            if (actualCredit.compareTo(BigDecimal.ZERO) <= 0) continue;

            User user = userRepository.findById(cashback.getUserId()).orElse(null);
            if (user == null) continue;

            BigDecimal luckyDrawCredit = actualCredit.multiply(new BigDecimal("0.20"));
            BigDecimal directCredit = actualCredit.subtract(luckyDrawCredit);

            user.setDirectWallet(user.getDirectWallet().add(directCredit));
            user.setLuckyDrawWallet(user.getLuckyDrawWallet().add(luckyDrawCredit));
            user.setCashbackReceived(user.getCashbackReceived().add(actualCredit));
            userRepository.save(user);

            cashback.setTotalReceived(cashback.getTotalReceived().add(actualCredit));
            cashback.setLastCreditDate(today);
            cashbackRepository.save(cashback);

            Income income = Income.builder()
                    .userId(user.getId())
                    .type(Income.IncomeType.CASHBACK)
                    .amount(actualCredit)
                    .description("Daily cashback (80% direct, 20% lucky draw)")
                    .build();
            incomeRepository.save(income);

            Transaction transaction = Transaction.builder()
                    .userId(user.getId())
                    .type(Transaction.TransactionType.CASHBACK)
                    .amount(actualCredit)
                    .walletType(Transaction.WalletType.DIRECT)
                    .description("Daily cashback credit")
                    .build();
            transactionRepository.save(transaction);

            processRoiOnRoi(user, actualCredit);
        }
    }

    private void processRoiOnRoi(User user, BigDecimal cashbackAmount) {
        if (user.getReferredBy() == null) return;

        BigDecimal distributableAmount = cashbackAmount.multiply(new BigDecimal("0.50"));

        User referrer = userRepository.findByReferralCode(user.getReferredBy()).orElse(null);
        processRoiOnRoiChain(referrer, user, distributableAmount, 1);
    }

    private void processRoiOnRoiChain(User referrer, User source, BigDecimal distributableAmount, int level) {
        if (level > 15 || referrer == null || !referrer.getActivated()) return;

        BigDecimal commissionRate = getRoiOnRoiRate(level);
        BigDecimal commission = distributableAmount.multiply(commissionRate);

        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            referrer.setDirectWallet(referrer.getDirectWallet().add(commission));
            userRepository.save(referrer);

            Income income = Income.builder()
                    .userId(referrer.getId())
                    .type(Income.IncomeType.ROI_ON_ROI)
                    .amount(commission)
                    .sourceUserId(source.getId())
                    .level(level)
                    .description("ROI on ROI from " + source.getUsername() + " (Level " + level + ")")
                    .build();
            incomeRepository.save(income);
        }

        if (referrer.getReferredBy() != null) {
            User nextReferrer = userRepository.findByReferralCode(referrer.getReferredBy()).orElse(null);
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
        Cashback cashback = cashbackRepository.findByUserId(user.getId()).orElse(null);
        if (cashback == null || !cashback.getActive()) return;

        int directRefs = user.getDirectReferrals();
        int newMultiplier = 1;
        if (directRefs >= 20) newMultiplier = 8;
        else if (directRefs >= 10) newMultiplier = 4;
        else if (directRefs >= 5) newMultiplier = 2;

        if (newMultiplier > cashback.getCappingMultiplier()) {
            cashback.setCappingMultiplier(newMultiplier);
            cashback.setMaxCapping(cashback.getTotalLosses().multiply(new BigDecimal(newMultiplier)));
            cashbackRepository.save(cashback);
        }
    }
}
