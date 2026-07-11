package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.dto.response.TutorialRes;
import com.lms.lms.dto.response.UserRes;
import com.lms.lms.modals.Tutorial;
import com.lms.lms.repo.TutorialRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tutorials")
public class TutorialController {

    @Autowired
    private TutorialRepo tutorialRepo;

    @GetMapping("")
    public ResponseEntity<?> getTutorials(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            String search = (q != null && !q.isBlank()) ? q : null;
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
