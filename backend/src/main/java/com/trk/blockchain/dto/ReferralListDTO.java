package com.trk.blockchain.dto;

import com.trk.blockchain.entity.Referral;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralListDTO {
    private Long id;
    private Long userId;
    private Long referralId;
    private Integer level;
    private LocalDateTime createdAt;

    public static ReferralListDTO fromEntity(Referral referral) {
        return ReferralListDTO.builder()
                .id(referral.getId())
                .userId(referral.getUserId())
                .referralId(referral.getReferralId())
                .level(referral.getLevel())
                .createdAt(referral.getCreatedAt())
                .build();
    }
}
