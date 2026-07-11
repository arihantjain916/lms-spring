package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.HostApplicationReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LessonResourceRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.dto.response.UserRes;
import com.lms.lms.dto.response.WebinarRegistrationRes;
import com.lms.lms.dto.response.WebinarRes;
import com.lms.lms.modals.User;
import com.lms.lms.modals.Webinar;
import com.lms.lms.modals.WebinarHostApplication;
import com.lms.lms.modals.WebinarRegistration;
import com.lms.lms.repo.WebinarHostApplicationRepo;
import com.lms.lms.repo.WebinarRegistrationRepo;
import com.lms.lms.repo.WebinarRepo;
import com.lms.lms.repo.WebinarResourceRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
public class WebinarController {

    @Autowired
    private WebinarRepo webinarRepo;

    @Autowired
    private WebinarRegistrationRepo webinarRegistrationRepo;

    @Autowired
    private WebinarResourceRepo webinarResourceRepo;

    @Autowired
    private WebinarHostApplicationRepo webinarHostApplicationRepo;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/webinars")
    public ResponseEntity<?> getWebinars(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            String statusFilter = null;
            if (status != null && !status.isBlank()) {
                if (!status.equalsIgnoreCase("upcoming") && !status.equalsIgnoreCase("past")) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Status. Allowed: upcoming, past", false, null, null));
                }
                statusFilter = status.toLowerCase();
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Sort sort = "upcoming".equals(statusFilter) ? Sort.by("scheduledAt").ascending() : Sort.by("scheduledAt").descending();
            Pageable pageable = PageRequest.of(pageNumber, limit, sort);

            String search = (q != null && !q.isBlank()) ? q : null;
            String categoryFilter = (category != null && !category.isBlank()) ? category : null;

            Page<Webinar> webinars = webinarRepo.searchWebinars(search, categoryFilter, statusFilter, new Date(), pageable);
            List<WebinarRes> webinarList = webinars
                    .stream()
                    .map(this::toWebinarRes)
                    .toList();

            PaginatedResponse<WebinarRes> paginatedResponse = new PaginatedResponse<>(
                    "Webinars Fetched Successfully",
                    true,
                    webinarList,
                    webinars.getNumber() + 1,
                    webinars.getSize(),
                    webinars.getTotalElements(),
                    webinars.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/webinars/{slug}")
    public ResponseEntity<Default> getWebinarBySlug(@PathVariable String slug) {
        try {
            Webinar webinar = webinarRepo.findBySlug(slug).orElse(null);
            if (webinar == null) {
                return new ResponseEntity<>(new Default("Webinar Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(new Default("Webinar Fetched Successfully", true, null, this.toWebinarRes(webinar)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/webinars/{webinarId}/registrations")
    public ResponseEntity<Default> registerForWebinar(@PathVariable String webinarId) {
        try {
            Webinar webinar = webinarRepo.findById(webinarId).orElse(null);
            if (webinar == null) {
                return new ResponseEntity<>(new Default("Webinar Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isAlreadyRegistered = webinarRegistrationRepo.existsByUser_IdAndWebinar_Id(user.getId(), webinarId);
            if (isAlreadyRegistered) {
                return ResponseEntity.badRequest().body(new Default("User Already Registered", false, null, null));
            }

            WebinarRegistration registration = new WebinarRegistration();
            registration.setWebinar(webinar);
            registration.setUser(user);
            webinarRegistrationRepo.save(registration);

            return ResponseEntity.ok(new Default("Registered For Webinar Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/webinars/{webinarId}/registrations")
    @Transactional
    public ResponseEntity<Default> unregisterFromWebinar(@PathVariable String webinarId) {
        try {
            Webinar webinar = webinarRepo.findById(webinarId).orElse(null);
            if (webinar == null) {
                return new ResponseEntity<>(new Default("Webinar Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isRegistered = webinarRegistrationRepo.existsByUser_IdAndWebinar_Id(user.getId(), webinarId);
            if (!isRegistered) {
                return ResponseEntity.badRequest().body(new Default("User Is Not Registered", false, null, null));
            }

            webinarRegistrationRepo.deleteByUser_IdAndWebinar_Id(user.getId(), webinarId);
            return ResponseEntity.ok(new Default("Registration Cancelled Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/users/me/webinar-registrations")
    public ResponseEntity<?> getMyWebinarRegistrations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("registeredAt").descending());

            Page<WebinarRegistration> registrations = webinarRegistrationRepo.findByUser_Id(user.getId(), pageable);
            List<WebinarRegistrationRes> registrationList = registrations
                    .stream()
                    .map(registration -> new WebinarRegistrationRes(registration.getId(), registration.getRegisteredAt(), this.toWebinarRes(registration.getWebinar())))
                    .toList();

            PaginatedResponse<WebinarRegistrationRes> paginatedResponse = new PaginatedResponse<>(
                    "Webinar Registrations Fetched Successfully",
                    true,
                    registrationList,
                    registrations.getNumber() + 1,
                    registrations.getSize(),
                    registrations.getTotalElements(),
                    registrations.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/webinars/{webinarId}/recording")
    public ResponseEntity<Default> getWebinarRecording(@PathVariable String webinarId) {
        try {
            Webinar webinar = webinarRepo.findById(webinarId).orElse(null);
            if (webinar == null) {
                return new ResponseEntity<>(new Default("Webinar Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessWebinar(user, webinar)) {
                return new ResponseEntity<>(new Default("User Is Not Registered For This Webinar", false, null, null), HttpStatus.FORBIDDEN);
            }

            if (webinar.getRecordingUrl() == null || webinar.getRecordingUrl().isBlank()) {
                return new ResponseEntity<>(new Default("Recording Not Available Yet", false, null, null), HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(new Default("Recording Fetched Successfully", true, null, webinar.getRecordingUrl()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/webinars/{webinarId}/resources")
    public ResponseEntity<Default> getWebinarResources(@PathVariable String webinarId) {
        try {
            Webinar webinar = webinarRepo.findById(webinarId).orElse(null);
            if (webinar == null) {
                return new ResponseEntity<>(new Default("Webinar Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessWebinar(user, webinar)) {
                return new ResponseEntity<>(new Default("User Is Not Registered For This Webinar", false, null, null), HttpStatus.FORBIDDEN);
            }

            List<LessonResourceRes> resources = webinarResourceRepo.findByWebinar_IdOrderByCreatedAtAsc(webinarId)
                    .stream()
                    .map(resource -> new LessonResourceRes(resource.getId(), resource.getTitle(), resource.getType(), resource.getCreatedAt()))
                    .toList();
            return ResponseEntity.ok(new Default("Resources Fetched Successfully", true, null, resources));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/webinar-host-applications")
    public ResponseEntity<Default> applyToHostWebinar(@Valid @RequestBody HostApplicationReq req) {
        try {
            WebinarHostApplication application = new WebinarHostApplication();
            application.setName(req.getName());
            application.setEmail(req.getEmail());
            application.setTopic(req.getTopic());
            application.setMessage(req.getMessage());
            webinarHostApplicationRepo.save(application);

            return ResponseEntity.ok(new Default("Application Submitted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private WebinarRes toWebinarRes(Webinar webinar) {
        return new WebinarRes(
                webinar.getId(),
                webinar.getTitle(),
                webinar.getSlug(),
                webinar.getDescription(),
                webinar.getThumbnailUrl(),
                webinar.getScheduledAt(),
                webinar.getDurationMinutes(),
                webinar.getScheduledAt().after(new Date()) ? "upcoming" : "past",
                webinar.getCategory() != null ? webinar.getCategory().getId() : null,
                webinar.getCategory() != null ? webinar.getCategory().getName() : null,
                new UserRes(webinar.getHost().getId(), webinar.getHost().getUsername(), webinar.getHost().getName()),
                webinarRegistrationRepo.countByWebinar_Id(webinar.getId()),
                webinar.getRecordingUrl() != null && !webinar.getRecordingUrl().isBlank()
        );
    }

    private boolean canAccessWebinar(User user, Webinar webinar) {
        if (user == null || user.getIsDeleted()) {
            return false;
        }
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (webinar.getHost() != null && webinar.getHost().getId().equals(user.getId())) {
            return true;
        }
        return webinarRegistrationRepo.existsByUser_IdAndWebinar_Id(user.getId(), webinar.getId());
    }
}
