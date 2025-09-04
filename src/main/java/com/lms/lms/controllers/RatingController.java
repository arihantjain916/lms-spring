package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.RatingRes;
import com.lms.lms.mappers.RatingMapper;
import com.lms.lms.repo.RatingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingRepo ratingRepo;

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
}
