package com.woltaxi.aiml.controller;

import com.woltaxi.aiml.dto.*;
import com.woltaxi.aiml.service.MLModelService;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Machine Learning Model Management Controller
 * 
 * AI/ML modellerinin yönetimi, eğitimi, deployment ve serving işlemleri
 */
@RestController
@RequestMapping("/api/ai-ml/models")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ML Model Management", description = "Machine Learning model yönetimi ve serving API'leri")
public class MLModelController {

    private final MLModelService mlModelService;

    @PostMapping
    @Operation(summary = "Yeni ML model oluştur", description = "Yeni bir machine learning modeli kaydet")
    public ResponseEntity<MLModelResponse> createModel(
            @Valid @RequestBody CreateMLModelRequest request) {
        log.info("Creating new ML model: {}", request.getModelName());
        MLModelResponse response = mlModelService.createModel(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    @Operation(summary = "Model dosyası yükle", description = "Eğitilmiş model dosyasını sisteme yükle")
    public ResponseEntity<MLModelResponse> uploadModel(
            @Parameter(description = "Model dosyası") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Model bilgileri") @Valid @RequestBody ModelUploadRequest request) {
        log.info("Uploading model file: {}", file.getOriginalFilename());
        MLModelResponse response = mlModelService.uploadModel(file, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Modelleri listele", description = "Sistemdeki tüm ML modellerini listele")
    public ResponseEntity<Page<MLModelResponse>> getModels(
            @Parameter(description = "Model tipi filtresi") @RequestParam(required = false) String modelType,
            @Parameter(description = "Model durumu filtresi") @RequestParam(required = false) String status,
            @Parameter(description = "Framework filtresi") @RequestParam(required = false) String framework,
            Pageable pageable) {
        log.info("Fetching models with filters - type: {}, status: {}, framework: {}", 
                modelType, status, framework);
        Page<MLModelResponse> response = mlModelService.getModels(modelType, status, framework, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Model detayını getir", description = "Belirtilen ID'deki modelin detay bilgilerini getir")
    public ResponseEntity<MLModelResponse> getModel(@PathVariable Long id) {
        log.info("Fetching model details for ID: {}", id);
        MLModelResponse response = mlModelService.getModelById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Model güncelle", description = "Mevcut modelin bilgilerini güncelle")
    public ResponseEntity<MLModelResponse> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMLModelRequest request) {
        log.info("Updating model with ID: {}", id);
        MLModelResponse response = mlModelService.updateModel(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deploy")
    @Operation(summary = "Model deploy et", description = "Modeli production ortamına deploy et")
    public ResponseEntity<DeploymentResponse> deployModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelDeploymentRequest request) {
        log.info("Deploying model with ID: {} to environment: {}", id, request.getEnvironment());
        DeploymentResponse response = mlModelService.deployModel(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/undeploy")
    @Operation(summary = "Model deployment'ını geri al", description = "Modelin deployment'ını devre dışı bırak")
    public ResponseEntity<Void> undeployModel(@PathVariable Long id) {
        log.info("Undeploying model with ID: {}", id);
        mlModelService.undeployModel(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/predict")
    @Operation(summary = "Tahmin yap", description = "Belirtilen model ile tahmin yap")
    public ResponseEntity<PredictionResponse> predict(
            @PathVariable Long id,
            @Valid @RequestBody PredictionRequestDto request) {
        log.info("Making prediction with model ID: {}", id);
        PredictionResponse response = mlModelService.predict(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/predict/batch")
    @Operation(summary = "Toplu tahmin yap", description = "Birden fazla veri noktası için tahmin yap")
    public ResponseEntity<List<PredictionResponse>> batchPredict(
            @Valid @RequestBody BatchPredictionRequest request) {
        log.info("Making batch predictions for model ID: {} with {} data points", 
                request.getModelId(), request.getDataPoints().size());
        List<PredictionResponse> response = mlModelService.batchPredict(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/performance")
    @Operation(summary = "Model performansı", description = "Modelin performans metriklerini getir")
    public ResponseEntity<ModelPerformanceResponse> getModelPerformance(
            @PathVariable Long id,
            @Parameter(description = "Başlangıç tarihi (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi (YYYY-MM-DD)") @RequestParam(required = false) String endDate) {
        log.info("Fetching performance metrics for model ID: {}", id);
        ModelPerformanceResponse response = mlModelService.getModelPerformance(id, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retrain")
    @Operation(summary = "Model yeniden eğit", description = "Yeni verilerle modeli yeniden eğit")
    public ResponseEntity<TrainingResponse> retrainModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelRetrainingRequest request) {
        log.info("Starting retraining for model ID: {}", id);
        TrainingResponse response = mlModelService.retrainModel(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/predictions")
    @Operation(summary = "Model tahminlerini getir", description = "Modelin yaptığı tahminlerin geçmişini getir")
    public ResponseEntity<Page<PredictionHistoryResponse>> getModelPredictions(
            @PathVariable Long id,
            @Parameter(description = "Başlangıç tarihi") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi") @RequestParam(required = false) String endDate,
            Pageable pageable) {
        log.info("Fetching prediction history for model ID: {}", id);
        Page<PredictionHistoryResponse> response = mlModelService.getModelPredictions(id, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Model doğrula", description = "Test verisi ile model performansını doğrula")
    public ResponseEntity<ValidationResponse> validateModel(
            @PathVariable Long id,
            @Valid @RequestBody ModelValidationRequest request) {
        log.info("Validating model with ID: {}", id);
        ValidationResponse response = mlModelService.validateModel(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Aktif modelleri getir", description = "Şu anda serving yapan aktif modelleri listele")
    public ResponseEntity<List<MLModelResponse>> getActiveModels() {
        log.info("Fetching active models");
        List<MLModelResponse> response = mlModelService.getActiveModels();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/types")
    @Operation(summary = "Model tiplerini getir", description = "Sistemdeki mevcut model tiplerini listele")
    public ResponseEntity<List<String>> getModelTypes() {
        log.info("Fetching available model types");
        List<String> response = mlModelService.getModelTypes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/frameworks")
    @Operation(summary = "Framework'leri getir", description = "Desteklenen ML framework'lerini listele")
    public ResponseEntity<List<String>> getSupportedFrameworks() {
        log.info("Fetching supported frameworks");
        List<String> response = mlModelService.getSupportedFrameworks();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/drift-detection")
    @Operation(summary = "Model drift kontrolü", description = "Model drift analizi yap")
    public ResponseEntity<DriftDetectionResponse> checkModelDrift(
            @PathVariable Long id,
            @Valid @RequestBody DriftDetectionRequest request) {
        log.info("Checking model drift for ID: {}", id);
        DriftDetectionResponse response = mlModelService.checkModelDrift(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Model sil", description = "Modeli sistemden kaldır")
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        log.info("Deleting model with ID: {}", id);
        mlModelService.deleteModel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Model servis durumu", description = "Tüm modellerin servis durumunu kontrol et")
    public ResponseEntity<Map<String, Object>> getModelsHealth() {
        log.info("Checking models health status");
        Map<String, Object> response = mlModelService.getModelsHealthStatus();
        return ResponseEntity.ok(response);
    }
}