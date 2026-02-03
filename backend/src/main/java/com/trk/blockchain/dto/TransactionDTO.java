package com.trk.blockchain.dto;

import com.trk.blockchain.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TransactionDTO {

    public TransactionDTO(Long id1, String type1, BigDecimal amount1, String walletType1, String description1, String txHash1, LocalDateTime timestamp1) {
    }

    private Long id;
    private String type;
    private BigDecimal amount;
    private String walletType;
    private String description;
    private String txHash;
    private LocalDateTime timestamp;

    public static class Builder {

        private Long id;
        private String type;
        private BigDecimal amount;
        private String walletType;
        private String description;
        private String txHash;
        private LocalDateTime timestamp;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder walletType(String walletType) {
            this.walletType = walletType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder txHash(String txHash) {
            this.txHash = txHash;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TransactionDTO build() {
            return new TransactionDTO(
                    id,
                    type,
                    amount,
                    walletType,
                    description,
                    txHash,
                    timestamp
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TransactionDTO fromEntity(Transaction transaction) {

        return TransactionDTO.builder()
                .id(transaction.id)
                .type(transaction.type != null ? transaction.type.name() : null)
                .amount(transaction.amount)
                .walletType(transaction.walletType != null ? transaction.walletType.name() : null)
                .description(transaction.description)
                .txHash(transaction.txHash)
                .timestamp(transaction.timestamp)
                .build();
    }
}