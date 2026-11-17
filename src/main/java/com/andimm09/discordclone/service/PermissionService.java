package com.andimm09.discordclone.service;

import com.andimm09.discordclone.domain.Membership;
import com.andimm09.discordclone.domain.Permission;
import com.andimm09.discordclone.exception.ForbiddenException;
import com.andimm09.discordclone.repository.MembershipRepository;
import org.springframework.stereotype.Service;

/** Verificari: e userul membru pe server? are permisiunea X? */
@Service
public class PermissionService {

    private final MembershipRepository memberships;

    public PermissionService(MembershipRepository memberships) {
        this.memberships = memberships;
    }

    public Membership requireMember(Long userId, Long serverId) {
        return memberships.findByUserIdAndServerId(userId, serverId)
                .orElseThrow(() -> new ForbiddenException("User " + userId + " is not a member of server " + serverId));
    }

    public Membership requirePermission(Long userId, Long serverId, Permission permission) {
        Membership membership = requireMember(userId, serverId);
        if (!membership.hasPermission(permission)) {
            throw new ForbiddenException("Role " + membership.getRole() + " does not have " + permission);
        }
        return membership;
    }
}
