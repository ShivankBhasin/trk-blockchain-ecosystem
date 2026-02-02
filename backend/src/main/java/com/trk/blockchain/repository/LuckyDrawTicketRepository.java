package com.trk.blockchain.repository;

import com.trk.blockchain.entity.LuckyDrawTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LuckyDrawTicketRepository extends JpaRepository<LuckyDrawTicket, Long> {
    List<LuckyDrawTicket> findByUserId(Long userId);
    List<LuckyDrawTicket> findByDrawId(Long drawId);
    List<LuckyDrawTicket> findByUserIdAndDrawId(Long userId, Long drawId);
    List<LuckyDrawTicket> findByDrawIdAndIsWinnerTrue(Long drawId);

    @Query("SELECT COUNT(t) FROM LuckyDrawTicket t WHERE t.userId = ?1 AND t.drawId = ?2")
    Integer countByUserIdAndDrawId(Long userId, Long drawId);

    @Query("SELECT MAX(t.ticketNumber) FROM LuckyDrawTicket t WHERE t.drawId = ?1")
    Integer findMaxTicketNumberByDrawId(Long drawId);
}
