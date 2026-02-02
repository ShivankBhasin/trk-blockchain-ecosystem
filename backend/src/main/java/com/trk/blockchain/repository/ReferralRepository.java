package com.trk.blockchain.repository;

import com.trk.blockchain.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByUserId(Long userId);
    List<Referral> findByReferralId(Long referralId);
    List<Referral> findByUserIdAndLevel(Long userId, Integer level);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.userId = ?1")
    Integer countTotalReferrals(Long userId);

    @Query("SELECT r FROM Referral r WHERE r.userId = ?1 AND r.level <= ?2 ORDER BY r.level")
    List<Referral> findByUserIdAndLevelLessThanEqual(Long userId, Integer maxLevel);
}
