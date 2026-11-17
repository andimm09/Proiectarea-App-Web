package com.andimm09.discordclone.repository;

import com.andimm09.discordclone.domain.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, String> {
}
