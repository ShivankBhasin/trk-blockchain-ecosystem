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
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public IncomeDTO getIncomeOverview(User user) {
        BigDecimal totalIncome = incomeRepository.sumTotalIncomeByUserId(user.id);
        BigDecimal directLevelIncome = incomeRepository.sumIncomeByUserIdAndType(user.id, Income.IncomeType.DIRECT_LEVEL);
        BigDecimal winnerLevelIncome = incomeRepository.sumIncomeByUserIdAndType(user.id, Income.IncomeType.WINNER_LEVEL);
        BigDecimal cashbackIncome = incomeRepository.sumIncomeByUserIdAndType(user.id, Income.IncomeType.CASHBACK);
        BigDecimal roiOnRoiIncome = incomeRepository.sumIncomeByUserIdAndType(user.id, Income.IncomeType.ROI_ON_ROI);
        BigDecimal clubIncome = incomeRepository.sumIncomeByUserIdAndType(user.id, Income.IncomeType.CLUB);
        BigDecimal luckyDrawIncome = incomeRepository.sumIncomeByUserIdAndType(user.id, Income.IncomeType.LUCKY_DRAW);

        List<Income> recentIncomes = incomeRepository.findByUserIdOrderByTimestampDesc(user.id);
        List<IncomeDTO.IncomeHistory> incomeHistory = recentIncomes.stream()
                .limit(20)
                .map(income -> {
                    IncomeDTO.IncomeHistory history = new IncomeDTO.IncomeHistory();
                    history.type = income.type.name();
                    history.amount = income.amount;
                    history.source = "Level " + income.level;
                    history.timestamp = income.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return history;
                })
                .collect(Collectors.toList());

        IncomeDTO incomeDTO = new IncomeDTO();
        incomeDTO.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        incomeDTO.winnersIncome = user.totalWinnings;
        incomeDTO.directLevelIncome = directLevelIncome != null ? directLevelIncome : BigDecimal.ZERO;
        incomeDTO.winnerLevelIncome = winnerLevelIncome != null ? winnerLevelIncome : BigDecimal.ZERO;
        incomeDTO.cashbackIncome = cashbackIncome != null ? cashbackIncome : BigDecimal.ZERO;
        incomeDTO.roiOnRoiIncome = roiOnRoiIncome != null ? roiOnRoiIncome : BigDecimal.ZERO;
        incomeDTO.clubIncome = clubIncome != null ? clubIncome : BigDecimal.ZERO;
        incomeDTO.luckyDrawIncome = luckyDrawIncome != null ? luckyDrawIncome : BigDecimal.ZERO;
        incomeDTO.recentIncomes = incomeHistory;

        return incomeDTO;
    }

    public List<Income> getIncomeHistory(Long userId) {
        return incomeRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}