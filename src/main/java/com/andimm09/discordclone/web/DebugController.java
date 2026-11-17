package com.andimm09.discordclone.web;

import com.andimm09.discordclone.repository.*;
import com.andimm09.discordclone.web.dto.Responses.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// doar pentru debug, exista doar cu profilul "dev" (spring.profiles.active din application.yml)
@Profile("dev")
@RestController
public class DebugController {

    private final UserRepository users;
    private final ServerRepository servers;
    private final MembershipRepository memberships;
    private final ChannelRepository channels;
    private final MessageRepository messages;
    private final InviteRepository invites;

    public DebugController(UserRepository users, ServerRepository servers, MembershipRepository memberships,
                           ChannelRepository channels, MessageRepository messages, InviteRepository invites) {
        this.users = users;
        this.servers = servers;
        this.memberships = memberships;
        this.channels = channels;
        this.messages = messages;
        this.invites = invites;
    }

    @GetMapping({"/", "/api/debug"})
    @Transactional(readOnly = true)
    public Map<String, List<?>> everything() {
        Map<String, List<?>> result = new LinkedHashMap<>();
        result.put("users", users.findAll(Sort.by("id")).stream().map(UserResponse::from).toList());
        result.put("servers", servers.findAll(Sort.by("id")).stream().map(ServerResponse::from).toList());
        result.put("members", memberships.findAll(Sort.by("id")).stream()
                .map(m -> Map.of("serverId", m.getServer().getId(), "member", MemberResponse.from(m)))
                .toList());
        result.put("channels", channels.findAll(Sort.by("server.id", "position")).stream().map(ChannelResponse::from).toList());
        result.put("invites", invites.findAll(Sort.by("createdAt")).stream().map(InviteResponse::from).toList());
        result.put("latestMessages", messages.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "sentAt")))
                .map(MessageResponse::from).getContent());
        return result;
    }
}
