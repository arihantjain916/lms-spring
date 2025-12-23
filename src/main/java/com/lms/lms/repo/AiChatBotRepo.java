package com.lms.lms.repo;

import com.lms.lms.modals.AiChatBot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatBotRepo extends JpaRepository<AiChatBot, String> {
    List<AiChatBot> findByUserId(String userId);
}
