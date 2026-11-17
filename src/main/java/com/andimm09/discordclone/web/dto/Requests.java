package com.andimm09.discordclone.web.dto;

import com.andimm09.discordclone.domain.ChannelType;
import jakarta.validation.constraints.*;

/** Body-urile JSON primite de controllere. */
public final class Requests {

    private Requests() {
    }

    public record RegisterUser(
            @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "[a-zA-Z0-9_.]+") String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record Login(@NotBlank String username, @NotBlank String password) {
    }

    public record CreateServer(@NotBlank @Size(max = 100) String name) {
    }

    public record CreateChannel(@NotBlank @Size(max = 100) String name, @NotNull ChannelType type) {
    }

    public record CreateInvite(@Positive Integer expiresInHours, @Positive Integer maxUses) {
    }

    public record SendMessage(@NotBlank @Size(max = 2000) String content) {
    }
}
