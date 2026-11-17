package com.andimm09.discordclone.web;

import com.andimm09.discordclone.security.CurrentUserId;
import com.andimm09.discordclone.service.InviteService;
import com.andimm09.discordclone.web.dto.Responses.MemberResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/{code}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse join(@CurrentUserId Long userId, @PathVariable String code) {
        return MemberResponse.from(inviteService.join(userId, code));
    }
}
