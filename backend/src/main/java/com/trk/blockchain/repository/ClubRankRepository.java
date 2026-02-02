package com.trk.blockchain.repository;

import com.trk.blockchain.entity.ClubRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRankRepository extends JpaRepository<ClubRank, Long> {
    Optional<ClubRank> findByUserId(Long userId);
    List<ClubRank> findByRankAndQualifiedTrue(Integer rank);
    List<ClubRank> findByQualifiedTrue();
}
