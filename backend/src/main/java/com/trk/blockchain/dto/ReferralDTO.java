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
    private String referralCode;
    private String referralLink;
    private Integer totalReferrals;
    private Integer directReferrals;
    private BigDecimal totalEarnings;
    private List<ReferralMember> directMembers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralMember {
        private Long id;
        private String username;
        private Integer level;
        private BigDecimal deposits;
        private LocalDateTime joinedAt;
        private Boolean activated;
    }
}
