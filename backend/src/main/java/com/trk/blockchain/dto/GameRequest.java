package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {
    @NotNull
    public String gameType;

    @NotNull
    @Min(1)
    public BigDecimal betAmount;

    @NotNull
    @Min(value = 1, message = "Selected number must be between 1 and 8")
    @Max(value = 8, message = "Selected number must be between 1 and 8")
    public Integer selectedNumber;

    public BigDecimal getBetAmount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getSelectedNumber() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
