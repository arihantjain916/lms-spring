package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.NotificationRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.modals.Notification;
import com.lms.lms.modals.User;
import com.lms.lms.repo.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private NotificationRepo notificationRepo;

    @GetMapping("")
    public ResponseEntity<?> myNotifications(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Notification> notifications = unreadOnly
                    ? notificationRepo.findByUser_IdAndIsReadFalse(user.getId(), pageable)
                    : notificationRepo.findByUser_Id(user.getId(), pageable);

            List<NotificationRes> data = notifications.getContent().stream().map(NotificationRes::from).toList();
            return ResponseEntity.ok(new PaginatedResponse<>(
                    "Notifications fetched successfully", true, data,
                    notifications.getNumber(), notifications.getSize(),
                    notifications.getTotalElements(), notifications.getTotalPages()));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Backs the unread badge; kept separate so the client can poll it cheaply. */
    @GetMapping("/unread-count")
    public ResponseEntity<Default> unreadCount() {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            long count = notificationRepo.countByUser_IdAndIsReadFalse(user.getId());
            return ResponseEntity.ok(new Default("Unread count fetched successfully", true, null, count));
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

            // looked up by id *and* owner so one account cannot touch another's notification
            Notification notification = notificationRepo.findByIdAndUser_Id(id, user.getId()).orElse(null);
            if (notification == null) {
                return new ResponseEntity<>(new Default("Notification not found", false, null, null), HttpStatus.NOT_FOUND);
            }

            notification.setIsRead(true);
            notificationRepo.save(notification);

            return ResponseEntity.ok(new Default("Notification marked as read", true, null, NotificationRes.from(notification)));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<Default> markAllRead() {
        try {
            User user = userDetails.userDetails();
            if (user == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            int updated = notificationRepo.markAllRead(user.getId());
            return ResponseEntity.ok(new Default("Notifications marked as read", true, null, updated));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
