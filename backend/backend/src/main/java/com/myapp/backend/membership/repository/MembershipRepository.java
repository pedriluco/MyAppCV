package com.myapp.backend.membership.repository;

import com.myapp.backend.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByBusinessIdAndUserId(Long businessId, Long userId);
    boolean existsByUserIdAndBusinessId(Long userId, Long businessId);

    List<Membership> findAllByBusinessId(Long businessId);
}
