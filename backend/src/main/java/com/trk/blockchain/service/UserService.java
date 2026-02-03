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

    private UserRepository userRepository;
    private GameRepository gameRepository;
    private ReferralRepository referralRepository;
    private IncomeRepository incomeRepository;
    private CashbackRepository cashbackRepository;

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserDTO getUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.id = user.id;
        userDTO.email = user.email;
        userDTO.username = user.username;
        userDTO.referralCode = user.referralCode;
        userDTO.referredBy = user.referredBy;
        userDTO.practiceBalance = user.practiceBalance;
        userDTO.cashBalance = user.cashBalance;
        userDTO.directWallet = user.directWallet;
        userDTO.luckyDrawWallet = user.luckyDrawWallet;
        userDTO.totalDeposits = user.totalDeposits;
        userDTO.totalLosses = user.totalLosses;
        userDTO.cashbackReceived = user.cashbackReceived;
        userDTO.totalWinnings = user.totalWinnings;
        userDTO.registrationDate = user.registrationDate;
        userDTO.activated = user.activated;
        userDTO.activationDate = user.activationDate;
        userDTO.directReferrals = user.directReferrals;
        return userDTO;
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

    public DashboardDTO getDashboard(User user) {
        Integer totalGames = gameRepository.countByUserId(user.id);
        Integer gamesWon = gameRepository.countWinsByUserId(user.id);
        Integer gamesLost = gameRepository.countLossesByUserId(user.id);

        BigDecimal winRate = BigDecimal.ZERO;
        if (totalGames > 0) {
            winRate = new BigDecimal(gamesWon)
                    .divide(new BigDecimal(totalGames), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        Integer totalTeamSize = referralRepository.countTotalReferrals(user.id);
        BigDecimal totalIncome = incomeRepository.sumTotalIncomeByUserId(user.id);

        long daysSinceRegistration = ChronoUnit.DAYS.between(user.registrationDate, LocalDateTime.now());
        int daysUntilExpiry = Math.max(0, 30 - (int) daysSinceRegistration);
        boolean isPracticeExpiring = !user.activated && daysUntilExpiry <= 7;

        Cashback cashback = cashbackRepository.findByUserId(user.id).orElse(null);
        DashboardDTO.CashbackInfo cashbackInfo = null;
        if (cashback != null) {
            BigDecimal remaining = cashback.maxCapping.subtract(cashback.totalReceived);
            
            DashboardDTO.CashbackInfo info = new DashboardDTO.CashbackInfo();
            info.active = cashback.active;
            info.totalLosses = cashback.totalLosses;
            info.dailyRate = cashback.dailyRate;
            info.totalReceived = cashback.totalReceived;
            info.maxCapping = cashback.maxCapping;
            info.remaining = remaining.max(BigDecimal.ZERO);
            cashbackInfo = info;
        }

        DashboardDTO dashboardDTO = new DashboardDTO();
        dashboardDTO.user = getUserDTO(user);
        dashboardDTO.wallet = getWalletDTO(user);
        dashboardDTO.totalIncome = totalIncome;
        dashboardDTO.totalGamesPlayed = totalGames;
        dashboardDTO.gamesWon = gamesWon;
        dashboardDTO.gamesLost = gamesLost;
        dashboardDTO.winRate = winRate;
        dashboardDTO.directReferrals = user.directReferrals;
        dashboardDTO.totalTeamSize = totalTeamSize != null ? totalTeamSize : 0;
        dashboardDTO.daysUntilExpiry = daysUntilExpiry;
        dashboardDTO.isPracticeExpiring = isPracticeExpiring;
        dashboardDTO.cashback = cashbackInfo;
        
        return dashboardDTO;
    }
}