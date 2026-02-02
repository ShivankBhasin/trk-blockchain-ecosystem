package com.trk.blockchain.repository;

import com.trk.blockchain.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByUserIdOrderByTimestampDesc(Long userId);
    List<Game> findByUserIdAndGameType(Long userId, Game.GameType gameType);

    @Query("SELECT COUNT(g) FROM Game g WHERE g.userId = ?1")
    Integer countByUserId(Long userId);

    @Query("SELECT COUNT(g) FROM Game g WHERE g.userId = ?1 AND g.result = 'WIN'")
    Integer countWinsByUserId(Long userId);

    @Query("SELECT COUNT(g) FROM Game g WHERE g.userId = ?1 AND g.result = 'LOSS'")
    Integer countLossesByUserId(Long userId);
}
