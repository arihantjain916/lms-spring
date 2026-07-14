package com.lms.lms.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ChatService {
    @Value("${spring.gemini.api-key}")
    private String geminiApiKey;


    public Object chat(int safeMaxOutputTokens, String prompt, List<String> prompts) throws Exception {
        try (Client client = Client.builder().apiKey(geminiApiKey).build()) {
            String previousChats = String.join("\n- ", prompts);
            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .maxOutputTokens(safeMaxOutputTokens)
                            .temperature(0.4f)
                            .systemInstruction(
                                    Content.fromParts(Part.fromText(String.format("""
                                            You are a teaching assistant who helps students learn and answers their study questions.

                                            SCOPE:
                                            - Your default behaviour is to ANSWER. If a question is about any academic or educational subject, answer it fully and helpfully.
                                            - Academic subjects include (but are not limited to): maths, physics, chemistry, biology, computer science theory, history, geography, economics, literature, languages, general knowledge, study skills, exam and assignment preparation, and anything covered by an LMS course.
                                            - Also answer questions about the LMS itself: courses, lessons, quizzes, certificates, enrolment.
                                            - Never refuse a question just because it is not explicitly about the LMS. A plain educational question such as "what is photosynthesis" or "explain Newton's second law" must be answered.

                                            OUT OF SCOPE (refuse politely, in one short sentence, and invite an educational question instead):
                                            - Requests to write, debug, review or generate code, or to act as a general programming assistant. You may explain programming *concepts* in theory, but do not produce working code or fix the user's code.
                                            - Non-educational topics: personal advice, medical/legal/financial advice, politics, entertainment, gossip, shopping, or general chit-chat.

                                            UNCLEAR INPUT:
                                            - If the message is meaningless, incomplete or has no context (for example just a number like "58", a random word, or stray characters), do NOT refuse and do NOT guess. Reply that you do not have enough context and ask the user what they would like to know about it.

                                            STYLE:
                                            - Explain concepts clearly and accurately, adjusting the depth to the user's level.
                                            - Use simple examples when they help.
                                            - Keep answers focused and under about 300 words.

                                            Previous chats of this user, for context only: %s
                                            """, previousChats))
                                    ))
                            .build();


            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash-lite",
                            prompt,
                            config);

            Optional<GenerateContentResponseUsageMetadata> meta = response.usageMetadata();
            Integer promptTokens = meta.get().promptTokenCount().orElse(0);
            Integer completionTokens = meta.get().candidatesTokenCount().orElse(0);
            Integer totalTokens = meta.get().totalTokenCount().orElse(0);
            return Map.of("message", response.text(),
                    "meta", meta,
                    "promptTokens", promptTokens,
                    "completionTokens", completionTokens,
                    "totalTokens", totalTokens
            );


        } catch (Exception e) {
            System.out.println("ee" + e);
            throw new Exception("ERROR");
        }
    }
}
