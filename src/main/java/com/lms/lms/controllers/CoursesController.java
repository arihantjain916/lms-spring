package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.CourseReviewReq;
import com.lms.lms.dto.response.CertificateRes;
import com.lms.lms.dto.response.CourseProgressRes;
import com.lms.lms.dto.response.CourseRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.InstructorRes;
import com.lms.lms.dto.response.LessonRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.dto.response.RatingRes;
import com.lms.lms.mappers.CourseMapper;
import com.lms.lms.mappers.LessonMapper;
import com.lms.lms.mappers.RatingMapper;
import com.lms.lms.modals.Certificate;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Enrollment;
import com.lms.lms.modals.Lesson;
import com.lms.lms.modals.Ratings;
import com.lms.lms.modals.Review;
import com.lms.lms.modals.User;
import com.lms.lms.repo.CertificateRepo;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.LessonProgressRepo;
import com.lms.lms.repo.LessonRepo;
import com.lms.lms.repo.PricingRepo;
import com.lms.lms.repo.RatingRepo;
import com.lms.lms.repo.ReviewRepo;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
public class CoursesController {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private PricingRepo pricingRepo;

    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private LessonMapper lessonMapper;

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private LessonProgressRepo lessonProgressRepo;

    @Autowired
    private CertificateRepo certificateRepo;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("")
    public ResponseEntity<?> getCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String subcategoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Courses.Level levelFilter = null;
            if (level != null && !level.isBlank()) {
                try {
                    levelFilter = Courses.Level.valueOf(level.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Level. Allowed: BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS", false, null, null));
                }
            }

            String priceFilter = null;
            Double maxPrice = null;
            if (price != null && !price.isBlank()) {
                if (price.equalsIgnoreCase("free") || price.equalsIgnoreCase("paid")) {
                    priceFilter = price.toLowerCase();
                } else {
                    try {
                        maxPrice = Double.parseDouble(price);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest().body(new Default("Invalid Price. Allowed: free, paid or a max price number", false, null, null));
                    }
                }
            }

            // subcategoryId maps to the same category table until categories get a hierarchy
            String categoryFilter = (subcategoryId != null && !subcategoryId.isBlank()) ? subcategoryId : categoryId;
            // Keep this parameter non-null so PostgreSQL/Hibernate binds it as text.
            // A nullable value can be inferred as bytea inside lower(concat(...)).
            String search = (q != null && !q.isBlank()) ? q.trim() : "";

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, this.resolveSort(sort));

            Page<Courses> allCourses = coursesRepo.searchCourses(search, categoryFilter, levelFilter, featured, rating, priceFilter, maxPrice, pageable);
            List<CourseRes> courseList = allCourses
                    .stream()
                    .map(this::toCourseRes)
                    .toList();

