package com.lms.lms.repo;

import com.lms.lms.modals.AiChatBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiChatBotRepo extends JpaRepository<AiChatBot, String> {
    List<AiChatBot> findByUserId(String userId);

    @Query("SELECT a.prompt FROM AiChatBot a WHERE a.userId = :userId order by a.createdAt desc limit 10")
    List<String> getPromptsByUserId(@Param("userId") String userId);

}
