package com.trk.blockchain.dto;

import com.trk.blockchain.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String walletType;
    private String description;
    private String txHash;
    private LocalDateTime timestamp;

    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType() != null ? transaction.getType().name() : null)
                .amount(transaction.getAmount())
                .walletType(transaction.getWalletType() != null ? transaction.getWalletType().name() : null)
                .description(transaction.getDescription())
                .txHash(transaction.getTxHash())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
