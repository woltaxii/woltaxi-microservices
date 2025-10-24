package com.woltaxi.aiml.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Natural Language Processing Task Entity
 * 
 * Doğal dil işleme görevlerini takip eder.
 * Metin analizi, çeviri, sentiment analizi, chatbot etkileşimleri vb.
 */
@Entity
@Table(name = "nlp_tasks", indexes = {
    @Index(name = "idx_nlp_task_type", columnList = "taskType"),
    @Index(name = "idx_nlp_language", columnList = "language"),
    @Index(name = "idx_nlp_date", columnList = "createdAt"),
    @Index(name = "idx_nlp_user", columnList = "userId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskType; // SENTIMENT_ANALYSIS, TRANSLATION, CHATBOT, TEXT_CLASSIFICATION, ENTITY_EXTRACTION

    @Column
    private Long userId;

    @Column
    private String serviceName; // WOLTAXI, WOLKURYE, CUSTOMER_SERVICE

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputText;

    @Column(columnDefinition = "TEXT")
    private String outputText;

    @Column(nullable = false)
    private String language; // TR, EN, AR, etc.

    @Column
    private String targetLanguage; // For translation tasks

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(precision = 10, scale = 4)
    private BigDecimal confidence;

    @Column(precision = 10, scale = 2)
    private BigDecimal processingTimeMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // Sentiment Analysis
    @Column
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL

    @Column(precision = 10, scale = 4)
    private BigDecimal sentimentScore; // -1.0 to 1.0

    @Column(precision = 10, scale = 4)
    private BigDecimal positivityScore;

    @Column(precision = 10, scale = 4)
    private BigDecimal negativityScore;

    @Column(precision = 10, scale = 4)
    private BigDecimal neutralityScore;

    // Emotion Detection
    @Column(columnDefinition = "TEXT")
    private String emotions; // JSON array of detected emotions

    @Column
    private String dominantEmotion; // JOY, ANGER, FEAR, SADNESS, SURPRISE, DISGUST

    // Text Classification
    @Column
    private String category;

    @Column
    private String subCategory;

    @Column(columnDefinition = "TEXT")
    private String topics; // JSON array of identified topics

    // Named Entity Recognition
    @Column(columnDefinition = "TEXT")
    private String entities; // JSON array of extracted entities

    @Column(columnDefinition = "TEXT")
    private String persons; // Detected person names

    @Column(columnDefinition = "TEXT")
    private String locations; // Detected locations

    @Column(columnDefinition = "TEXT")
    private String organizations; // Detected organizations

    @Column(columnDefinition = "TEXT")
    private String dates; // Detected dates and times

    // Intent Detection (for chatbot)
    @Column
    private String detectedIntent;

    @Column(precision = 10, scale = 4)
    private BigDecimal intentConfidence;

    @Column(columnDefinition = "TEXT")
    private String extractedParameters; // JSON format

    // Language Detection
    @Column
    private String detectedLanguage;

    @Column(precision = 10, scale = 4)
    private BigDecimal languageConfidence;

    // Text Quality Metrics
    @Column
    private Integer wordCount;

    @Column
    private Integer sentenceCount;

    @Column
    private Integer characterCount;

    @Column(precision = 10, scale = 4)
    private BigDecimal readabilityScore;

    @Column
    private String readabilityLevel; // EASY, MEDIUM, HARD

    // Translation Specific
    @Column(precision = 10, scale = 4)
    private BigDecimal translationQuality;

    @Column
    private Boolean humanReviewRequired = false;

    // Customer Service Context
    @Column
    private String customerIntent; // COMPLAINT, QUESTION, COMPLIMENT, REQUEST

    @Column
    private String urgencyLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column
    private Boolean requiresHumanAgent = false;

    @Column(columnDefinition = "TEXT")
    private String suggestedResponse;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String processedBy; // Model or service name

    public enum TaskStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        TIMEOUT,
        CANCELLED
    }
}