package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LessonRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.mappers.LessonMapper;
import com.lms.lms.modals.Lesson;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.LessonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/lesson")

public class LessonController {

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private LessonMapper lessonMapper;

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getByCourseId(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {

        try {
            var course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.internalServerError().body(new Default("Course Not Found", false, null, null));
            }
            int pageNumber = page > 0 ? page - 1 : 0;
            Sort sort = Objects.equals(order, "asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, size, sort);

            Page<Lesson> lessons = lessonRepo.findByCourses_Id(courseId, pageable);

            List<LessonRes> blogRes = lessons.
                    stream()
                    .map(lessonMapper::toDto)
                    .toList();

            PaginatedResponse<LessonRes> paginatedResponse = new PaginatedResponse<>(
                    "Lesson Fetched Successfully",
                    true,
                    blogRes,
                    lessons.getNumber() + 1,
                    lessons.getSize(),
                    lessons.getTotalElements(),
                    lessons.getTotalPages()
            );

            return ResponseEntity.internalServerError().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
