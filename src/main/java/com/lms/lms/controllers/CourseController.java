package com.lms.lms.controllers;

import com.lms.lms.dto.request.CourseReq;
import com.lms.lms.dto.response.CourseRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.mappers.CourseMapper;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Review;
import com.lms.lms.repo.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public Iterable<CourseRes> getCourses()
    {
        return coursesRepo.findAll()
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
    }

    @GetMapping("/{id}")
    public ResponseEntity<Default> getCourseById(@PathVariable Long id) {
       try{
           Courses course = coursesRepo.findById(id).orElse(null);
           if (course == null) {
               return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
           }
           return ResponseEntity.ok().body(new Default("Course Found", true, null, courseMapper.toDto(course)));
       } catch (Exception e) {
           return ResponseEntity.internalServerError().body(new Default("Internal Server Error", false, null, null));
       }
    }

    @GetMapping("/category/{category_id}")
    public ResponseEntity<Default> getCoursebyCateoryId(@PathVariable String category_id){
       List<CourseRes> courses = coursesRepo.findByCategoryId(category_id)
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


       if(courses.isEmpty()){
           return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
       }

       return ResponseEntity.ok().body(new Default("Course Found", true, null, courses));

    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Default> getCoursebySlug(@PathVariable String slug){
        List<CourseRes> courses= coursesRepo.findAllBySlug(slug)
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

        if(courses.isEmpty()){
            return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
        }

        return ResponseEntity.ok().body(new Default("Course Found", true, null, courses));
    }

    @PostMapping("/add")
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
            coursesRepo.save(course);

            return ResponseEntity.ok().body(new Default("Course Added Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

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
            coursesRepo.save(isCourseExist);

            return ResponseEntity.ok().body(new Default("Course Updated Successfully", true, null, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteCourse(@PathVariable("id") Long id){
        try{
            var isCourseExist = coursesRepo.findById(id).orElse(null);
            if(isCourseExist == null){
                return  ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            coursesRepo.deleteById(id);
            return ResponseEntity.ok().body(new Default("Course Deleted Successfully", true, null, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null,null));
        }
    }
}
