package com.lms.lms.repo;

import com.lms.lms.modals.QuestionReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionReplyRepo extends JpaRepository<QuestionReply, String> {

    List<QuestionReply> findByQuestion_IdOrderByCreatedAtAsc(String questionId);

    Integer countByQuestion_Id(String questionId);

    int deleteByQuestion_Id(String questionId);
}
