package com.andimm09.discordclone.repository;

import com.andimm09.discordclone.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {


    @EntityGraph(attributePaths = "author")
    Page<Message> findAllByChannelIdOrderBySentAtDesc(Long channelId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "channel"})
    Optional<Message> findWithAuthorById(Long id);
}
