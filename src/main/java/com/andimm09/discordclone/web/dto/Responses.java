package com.andimm09.discordclone.web.dto;

import com.andimm09.discordclone.domain.*;

import java.time.LocalDateTime;

/** Ce trimitem inapoi ca JSON  */
public final class Responses {

    private Responses() {
    }

    public record UserResponse(Long id, String username, String avatarUrl, PresenceStatus status, LocalDateTime createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getAvatarUrl(), u.getStatus(), u.getCreatedAt());
        }
    }

    /** Raspunsul la login: token-ul se trimite apoi in header-ul "Authorization: Bearer <accessToken>". */
    public record LoginResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {
    }

    public record ServerResponse(Long id, String name, String iconUrl, Long ownerId, LocalDateTime createdAt) {
        public static ServerResponse from(Server s) {
            return new ServerResponse(s.getId(), s.getName(), s.getIconUrl(), s.getOwner().getId(), s.getCreatedAt());
        }
    }

    public record MemberResponse(Long userId, String username, String nickname, Role role, LocalDateTime joinedAt) {
        public static MemberResponse from(Membership m) {
            return new MemberResponse(m.getUser().getId(), m.getUser().getUsername(), m.getNickname(), m.getRole(), m.getJoinedAt());
        }
    }

    public record ChannelResponse(Long id, Long serverId, String name, ChannelType type, int position) {
        public static ChannelResponse from(Channel c) {
            return new ChannelResponse(c.getId(), c.getServer().getId(), c.getName(), c.getType(), c.getPosition());
        }
    }

    public record MessageResponse(Long id, Long channelId, Long authorId, String authorUsername, String content,
                                  LocalDateTime sentAt, LocalDateTime editedAt, boolean deleted) {
        public static MessageResponse from(Message m) {
            // mesajele sterse raman in istoric, dar fara continut
            String content = m.isDeleted() ? null : m.getContent();
            return new MessageResponse(m.getId(), m.getChannel().getId(), m.getAuthor().getId(), m.getAuthor().getUsername(),
                    content, m.getSentAt(), m.getEditedAt(), m.isDeleted());
        }
    }

    public record InviteResponse(String code, Long serverId, Long createdBy, LocalDateTime expiresAt, Integer maxUses,
                                 int uses, InviteStatus status) {
        public static InviteResponse from(Invite i) {
            return new InviteResponse(i.getCode(), i.getServer().getId(), i.getCreatedBy().getId(), i.getExpiresAt(),
                    i.getMaxUses(), i.getUses(), i.getStatus());
        }
    }

    public record PageResponse<T>(java.util.List<T> items, int page, int size, long totalItems, boolean hasNext) {
        public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.hasNext());
        }
    }
}
