package com.andimm09.discordclone.domain;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
    OWNER(EnumSet.allOf(Permission.class)),
    ADMIN(EnumSet.of(
            Permission.MANAGE_CHANNELS,
            Permission.KICK_MEMBERS,
            Permission.CREATE_INVITE,
            Permission.DELETE_ANY_MESSAGE,
            Permission.SEND_MESSAGES,
            Permission.CONNECT_VOICE)),
    MEMBER(EnumSet.of(
            Permission.CREATE_INVITE,
            Permission.SEND_MESSAGES,
            Permission.CONNECT_VOICE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean has(Permission permission) {
        return permissions.contains(permission);
    }
}
