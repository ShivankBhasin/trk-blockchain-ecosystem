package com.trk.blockchain.dto;

import com.trk.blockchain.entity.Referral;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReferralListDTO {

    public ReferralListDTO(Long id1, Long userId1, Long referralId1, Integer level1, LocalDateTime createdAt1) {
    }

    private Long id;
    private Long userId;
    private Long referralId;
    private Integer level;
    private LocalDateTime createdAt;

    public static class Builder {

        private Long id;
        private Long userId;
        private Long referralId;
        private Integer level;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder referralId(Long referralId) {
            this.referralId = referralId;
            return this;
        }

        public Builder level(Integer level) {
            this.level = level;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ReferralListDTO build() {
            return new ReferralListDTO(
                    id,
                    userId,
                    referralId,
                    level,
                    createdAt
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ReferralListDTO fromEntity(Referral referral) {

        return ReferralListDTO.builder()
                .id(referral.id)
                .userId(referral.userId)
                .referralId(referral.referralId)
                .level(referral.level)
                .createdAt(referral.createdAt)
                .build();
    }
}
