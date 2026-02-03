package com.trk.blockchain.service;

import com.trk.blockchain.dto.DashboardDTO;
import com.trk.blockchain.dto.UserDTO;
import com.trk.blockchain.dto.WalletDTO;
import com.trk.blockchain.entity.Cashback;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.exception.ResourceNotFoundException;
import com.trk.blockchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final ReferralRepository referralRepository;
    private final IncomeRepository incomeRepository;
    private final CashbackRepository cashbackRepository;

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserDTO getUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .referralCode(user.getReferralCode())
                .referredBy(user.getReferredBy())
                .practiceBalance(user.getPracticeBalance())
                .cashBalance(user.getCashBalance())
                .directWallet(user.getDirectWallet())
                .luckyDrawWallet(user.getLuckyDrawWallet())
                .totalDeposits(user.getTotalDeposits())
                .totalLosses(user.getTotalLosses())
                .cashbackReceived(user.getCashbackReceived())
                .totalWinnings(user.getTotalWinnings())
                .registrationDate(user.getRegistrationDate())
                .activated(user.getActivated())
                .activationDate(user.getActivationDate())
                .directReferrals(user.getDirectReferrals())
                .build();
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

    public DashboardDTO getDashboard(User user) {
        Integer totalGames = gameRepository.countByUserId(user.getId());
        Integer gamesWon = gameRepository.countWinsByUserId(user.getId());
        Integer gamesLost = gameRepository.countLossesByUserId(user.getId());

        BigDecimal winRate = BigDecimal.ZERO;
        if (totalGames > 0) {
            winRate = new BigDecimal(gamesWon)
                    .divide(new BigDecimal(totalGames), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        Integer totalTeamSize = referralRepository.countTotalReferrals(user.getId());
        BigDecimal totalIncome = incomeRepository.sumTotalIncomeByUserId(user.getId());

        long daysSinceRegistration = ChronoUnit.DAYS.between(user.getRegistrationDate(), LocalDateTime.now());
        int daysUntilExpiry = Math.max(0, 30 - (int) daysSinceRegistration);
        boolean isPracticeExpiring = !user.getActivated() && daysUntilExpiry <= 7;

        Cashback cashback = cashbackRepository.findByUserId(user.getId()).orElse(null);
        DashboardDTO.CashbackInfo cashbackInfo = null;
        if (cashback != null) {
            BigDecimal remaining = cashback.getMaxCapping().subtract(cashback.getTotalReceived());
            cashbackInfo = DashboardDTO.CashbackInfo.builder()
                    .active(cashback.getActive())
                    .totalLosses(cashback.getTotalLosses())
                    .dailyRate(cashback.getDailyRate())
                    .totalReceived(cashback.getTotalReceived())
                    .maxCapping(cashback.getMaxCapping())
                    .remaining(remaining.max(BigDecimal.ZERO))
                    .build();
        }

        return DashboardDTO.builder()
                .user(getUserDTO(user))
                .wallet(getWalletDTO(user))
                .totalIncome(totalIncome)
                .totalGamesPlayed(totalGames)
                .gamesWon(gamesWon)
                .gamesLost(gamesLost)
                .winRate(winRate)
                .directReferrals(user.getDirectReferrals())
                .totalTeamSize(totalTeamSize != null ? totalTeamSize : 0)
                .daysUntilExpiry(daysUntilExpiry)
                .isPracticeExpiring(isPracticeExpiring)
                .cashback(cashbackInfo)
                .build();
    }
}
