package com.trk.blockchain.repository;

import com.trk.blockchain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByReferralCode(String referralCode);
    List<User> findByReferredBy(String referralCode);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByReferralCode(String referralCode);

    @Query("SELECT COUNT(u) FROM User u WHERE u.referredBy = ?1")
    Integer countDirectReferrals(String referralCode);
}
