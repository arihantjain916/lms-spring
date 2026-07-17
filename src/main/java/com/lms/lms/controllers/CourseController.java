package com.lms.lms.controllers;

import com.lms.lms.dto.request.CourseReq;
import com.lms.lms.dto.response.CourseRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.mappers.CourseMapper;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Pricing_Plans;
import com.lms.lms.modals.Review;
import com.lms.lms.modals.User;
import com.lms.lms.repo.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PricingRepo pricingRepo;

    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @GetMapping("/all")
    public ResponseEntity<PaginatedResponse<CourseRes>> getCourses(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    )
    {

        int pageNumber = page > 0 ? page - 1 : 0;
        Sort sort = Objects.equals(order, "asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        Page<Courses> allCourses = coursesRepo.findAll(pageable);
        String uid = this.currentUserId();
        List<CourseRes> courseList = allCourses
                .stream()
                .map(course -> {
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
                    dto.setIsEnrolled(this.isEnrolled(uid, course.getId()));
                    return dto;
                })
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
    }

    @GetMapping("/{id}")
    public ResponseEntity<Default> getCourseById(@PathVariable Long id) {
       try{
           Courses course = coursesRepo.findById(id).orElse(null);
           if (course == null) {
               return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
           }
           CourseRes dto = courseMapper.toDto(course);
           dto.setIsEnrolled(this.isEnrolled(this.currentUserId(), course.getId()));
           return ResponseEntity.ok().body(new Default("Course Found", true, null, dto));
       } catch (Exception e) {
           return ResponseEntity.internalServerError().body(new Default("Internal Server Error", false, null, null));
       }
    }

    @GetMapping("/category/{category_id}")
    public ResponseEntity<PaginatedResponse<CourseRes>> getCoursebyCateoryId(
            @PathVariable String category_id, @RequestParam(required = false) String userId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {

        int pageNumber = page > 0 ? page - 1 : 0;
        Sort sort = Objects.equals(order, "asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        Page<Courses> allCourses = coursesRepo.findByCategoryId(category_id, pageable);
        String uid = this.currentUserId();
        List<CourseRes> courses = allCourses
               .stream()
               .map(course -> {
                   CourseRes dto = courseMapper.toDto(course);
                   Double price = pricingRepo.getMinPlanPriceByCourseId(course.getId());
                   Double avgRating = ratingRepo.avgRatingOfCourse(course.getId());
                   Integer totalRating = ratingRepo.totalRatingofCourse(course.getId());
                   Integer upcount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.UPVOTE);
                   Integer downcount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.DOWNVOTE);
                   dto.setPrice(price);
                   dto.setAvgRating(avgRating);
                   dto.setTotalRating(totalRating);
                   dto.setUpvote(upcount);
                   dto.setDownvote(downcount);
                   dto.setIsEnrolled(this.isEnrolled(uid, course.getId()));
                   return dto;
               })
               .toList();

        PaginatedResponse<CourseRes> paginatedResponse = new PaginatedResponse<>(
                "Courses Fetched Successfully",
                true,
                courses,
                allCourses.getNumber() + 1,
                allCourses.getSize(),
                allCourses.getTotalElements(),
                allCourses.getTotalPages()
        );
        return ResponseEntity.ok().body(paginatedResponse);

    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Default> getCoursebySlug(@PathVariable String slug){
        Courses course = coursesRepo.findBySlug(slug).orElse(null);
        if (course == null) {
            return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
        }
        CourseRes courseRes = courseMapper.toDto(course);
        Double price = pricingRepo.getMinPlanPriceByCourseId(course.getId());
        Double avgRating = ratingRepo.avgRatingOfCourse(course.getId());
        Integer totalRating = ratingRepo.totalRatingofCourse(course.getId());
        Integer upCount = reviewRepo.countReviewByCourseIdAndVoteType(course.getId(), Review.VoteType.UPVOTE);
        Integer downCount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.DOWNVOTE);
        courseRes.setPrice(price);
        courseRes.setAvgRating(avgRating);
        courseRes.setTotalRating(totalRating);
        courseRes.setUpvote(upCount);
        courseRes.setDownvote(downCount);
        courseRes.setIsEnrolled(this.isEnrolled(this.currentUserId(), course.getId()));


        return ResponseEntity.ok().body(new Default("Course Found", true, null, courseRes));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addCourse(@Valid @RequestBody CourseReq courses){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();

            var slug= courses.getSlug();

            var isSlugExist = coursesRepo.findBySlug(slug).orElse(null);

            var isCategoryExist = categoryRepo.findById(courses.getCategoryId()).orElse(null);

            var isUserExist = userRepo.findById(user.getUsername()).orElse(null);

            if(isUserExist == null){
                return ResponseEntity.badRequest().body(new Default("User don't exist", false, null,null));
            }

            if(isSlugExist != null){
                return ResponseEntity.badRequest().body(new Default("Slug Already Exist. Please try with another one", false, null, null));
            }

            if(isCategoryExist == null){
                return ResponseEntity.badRequest().body(new Default("Category Don't Exist", false, null,null));
            }

            Courses course = new Courses();
            course.setTitle(courses.getTitle());
            course.setSlug(slug);
            course.setDescription(courses.getDescription());
            course.setCategory(isCategoryExist);
            course.setUser(isUserExist);
            course.setIsFeatured(courses.getIsFeatured());
            if (courses.getLevel() != null && !courses.getLevel().isBlank()) {
                course.setLevel(Courses.Level.valueOf(courses.getLevel()));
            }
            coursesRepo.save(course);

            // a course with no plan is treated as free at checkout, so only seed a plan when a price was given
            if (courses.getPrice() != null) {
                Pricing_Plans plan = new Pricing_Plans();
                plan.setCourses(course);
                plan.setTitle(course.getTitle());
                plan.setDescription(course.getDescription());
                plan.setPrice(courses.getPrice());
                plan.setCurrency(courses.getCurrency() == null || courses.getCurrency().isBlank() ? "INR" : courses.getCurrency());
                plan.setPlanType(courses.getPlanType() == null || courses.getPlanType().isBlank()
                        ? Pricing_Plans.PlanType.LIFETIME
                        : Pricing_Plans.PlanType.valueOf(courses.getPlanType()));
                pricingRepo.save(plan);
            }

            return ResponseEntity.ok().body(new Default("Course Added Successfully", true, null, null));
        } catch (Exception e) {
            // this catch swallows the exception, so the transaction would otherwise commit
            // and leave a priced course with no plan behind the 500
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/update")
    public ResponseEntity<?> updateCourse (@Valid @RequestBody CourseReq course){
        try{
            var id = course.getId();

            var isCourseExist = coursesRepo.findById(id).orElse(null);

            var isCategoryExist = categoryRepo.findById(course.getCategoryId()).orElse(null);

            var isSlugExist = coursesRepo.findBySlug(course.getSlug()).orElse(null);

            if(isCourseExist == null){
                return ResponseEntity.badRequest().body(new Default("Invalid Course Id", false, null, null));
            }

            if (!this.canManageCourse(isCourseExist)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to modify this course", false, null, null));
            }

            if(isCategoryExist == null){
                return ResponseEntity.badRequest().body(new Default("Category Don't Exist", false, null, null));
            }

            if (isSlugExist != null && !isSlugExist.getId().equals(course.getId())) {
                return ResponseEntity.badRequest().body(new Default("Course Already Exist With Same Slug", false, null, null));
            }

            isCourseExist.setTitle(course.getTitle());
            isCourseExist.setSlug(course.getSlug());
            isCourseExist.setDescription(course.getDescription());
            isCourseExist.setCategory(isCategoryExist);
            isCategoryExist.setIsFeatured(course.getIsFeatured());
            if (course.getLevel() != null && !course.getLevel().isBlank()) {
                isCourseExist.setLevel(Courses.Level.valueOf(course.getLevel()));
            }
            coursesRepo.save(isCourseExist);

            return ResponseEntity.ok().body(new Default("Course Updated Successfully", true, null, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteCourse(@PathVariable("id") Long id){
        try{
            var isCourseExist = coursesRepo.findById(id).orElse(null);
            if(isCourseExist == null){
                return  ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            if (!this.canManageCourse(isCourseExist)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to delete this course", false, null, null));
            }

            coursesRepo.deleteById(id);
            return ResponseEntity.ok().body(new Default("Course Deleted Successfully", true, null, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null,null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Default> getUserCourse() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();

            var userInfo = userRepo.findById(user.getUsername()).orElse(null);
            if (userInfo == null) {
                return ResponseEntity.badRequest().body(new Default("User Don't Exist", false, null, null));
            }
            List<CourseRes> courseRes = coursesRepo.findAllByUserId(userInfo.getId())
                    .stream()
                    .map(course -> {
                        CourseRes dto = courseMapper.toDto(course);
                        Double price = pricingRepo.getMinPlanPriceByCourseId(course.getId());
                        Double avgRating = ratingRepo.avgRatingOfCourse(course.getId());
                        Integer totalRating = ratingRepo.totalRatingofCourse(course.getId());
                        Integer upcount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.UPVOTE);
                        Integer downcount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.DOWNVOTE);
                        dto.setPrice(price);
                        dto.setAvgRating(avgRating);
                        dto.setTotalRating(totalRating);
                        dto.setUpvote(upcount);
                        dto.setDownvote(downcount);
                        return dto;
                    })
                    .toList();

            return ResponseEntity.ok().body(new Default("Blog Fetched Successfully", true, null, courseRes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // resolves the current user's id, or null for anonymous/unauthenticated (course reads are public)
    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return null;
        }
        var user = userRepo.findById(((UserDetails) principal).getUsername()).orElse(null);
        return user != null ? user.getId() : null;
    }

    private boolean isEnrolled(String currentUserId, Long courseId) {
        return currentUserId != null && enrollmentRepo.existsByUser_IdAndCourses_Id(currentUserId, courseId);
    }

    // an instructor may only manage their own courses; admins may manage any
    private boolean canManageCourse(Courses course) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findById(principal.getUsername()).orElse(null);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRole() == User.Role.ADMIN) {
            return true;
        }
        return course.getUser() != null && course.getUser().getId().equals(currentUser.getId());
    }
}
