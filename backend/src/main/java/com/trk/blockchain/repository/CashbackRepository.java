package com.trk.blockchain.repository;

import com.trk.blockchain.entity.Cashback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashbackRepository extends JpaRepository<Cashback, Long> {
    Optional<Cashback> findByUserId(Long userId);

    @Query("SELECT c FROM Cashback c WHERE c.active = true AND (c.lastCreditDate IS NULL OR c.lastCreditDate < ?1)")
    List<Cashback> findActiveCashbacksDueForCredit(LocalDate today);

    @Query("SELECT c FROM Cashback c WHERE c.active = true AND c.totalReceived < c.maxCapping")
    List<Cashback> findActiveCashbacksNotCapped();
}
