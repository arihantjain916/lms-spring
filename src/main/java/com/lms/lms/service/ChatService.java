package com.lms.lms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
public class ChatService {
    @Value("${spring.gemini.api-key}")
    private String geminiApiKey;

    @MessageMapping("/chat")
    @SendTo("/topic/chat/response")
    public Object chat(String message) throws Exception {
        try (Client client = Client.builder().apiKey(geminiApiKey).build()) {
            System.out.println("message" + message);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message);

            String name = jsonNode.get("message").asText();


            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .systemInstruction(
                                    Content.fromParts(Part.fromText("You are a teaching assistant who help students in learning new things and answer their questions. Limit your answer in about 500 words maximum.")))
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash-lite",
                            name,
                            config);

            return Map.of("message", response.text(), "status", true);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
