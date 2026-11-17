package com.andimm09.discordclone.service;

import com.andimm09.discordclone.domain.*;
import com.andimm09.discordclone.exception.ForbiddenException;
import com.andimm09.discordclone.exception.NotFoundException;
import com.andimm09.discordclone.repository.ChannelRepository;
import com.andimm09.discordclone.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository messages;
    private final ChannelRepository channels;
    private final PermissionService permissions;

    public MessageService(MessageRepository messages, ChannelRepository channels, PermissionService permissions) {
        this.messages = messages;
        this.channels = channels;
        this.permissions = permissions;
    }

    @Transactional(readOnly = true)
    public Page<Message> history(Long userId, Long channelId, int page, int size) {
        Channel channel = getTextChannel(channelId);
        permissions.requireMember(userId, channel.getServer().getId());
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return messages.findAllByChannelIdOrderBySentAtDesc(channelId, PageRequest.of(Math.max(page, 0), pageSize));
    }

    @Transactional
    public Message send(Long userId, Long channelId, String content) {
        Channel channel = getTextChannel(channelId);
        Membership membership = permissions.requirePermission(userId, channel.getServer().getId(), Permission.SEND_MESSAGES);
        return messages.save(new Message(channel, membership.getUser(), content));
    }

    @Transactional
    public Message edit(Long userId, Long messageId, String content) {
        Message message = getMessage(messageId);
        if (!message.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Only the author can edit a message");
        }
        message.edit(content);
        return message;
    }

    // Autorul isi poate sterge mesajul altcineva doar cu DELETE_ANY_MESSAGE
    @Transactional
    public void delete(Long userId, Long messageId) {
        Message message = getMessage(messageId);
        if (!message.getAuthor().getId().equals(userId)) {
            permissions.requirePermission(userId, message.getChannel().getServer().getId(), Permission.DELETE_ANY_MESSAGE);
        }
        message.softDelete();
    }

    private Message getMessage(Long messageId) {
        Message message = messages.findWithAuthorById(messageId).orElseThrow(() -> new NotFoundException("Message", messageId));
        if (message.isDeleted()) {
            throw new NotFoundException("Message", messageId);
        }
        return message;
    }

    private Channel getTextChannel(Long channelId) {
        Channel channel = channels.findById(channelId).orElseThrow(() -> new NotFoundException("Channel", channelId));
        if (channel.isVoice()) {
            throw new IllegalArgumentException("Channel " + channelId + " is a voice channel");
        }
        return channel;
    }
}
