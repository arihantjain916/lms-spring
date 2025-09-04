package com.lms.lms.controllers;

import com.lms.lms.dto.request.RatingReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.RatingRes;
import com.lms.lms.mappers.RatingMapper;
import com.lms.lms.modals.Ratings;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.RatingRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingRepo ratingRepo;

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
            var isCourseExist = coursesRepo.findById(rating.getCourseId()).orElse(null);
            if (isCourseExist == null) {
                return ResponseEntity.badRequest().body(new Default("Course Not Found", false, null, null));
            }
            Ratings ratingmodel = new Ratings();
            ratingmodel.setCourse(isCourseExist);
            ratingmodel.setComment(rating.getComment());
            ratingmodel.setRating(rating.getRating());
            ratingRepo.save(ratingmodel);
            return ResponseEntity.ok().body(new Default("Rating Added Successfully", true, null, null));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Default> deleteRating(@PathVariable String id) {
        try {
            var isRatingExist = ratingRepo.findById(id).orElse(null);
            if (isRatingExist == null) {
                return ResponseEntity.badRequest().body(new Default("Rating Not Found", false, null, null));
            }
            ratingRepo.deleteById(id);
            return ResponseEntity.ok().body(new Default("Rating Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
