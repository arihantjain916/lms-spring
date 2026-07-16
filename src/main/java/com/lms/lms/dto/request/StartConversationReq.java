package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StartConversationReq {

    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    // required for DIRECT threads: the instructor/admin being contacted
    private String recipientId;

    // course the thread is about; also what the enrolment check is run against
    private Long courseId;

    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String content;
}
