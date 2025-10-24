package com.woltaxi.aiml.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Natural Language Processing DTOs
 * Doğal dil işleme işlemleri için data transfer objects
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalysisRequest {
    @NotBlank(message = "Metin boş olamaz")
    @Size(max = 10000, message = "Metin 10,000 karakteri geçemez")
    private String text;
    
    @NotBlank(message = "Dil belirtilmelidir")
    private String language; // TR, EN, AR
    
    private Long userId;
    private String serviceName;
    private String context;
    private Map<String, Object> metadata;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentAnalysisResponse {
    private Long taskId;
    private String status;
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private BigDecimal sentimentScore; // -1.0 to 1.0
    private BigDecimal positivityScore;
    private BigDecimal negativityScore;
    private BigDecimal neutralityScore;
    private BigDecimal confidence;
    private Map<String, BigDecimal> aspectSentiments;
    private List<String> keywords;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDetectionRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    @NotBlank(message = "Dil belirtilmelidir")
    private String language;
    
    private Long userId;
    private String serviceName;
    private Boolean detectIntensity;
    private Map<String, Object> context;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDetectionResponse {
    private Long taskId;
    private String status;
    private String dominantEmotion; // JOY, ANGER, FEAR, SADNESS, SURPRISE, DISGUST
    private Map<String, BigDecimal> emotions;
    private BigDecimal emotionalIntensity;
    private List<EmotionSegment> emotionSegments;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionSegment {
        private String text;
        private String emotion;
        private BigDecimal intensity;
        private Integer startPosition;
        private Integer endPosition;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    @NotBlank(message = "Hedef dil belirtilmelidir")
    private String targetLanguage;
    
    private String sourceLanguage; // Otomatik tespit edilecekse null
    private Long userId;
    private String serviceName;
    private String domain; // GENERAL, TECHNICAL, MEDICAL, LEGAL
    private Boolean preserveFormatting;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    private Long taskId;
    private String status;
    private String originalText;
    private String translatedText;
    private String sourceLanguage;
    private String targetLanguage;
    private BigDecimal translationQuality;
    private BigDecimal confidence;
    private Boolean humanReviewRequired;
    private List<String> alternativeTranslations;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    private String errorMessage;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDetectionRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    private Long userId;
    private Integer maxResults;
    private Boolean returnConfidences;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDetectionResponse {
    private Long taskId;
    private String status;
    private String detectedLanguage;
    private BigDecimal languageConfidence;
    private List<LanguageCandidate> languageCandidates;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageCandidate {
        private String language;
        private String languageName;
        private BigDecimal confidence;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityExtractionRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    @NotBlank(message = "Dil belirtilmelidir")
    private String language;
    
    private List<String> entityTypes; // PERSON, LOCATION, ORGANIZATION, DATE, TIME, MONEY
    private Long userId;
    private String serviceName;
    private Boolean linkEntities;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityExtractionResponse {
    private Long taskId;
    private String status;
    private List<ExtractedEntity> entities;
    private Map<String, List<String>> entitiesByType;
    private Integer totalEntities;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedEntity {
        private String text;
        private String type;
        private String subType;
        private BigDecimal confidence;
        private Integer startPosition;
        private Integer endPosition;
        private String linkedId;
        private Map<String, Object> metadata;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextClassificationRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    @NotBlank(message = "Model tipi belirtilmelidir")
    private String modelType; // TOPIC, INTENT, CATEGORY, SPAM
    
    private String language;
    private Long userId;
    private String serviceName;
    private Integer maxCategories;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextClassificationResponse {
    private Long taskId;
    private String status;
    private String primaryCategory;
    private List<CategoryScore> categories;
    private BigDecimal confidence;
    private String modelUsed;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryScore {
        private String category;
        private String label;
        private BigDecimal score;
        private BigDecimal confidence;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentDetectionRequest {
    @NotBlank(message = "Mesaj boş olamaz")
    private String message;
    
    private String language;
    private String context; // CUSTOMER_SERVICE, BOOKING, COMPLAINT
    private Long userId;
    private String serviceName;
    private Map<String, Object> conversationHistory;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentDetectionResponse {
    private Long taskId;
    private String status;
    private String detectedIntent;
    private BigDecimal intentConfidence;
    private Map<String, Object> extractedParameters;
    private List<IntentCandidate> intentCandidates;
    private String suggestedAction;
    private Boolean requiresHumanAgent;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntentCandidate {
        private String intent;
        private BigDecimal confidence;
        private Map<String, Object> parameters;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {
    @NotBlank(message = "Mesaj boş olamaz")
    private String message;
    
    @NotNull(message = "Kullanıcı ID belirtilmelidir")
    private Long userId;
    
    private String language;
    private String sessionId;
    private String serviceName;
    private Map<String, Object> userContext;
    private List<ChatMessage> conversationHistory;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender; // USER, BOT
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    private Long taskId;
    private String status;
    private String response;
    private String sessionId;
    private String detectedIntent;
    private Map<String, Object> extractedParameters;
    private List<String> suggestedActions;
    private Boolean conversationEnded;
    private Boolean transferToHuman;
    private String transferReason;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime responseTime;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    private String language;
    private String summaryType; // EXTRACTIVE, ABSTRACTIVE
    private Integer maxSentences;
    private Integer maxWords;
    private BigDecimal compressionRatio; // 0.1 - 0.8
    private Long userId;
    private String serviceName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationResponse {
    private Long taskId;
    private String status;
    private String originalText;
    private String summary;
    private String summaryType;
    private Integer originalWordCount;
    private Integer summaryWordCount;
    private BigDecimal compressionRatio;
    private List<String> keyPoints;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractionRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    private String language;
    private Integer maxKeywords;
    private String extractionMethod; // TF_IDF, YAKE, RAKE
    private Boolean includePhrases;
    private Long userId;
    private String serviceName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractionResponse {
    private Long taskId;
    private String status;
    private List<ExtractedKeyword> keywords;
    private List<ExtractedKeyword> phrases;
    private String extractionMethod;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedKeyword {
        private String keyword;
        private BigDecimal relevanceScore;
        private Integer frequency;
        private String type; // KEYWORD, PHRASE
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextSimilarityRequest {
    @NotBlank(message = "İlk metin boş olamaz")
    private String text1;
    
    @NotBlank(message = "İkinci metin boş olamaz")
    private String text2;
    
    private String language;
    private String similarityMethod; // COSINE, JACCARD, SEMANTIC
    private Long userId;
    private String serviceName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextSimilarityResponse {
    private Long taskId;
    private String status;
    private BigDecimal similarityScore; // 0.0 - 1.0
    private String similarityLevel; // LOW, MEDIUM, HIGH
    private String similarityMethod;
    private Map<String, BigDecimal> detailedMetrics;
    private List<String> commonKeywords;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadabilityAnalysisRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    private String language;
    private List<String> readabilityMetrics; // FLESCH, GUNNING_FOG, COLEMAN_LIAU
    private Long userId;
    private String serviceName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadabilityAnalysisResponse {
    private Long taskId;
    private String status;
    private BigDecimal readabilityScore;
    private String readabilityLevel; // VERY_EASY, EASY, FAIRLY_EASY, STANDARD, FAIRLY_DIFFICULT, DIFFICULT, VERY_DIFFICULT
    private Map<String, BigDecimal> readabilityMetrics;
    private TextStatistics textStatistics;
    private List<String> suggestions;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextStatistics {
        private Integer wordCount;
        private Integer sentenceCount;
        private Integer paragraphCount;
        private Integer characterCount;
        private BigDecimal avgWordsPerSentence;
        private BigDecimal avgSyllablesPerWord;
        private Integer complexWords;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpamDetectionRequest {
    @NotBlank(message = "Metin boş olamaz")
    private String text;
    
    @NotBlank(message = "Metin tipi belirtilmelidir")
    private String textType; // EMAIL, SMS, COMMENT, REVIEW
    
    private String language;
    private Long userId;
    private String serviceName;
    private Map<String, Object> context;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpamDetectionResponse {
    private Long taskId;
    private String status;
    private Boolean isSpam;
    private BigDecimal spamProbability;
    private String spamType; // PROMOTIONAL, PHISHING, SCAM, ADULT
    private List<String> spamIndicators;
    private BigDecimal confidence;
    private String action; // ALLOW, BLOCK, REVIEW
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicModelingRequest {
    @NotNull(message = "Metin listesi boş olamaz")
    private List<String> texts;
    
    private String language;
    private Integer numTopics; // Default: 5
    private String algorithm; // LDA, NMF, BERT_TOPIC
    private Long userId;
    private String serviceName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicModelingResponse {
    private Long taskId;
    private String status;
    private List<ExtractedTopic> topics;
    private Map<Integer, String> documentTopicMapping;
    private String algorithm;
    private BigDecimal processingTimeMs;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedTopic {
        private Integer topicId;
        private String topicLabel;
        private List<TopicKeyword> keywords;
        private BigDecimal coherenceScore;
        private List<Integer> documentIds;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicKeyword {
        private String keyword;
        private BigDecimal weight;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchNLPRequest {
    @NotNull(message = "Metin listesi boş olamaz")
    private List<String> texts;
    
    @NotNull(message = "İşlem türleri belirtilmelidir")
    private List<String> operations; // SENTIMENT, EMOTION, TRANSLATION, ENTITY_EXTRACTION
    
    private String language;
    private String targetLanguage; // For translation
    private Long userId;
    private String serviceName;
    private Map<String, Object> config;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchNLPResponse {
    private String batchId;
    private String status;
    private Integer totalTexts;
    private Integer processedTexts;
    private Integer failedTexts;
    private List<BatchNLPResult> results;
    private BigDecimal totalProcessingTimeMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchNLPResult {
        private Integer textIndex;
        private String text;
        private String status;
        private Map<String, Object> operationResults;
        private String errorMessage;
        private BigDecimal processingTimeMs;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPTaskResponse {
    private Long id;
    private String taskType;
    private Long userId;
    private String serviceName;
    private String inputText;
    private String outputText;
    private String language;
    private String targetLanguage;
    private String status;
    private BigDecimal confidence;
    private BigDecimal processingTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    
    // Analysis Results
    private String sentiment;
    private BigDecimal sentimentScore;
    private String dominantEmotion;
    private String detectedIntent;
    private String category;
    private Map<String, Object> entities;
    private String translationQuality;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageInfo {
    private String code;
    private String name;
    private String nativeName;
    private Boolean supported;
    private List<String> supportedOperations;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPStatisticsResponse {
    private LocalDateTime reportDate;
    private String timeframe;
    
    // Task Statistics
    private Long totalTasks;
    private Long completedTasks;
    private Long failedTasks;
    private Map<String, Long> tasksByType;
    private Map<String, Long> tasksByLanguage;
    
    // Performance Metrics
    private BigDecimal avgProcessingTime;
    private BigDecimal avgConfidence;
    private BigDecimal successRate;
    
    // Analysis Results
    private Map<String, Long> sentimentDistribution;
    private Map<String, Long> emotionDistribution;
    private Map<String, Long> languageDistribution;
    private Long totalTranslations;
    private Long totalEntitiesExtracted;
    
    // Usage by Service
    private Map<String, Long> usageByService;
    private Map<String, BigDecimal> performanceByService;
    
    // Trends
    private List<Map<String, Object>> dailyUsageTrend;
    private List<Map<String, Object>> performanceTrend;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomModelTrainingRequest {
    @NotBlank(message = "Model adı boş olamaz")
    private String modelName;
    
    @NotBlank(message = "Model tipi belirtilmelidir")
    private String modelType; // TEXT_CLASSIFICATION, NER, SENTIMENT
    
    private String trainingDataPath;
    private String validationDataPath;
    private String language;
    private Map<String, Object> trainingConfig;
    private Long userId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelTrainingResponse {
    private String trainingJobId;
    private String modelName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime estimatedCompletionTime;
    private String progressUrl;
    private Map<String, Object> trainingConfig;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPFeedbackRequest {
    @NotNull(message = "Görev ID belirtilmelidir")
    private Long taskId;
    
    private Boolean wasCorrect;
    private String correctResult;
    private BigDecimal userRating; // 1-5
    private String feedback;
    private Long userId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPHealthResponse {
    private String status; // HEALTHY, DEGRADED, DOWN
    private Map<String, String> serviceStatuses;
    private Map<String, BigDecimal> performanceMetrics;
    private List<String> warnings;
    private List<String> errors;
    private LocalDateTime lastChecked;
}