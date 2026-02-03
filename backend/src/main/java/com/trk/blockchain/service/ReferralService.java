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

    public ReferralDTO getReferralInfo(User user) {
        List<User> directReferrals = userRepository.findByReferredBy(user.getReferralCode());
        Integer totalReferrals = referralRepository.countTotalReferrals(user.getId());
        BigDecimal totalEarnings = incomeRepository.sumTotalIncomeByUserId(user.getId());

        List<ReferralDTO.ReferralMember> members = directReferrals.stream()
                .map(ref -> ReferralDTO.ReferralMember.builder()
                        .id(ref.getId())
                        .username(ref.getUsername())
                        .level(1)
                        .deposits(ref.getTotalDeposits())
                        .joinedAt(ref.getRegistrationDate())
                        .activated(ref.getActivated())
                        .build())
                .collect(Collectors.toList());

        return ReferralDTO.builder()
                .referralCode(user.getReferralCode())
                .referralLink("https://trk.blockchain/ref/" + user.getReferralCode())
                .totalReferrals(totalReferrals != null ? totalReferrals : 0)
                .directReferrals(user.getDirectReferrals())
                .totalEarnings(totalEarnings != null ? totalEarnings : BigDecimal.ZERO)
                .directMembers(members)
                .build();
    }

    public List<Referral> getReferralsByLevel(Long userId, int level) {
        return referralRepository.findByUserIdAndLevel(userId, level);
    }

    public List<Referral> getAllReferrals(Long userId) {
        return referralRepository.findByUserId(userId);
    }
}
