package com.trk.blockchain.service;

import com.trk.blockchain.dto.AuthRequest;
import com.trk.blockchain.dto.AuthResponse;
import com.trk.blockchain.dto.RegisterRequest;
import com.trk.blockchain.entity.Cashback;
import com.trk.blockchain.entity.Income;
import com.trk.blockchain.entity.Referral;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.exception.BadRequestException;
import com.trk.blockchain.repository.CashbackRepository;
import com.trk.blockchain.repository.IncomeRepository;
import com.trk.blockchain.repository.ReferralRepository;
import com.trk.blockchain.repository.UserRepository;
import com.trk.blockchain.security.JwtUtils;
import com.trk.blockchain.security.UserDetailsImpl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;
    private final IncomeRepository incomeRepository;
    private final CashbackRepository cashbackRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager, CashbackRepository cashbackRepository, PasswordEncoder encoder, IncomeRepository incomeRepository, JwtUtils jwtUtils, ReferralRepository referralRepository, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.cashbackRepository = cashbackRepository;
        this.encoder = encoder;
        this.incomeRepository = incomeRepository;
        this.jwtUtils = jwtUtils;
        this.referralRepository = referralRepository;
        this.userRepository = userRepository;
    }

    public AuthResponse authenticateUser(AuthRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.email,
                                loginRequest.password
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        User user =
                userRepository
                        .findByEmail(userDetails.email)
                        .orElseThrow();

        AuthResponse response = new AuthResponse();
        response.token = jwt;
        response.type = "Bearer";
        response.id = user.id;
        response.email = user.email;
        response.username = user.username;
        response.referralCode = user.referralCode;

        return response;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.email)) {
            throw new BadRequestException("Email is already in use");
        }

        if (userRepository.existsByUsername(signUpRequest.username)) {
            throw new BadRequestException("Username is already taken");
        }

        String referralCode = generateUniqueReferralCode();

        User user = new User();
        user.email = signUpRequest.email;
        user.password = encoder.encode(signUpRequest.password);
        user.username = signUpRequest.username;
        user.referralCode = referralCode;
        user.referredBy = signUpRequest.referralCode;
        user.directReferrals = 0;
        user.practiceBalance = BigDecimal.ZERO;

        user = userRepository.save(user);

        Cashback cashback = new Cashback();
        cashback.userId = user.id;
        cashbackRepository.save(cashback);

        if (signUpRequest.referralCode != null &&
                !signUpRequest.referralCode.isEmpty()) {
            processReferral(user, signUpRequest.referralCode);
        }

        String jwt =
                jwtUtils.generateTokenFromEmail(user.email);

        AuthResponse response = new AuthResponse();
        response.token = jwt;
        response.type = "Bearer";
        response.id = user.id;
        response.email = user.email;
        response.username = user.username;
        response.referralCode = user.referralCode;

        return response;
    }

    private void processReferral(User newUser, String referrerCode) {

        User referrer =
                userRepository
                        .findByReferralCode(referrerCode)
                        .orElse(null);

        if (referrer == null) return;

        referrer.directReferrals =
                referrer.directReferrals + 1;
        userRepository.save(referrer);

        Referral directReferral = new Referral();
        directReferral.userId = referrer.id;
        directReferral.referralId = newUser.id;
        directReferral.level = 1;
        referralRepository.save(directReferral);

        BigDecimal practiceBonus = new BigDecimal("10.00");

        referrer.practiceBalance =
                referrer.practiceBalance.add(practiceBonus);
        userRepository.save(referrer);

        Income income = new Income();
        income.userId = referrer.id;
        income.type = Income.IncomeType.PRACTICE_REFERRAL;
        income.amount = practiceBonus;
        income.sourceUserId = newUser.id;
        income.level = 1;
        income.description =
                "Practice referral bonus from " + newUser.username;
        incomeRepository.save(income);

        buildReferralChain(referrer, newUser, 2);
    }

    private void buildReferralChain(User currentReferrer,
                                    User newUser,
                                    int level) {

        if (level > 15 || currentReferrer.referredBy == null)
            return;

        User uplineReferrer =
                userRepository
                        .findByReferralCode(
                                currentReferrer.referredBy
                        )
                        .orElse(null);

        if (uplineReferrer == null) return;

        Referral referral = new Referral();
        referral.userId = uplineReferrer.id;
        referral.referralId = newUser.id;
        referral.level = level;
        referralRepository.save(referral);

        BigDecimal bonus = getPracticeReferralBonus(level);

        if (bonus.compareTo(BigDecimal.ZERO) > 0) {

            uplineReferrer.practiceBalance =
                    uplineReferrer.practiceBalance.add(bonus);
            userRepository.save(uplineReferrer);

            Income income = new Income();
            income.userId = uplineReferrer.id;
            income.type = Income.IncomeType.PRACTICE_REFERRAL;
            income.amount = bonus;
            income.sourceUserId = newUser.id;
            income.level = level;
            income.description =
                    "Level " + level + " practice referral bonus";
            incomeRepository.save(income);
        }

        buildReferralChain(uplineReferrer, newUser, level + 1);
    }

    // ================= BONUS TABLE =================
    private BigDecimal getPracticeReferralBonus(int level) {

        if (level >= 2 && level <= 5)
            return new BigDecimal("2.00");

        if (level >= 6 && level <= 10)
            return new BigDecimal("1.00");

        if (level >= 11 && level <= 15)
            return new BigDecimal("0.50");

        if (level >= 16 && level <= 50)
            return new BigDecimal("0.25");

        if (level >= 51 && level <= 100)
            return new BigDecimal("0.10");

        return BigDecimal.ZERO;
    }

    private String generateUniqueReferralCode() {

        String code;

        do {
            code =
                    "TRK" +
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                                    .toUpperCase();
        }
        while (userRepository.existsByReferralCode(code));

        return code;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
}