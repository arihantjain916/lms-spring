package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.FacetCountRes;
import com.lms.lms.dto.response.SearchFacetsRes;
import com.lms.lms.dto.response.SearchItemRes;
import com.lms.lms.dto.response.SearchRes;
import com.lms.lms.modals.Blog;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Webinar;
import com.lms.lms.repo.BlogRepo;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.WebinarRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private WebinarRepo webinarRepo;

    @Autowired
    private BlogRepo blogRepo;

    @GetMapping("")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "course,webinar,blog") String types,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String levels,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            String search = (q != null && !q.isBlank()) ? q : null;

            Set<String> requestedTypes = Arrays.stream(types.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(type -> !type.isBlank())
                    .collect(Collectors.toSet());

            List<String> categoryIds = this.splitCsv(categories);
            boolean hasCategories = !categoryIds.isEmpty();
            if (!hasCategories) {
                categoryIds = List.of("__none__");
            }

            List<Courses.Level> levelFilters = new ArrayList<>();
            if (levels != null && !levels.isBlank()) {
                for (String level : this.splitCsv(levels)) {
                    try {
                        levelFilters.add(Courses.Level.valueOf(level.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(new Default("Invalid Level. Allowed: BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS", false, null, null));
                    }
                }
            }
            boolean hasLevels = !levelFilters.isEmpty();
            if (!hasLevels) {
                levelFilters = List.of(Courses.Level.BEGINNER);
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, this.resolveSort(sort));

            List<SearchItemRes> courseList = List.of();
            List<SearchItemRes> webinarList = List.of();
            List<SearchItemRes> blogList = List.of();
            Map<String, Long> totals = new HashMap<>();

            if (requestedTypes.contains("course")) {
                Page<Courses> courses = coursesRepo.searchCatalog(search, hasCategories, categoryIds, hasLevels, levelFilters, pageable);
                courseList = courses.stream().map(this::toSearchItem).toList();
                totals.put("course", courses.getTotalElements());
            }

            if (requestedTypes.contains("webinar")) {
                Page<Webinar> webinars = webinarRepo.searchCatalog(search, hasCategories, categoryIds, pageable);
                webinarList = webinars.stream().map(this::toSearchItem).toList();
                totals.put("webinar", webinars.getTotalElements());
            }

            if (requestedTypes.contains("blog")) {
                Page<Blog> blogs = blogRepo.searchCatalog(search, Blog.Staus.PUBLISHED, pageable);
                blogList = blogs.stream().map(this::toSearchItem).toList();
                totals.put("blog", blogs.getTotalElements());
            }

            SearchRes res = new SearchRes(courseList, webinarList, blogList, totals, page, limit);
            return ResponseEntity.ok(new Default("Search Results Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Default> suggestions(@RequestParam String q) {
        try {
            if (q.isBlank()) {
                return ResponseEntity.ok(new Default("Suggestions Fetched Successfully", true, null, List.of()));
            }

            List<Map<String, String>> suggestions = new ArrayList<>();
            coursesRepo.findTop5ByTitleContainingIgnoreCase(q)
                    .forEach(course -> suggestions.add(Map.of("type", "course", "title", course.getTitle(), "slug", course.getSlug())));
            webinarRepo.findTop5ByTitleContainingIgnoreCase(q)
                    .forEach(webinar -> suggestions.add(Map.of("type", "webinar", "title", webinar.getTitle(), "slug", webinar.getSlug())));
            blogRepo.findTop5ByTitleContainingIgnoreCaseAndStatus(q, Blog.Staus.PUBLISHED)
                    .forEach(blog -> suggestions.add(Map.of("type", "blog", "title", blog.getTitle(), "slug", blog.getSlug())));

            return ResponseEntity.ok(new Default("Suggestions Fetched Successfully", true, null, suggestions));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/facets")
    public ResponseEntity<Default> facets(@RequestParam(required = false) String q) {
        try {
            String search = (q != null && !q.isBlank()) ? q : null;
            Pageable one = PageRequest.of(0, 1);

            Map<String, Long> types = new HashMap<>();
            types.put("course", coursesRepo.searchCatalog(search, false, List.of("__none__"), false, List.of(Courses.Level.BEGINNER), one).getTotalElements());
            types.put("webinar", webinarRepo.searchCatalog(search, false, List.of("__none__"), one).getTotalElements());
            types.put("blog", blogRepo.searchCatalog(search, Blog.Staus.PUBLISHED, one).getTotalElements());

            List<FacetCountRes> categories = coursesRepo.countByCategoryForSearch(search)
                    .stream()
                    .map(row -> new FacetCountRes((String) row[0], (String) row[1], (Long) row[2]))
                    .toList();

            List<FacetCountRes> levels = coursesRepo.countByLevelForSearch(search)
                    .stream()
                    .map(row -> new FacetCountRes(((Courses.Level) row[0]).name(), ((Courses.Level) row[0]).name(), (Long) row[1]))
                    .toList();

            SearchFacetsRes res = new SearchFacetsRes(types, categories, levels);
            return ResponseEntity.ok(new Default("Facets Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private SearchItemRes toSearchItem(Courses course) {
        return new SearchItemRes(
                "course",
                String.valueOf(course.getId()),
                course.getTitle(),
                course.getSlug(),
                course.getDescription(),
                null,
                course.getCategory() != null ? course.getCategory().getName() : null,
                course.getLevel() != null ? course.getLevel().name() : null
        );
    }

    private SearchItemRes toSearchItem(Webinar webinar) {
        return new SearchItemRes(
                "webinar",
                webinar.getId(),
                webinar.getTitle(),
                webinar.getSlug(),
                webinar.getDescription(),
                webinar.getThumbnailUrl(),
                webinar.getCategory() != null ? webinar.getCategory().getName() : null,
                null
        );
    }

    private SearchItemRes toSearchItem(Blog blog) {
        return new SearchItemRes(
                "blog",
                blog.getId(),
                blog.getTitle(),
                blog.getSlug(),
                blog.getDescription(),
                blog.getImageUrl(),
                blog.getCategory() != null ? blog.getCategory().name() : null,
                null
        );
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "newest" : sort) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "title" -> Sort.by("title").ascending();
            case "title-desc" -> Sort.by("title").descending();
            default -> Sort.by("createdAt").descending();
        };
    }
}
