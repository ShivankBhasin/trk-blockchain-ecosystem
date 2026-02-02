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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;
    private final IncomeRepository incomeRepository;
    private final CashbackRepository cashbackRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthResponse authenticateUser(AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow();

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .referralCode(user.getReferralCode())
                .build();
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        String referralCode = generateUniqueReferralCode();

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .username(signUpRequest.getUsername())
                .referralCode(referralCode)
                .referredBy(signUpRequest.getReferralCode())
                .build();

        user = userRepository.save(user);

        Cashback cashback = Cashback.builder()
                .userId(user.getId())
                .build();
        cashbackRepository.save(cashback);

        if (signUpRequest.getReferralCode() != null && !signUpRequest.getReferralCode().isEmpty()) {
            processReferral(user, signUpRequest.getReferralCode());
        }

        String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .referralCode(user.getReferralCode())
                .build();
    }

    private void processReferral(User newUser, String referrerCode) {
        User referrer = userRepository.findByReferralCode(referrerCode).orElse(null);
        if (referrer == null) return;

        referrer.setDirectReferrals(referrer.getDirectReferrals() + 1);
        userRepository.save(referrer);

        Referral directReferral = Referral.builder()
                .userId(referrer.getId())
                .referralId(newUser.getId())
                .level(1)
                .build();
        referralRepository.save(directReferral);

        BigDecimal practiceBonus = new BigDecimal("10.00");
        referrer.setPracticeBalance(referrer.getPracticeBalance().add(practiceBonus));
        userRepository.save(referrer);

        Income income = Income.builder()
                .userId(referrer.getId())
                .type(Income.IncomeType.PRACTICE_REFERRAL)
                .amount(practiceBonus)
                .sourceUserId(newUser.getId())
                .level(1)
                .description("Practice referral bonus from " + newUser.getUsername())
                .build();
        incomeRepository.save(income);

        buildReferralChain(referrer, newUser, 2);
    }

    private void buildReferralChain(User currentReferrer, User newUser, int level) {
        if (level > 15 || currentReferrer.getReferredBy() == null) return;

        User uplineReferrer = userRepository.findByReferralCode(currentReferrer.getReferredBy()).orElse(null);
        if (uplineReferrer == null) return;

        Referral referral = Referral.builder()
                .userId(uplineReferrer.getId())
                .referralId(newUser.getId())
                .level(level)
                .build();
        referralRepository.save(referral);

        BigDecimal bonus = getPracticeReferralBonus(level);
        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            uplineReferrer.setPracticeBalance(uplineReferrer.getPracticeBalance().add(bonus));
            userRepository.save(uplineReferrer);

            Income income = Income.builder()
                    .userId(uplineReferrer.getId())
                    .type(Income.IncomeType.PRACTICE_REFERRAL)
                    .amount(bonus)
                    .sourceUserId(newUser.getId())
                    .level(level)
                    .description("Level " + level + " practice referral bonus")
                    .build();
            incomeRepository.save(income);
        }

        buildReferralChain(uplineReferrer, newUser, level + 1);
    }

    private BigDecimal getPracticeReferralBonus(int level) {
        if (level >= 2 && level <= 5) return new BigDecimal("2.00");
        if (level >= 6 && level <= 10) return new BigDecimal("1.00");
        if (level >= 11 && level <= 15) return new BigDecimal("0.50");
        if (level >= 16 && level <= 50) return new BigDecimal("0.25");
        if (level >= 51 && level <= 100) return new BigDecimal("0.10");
        return BigDecimal.ZERO;
    }

    private String generateUniqueReferralCode() {
        String code;
        do {
            code = "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByReferralCode(code));
        return code;
    }
}
