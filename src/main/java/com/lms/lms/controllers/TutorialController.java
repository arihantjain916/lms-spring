package com.lms.lms.controllers;

import com.github.slugify.Slugify;
import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.TutorialReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.dto.response.TutorialRes;
import com.lms.lms.dto.response.UserRes;
import com.lms.lms.modals.Category;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Tutorial;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CategoryRepo;
import com.lms.lms.repo.TutorialRepo;
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

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/tutorials")
public class TutorialController {

    @Autowired
    private TutorialRepo tutorialRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("")
    public ResponseEntity<?> getTutorials(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            String search = (q != null && !q.isBlank()) ? q : "%";
            boolean hasCategory = category != null && !category.isBlank();
            List<String> categoryIds = hasCategory ? List.of(category) : List.of("__none__");

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<Tutorial> tutorials = tutorialRepo.searchCatalog(search, hasCategory, categoryIds, pageable);
            List<TutorialRes> tutorialList = tutorials
                    .stream()
                    .map(this::toTutorialRes)
                    .toList();

            PaginatedResponse<TutorialRes> paginatedResponse = new PaginatedResponse<>(
                    "Tutorials Fetched Successfully",
                    true,
                    tutorialList,
                    tutorials.getNumber() + 1,
                    tutorials.getSize(),
                    tutorials.getTotalElements(),
                    tutorials.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Default> getTutorialBySlug(@PathVariable String slug) {
        try {
            Tutorial tutorial = tutorialRepo.findBySlug(slug).orElse(null);
            if (tutorial == null) {
                return new ResponseEntity<>(new Default("Tutorial Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(new Default("Tutorial Fetched Successfully", true, null, this.toTutorialRes(tutorial)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("")
    public ResponseEntity<Default> createTutorial(@Valid @RequestBody TutorialReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Category category = null;
            if (req.getCategoryId() != null && !req.getCategoryId().isBlank()) {
                category = categoryRepo.findById(req.getCategoryId()).orElse(null);
                if (category == null) {
                    return ResponseEntity.badRequest().body(new Default("Category Not Found", false, null, null));
                }
            }

            Tutorial tutorial = new Tutorial();
            tutorial.setTitle(req.getTitle());
            tutorial.setSlug(this.generateUniqueSlug(req.getTitle(), null));
            tutorial.setDescription(req.getDescription());
            tutorial.setContent(req.getContent());
            tutorial.setVideoUrl(req.getVideoUrl());
            tutorial.setThumbnailUrl(req.getThumbnailUrl());
            if (req.getLevel() != null && !req.getLevel().isBlank()) {
                tutorial.setLevel(Courses.Level.valueOf(req.getLevel()));
            }
            tutorial.setCategory(category);
            tutorial.setUser(user);
            tutorialRepo.save(tutorial);

            return ResponseEntity.ok(new Default("Tutorial Created Successfully", true, null, this.toTutorialRes(tutorial)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/{tutorialId}")
    public ResponseEntity<Default> updateTutorial(@PathVariable String tutorialId, @Valid @RequestBody TutorialReq req) {
        try {
            Tutorial tutorial = tutorialRepo.findById(tutorialId).orElse(null);
            if (tutorial == null) {
                return new ResponseEntity<>(new Default("Tutorial Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManage(tutorial)) {
                return new ResponseEntity<>(new Default("You are not authorized to modify this tutorial", false, null, null), HttpStatus.FORBIDDEN);
            }

            if (req.getCategoryId() != null && !req.getCategoryId().isBlank()) {
                Category category = categoryRepo.findById(req.getCategoryId()).orElse(null);
                if (category == null) {
                    return ResponseEntity.badRequest().body(new Default("Category Not Found", false, null, null));
                }
                tutorial.setCategory(category);
            } else {
                tutorial.setCategory(null);
            }

            if (!tutorial.getTitle().equals(req.getTitle())) {
                tutorial.setSlug(this.generateUniqueSlug(req.getTitle(), tutorial.getId()));
            }
            tutorial.setTitle(req.getTitle());
            tutorial.setDescription(req.getDescription());
            tutorial.setContent(req.getContent());
            tutorial.setVideoUrl(req.getVideoUrl());
            tutorial.setThumbnailUrl(req.getThumbnailUrl());
            if (req.getLevel() != null && !req.getLevel().isBlank()) {
                tutorial.setLevel(Courses.Level.valueOf(req.getLevel()));
            }
            tutorialRepo.save(tutorial);

            return ResponseEntity.ok(new Default("Tutorial Updated Successfully", true, null, this.toTutorialRes(tutorial)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/{tutorialId}")
    public ResponseEntity<Default> deleteTutorial(@PathVariable String tutorialId) {
        try {
            Tutorial tutorial = tutorialRepo.findById(tutorialId).orElse(null);
            if (tutorial == null) {
                return new ResponseEntity<>(new Default("Tutorial Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManage(tutorial)) {
                return new ResponseEntity<>(new Default("You are not authorized to delete this tutorial", false, null, null), HttpStatus.FORBIDDEN);
            }

            tutorialRepo.delete(tutorial);
            return ResponseEntity.ok(new Default("Tutorial Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // an instructor may only manage their own tutorials; admins may manage any
    private boolean canManage(Tutorial tutorial) {
        User user = userDetails.userDetails();
        if (user == null || user.getIsDeleted()) {
            return false;
        }
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        return tutorial.getUser() != null && tutorial.getUser().getId().equals(user.getId());
    }

    private String generateUniqueSlug(String title, String currentId) {
        Slugify slugify = new Slugify();
        String slug = slugify.slugify(title);
        Tutorial existing = tutorialRepo.findBySlug(slug).orElse(null);
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            slug = slug + "-" + new Random().nextInt(10000);
        }
        return slug;
    }

    private TutorialRes toTutorialRes(Tutorial tutorial) {
        return new TutorialRes(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getSlug(),
                tutorial.getDescription(),
                tutorial.getContent(),
                tutorial.getVideoUrl(),
                tutorial.getThumbnailUrl(),
                tutorial.getLevel() != null ? tutorial.getLevel().name() : null,
                tutorial.getCategory() != null ? tutorial.getCategory().getId() : null,
                tutorial.getCategory() != null ? tutorial.getCategory().getName() : null,
                new UserRes(tutorial.getUser().getId(), tutorial.getUser().getUsername(), tutorial.getUser().getName()),
                tutorial.getCreatedAt(),
                tutorial.getUpdatedAt()
        );
    }
}
