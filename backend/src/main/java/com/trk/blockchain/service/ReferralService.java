package com.trk.blockchain.service;

import com.trk.blockchain.dto.ReferralDTO;
import com.trk.blockchain.entity.Referral;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.repository.IncomeRepository;
import com.trk.blockchain.repository.ReferralRepository;
import com.trk.blockchain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;
    private final IncomeRepository incomeRepository;

    public ReferralService(IncomeRepository incomeRepository, ReferralRepository referralRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.referralRepository = referralRepository;
        this.userRepository = userRepository;
    }

    public ReferralDTO getReferralInfo(User user) {
        List<User> directReferrals = userRepository.findByReferredBy(user.referralCode);
        Integer totalReferrals = referralRepository.countTotalReferrals(user.id);
        BigDecimal totalEarnings = incomeRepository.sumTotalIncomeByUserId(user.id);

        List<ReferralDTO.ReferralMember> members = directReferrals.stream()
                .map(ref -> {
                    ReferralDTO.ReferralMember member = new ReferralDTO.ReferralMember();
                    member.id = ref.id;
                    member.username = ref.username;
                    member.level = 1;
                    member.deposits = ref.totalDeposits;
                    member.joinedAt = ref.registrationDate;
                    member.activated = ref.activated;
                    return member;
                })
                .collect(Collectors.toList());

        ReferralDTO referralDTO = new ReferralDTO();
        referralDTO.referralCode = user.referralCode;
        referralDTO.referralLink = "https://trk.blockchain/ref/" + user.referralCode;
        referralDTO.totalReferrals = totalReferrals != null ? totalReferrals : 0;
        referralDTO.directReferrals = user.directReferrals;
        referralDTO.totalEarnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
        referralDTO.directMembers = members;

        return referralDTO;
    }

    public List<Referral> getReferralsByLevel(Long userId, int level) {
        return referralRepository.findByUserIdAndLevel(userId, level);
    }

    public List<Referral> getAllReferrals(Long userId) {
        return referralRepository.findByUserId(userId);
    }
}