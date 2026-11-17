package com.andimm09.discordclone.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected Message() {
    }

    public Message(Channel channel, User author, String content) {
        this.channel = channel;
        this.author = author;
        this.content = content;
    }

    public void edit(String newContent) {
        this.content = newContent;
        this.editedAt = LocalDateTime.now();
    }


    public void softDelete() {
        this.deleted = true;
    }

    public Long getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
