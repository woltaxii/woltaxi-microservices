package com.woltaxi.aiml.controller;

import com.woltaxi.aiml.dto.*;
import com.woltaxi.aiml.service.ComputerVisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Computer Vision Controller
 * 
 * Görüntü işleme, nesne tespiti, yüz tanıma, OCR ve diğer
 * bilgisayarlı görü işlemlerini yöneten controller
 */
@RestController
@RequestMapping("/api/ai-ml/computer-vision")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Computer Vision", description = "Bilgisayarlı görü ve görüntü işleme API'leri")
public class ComputerVisionController {

    private final ComputerVisionService computerVisionService;

    @PostMapping("/face-recognition")
    @Operation(summary = "Yüz tanıma", description = "Görüntüdeki yüzleri tanı ve kimlikleri tespit et")
    public ResponseEntity<FaceRecognitionResponse> recognizeFaces(
            @Parameter(description = "Analiz edilecek görüntü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Servis adı") @RequestParam(required = false) String serviceName) {
        log.info("Processing face recognition for image: {}", image.getOriginalFilename());
        FaceRecognitionResponse response = computerVisionService.recognizeFaces(image, userId, serviceName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/object-detection")
    @Operation(summary = "Nesne tespiti", description = "Görüntüdeki nesneleri tespit et ve sınıflandır")
    public ResponseEntity<ObjectDetectionResponse> detectObjects(
            @Parameter(description = "Analiz edilecek görüntü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Tespit edilecek nesne tipleri") @RequestParam(required = false) List<String> objectTypes,
            @Parameter(description = "Minimum güven skoru") @RequestParam(defaultValue = "0.5") double minConfidence,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId) {
        log.info("Processing object detection for image: {}", image.getOriginalFilename());
        ObjectDetectionResponse response = computerVisionService.detectObjects(image, objectTypes, minConfidence, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ocr")
    @Operation(summary = "Optik karakter tanıma", description = "Görüntüdeki metni tanı ve çıkar")
    public ResponseEntity<OCRResponse> extractText(
            @Parameter(description = "OCR yapılacak görüntü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Metin dili (tr, en, ar)") @RequestParam(defaultValue = "tr") String language,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId) {
        log.info("Processing OCR for image: {} in language: {}", image.getOriginalFilename(), language);
        OCRResponse response = computerVisionService.extractText(image, language, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/license-plate")
    @Operation(summary = "Plaka tanıma", description = "Araç plakalarını tespit et ve oku")
    public ResponseEntity<LicensePlateResponse> recognizeLicensePlate(
            @Parameter(description = "Plaka görüntüsü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Servis adı") @RequestParam(required = false) String serviceName) {
        log.info("Processing license plate recognition for image: {}", image.getOriginalFilename());
        LicensePlateResponse response = computerVisionService.recognizeLicensePlate(image, userId, serviceName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicle-detection")
    @Operation(summary = "Araç tespiti", description = "Görüntüdeki araçları tespit et ve sınıflandır")
    public ResponseEntity<VehicleDetectionResponse> detectVehicles(
            @Parameter(description = "Analiz edilecek görüntü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Araç tiplerini tespit et") @RequestParam(defaultValue = "true") boolean detectType,
            @Parameter(description = "Renk tespiti yap") @RequestParam(defaultValue = "true") boolean detectColor,
            @Parameter(description = "Marka/model tespiti") @RequestParam(defaultValue = "false") boolean detectBrand,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId) {
        log.info("Processing vehicle detection for image: {}", image.getOriginalFilename());
        VehicleDetectionResponse response = computerVisionService.detectVehicles(
            image, detectType, detectColor, detectBrand, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/safety-analysis")
    @Operation(summary = "Güvenlik analizi", description = "Görüntüdeki güvenlik ihlallerini ve tehlikeleri tespit et")
    public ResponseEntity<SafetyAnalysisResponse> analyzeSafety(
            @Parameter(description = "Analiz edilecek görüntü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Acil durum tespiti") @RequestParam(defaultValue = "true") boolean detectEmergency,
            @Parameter(description = "Trafik ihlali tespiti") @RequestParam(defaultValue = "true") boolean detectViolations,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId) {
        log.info("Processing safety analysis for image: {}", image.getOriginalFilename());
        SafetyAnalysisResponse response = computerVisionService.analyzeSafety(
            image, detectEmergency, detectViolations, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/image-quality")
    @Operation(summary = "Görüntü kalitesi analizi", description = "Görüntü kalitesini değerlendir ve iyileştirme önerileri sun")
    public ResponseEntity<ImageQualityResponse> analyzeImageQuality(
            @Parameter(description = "Kalitesi analiz edilecek görüntü") @RequestParam("image") MultipartFile image,
            @Parameter(description = "Otomatik iyileştirme uygula") @RequestParam(defaultValue = "false") boolean autoEnhance) {
        log.info("Analyzing image quality for: {}", image.getOriginalFilename());
        ImageQualityResponse response = computerVisionService.analyzeImageQuality(image, autoEnhance);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-analysis")
    @Operation(summary = "Toplu görüntü analizi", description = "Birden fazla görüntüyü aynı anda analiz et")
    public ResponseEntity<BatchAnalysisResponse> batchAnalysis(
            @Parameter(description = "Analiz edilecek görüntüler") @RequestParam("images") List<MultipartFile> images,
            @Valid @RequestBody BatchAnalysisRequest request) {
        log.info("Processing batch analysis for {} images", images.size());
        BatchAnalysisResponse response = computerVisionService.batchAnalysis(images, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    @Operation(summary = "CV görevlerini listele", description = "Bilgisayarlı görü görevlerinin geçmişini listele")
    public ResponseEntity<Page<ComputerVisionTaskResponse>> getTasks(
            @Parameter(description = "Görev tipi filtresi") @RequestParam(required = false) String taskType,
            @Parameter(description = "Durum filtresi") @RequestParam(required = false) String status,
            @Parameter(description = "Kullanıcı ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) String endDate,
            Pageable pageable) {
        log.info("Fetching CV tasks with filters");
        Page<ComputerVisionTaskResponse> response = computerVisionService.getTasks(
            taskType, status, userId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "CV görev detayı", description = "Belirtilen görevin detay bilgilerini getir")
    public ResponseEntity<ComputerVisionTaskResponse> getTask(@PathVariable Long id) {
        log.info("Fetching CV task details for ID: {}", id);
        ComputerVisionTaskResponse response = computerVisionService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{id}/results")
    @Operation(summary = "Görev sonuçlarını getir", description = "CV görevinin detaylı analiz sonuçlarını getir")
    public ResponseEntity<TaskResultsResponse> getTaskResults(@PathVariable Long id) {
        log.info("Fetching task results for ID: {}", id);
        TaskResultsResponse response = computerVisionService.getTaskResults(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/person-registration")
    @Operation(summary = "Kişi kaydı", description = "Yüz tanıma sistemi için yeni kişi kaydet")
    public ResponseEntity<PersonRegistrationResponse> registerPerson(
            @Parameter(description = "Kişinin fotoğrafı") @RequestParam("photo") MultipartFile photo,
            @Valid @RequestBody PersonRegistrationRequest request) {
        log.info("Registering new person: {}", request.getPersonName());
        PersonRegistrationResponse response = computerVisionService.registerPerson(photo, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "CV istatistikleri", description = "Bilgisayarlı görü sisteminin performans istatistikleri")
    public ResponseEntity<ComputerVisionStatsResponse> getStatistics(
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) String endDate,
            @Parameter(description = "Görev tipi") @RequestParam(required = false) String taskType) {
        log.info("Fetching CV statistics");
        ComputerVisionStatsResponse response = computerVisionService.getStatistics(startDate, endDate, taskType);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/real-time-stream")
    @Operation(summary = "Gerçek zamanlı video analizi", description = "Video stream için gerçek zamanlı görüntü analizi başlat")
    public ResponseEntity<StreamAnalysisResponse> startStreamAnalysis(
            @Valid @RequestBody StreamAnalysisRequest request) {
        log.info("Starting real-time stream analysis");
        StreamAnalysisResponse response = computerVisionService.startStreamAnalysis(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/real-time-stream/{sessionId}")
    @Operation(summary = "Stream analizi durdur", description = "Gerçek zamanlı video analizini durdur")
    public ResponseEntity<Void> stopStreamAnalysis(@PathVariable String sessionId) {
        log.info("Stopping stream analysis for session: {}", sessionId);
        computerVisionService.stopStreamAnalysis(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/supported-formats")
    @Operation(summary = "Desteklenen formatlar", description = "Desteklenen görüntü formatlarını listele")
    public ResponseEntity<List<String>> getSupportedFormats() {
        List<String> formats = computerVisionService.getSupportedImageFormats();
        return ResponseEntity.ok(formats);
    }
}