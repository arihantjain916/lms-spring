package com.lms.lms.controllers;

import com.lms.lms.dto.request.ReviewReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Review;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.ReviewRepo;
import com.lms.lms.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CoursesRepo coursesRepo;

//    @GetMapping("/test")
//    public  String test(){
//
//        Integer uptimecount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.UPVOTE);
//        Integer downcount = reviewRepo.countReviewByCourseIdAndVoteType(9L, Review.VoteType.DOWNVOTE);
//        System.out.println(uptimecount + " " + downcount);
//        return "Arihant";
//    }

    @PostMapping("/add")
    public ResponseEntity<Default> addReview(@Valid @RequestBody ReviewReq reviewReq) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = (UserDetails) authentication.getPrincipal();

            var isUserExist = userRepo.findById(user.getUsername()).orElse(null);
            var isCourseExist = coursesRepo.findById(reviewReq.getCourse_id()).orElse(null);

            if (isUserExist == null) {
                return ResponseEntity.badRequest().body(new Default("User don't exist", false, null, null));
            }

            if (isCourseExist == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid CourseId", false, null, null));
            }

            Review review = new Review();
            review.setUser(isUserExist);
            review.setCourse(isCourseExist);
            review.setVote_type(reviewReq.getVote_type());
            return ResponseEntity.internalServerError().body(new Default("Review Added Successfully", false, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
