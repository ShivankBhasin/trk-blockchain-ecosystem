package com.trk.blockchain.service;

import com.trk.blockchain.dto.IncomeDTO;
import com.trk.blockchain.entity.Income;
import com.trk.blockchain.entity.User;
import com.trk.blockchain.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeDTO getIncomeOverview(User user) {
        BigDecimal totalIncome = incomeRepository.sumTotalIncomeByUserId(user.getId());
        BigDecimal directLevelIncome = incomeRepository.sumIncomeByUserIdAndType(user.getId(), Income.IncomeType.DIRECT_LEVEL);
        BigDecimal winnerLevelIncome = incomeRepository.sumIncomeByUserIdAndType(user.getId(), Income.IncomeType.WINNER_LEVEL);
        BigDecimal cashbackIncome = incomeRepository.sumIncomeByUserIdAndType(user.getId(), Income.IncomeType.CASHBACK);
        BigDecimal roiOnRoiIncome = incomeRepository.sumIncomeByUserIdAndType(user.getId(), Income.IncomeType.ROI_ON_ROI);
        BigDecimal clubIncome = incomeRepository.sumIncomeByUserIdAndType(user.getId(), Income.IncomeType.CLUB);
        BigDecimal luckyDrawIncome = incomeRepository.sumIncomeByUserIdAndType(user.getId(), Income.IncomeType.LUCKY_DRAW);

        List<Income> recentIncomes = incomeRepository.findByUserIdOrderByTimestampDesc(user.getId());
        List<IncomeDTO.IncomeHistory> incomeHistory = recentIncomes.stream()
                .limit(20)
                .map(income -> IncomeDTO.IncomeHistory.builder()
                        .type(income.getType().name())
                        .amount(income.getAmount())
                        .source("Level " + income.getLevel())
                        .timestamp(income.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .build())
                .collect(Collectors.toList());

        return IncomeDTO.builder()
                .totalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .winnersIncome(user.getTotalWinnings())
                .directLevelIncome(directLevelIncome != null ? directLevelIncome : BigDecimal.ZERO)
                .winnerLevelIncome(winnerLevelIncome != null ? winnerLevelIncome : BigDecimal.ZERO)
                .cashbackIncome(cashbackIncome != null ? cashbackIncome : BigDecimal.ZERO)
                .roiOnRoiIncome(roiOnRoiIncome != null ? roiOnRoiIncome : BigDecimal.ZERO)
                .clubIncome(clubIncome != null ? clubIncome : BigDecimal.ZERO)
                .luckyDrawIncome(luckyDrawIncome != null ? luckyDrawIncome : BigDecimal.ZERO)
                .recentIncomes(incomeHistory)
                .build();
    }

    public List<Income> getIncomeHistory(Long userId) {
        return incomeRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
