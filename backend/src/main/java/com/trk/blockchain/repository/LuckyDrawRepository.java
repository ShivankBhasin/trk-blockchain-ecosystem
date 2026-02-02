package com.trk.blockchain.repository;

import com.trk.blockchain.entity.LuckyDraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface LuckyDrawRepository extends JpaRepository<LuckyDraw, Long> {
    Optional<LuckyDraw> findFirstByStatusOrderByCreatedAtDesc(LuckyDraw.DrawStatus status);
    List<LuckyDraw> findByStatusOrderByCreatedAtDesc(LuckyDraw.DrawStatus status);
    List<LuckyDraw> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM LuckyDraw d WHERE d.id = :id")
    Optional<LuckyDraw> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM LuckyDraw d WHERE d.status = :status ORDER BY d.createdAt DESC")
    Optional<LuckyDraw> findFirstByStatusWithLock(@Param("status") LuckyDraw.DrawStatus status);
}
