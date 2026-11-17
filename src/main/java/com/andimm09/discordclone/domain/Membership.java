package com.andimm09.discordclone.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Clasa de asociere User - Server
 */
@Entity
@Table(name = "memberships",
        uniqueConstraints = @UniqueConstraint(name = "uq_membership_user_server", columnNames = {"user_id", "server_id"}))
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(length = 32)
    private String nickname;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    protected Membership() {
    }

    public Membership(User user, Server server, Role role) {
        this.user = user;
        this.server = server;
        this.role = role;
    }

    public boolean hasPermission(Permission permission) {
        return role.has(permission);
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Server getServer() {
        return server;
    }

    public Role getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
