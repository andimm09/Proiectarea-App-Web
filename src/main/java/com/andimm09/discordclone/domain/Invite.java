package com.andimm09.discordclone.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invites")
public class Invite {

    @Id
    @Column(length = 16)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** null = nu expira niciodata */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** null = utilizari nelimitate */
    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(nullable = false)
    private int uses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InviteStatus status = InviteStatus.ACTIVE;

    protected Invite() {
    }

    public Invite(String code, Server server, User createdBy, LocalDateTime expiresAt, Integer maxUses) {
        this.code = code;
        this.server = server;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
    }

    /** Actualizeaza statusul (Active -> Expired/Exhausted) spune daca invitatia mai poate fi folosita. */
    public boolean isValid() {
        if (status == InviteStatus.ACTIVE && expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            status = InviteStatus.EXPIRED;
        }
        return status == InviteStatus.ACTIVE;
    }

    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("Invite " + code + " is " + status);
        }
        uses++;
        if (maxUses != null && uses >= maxUses) {
            status = InviteStatus.EXHAUSTED;
        }
    }

    public void revoke() {
        status = InviteStatus.REVOKED;
    }

    public String getCode() {
        return code;
    }

    public Server getServer() {
        return server;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public int getUses() {
        return uses;
    }

    public InviteStatus getStatus() {
        return status;
    }
}
