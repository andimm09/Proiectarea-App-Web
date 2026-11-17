package com.andimm09.discordclone.service;

import com.andimm09.discordclone.domain.*;
import com.andimm09.discordclone.exception.NotFoundException;
import com.andimm09.discordclone.repository.ChannelRepository;
import com.andimm09.discordclone.repository.MembershipRepository;
import com.andimm09.discordclone.repository.ServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServerService {

    private final ServerRepository servers;
    private final MembershipRepository memberships;
    private final ChannelRepository channels;
    private final UserService userService;
    private final PermissionService permissions;

    public ServerService(ServerRepository servers, MembershipRepository memberships, ChannelRepository channels,
                         UserService userService, PermissionService permissions) {
        this.servers = servers;
        this.memberships = memberships;
        this.channels = channels;
        this.userService = userService;
        this.permissions = permissions;
    }

    // Creeaza serverul, il face pe creator OWNER si adauga canalele implicite
    @Transactional
    public Server create(Long ownerId, String name) {
        User owner = userService.get(ownerId);
        Server server = servers.save(new Server(name, owner));
        memberships.save(new Membership(owner, server, Role.OWNER));
        channels.save(new Channel(server, "general", ChannelType.TEXT, 0));
        channels.save(new Channel(server, "General", ChannelType.VOICE, 1));
        return server;
    }

    @Transactional(readOnly = true)
    public Server get(Long serverId) {
        return servers.findById(serverId).orElseThrow(() -> new NotFoundException("Server", serverId));
    }

    /** Detaliile serverului le vad doar membrii lui. */
    @Transactional(readOnly = true)
    public Server getForMember(Long userId, Long serverId) {
        Server server = get(serverId);
        permissions.requireMember(userId, serverId);
        return server;
    }

    /** Doar OWNER-ul are DELETE_SERVER. Canalele, mesajele, membrii si invitatiile se sterg prin ON DELETE CASCADE. */
    @Transactional
    public void delete(Long userId, Long serverId) {
        get(serverId);
        permissions.requirePermission(userId, serverId, Permission.DELETE_SERVER);
        servers.deleteServerById(serverId);
    }

    @Transactional(readOnly = true)
    public List<Server> listForUser(Long userId) {
        userService.get(userId);
        return servers.findAllByMemberId(userId);
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(Long userId, Long serverId) {
        get(serverId);
        permissions.requireMember(userId, serverId);
        return memberships.findAllByServerIdOrderByJoinedAt(serverId);
    }

    @Transactional(readOnly = true)
    public List<Channel> listChannels(Long userId, Long serverId) {
        get(serverId);
        permissions.requireMember(userId, serverId);
        return channels.findAllByServerIdOrderByPosition(serverId);
    }

    @Transactional
    public Channel createChannel(Long userId, Long serverId, String name, ChannelType type) {
        Server server = get(serverId);
        permissions.requirePermission(userId, serverId, Permission.MANAGE_CHANNELS);
        return channels.save(new Channel(server, name, type, channels.countByServerId(serverId)));
    }
}
