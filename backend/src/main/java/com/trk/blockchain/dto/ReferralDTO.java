package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDTO {
    public String referralCode;
    public String referralLink;
    public Integer totalReferrals;
    public Integer directReferrals;
    public BigDecimal totalEarnings;
    public List<ReferralMember> directMembers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralMember {
        public Long id;
        public String username;
        public Integer level;
        public BigDecimal deposits;
        public LocalDateTime joinedAt;
        public Boolean activated;
    }
}
