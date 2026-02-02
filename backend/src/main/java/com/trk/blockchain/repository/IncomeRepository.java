package com.trk.blockchain.repository;

import com.trk.blockchain.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserIdOrderByTimestampDesc(Long userId);
    List<Income> findByUserIdAndType(Long userId, Income.IncomeType type);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = ?1")
    BigDecimal sumTotalIncomeByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = ?1 AND i.type = ?2")
    BigDecimal sumIncomeByUserIdAndType(Long userId, Income.IncomeType type);
}
