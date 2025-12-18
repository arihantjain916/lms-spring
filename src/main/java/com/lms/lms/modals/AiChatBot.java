package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class AiChatBot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = true, columnDefinition = "text")
    private String response;

    @Column(nullable = false)
    private String model;

    private String userId;
    private String sessionId;

    private String conversationId;
    private Integer messageOrder;

    @Enumerated(EnumType.STRING)
    private MessageRole role;

    @Column(columnDefinition = "text", nullable = false)
    private String prompt;

    private Double temperature;
    private Integer maxTokens;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private BigDecimal cost;

    // Optional context
    private String courseId;
    private String lessonId;
    private String page;

    private Boolean helpful;
    private Boolean flagged;
    private String feedback;

    private Instant createdAt;
}

