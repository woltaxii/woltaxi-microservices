package com.woltaxi.aiml.controller;

import com.woltaxi.aiml.dto.*;
import com.woltaxi.aiml.service.NLPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Natural Language Processing Controller
 * 
 * Doğal dil işleme, metin analizi, çeviri, sentiment analizi
 * ve chatbot işlemlerini yöneten controller
 */
@RestController
@RequestMapping("/api/ai-ml/nlp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Natural Language Processing", description = "Doğal dil işleme ve metin analizi API'leri")
public class NLPController {

    private final NLPService nlpService;

    @PostMapping("/sentiment-analysis")
    @Operation(summary = "Sentiment analizi", description = "Metindeki duygu durumunu analiz et")
    public ResponseEntity<SentimentAnalysisResponse> analyzeSentiment(
            @Valid @RequestBody SentimentAnalysisRequest request) {
        log.info("Processing sentiment analysis for text in language: {}", request.getLanguage());
        SentimentAnalysisResponse response = nlpService.analyzeSentiment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/emotion-detection")
    @Operation(summary = "Duygu tespiti", description = "Metindeki detaylı duyguları tespit et")
    public ResponseEntity<EmotionDetectionResponse> detectEmotion(
            @Valid @RequestBody EmotionDetectionRequest request) {
        log.info("Processing emotion detection for text in language: {}", request.getLanguage());
        EmotionDetectionResponse response = nlpService.detectEmotion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate")
    @Operation(summary = "Metin çevirisi", description = "Metni hedef dile çevir")
    public ResponseEntity<TranslationResponse> translateText(
            @Valid @RequestBody TranslationRequest request) {
        log.info("Translating text from {} to {}", request.getSourceLanguage(), request.getTargetLanguage());
        TranslationResponse response = nlpService.translateText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/language-detection")
    @Operation(summary = "Dil tespiti", description = "Metnin dilini otomatik tespit et")
    public ResponseEntity<LanguageDetectionResponse> detectLanguage(
            @Valid @RequestBody LanguageDetectionRequest request) {
        log.info("Detecting language for provided text");
        LanguageDetectionResponse response = nlpService.detectLanguage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/entity-extraction")
    @Operation(summary = "Varlık çıkarımı", description = "Metinden kişi, yer, organizasyon gibi varlıkları çıkar")
    public ResponseEntity<EntityExtractionResponse> extractEntities(
            @Valid @RequestBody EntityExtractionRequest request) {
        log.info("Extracting entities from text in language: {}", request.getLanguage());
        EntityExtractionResponse response = nlpService.extractEntities(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/text-classification")
    @Operation(summary = "Metin sınıflandırma", description = "Metni kategorilere göre sınıflandır")
    public ResponseEntity<TextClassificationResponse> classifyText(
            @Valid @RequestBody TextClassificationRequest request) {
        log.info("Classifying text with model: {}", request.getModelType());
        TextClassificationResponse response = nlpService.classifyText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/intent-detection")
    @Operation(summary = "Niyet tespiti", description = "Kullanıcının niyetini tespit et (chatbot için)")
    public ResponseEntity<IntentDetectionResponse> detectIntent(
            @Valid @RequestBody IntentDetectionRequest request) {
        log.info("Detecting intent for user message in context: {}", request.getContext());
        IntentDetectionResponse response = nlpService.detectIntent(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chatbot/conversation")
    @Operation(summary = "Chatbot konuşması", description = "Chatbot ile doğal dil konuşması yap")
    public ResponseEntity<ChatbotResponse> chatWithBot(
            @Valid @RequestBody ChatbotRequest request) {
        log.info("Processing chatbot conversation for user: {}", request.getUserId());
        ChatbotResponse response = nlpService.processConversation(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/summarization")
    @Operation(summary = "Metin özetleme", description = "Uzun metni özetle")
    public ResponseEntity<SummarizationResponse> summarizeText(
            @Valid @RequestBody SummarizationRequest request) {
        log.info("Summarizing text with length: {} chars", request.getText().length());
        SummarizationResponse response = nlpService.summarizeText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/keyword-extraction")
    @Operation(summary = "Anahtar kelime çıkarımı", description = "Metinden önemli anahtar kelimeleri çıkar")
    public ResponseEntity<KeywordExtractionResponse> extractKeywords(
            @Valid @RequestBody KeywordExtractionRequest request) {
        log.info("Extracting keywords from text in language: {}", request.getLanguage());
        KeywordExtractionResponse response = nlpService.extractKeywords(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/text-similarity")
    @Operation(summary = "Metin benzerliği", description = "İki metin arasındaki benzerliği hesapla")
    public ResponseEntity<TextSimilarityResponse> calculateSimilarity(
            @Valid @RequestBody TextSimilarityRequest request) {
        log.info("Calculating text similarity using method: {}", request.getSimilarityMethod());
        TextSimilarityResponse response = nlpService.calculateSimilarity(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/readability-analysis")
    @Operation(summary = "Okunabilirlik analizi", description = "Metnin okunabilirlik seviyesini analiz et")
    public ResponseEntity<ReadabilityAnalysisResponse> analyzeReadability(
            @Valid @RequestBody ReadabilityAnalysisRequest request) {
        log.info("Analyzing readability for text in language: {}", request.getLanguage());
        ReadabilityAnalysisResponse response = nlpService.analyzeReadability(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/spam-detection")
    @Operation(summary = "Spam tespiti", description = "Metnin spam olup olmadığını tespit et")
    public ResponseEntity<SpamDetectionResponse> detectSpam(
            @Valid @RequestBody SpamDetectionRequest request) {
        log.info("Detecting spam in text with type: {}", request.getTextType());
        SpamDetectionResponse response = nlpService.detectSpam(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/topic-modeling")
    @Operation(summary = "Konu modelleme", description = "Metindeki ana konuları tespit et")
    public ResponseEntity<TopicModelingResponse> extractTopics(
            @Valid @RequestBody TopicModelingRequest request) {
        log.info("Extracting topics with {} topics requested", request.getNumTopics());
        TopicModelingResponse response = nlpService.extractTopics(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-processing")
    @Operation(summary = "Toplu metin işleme", description = "Birden fazla metni aynı anda işle")
    public ResponseEntity<BatchNLPResponse> processBatch(
            @Valid @RequestBody BatchNLPRequest request) {
        log.info("Processing batch NLP with {} texts and operations: {}", 
                request.getTexts().size(), request.getOperations());
        BatchNLPResponse response = nlpService.processBatch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    @Operation(summary = "NLP görevlerini listele", description = "Doğal dil işleme görevlerinin geçmişini listele")
    public ResponseEntity<Page<NLPTaskResponse>> getTasks(
            @Parameter(description = "Görev tipi") @RequestParam(required = false) String taskType,
            @Parameter(description = "Dil") @RequestParam(required = false) String language,
            @Parameter(description = "Durum") @RequestParam(required = false) String status,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) String endDate,
            Pageable pageable) {
        log.info("Fetching NLP tasks with filters");
        Page<NLPTaskResponse> response = nlpService.getTasks(
            taskType, language, status, userId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "NLP görev detayı", description = "Belirtilen görevin detay bilgilerini getir")
    public ResponseEntity<NLPTaskResponse> getTask(@PathVariable Long id) {
        log.info("Fetching NLP task details for ID: {}", id);
        NLPTaskResponse response = nlpService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supported-languages")
    @Operation(summary = "Desteklenen diller", description = "Sistemin desteklediği dilleri listele")
    public ResponseEntity<List<LanguageInfo>> getSupportedLanguages() {
        log.info("Fetching supported languages");
        List<LanguageInfo> response = nlpService.getSupportedLanguages();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "NLP istatistikleri", description = "Doğal dil işleme sisteminin performans istatistikleri")
    public ResponseEntity<NLPStatisticsResponse> getStatistics(
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) String endDate,
            @Parameter(description = "Görev tipi") @RequestParam(required = false) String taskType,
            @Parameter(description = "Dil") @RequestParam(required = false) String language) {
        log.info("Fetching NLP statistics");
        NLPStatisticsResponse response = nlpService.getStatistics(startDate, endDate, taskType, language);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/custom-model/train")
    @Operation(summary = "Özel model eğitimi", description = "Özel NLP modeli eğit")
    public ResponseEntity<ModelTrainingResponse> trainCustomModel(
            @Valid @RequestBody CustomModelTrainingRequest request) {
        log.info("Training custom NLP model: {}", request.getModelName());
        ModelTrainingResponse response = nlpService.trainCustomModel(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    @Operation(summary = "NLP geri bildirim", description = "NLP sonuçları için geri bildirim sağla")
    public ResponseEntity<Void> provideFeedback(
            @Valid @RequestBody NLPFeedbackRequest request) {
        log.info("Processing NLP feedback for task: {}", request.getTaskId());
        nlpService.provideFeedback(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    @Operation(summary = "NLP servis durumu", description = "NLP servislerinin sağlık durumunu kontrol et")
    public ResponseEntity<NLPHealthResponse> getHealthStatus() {
        log.info("Checking NLP service health");
        NLPHealthResponse response = nlpService.getHealthStatus();
        return ResponseEntity.ok(response);
    }
}