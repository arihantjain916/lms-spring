package com.lms.lms.controllers;


import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.EnrollmentRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Enrollment;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import jakarta.transaction.Transactional;
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
@RequestMapping("/enrollment")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private CoursesRepo coursesRepo;

    @GetMapping("{courseId}")
    public ResponseEntity<Default> enrollStudent(@PathVariable Long courseId) {
        try {
            var user = userDetails.userDetails();
            Boolean isUserAlreadyEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (isUserAlreadyEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User Already Enrolled", false, null, null));
            }

            Courses courses = coursesRepo.findById(courseId).orElse(null);
            if (courses == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setCourses(courses);
            enrollment.setUser(user);
            enrollmentRepo.save(enrollment);

            return ResponseEntity.ok().body(new Default("Student Enroll Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("{courseId}/unroll")
    @Transactional
    public ResponseEntity<Default> unenrollStudent(@PathVariable Long courseId) {
        try {
            var user = userDetails.userDetails();
            Courses courses = coursesRepo.findById(courseId).orElse(null);
            if (courses == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }


            enrollmentRepo.deleteByUser_IdAndCourses_Id(user.getId(), courseId);

            return ResponseEntity.ok().body(new Default("Student UnEnroll Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserEnrollments(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "enrolledAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        try {
            var user = userDetails.userDetails();
            int pageNumber = page > 0 ? page - 1 : 0;
            Sort sort = Objects.equals(order, "asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, size, sort);

            Page<Enrollment> userEnrollments = enrollmentRepo.findByUser_Id(user.getId(), pageable);
            List<EnrollmentRes> courseRes = userEnrollments.stream().map(enrollment -> new EnrollmentRes(
                    enrollment.getCourses().getId(),
                    enrollment.getCourses().getTitle(),
                    enrollment.getCourses().getDescription()
            )).toList();

            PaginatedResponse<EnrollmentRes> paginatedResponse = new PaginatedResponse<>(
                    "Enrollments Fetched Successfully",
                    true,
                    courseRes,
                    userEnrollments.getNumber() + 1,
                    userEnrollments.getSize(),
                    userEnrollments.getTotalElements(),
                    userEnrollments.getTotalPages()

            );
            return ResponseEntity.ok().body(new Default("Enrollments fetched Successfully", true, null, paginatedResponse));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
