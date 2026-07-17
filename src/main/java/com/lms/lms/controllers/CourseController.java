package com.lms.lms.controllers;

import com.lms.lms.dto.request.CoursePricingReq;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

            // a course with no plan is treated as free at checkout, so only price it when asked
            if (courses.getPricingPlanId() != null && !courses.getPricingPlanId().isBlank()) {
                Pricing_Plans plan = pricingRepo.findById(courses.getPricingPlanId()).orElse(null);
                if (plan == null) {
                    return ResponseEntity.badRequest().body(new Default("Pricing Plan Not Found", false, null, null));
                }
                plan.getCourses().add(course);
                pricingRepo.save(plan);
            } else if (courses.getPrice() != null) {
                Pricing_Plans plan = new Pricing_Plans();
                plan.setTitle(course.getTitle());
                plan.setDescription(course.getDescription());
                plan.setPrice(courses.getPrice());
                plan.setCurrency(courses.getCurrency() == null || courses.getCurrency().isBlank() ? "INR" : courses.getCurrency());
                plan.setPlanType(courses.getPlanType() == null || courses.getPlanType().isBlank()
                        ? Pricing_Plans.PlanType.LIFETIME
                        : Pricing_Plans.PlanType.valueOf(courses.getPlanType()));
                plan.getCourses().add(course);
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
    @Transactional
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

            if (course.getPrice() != null) {
                Default pricingError = this.applyPriceToOwnPlan(isCourseExist, course);
                if (pricingError != null) {
                    return ResponseEntity.badRequest().body(pricingError);
                }
            }

            return ResponseEntity.ok().body(new Default("Course Updated Successfully", true, null, null));
        }
        catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // Repoints the course's own price. Only touches a plan this course alone uses: editing a
    // plan shared with other courses would reprice them too, so that is refused here and has
    // to go through /pricing (edit the shared plan) or /course/{id}/pricing (move off it).
    // Returns null on success, or the error to send back.
    private Default applyPriceToOwnPlan(Courses course, CourseReq req) {
        List<Pricing_Plans> attached = pricingRepo.findByCourses_IdOrderByPriceAsc(course.getId());

        if (attached.size() > 1) {
            return new Default("Course Has " + attached.size() + " Pricing Plans. Use /pricing To Edit One", false, null, null);
        }

        Pricing_Plans.PlanType planType = req.getPlanType() == null || req.getPlanType().isBlank()
                ? Pricing_Plans.PlanType.LIFETIME
                : Pricing_Plans.PlanType.valueOf(req.getPlanType());
        String currency = req.getCurrency() == null || req.getCurrency().isBlank() ? "INR" : req.getCurrency();

        if (attached.isEmpty()) {
            Pricing_Plans plan = new Pricing_Plans();
            plan.setTitle(course.getTitle());
            plan.setDescription(course.getDescription());
            plan.setPrice(req.getPrice());
            plan.setCurrency(currency);
            plan.setPlanType(planType);
            plan.getCourses().add(course);
            pricingRepo.save(plan);
            return null;
        }

        Pricing_Plans plan = attached.get(0);
        long sharedWith = pricingRepo.countAttachedCourses(plan.getId());
        if (sharedWith > 1) {
            return new Default("Pricing Plan Is Shared By " + sharedWith + " Courses. Use /pricing To Edit It", false, null, null);
        }

        plan.setPrice(req.getPrice());
        plan.setCurrency(currency);
        plan.setPlanType(planType);
        pricingRepo.save(plan);
        return null;
    }

    // Sets which plans this course is sold on. The list is the whole truth: plans attached
    // now but missing from it are detached, so a tier swap is one atomic call rather than
    // a detach and an attach that can fail apart and leave the course free.
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/{courseId}/pricing")
    @Transactional
    public ResponseEntity<Default> updateCoursePricing(@PathVariable Long courseId, @Valid @RequestBody CoursePricingReq req) {
        try {
            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!this.canManageCourse(course)) {
                return ResponseEntity.status(403).body(new Default("You are not authorized to price this course", false, null, null));
            }

            List<String> requestedIds = req.getPricingPlanIds().stream().distinct().toList();

            List<Pricing_Plans> requested = new ArrayList<>();
            for (String planId : requestedIds) {
                Pricing_Plans plan = pricingRepo.findById(planId).orElse(null);
                if (plan == null) {
                    return ResponseEntity.badRequest().body(new Default("Pricing Plan Not Found: " + planId, false, null, null));
                }
                requested.add(plan);
            }

            // a course sells one plan per billing type, so the requested set must not collide with itself
            Set<Pricing_Plans.PlanType> seen = new HashSet<>();
            for (Pricing_Plans plan : requested) {
                if (!seen.add(plan.getPlanType())) {
                    return ResponseEntity.badRequest().body(new Default("More Than One " + plan.getPlanType().name() + " Plan Requested", false, null, null));
                }
            }

            for (Pricing_Plans attached : pricingRepo.findByCourses_IdOrderByPriceAsc(courseId)) {
                if (!requestedIds.contains(attached.getId())) {
                    attached.getCourses().removeIf(c -> c.getId().equals(courseId));
                    pricingRepo.save(attached);
                }
            }

            for (Pricing_Plans plan : requested) {
                if (plan.getCourses().stream().noneMatch(c -> c.getId().equals(courseId))) {
                    plan.getCourses().add(course);
                    pricingRepo.save(plan);
                }
            }

            return ResponseEntity.ok().body(new Default("Course Pricing Updated Successfully", true, null, requestedIds));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
