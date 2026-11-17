package com.andimm09.discordclone.repository;

import com.andimm09.discordclone.domain.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, Long> {

    @Query("select m.server from Membership m where m.user.id = :userId order by m.joinedAt")
    List<Server> findAllByMemberId(Long userId);

    // stergere direct in SQL: canalele, mesajele, membrii si invitatiile le sterge Postgres (ON DELETE CASCADE)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Server s where s.id = :serverId")
    void deleteServerById(Long serverId);
}
