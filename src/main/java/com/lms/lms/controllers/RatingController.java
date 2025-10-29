package com.lms.lms.controllers;

import com.lms.lms.dto.request.RatingReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.RatingRes;
import com.lms.lms.mappers.RatingMapper;
import com.lms.lms.modals.Ratings;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.RatingRepo;
import com.lms.lms.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingRepo ratingRepo;


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private RatingMapper ratingMapper;

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getRatingsByCourseId(@PathVariable Long courseId) {
        try {
            List<RatingRes> ratings = ratingRepo.findByCourseId(courseId)
                    .stream()
                    .map(ratingMapper::toDto)
                    .toList();
            return ResponseEntity.ok().body(new Default("Rating Fetched Successfully", true, null, ratings));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Default> addRatings(@Valid @RequestBody RatingReq rating) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();
            var isCourseExist = coursesRepo.findById(rating.getCourseId()).orElse(null);
            if (isCourseExist == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            var isUserExist = userRepo.findById(user.getUsername()).orElse(null);
            if (isUserExist == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            var isUserAlreadyRated = ratingRepo.existsByCourseIdAndUserId(isCourseExist.getId(), isUserExist.getId());

            if (isUserAlreadyRated) {
                return ResponseEntity.badRequest().body(new Default("User Already Rated", false, null, null));
            }
            Ratings ratingmodel = new Ratings();
            ratingmodel.setCourse(isCourseExist);
            ratingmodel.setComment(rating.getComment());
            ratingmodel.setRating(rating.getRating());
            ratingmodel.setUser(isUserExist);
            ratingRepo.save(ratingmodel);
            return ResponseEntity.ok().body(new Default("Rating Added Successfully", true, null, null));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteRating(@PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();
            var isRatingExist = ratingRepo.findById(id).orElse(null);
            if (isRatingExist == null) {
                return ResponseEntity.badRequest().body(new Default("Rating Not Found", false, null, null));
            }

            var isOwner = userRepo.findById(user.getUsername()).orElse(null);
            if (isOwner == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            if (!isRatingExist.getUser().getId().equals(isOwner.getId())) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            ratingRepo.deleteById(id);
            return ResponseEntity.ok().body(new Default("Rating Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Default> updateRatings(@Valid @RequestBody RatingReq rating) {
        try {
            if (rating.getId() == null || rating.getId().isBlank()) {
                return ResponseEntity.badRequest().body(new Default("Rating Id is required", false, null, null));
            }
            var isRatingExist = ratingRepo.findById(rating.getId()).orElse(null);
            if (isRatingExist == null) {
                return ResponseEntity.badRequest().body(new Default("Rating Not Found", false, null, null));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();

            var isOwner = userRepo.findById(user.getUsername()).orElse(null);
            if (isOwner == null) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            if (!isRatingExist.getUser().getId().equals(isOwner.getId())) {
                return ResponseEntity.badRequest().body(new Default("User Not Found", false, null, null));
            }

            var isCourseExist = coursesRepo.findById(rating.getCourseId()).orElse(null);
            if (isCourseExist == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }

            isRatingExist.setCourse(isCourseExist);
            isRatingExist.setComment(rating.getComment());
            isRatingExist.setRating(rating.getRating());
            ratingRepo.save(isRatingExist);
            return ResponseEntity.ok().body(new Default("Rating Updated Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
