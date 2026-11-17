package com.andimm09.discordclone.repository;

import com.andimm09.discordclone.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findAllByServerIdOrderByPosition(Long serverId);

    int countByServerId(Long serverId);
}
