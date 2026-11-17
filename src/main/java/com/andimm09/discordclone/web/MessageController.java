package com.andimm09.discordclone.web;

import com.andimm09.discordclone.security.CurrentUserId;
import com.andimm09.discordclone.service.MessageService;
import com.andimm09.discordclone.web.dto.Requests.SendMessage;
import com.andimm09.discordclone.web.dto.Responses.MessageResponse;
import com.andimm09.discordclone.web.dto.Responses.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Istoricul canalului, cele mai noi mesaje primele. */
    @GetMapping("/channels/{channelId}/messages")
    public PageResponse<MessageResponse> history(@CurrentUserId Long userId, @PathVariable Long channelId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return PageResponse.from(messageService.history(userId, channelId, page, size).map(MessageResponse::from));
    }

    @PostMapping("/channels/{channelId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@CurrentUserId Long userId, @PathVariable Long channelId,
                                @Valid @RequestBody SendMessage body) {
        return MessageResponse.from(messageService.send(userId, channelId, body.content()));
    }

    @PatchMapping("/messages/{messageId}")
    public MessageResponse edit(@CurrentUserId Long userId, @PathVariable Long messageId,
                                @Valid @RequestBody SendMessage body) {
        return MessageResponse.from(messageService.edit(userId, messageId, body.content()));
    }

    @DeleteMapping("/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long messageId) {
        messageService.delete(userId, messageId);
    }
}
