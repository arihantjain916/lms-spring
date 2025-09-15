package com.lms.lms.dto.request;

import com.lms.lms.modals.Review;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewReq {
    @Column(nullable = false)
    @NotBlank(message = "Course id is required")
    private Long course_id;

    @Column(nullable = false)
    @NotNull(message = "Vote type is required")
    private Review.VoteType vote_type;
}
