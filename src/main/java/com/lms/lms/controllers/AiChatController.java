package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.modals.AiChatBot;
import com.lms.lms.modals.AiDailyUsage;
import com.lms.lms.repo.AiChatBotRepo;
import com.lms.lms.repo.AiDailyUsageRepo;
import com.lms.lms.service.ChatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
public class AiChatController {

    private static final int STUDENT_DAILY_QUESTION_LIMIT = 10;
    private static final int STUDENT_DAILY_TOKEN_LIMIT = 20_000;
    private static final int STUDENT_MAX_RESPONSE_TOKENS = 1_000;


    @Autowired
    private AiDailyUsageRepo usageRepo;

    @Autowired
    private AiChatBotRepo chatRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private ChatService chatService;

    @Transactional
    @MessageMapping("/chat")
    @SendTo("/topic/chat/response")
    public Object askGemini(@RequestBody String prompt) {
        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);

            var isUserExist = userDetails.userDetails();

            if (isUserExist == null) {
                return Map.of("error", "Invalid User", "status", false);
            }
            AiDailyUsage usage = usageRepo
                    .findByUserIdAndUsageDateForUpdate(isUserExist.getId(), today)
                    .orElseGet(() -> {
                        AiDailyUsage u = new AiDailyUsage();

                        u.setUserId(isUserExist.getId());
                        u.setUsageDate(today);
                        u.setQuestionsUsed(0);
                        u.setTokensUsed(0);
                        return usageRepo.save(u);
                    });

            int safeMaxOutputTokens = calculateSafeMaxOutputTokens(prompt, usage.getTokensUsed());

            if (safeMaxOutputTokens <= 0) {
                throw new RuntimeException("Not enough AI tokens remaining today");
            }

            Map<String, Object> chat = (Map<String, Object>) chatService.chat(safeMaxOutputTokens, prompt);

            usage.setQuestionsUsed(usage.getQuestionsUsed() + 1);
            usage.setTokensUsed(usage.getTokensUsed() + (Integer) chat.get("totalTokens"));

            usageRepo.save(usage);


            AiChatBot msg = new AiChatBot();
            msg.setUserId("1a8f3943-acc7-42da-a007-83066bd39c52");
            msg.setPrompt(prompt);
            msg.setResponse((String) chat.get("message"));
            msg.setPromptTokens((Integer) chat.get("promptTokens"));
            msg.setCompletionTokens((Integer) chat.get("completionTokens"));
            msg.setTotalTokens((Integer) chat.get("completionTokens"));
            msg.setCreatedAt(Instant.now());
            msg.setModel("gemini-2.5-flash-lite");

            chatRepo.save(msg);

            return Map.of("message", chat.get("message"), "status", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage(), "status", false);
        }
    }


    private int calculateSafeMaxOutputTokens(String prompt, int usedTokens) {
        int estimatedPromptTokens = prompt.length() / 4;  // Rough estimate: 1 token ≈ 4 characters
        int remainingTokens = STUDENT_DAILY_TOKEN_LIMIT - usedTokens;

        return Math.min(
                STUDENT_MAX_RESPONSE_TOKENS,
                remainingTokens - estimatedPromptTokens
        );
    }
}
