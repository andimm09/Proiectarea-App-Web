package com.andimm09.discordclone.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ChannelType type;

    @Column(nullable = false)
    private int position;

    protected Channel() {
    }

    public Channel(Server server, String name, ChannelType type, int position) {
        this.server = server;
        this.name = name;
        this.type = type;
        this.position = position;
    }

    public boolean isVoice() {
        return type == ChannelType.VOICE;
    }

    public Long getId() {
        return id;
    }

    public Server getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelType getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
