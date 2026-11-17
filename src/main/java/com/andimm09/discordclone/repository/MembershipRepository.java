package com.andimm09.discordclone.repository;

import com.andimm09.discordclone.domain.Membership;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<Membership> findByUserIdAndServerId(Long userId, Long serverId);

    boolean existsByUserIdAndServerId(Long userId, Long serverId);

    @EntityGraph(attributePaths = "user")
    List<Membership> findAllByServerIdOrderByJoinedAt(Long serverId);
}
