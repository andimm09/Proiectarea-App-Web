package com.andimm09.discordclone.web;

import com.andimm09.discordclone.security.CurrentUserId;
import com.andimm09.discordclone.service.InviteService;
import com.andimm09.discordclone.service.ServerService;
import com.andimm09.discordclone.web.dto.Requests.CreateChannel;
import com.andimm09.discordclone.web.dto.Requests.CreateInvite;
import com.andimm09.discordclone.web.dto.Requests.CreateServer;
import com.andimm09.discordclone.web.dto.Responses.ChannelResponse;
import com.andimm09.discordclone.web.dto.Responses.InviteResponse;
import com.andimm09.discordclone.web.dto.Responses.MemberResponse;
import com.andimm09.discordclone.web.dto.Responses.ServerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;
    private final InviteService inviteService;

    public ServerController(ServerService serverService, InviteService inviteService) {
        this.serverService = serverService;
        this.inviteService = inviteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServerResponse create(@CurrentUserId Long userId, @Valid @RequestBody CreateServer body) {
        return ServerResponse.from(serverService.create(userId, body.name()));
    }

    @GetMapping("/{serverId}")
    public ServerResponse get(@CurrentUserId Long userId, @PathVariable Long serverId) {
        return ServerResponse.from(serverService.getForMember(userId, serverId));
    }

    @DeleteMapping("/{serverId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long serverId) {
        serverService.delete(userId, serverId);
    }

    @GetMapping("/{serverId}/members")
    public List<MemberResponse> members(@CurrentUserId Long userId, @PathVariable Long serverId) {
        return serverService.listMembers(userId, serverId).stream().map(MemberResponse::from).toList();
    }

    @GetMapping("/{serverId}/channels")
    public List<ChannelResponse> channels(@CurrentUserId Long userId, @PathVariable Long serverId) {
        return serverService.listChannels(userId, serverId).stream().map(ChannelResponse::from).toList();
    }

    @PostMapping("/{serverId}/channels")
    @ResponseStatus(HttpStatus.CREATED)
    public ChannelResponse createChannel(@CurrentUserId Long userId, @PathVariable Long serverId,
                                         @Valid @RequestBody CreateChannel body) {
        return ChannelResponse.from(serverService.createChannel(userId, serverId, body.name(), body.type()));
    }

    @PostMapping("/{serverId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(@CurrentUserId Long userId, @PathVariable Long serverId,
                                       @Valid @RequestBody(required = false) CreateInvite body) {
        CreateInvite options = body != null ? body : new CreateInvite(null, null);
        return InviteResponse.from(inviteService.create(userId, serverId, options.expiresInHours(), options.maxUses()));
    }
}
