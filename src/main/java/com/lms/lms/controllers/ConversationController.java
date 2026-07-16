package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.SendMessageReq;
import com.lms.lms.dto.request.StartConversationReq;
import com.lms.lms.dto.response.ConversationRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.MessageRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.modals.Conversation;
import com.lms.lms.modals.Message;
import com.lms.lms.modals.User;
import com.lms.lms.repo.ConversationRepo;
import com.lms.lms.repo.MessageRepo;
import com.lms.lms.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ConversationRepo conversationRepo;

    @Autowired
    private MessageRepo messageRepo;

    /** Opens or reuses the caller's support thread and posts the first message. */
    @PostMapping("/support")
    public ResponseEntity<Default> startSupport(@Valid @RequestBody StartConversationReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.startSupport(user, req.getSubject());
            messagingService.send(conversation, user, req.getContent());

            return new ResponseEntity<>(new Default("Support conversation started", true, null,
                    ConversationRes.from(conversation, messagingService.unreadCount(conversation, user))), HttpStatus.CREATED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.TOO_MANY_REQUESTS);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Opens or reuses a thread with an instructor (enrolment-gated) or an admin. */
    @PostMapping("/direct")
    public ResponseEntity<Default> startDirect(@Valid @RequestBody StartConversationReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.startDirect(user, req.getRecipientId(), req.getCourseId(), req.getSubject());
            messagingService.send(conversation, user, req.getContent());

            return new ResponseEntity<>(new Default("Conversation started", true, null,
                    ConversationRes.from(conversation, messagingService.unreadCount(conversation, user))), HttpStatus.CREATED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.TOO_MANY_REQUESTS);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** The caller's inbox: threads they started or were addressed in. */
    @GetMapping("")
    public ResponseEntity<?> myConversations(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Page<Conversation> conversations = conversationRepo.findMine(user.getId(),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt")));

            return ResponseEntity.ok(toPaginated("Conversations fetched successfully", conversations, user));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Default> getConversation(@PathVariable String id) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.requireAccess(id, user);
            return ResponseEntity.ok(new Default("Conversation fetched successfully", true, null,
                    ConversationRes.from(conversation, messagingService.unreadCount(conversation, user))));
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Newest first; the client reverses for display. Opening a thread marks it read. */
    @GetMapping("/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable String id,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "30") int size) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.requireAccess(id, user);

            // marked before the fetch so the response carries the fresh readAt stamps
            messagingService.markRead(conversation, user);

            Page<Message> messages = messageRepo.findByConversation_Id(conversation.getId(),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

            List<MessageRes> data = messages.getContent().stream().map(MessageRes::from).toList();
            return ResponseEntity.ok(new PaginatedResponse<>(
                    "Messages fetched successfully", true, data,
                    messages.getNumber(), messages.getSize(), messages.getTotalElements(), messages.getTotalPages()));
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Default> sendMessage(@PathVariable String id, @Valid @RequestBody SendMessageReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.requireAccess(id, user);
            Message message = messagingService.send(conversation, user, req.getContent());

            return new ResponseEntity<>(new Default("Message sent successfully", true, null,
                    MessageRes.from(message)), HttpStatus.CREATED);
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.TOO_MANY_REQUESTS);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Default> markRead(@PathVariable String id) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.requireAccess(id, user);
            int updated = messagingService.markRead(conversation, user);

            return ResponseEntity.ok(new Default("Conversation marked as read", true, null, updated));
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Close or reopen a thread. Any participant may; for support that is the handling admin. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Default> updateStatus(@PathVariable String id, @RequestParam Conversation.Status status) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Conversation conversation = messagingService.requireAccess(id, user);
            conversation.setStatus(status);
            conversationRepo.save(conversation);

            return ResponseEntity.ok(new Default("Conversation status updated", true, null,
                    ConversationRes.from(conversation, messagingService.unreadCount(conversation, user))));
        } catch (SecurityException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PaginatedResponse<ConversationRes> toPaginated(String message, Page<Conversation> conversations, User viewer) {
        List<ConversationRes> data = conversations.getContent().stream()
                .map(c -> ConversationRes.from(c, messagingService.unreadCount(c, viewer)))
                .toList();
        return new PaginatedResponse<>(message, true, data,
                conversations.getNumber(), conversations.getSize(),
                conversations.getTotalElements(), conversations.getTotalPages());
    }
}
