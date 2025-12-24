package com.lms.lms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.AiChatBot;
import com.lms.lms.modals.AiDailyUsage;
import com.lms.lms.repo.AiChatBotRepo;
import com.lms.lms.repo.AiDailyUsageRepo;
import com.lms.lms.service.ChatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
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
    //
    @Autowired
    private ChatService chatService;

    @Transactional
    @MessageMapping("/chat")
    @SendTo("/topic/chat/response")
    public Object askGemini(String prompt, Principal principal) {
        try {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);

            if (principal == null) {
                return Map.of("error", "Invalid User", "status", false);
            }

            AiDailyUsage usage = usageRepo
                    .findByUserIdAndUsageDateForUpdate(principal.getName(), today) // here in username id is stored.
                    .orElseGet(() -> {
                        AiDailyUsage u = new AiDailyUsage();

                        u.setUserId(principal.getName());
                        u.setUsageDate(today);
                        u.setQuestionsUsed(0);
                        u.setTokensUsed(0);
                        return usageRepo.save(u);
                    });

            int safeMaxOutputTokens = calculateSafeMaxOutputTokens(prompt, usage.getTokensUsed());

            if (safeMaxOutputTokens <= 0) {
                throw new RuntimeException("Not enough AI tokens remaining today");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(prompt);

            String message = json.get("message").asText();

            List<String> prompts = chatRepo.getPromptsByUserId(principal.getName());
            Map<String, Object> chat = (Map<String, Object>) chatService.chat(safeMaxOutputTokens, message, prompts);

            usage.setQuestionsUsed(usage.getQuestionsUsed() + 1);
            usage.setTokensUsed(usage.getTokensUsed() + (Integer) chat.get("totalTokens"));

            usageRepo.save(usage);


            AiChatBot msg = new AiChatBot();
            msg.setUserId("1a8f3943-acc7-42da-a007-83066bd39c52");
            msg.setPrompt(message);
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

    @GetMapping("")
    public ResponseEntity<Default> getUserChat() {
        try {
            var isUserExist = userDetails.userDetails();
            if (isUserExist == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            var data = chatRepo.findByUserId(isUserExist.getId());
            return new ResponseEntity<>(new Default("User Chat fetched Successfully", true, null, data), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
