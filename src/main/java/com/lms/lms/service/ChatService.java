package com.lms.lms.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Optional;

@Controller
public class ChatService {
    @Value("${spring.gemini.api-key}")
    private String geminiApiKey;


    public Object chat(int safeMaxOutputTokens, String prompt) throws Exception {
        try (Client client = Client.builder().apiKey(geminiApiKey).build()) {
            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .maxOutputTokens(safeMaxOutputTokens)
                            .temperature(0.4f)
                            .systemInstruction(
                                    Content.fromParts(Part.fromText("""
                                            You are a teaching assistant who help students in learning new things and answer their questions. Limit your answer in about 500 words maximum. Rules:
                                            - Explain concepts clearly and accurately
                                            - Adjust explanations based on the user's role
                                            - Use simple examples when helpful
                                            - Limit response to about 100 words
                                            - Only answer questions related to education or Learning Management System (LMS) courses.
                                            - Do not provide any additional information or context not directly related to the question.
                                            - This includes teaching, learning, academic subjects, coursework, assignments, exams, quizzes, certifications, and LMS-delivered content.
                                            - For any question not related to education or LMS courses, respond briefly that you can only assist with education and LMS-related topics.
                                            - Do not provide information on non-educational topics.
                                            """)))
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
            throw new Exception("ERROR");
        }
    }
}