            PaginatedResponse<CourseRes> paginatedResponse = new PaginatedResponse<>(
                    "Courses Fetched Successfully",
                    true,
                    courseList,
                    allCourses.getNumber() + 1,
                    allCourses.getSize(),
                    allCourses.getTotalElements(),
                    allCourses.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<Default> getFeaturedCourses(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<CourseRes> courses = coursesRepo.findByIsFeaturedTrue(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                    .stream()
                    .map(this::toCourseRes)
                    .toList();
            return ResponseEntity.ok().body(new Default("Featured Courses Fetched Successfully", true, null, courses));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{courseRef}")
    public ResponseEntity<Default> getCourseByIdOrSlug(@PathVariable String courseRef) {
        try {
            Courses course;
            try {
                course = coursesRepo.findById(Long.valueOf(courseRef)).orElse(null);
            } catch (NumberFormatException ignored) {
                course = coursesRepo.findBySlug(courseRef).orElse(null);
            }
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }
            return ResponseEntity.ok().body(new Default("Course Found", true, null, this.toCourseRes(course)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{courseId}/curriculum")
    public ResponseEntity<?> getCourseCurriculum(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").ascending());

            Page<Lesson> lessons = lessonRepo.findByCourses_Id(courseId, pageable);
            List<LessonRes> lessonList = lessons
                    .stream()
                    .map(lessonMapper::toDto)
                    .toList();

            PaginatedResponse<LessonRes> paginatedResponse = new PaginatedResponse<>(
                    "Curriculum Fetched Successfully",
                    true,
                    lessonList,
                    lessons.getNumber() + 1,
                    lessons.getSize(),
                    lessons.getTotalElements(),
                    lessons.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{courseId}/instructor")
    public ResponseEntity<Default> getCourseInstructor(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User instructor = course.getUser();
            if (instructor == null) {
                return ResponseEntity.badRequest().body(new Default("Instructor Not Found", false, null, null));
            }

            InstructorRes res = new InstructorRes(
                    instructor.getId(),
                    instructor.getUsername(),
                    instructor.getName(),
                    instructor.getAvatar(),
                    coursesRepo.findAllByUserId(instructor.getId()).size()
            );
            return ResponseEntity.ok().body(new Default("Instructor Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{courseId}/reviews")
    public ResponseEntity<?> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<Ratings> reviews = ratingRepo.findAllByCourseId(courseId, pageable);
            List<RatingRes> reviewList = reviews
                    .stream()
                    .map(ratingMapper::toDto)
                    .toList();

            PaginatedResponse<RatingRes> paginatedResponse = new PaginatedResponse<>(
                    "Reviews Fetched Successfully",
                    true,
                    reviewList,
                    reviews.getNumber() + 1,
                    reviews.getSize(),
                    reviews.getTotalElements(),
                    reviews.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/{courseId}/reviews")
    public ResponseEntity<Default> addCourseReview(@PathVariable Long courseId, @Valid @RequestBody CourseReviewReq review) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            var isUserAlreadyRated = ratingRepo.existsByCourseIdAndUserId(courseId, user.getId());
            if (isUserAlreadyRated) {
                return ResponseEntity.badRequest().body(new Default("User Already Reviewed This Course", false, null, null));
            }

            Ratings rating = new Ratings();
            rating.setCourse(course);
            rating.setUser(user);
            rating.setRating(review.getRating());
            rating.setComment(review.getComment());
            ratingRepo.save(rating);

            return ResponseEntity.ok().body(new Default("Review Added Successfully", true, null, ratingMapper.toDto(rating)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/{courseId}/related")
    public ResponseEntity<Default> getRelatedCourses(@PathVariable Long courseId, @RequestParam(defaultValue = "4") int limit) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            List<CourseRes> related = coursesRepo.findByCategoryIdAndIdNot(course.getCategory().getId(), courseId, PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                    .stream()
                    .map(this::toCourseRes)
                    .toList();
            return ResponseEntity.ok().body(new Default("Related Courses Fetched Successfully", true, null, related));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/{courseId}/enrollments")
    public ResponseEntity<Default> enrollInCourse(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isUserAlreadyEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (isUserAlreadyEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User Already Enrolled", false, null, null));
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setCourses(course);
            enrollment.setUser(user);
            enrollmentRepo.save(enrollment);

            return ResponseEntity.ok().body(new Default("Student Enrolled Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/{courseId}/enrollments")
    @Transactional
    public ResponseEntity<Default> unenrollFromCourse(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isUserEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (!isUserEnrolled) {
                return ResponseEntity.badRequest().body(new Default("User Is Not Enrolled", false, null, null));
            }

            enrollmentRepo.deleteByUser_IdAndCourses_Id(user.getId(), courseId);
            return ResponseEntity.ok().body(new Default("Student Unenrolled Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/{courseId}/progress")
    public ResponseEntity<Default> getCourseProgress(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isUserEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (!isUserEnrolled && user.getRole() != User.Role.ADMIN) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            Integer totalLessons = lessonRepo.countByCourses_Id(courseId);
            Integer completedLessons = lessonProgressRepo.countByUser_IdAndLesson_Courses_IdAndIsCompletedTrue(user.getId(), courseId);
            double percent = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;

            CourseProgressRes res = new CourseProgressRes(courseId, totalLessons, completedLessons, percent, totalLessons > 0 && completedLessons.equals(totalLessons));
            return ResponseEntity.ok(new Default("Course Progress Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/{courseId}/certificate")
    public ResponseEntity<Default> issueCertificate(@PathVariable Long courseId) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            Boolean isUserEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (!isUserEnrolled) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            Certificate existing = certificateRepo.findByUser_IdAndCourse_Id(user.getId(), courseId).orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(new Default("Certificate Already Issued", true, null, this.toCertificateRes(existing)));
            }

            Integer totalLessons = lessonRepo.countByCourses_Id(courseId);
            Integer completedLessons = lessonProgressRepo.countByUser_IdAndLesson_Courses_IdAndIsCompletedTrue(user.getId(), courseId);
            if (totalLessons == 0 || !completedLessons.equals(totalLessons)) {
                return ResponseEntity.badRequest().body(new Default("Course Is Not Completed Yet", false, null, null));
            }

            Certificate certificate = new Certificate();
            certificate.setUser(user);
            certificate.setCourse(course);
            certificate.setCertificateNumber("CERT-" + courseId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            certificateRepo.save(certificate);

            return ResponseEntity.ok(new Default("Certificate Issued Successfully", true, null, this.toCertificateRes(certificate)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private CertificateRes toCertificateRes(Certificate certificate) {
        return new CertificateRes(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getUser().getName(),
                certificate.getCourse().getTitle(),
                certificate.getIssuedAt()
        );
    }

    private CourseRes toCourseRes(Courses course) {
        CourseRes dto = courseMapper.toDto(course);
        Double price = pricingRepo.getMinPlanPriceByCourseId(course.getId());
        Double avgRating = ratingRepo.avgRatingOfCourse(course.getId());
        Integer totalRating = ratingRepo.totalRatingofCourse(course.getId());
        Integer upCount = reviewRepo.countReviewByCourseIdAndVoteType(course.getId(), Review.VoteType.UPVOTE);
        Integer downCount = reviewRepo.countReviewByCourseIdAndVoteType(course.getId(), Review.VoteType.DOWNVOTE);
        dto.setPrice(price);
        dto.setAvgRating(avgRating);
        dto.setTotalRating(totalRating);
        dto.setUpvote(upCount);
        dto.setDownvote(downCount);
        return dto;
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
