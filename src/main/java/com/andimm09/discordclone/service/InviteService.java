package com.andimm09.discordclone.service;

import com.andimm09.discordclone.domain.*;
import com.andimm09.discordclone.exception.ConflictException;
import com.andimm09.discordclone.exception.NotFoundException;
import com.andimm09.discordclone.repository.InviteRepository;
import com.andimm09.discordclone.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class InviteService {


    private static final String CODE_ALPHABET = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();
    private final InviteRepository invites;
    private final MembershipRepository memberships;
    private final ServerService serverService;
    private final UserService userService;
    private final PermissionService permissions;

    public InviteService(InviteRepository invites, MembershipRepository memberships, ServerService serverService,
                         UserService userService, PermissionService permissions) {
        this.invites = invites;
        this.memberships = memberships;
        this.serverService = serverService;
        this.userService = userService;
        this.permissions = permissions;
    }

    @Transactional
    public Invite create(Long userId, Long serverId, Integer expiresInHours, Integer maxUses) {
        Server server = serverService.get(serverId);
        Membership membership = permissions.requirePermission(userId, serverId, Permission.CREATE_INVITE);
        LocalDateTime expiresAt = expiresInHours == null ? null : LocalDateTime.now().plusHours(expiresInHours);
        return invites.save(new Invite(generateCode(), server, membership.getUser(), expiresAt, maxUses));
    }

    @Transactional
    public Membership join(Long userId, String code) {
        Invite invite = invites.findById(code).orElseThrow(() -> new NotFoundException("Invite", code));
        User user = userService.get(userId);
        Server server = invite.getServer();
        if (memberships.existsByUserIdAndServerId(userId, server.getId())) {
            throw new ConflictException("User " + userId + " is already a member of server " + server.getId());
        }
        invite.use();
        return memberships.save(new Membership(user, server, Role.MEMBER));
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }
}
