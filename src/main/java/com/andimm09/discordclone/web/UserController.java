package com.andimm09.discordclone.web;

import com.andimm09.discordclone.security.CurrentUserId;
import com.andimm09.discordclone.service.ServerService;
import com.andimm09.discordclone.service.UserService;
import com.andimm09.discordclone.web.dto.Responses.ServerResponse;
import com.andimm09.discordclone.web.dto.Responses.UserResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ServerService serverService;

    public UserController(UserService userService, ServerService serverService) {
        this.userService = userService;
        this.serverService = serverService;
    }

    /** Doar serverele userului logat: nu poti vedea pe ce servere e altcineva. */
    @GetMapping("/me/servers")
    public List<ServerResponse> myServers(@CurrentUserId Long userId) {
        return serverService.listForUser(userId).stream().map(ServerResponse::from).toList();
    }

    /** Profilul public al oricarui user (fara email). */
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.get(id));
    }
}
