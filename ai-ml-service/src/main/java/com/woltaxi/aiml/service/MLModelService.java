package com.woltaxi.aiml.service;

import com.woltaxi.aiml.dto.*;
import com.woltaxi.aiml.entity.MLModel;
import com.woltaxi.aiml.entity.PredictionRequest;
import com.woltaxi.aiml.repository.MLModelRepository;
import com.woltaxi.aiml.repository.PredictionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Machine Learning Model Service
 * 
 * AI/ML modellerinin yönetimi, eğitimi, deployment ve serving işlemlerini yönetir.
 * Cross-platform compatibility ile Windows, macOS, Linux ve mobile desteği sağlar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MLModelService {

    private final MLModelRepository mlModelRepository;
    private final PredictionRequestRepository predictionRequestRepository;
    private final TensorFlowService tensorFlowService;
    private final ModelMonitoringService modelMonitoringService;
    private final SecurityService securityService;

    @Value("${woltaxi.aiml.models.storage-path:/app/models}")
    private String modelsStoragePath;

    @Value("${woltaxi.aiml.models.max-file-size:500MB}")
    private String maxFileSize;

    /**
     * Yeni ML model oluştur
     */
    public MLModelResponse createModel(CreateMLModelRequest request) {
        log.info("Creating new ML model: {}", request.getModelName());
        
        // Model adı kontrolü
        if (mlModelRepository.existsByModelName(request.getModelName())) {
            throw new IllegalArgumentException("Model with name '" + request.getModelName() + "' already exists");
        }
        
        MLModel model = MLModel.builder()
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .framework(request.getFramework())
                .description(request.getDescription())
                .version(request.getVersion() != null ? request.getVersion() : "1.0.0")
                .status(MLModel.ModelStatus.TRAINING)
                .isActive(false)
                .totalPredictions(0L)
                .hyperparameters(convertToJson(request.getHyperparameters()))
                .features(convertToJson(request.getFeatures()))
                .labels(convertToJson(request.getLabels()))
                .createdBy(request.getCreatedBy())
                .build();
        
        model = mlModelRepository.save(model);
        log.info("Created ML model with ID: {}", model.getId());
        
        return convertToResponse(model);
    }

    /**
     * Model dosyası yükle
     */
    public MLModelResponse uploadModel(MultipartFile file, ModelUploadRequest request) {
        log.info("Uploading model file: {}", file.getOriginalFilename());
        
        try {
            // Dosya güvenlik kontrolü
            validateModelFile(file);
            
            // Model kaydı oluştur
            MLModel model = MLModel.builder()
                    .modelName(request.getModelName())
                    .modelType(request.getModelType())
                    .framework(request.getFramework())
                    .version(request.getVersion() != null ? request.getVersion() : "1.0.0")
                    .description(request.getDescription())
                    .status(MLModel.ModelStatus.TRAINED)
                    .isActive(false)
                    .totalPredictions(0L)
                    .hyperparameters(convertToJson(request.getHyperparameters()))
                    .features(convertToJson(request.getFeatures()))
                    .labels(convertToJson(request.getLabels()))
                    .build();
            
            // Dosyayı güvenli lokasyona kaydet
            String fileName = saveModelFile(file, model);
            model.setFilePath(fileName);
            
            model = mlModelRepository.save(model);
            log.info("Uploaded model file successfully: {}", fileName);
            
            return convertToResponse(model);
            
        } catch (IOException e) {
            log.error("Error uploading model file", e);
            throw new RuntimeException("Failed to upload model file", e);
        }
    }

    /**
     * Modelleri listele
     */
    @Transactional(readOnly = true)
    public Page<MLModelResponse> getModels(String modelType, String status, String framework, Pageable pageable) {
        log.info("Fetching models with filters - type: {}, status: {}, framework: {}", modelType, status, framework);
        
        Page<MLModel> models;
        
        if (modelType != null || status != null || framework != null) {
            models = mlModelRepository.findByFilters(modelType, status, framework, pageable);
        } else {
            models = mlModelRepository.findAll(pageable);
        }
        
        return models.map(this::convertToResponse);
    }

    /**
     * Model detayını getir
     */
    @Transactional(readOnly = true)
    public MLModelResponse getModelById(Long id) {
        log.info("Fetching model details for ID: {}", id);
        
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + id));
        
        return convertToResponse(model);
    }

    /**
     * Model güncelle
     */
    public MLModelResponse updateModel(Long id, UpdateMLModelRequest request) {
        log.info("Updating model with ID: {}", id);
        
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + id));
        
        // Güncellenebilir alanları set et
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }
        if (request.getVersion() != null) {
            model.setVersion(request.getVersion());
        }
        if (request.getAccuracy() != null) {
            model.setAccuracy(request.getAccuracy());
        }
        if (request.getPrecision() != null) {
            model.setPrecision(request.getPrecision());
        }
        if (request.getRecall() != null) {
            model.setRecall(request.getRecall());
        }
        if (request.getF1Score() != null) {
            model.setF1Score(request.getF1Score());
        }
        if (request.getHyperparameters() != null) {
            model.setHyperparameters(convertToJson(request.getHyperparameters()));
        }
        if (request.getFeatures() != null) {
            model.setFeatures(convertToJson(request.getFeatures()));
        }
        if (request.getLabels() != null) {
            model.setLabels(convertToJson(request.getLabels()));
        }
        
        model.setUpdatedBy(request.getUpdatedBy());
        model = mlModelRepository.save(model);
        
        log.info("Updated model with ID: {}", id);
        return convertToResponse(model);
    }

    /**
     * Model deploy et
     */
    public DeploymentResponse deployModel(Long id, ModelDeploymentRequest request) {
        log.info("Deploying model with ID: {} to environment: {}", id, request.getEnvironment());
        
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + id));
        
        if (!MLModel.ModelStatus.TRAINED.equals(model.getStatus()) && 
            !MLModel.ModelStatus.VALIDATED.equals(model.getStatus())) {
            throw new IllegalStateException("Model must be trained or validated before deployment");
        }
        
        try {
            // Model deployment işlemi
            String endpoint = deployModelToEnvironment(model, request);
            
            // Model durumunu güncelle
            model.setStatus(MLModel.ModelStatus.DEPLOYED);
            model.setDeploymentEnvironment(request.getEnvironment());
            model.setServingEndpoint(endpoint);
            model.setDeploymentDate(LocalDateTime.now());
            model.setIsActive(true);
            
            mlModelRepository.save(model);
            
            // Monitoring başlat
            modelMonitoringService.startMonitoring(model);
            
            return DeploymentResponse.builder()
                    .modelId(model.getId())
                    .modelName(model.getModelName())
                    .deploymentEnvironment(request.getEnvironment())
                    .servingEndpoint(endpoint)
                    .status("DEPLOYED")
                    .deploymentDate(LocalDateTime.now())
                    .deploymentDetails(Map.of(
                        "replicas", request.getReplicas() != null ? request.getReplicas() : 1,
                        "resources", request.getResourceRequirements() != null ? request.getResourceRequirements() : "standard"
                    ))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error deploying model", e);
            model.setStatus(MLModel.ModelStatus.FAILED);
            mlModelRepository.save(model);
            throw new RuntimeException("Failed to deploy model", e);
        }
    }

    /**
     * Model deployment'ını geri al
     */
    public void undeployModel(Long id) {
        log.info("Undeploying model with ID: {}", id);
        
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + id));
        
        if (!model.getIsActive()) {
            throw new IllegalStateException("Model is not currently deployed");
        }
        
        try {
            // Deployment'ı kaldır
            undeployModelFromEnvironment(model);
            
            // Model durumunu güncelle
            model.setStatus(MLModel.ModelStatus.TRAINED);
            model.setIsActive(false);
            model.setServingEndpoint(null);
            
            mlModelRepository.save(model);
            
            // Monitoring durdur
            modelMonitoringService.stopMonitoring(model);
            
            log.info("Undeployed model with ID: {}", id);
            
        } catch (Exception e) {
            log.error("Error undeploying model", e);
            throw new RuntimeException("Failed to undeploy model", e);
        }
    }

    /**
     * Tahmin yap
     */
    public PredictionResponse predict(Long id, PredictionRequestDto request) {
        log.info("Making prediction with model ID: {}", id);
        
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + id));
        
        if (!model.getIsActive()) {
            throw new IllegalStateException("Model is not active for predictions");
        }
        
        // Prediction request kaydı oluştur
        PredictionRequest predictionRequest = PredictionRequest.builder()
                .modelId(model.getId())
                .userId(request.getUserId())
                .serviceName(request.getServiceName())
                .predictionType(model.getModelType())
                .inputData(convertToJson(request.getInputData()))
                .status(PredictionRequest.RequestStatus.PROCESSING)
                .sessionId(request.getSessionId())
                .requestSource(request.getRequestSource())
                .build();
        
        predictionRequest = predictionRequestRepository.save(predictionRequest);
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Model ile tahmin yap
            Map<String, Object> predictions = makePredictionWithModel(model, request.getInputData());
            BigDecimal confidence = calculateConfidence(predictions);
            
            long endTime = System.currentTimeMillis();
            BigDecimal responseTime = BigDecimal.valueOf(endTime - startTime);
            
            // Sonuçları kaydet
            predictionRequest.setOutputData(convertToJson(predictions));
            predictionRequest.setConfidence(confidence);
            predictionRequest.setResponseTimeMs(responseTime);
            predictionRequest.setStatus(PredictionRequest.RequestStatus.COMPLETED);
            predictionRequest.setCompletedDate(LocalDateTime.now());
            
            predictionRequestRepository.save(predictionRequest);
            
            // Model istatistiklerini güncelle
            updateModelStats(model, responseTime);
            
            return PredictionResponse.builder()
                    .requestId(predictionRequest.getId())
                    .modelId(model.getId())
                    .modelName(model.getModelName())
                    .inputData(request.getInputData())
                    .predictions(predictions)
                    .confidence(confidence)
                    .responseTimeMs(responseTime)
                    .status("COMPLETED")
                    .requestDate(predictionRequest.getRequestDate())
                    .completedDate(predictionRequest.getCompletedDate())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error making prediction", e);
            
            predictionRequest.setStatus(PredictionRequest.RequestStatus.FAILED);
            predictionRequest.setErrorMessage(e.getMessage());
            predictionRequest.setCompletedDate(LocalDateTime.now());
            predictionRequestRepository.save(predictionRequest);
            
            throw new RuntimeException("Prediction failed", e);
        }
    }

    /**
     * Toplu tahmin yap
     */
    public List<PredictionResponse> batchPredict(BatchPredictionRequest request) {
        log.info("Making batch predictions for model ID: {} with {} data points", 
                request.getModelId(), request.getDataPoints().size());
        
        MLModel model = mlModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + request.getModelId()));
        
        if (!model.getIsActive()) {
            throw new IllegalStateException("Model is not active for predictions");
        }
        
        List<PredictionResponse> responses = new ArrayList<>();
        
        for (Map<String, Object> dataPoint : request.getDataPoints()) {
            try {
                PredictionRequestDto predictionRequest = PredictionRequestDto.builder()
                        .inputData(dataPoint)
                        .userId(request.getUserId())
                        .serviceName(request.getServiceName())
                        .sessionId(request.getSessionId())
                        .requestSource("BATCH")
                        .build();
                
                PredictionResponse response = predict(request.getModelId(), predictionRequest);
                responses.add(response);
                
            } catch (Exception e) {
                log.error("Error in batch prediction for data point", e);
                // Hatalı tahminleri de listeye ekle
                responses.add(PredictionResponse.builder()
                        .modelId(model.getId())
                        .modelName(model.getModelName())
                        .inputData(dataPoint)
                        .status("FAILED")
                        .errorMessage(e.getMessage())
                        .requestDate(LocalDateTime.now())
                        .build());
            }
        }
        
        return responses;
    }

    /**
     * Aktif modelleri getir
     */
    @Transactional(readOnly = true)
    public List<MLModelResponse> getActiveModels() {
        log.info("Fetching active models");
        
        List<MLModel> activeModels = mlModelRepository.findByIsActiveTrue();
        return activeModels.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Model tiplerini getir
     */
    @Transactional(readOnly = true)
    public List<String> getModelTypes() {
        return Arrays.asList(
            "CLASSIFICATION", "REGRESSION", "CLUSTERING", 
            "NLP", "COMPUTER_VISION", "TIME_SERIES",
            "ANOMALY_DETECTION", "RECOMMENDATION"
        );
    }

    /**
     * Desteklenen framework'leri getir
     */
    @Transactional(readOnly = true)
    public List<String> getSupportedFrameworks() {
        return Arrays.asList(
            "TENSORFLOW", "PYTORCH", "SKLEARN", "WEKA",
            "XGBOOST", "LIGHTGBM", "CATBOOST", "KERAS"
        );
    }

    /**
     * Model sil
     */
    public void deleteModel(Long id) {
        log.info("Deleting model with ID: {}", id);
        
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with ID: " + id));
        
        if (model.getIsActive()) {
            throw new IllegalStateException("Cannot delete active model. Please undeploy first.");
        }
        
        // Model dosyasını sil
        if (model.getFilePath() != null) {
            deleteModelFile(model.getFilePath());
        }
        
        // İlgili prediction requestleri sil
        predictionRequestRepository.deleteByModelId(id);
        
        // Model kaydını sil
        mlModelRepository.delete(model);
        
        log.info("Deleted model with ID: {}", id);
    }

    // Private helper methods
    
    private void validateModelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Model file cannot be empty");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
        
        // Güvenlik kontrolü - sadece belirli uzantılara izin ver
        String[] allowedExtensions = {".h5", ".pb", ".pkl", ".joblib", ".model", ".onnx"};
        boolean validExtension = Arrays.stream(allowedExtensions)
                .anyMatch(ext -> filename.toLowerCase().endsWith(ext));
        
        if (!validExtension) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + String.join(", ", allowedExtensions));
        }
        
        // Dosya boyutu kontrolü (500MB limit)
        if (file.getSize() > 500 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 500MB");
        }
    }

    private String saveModelFile(MultipartFile file, MLModel model) throws IOException {
        Path uploadPath = Paths.get(modelsStoragePath);
        
        // Dizin yoksa oluştur
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Güvenli dosya adı oluştur
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String safeFilename = model.getModelName().replaceAll("[^a-zA-Z0-9_-]", "_") + 
                             "_v" + model.getVersion() + extension;
        
        Path filePath = uploadPath.resolve(safeFilename);
        
        // Dosyayı kaydet
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return safeFilename;
    }

    private void deleteModelFile(String fileName) {
        try {
            Path filePath = Paths.get(modelsStoragePath, fileName);
            Files.deleteIfExists(filePath);
            log.info("Deleted model file: {}", fileName);
        } catch (IOException e) {
            log.error("Error deleting model file: {}", fileName, e);
        }
    }

    private String convertToJson(Object object) {
        if (object == null) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error converting to JSON", e);
            return null;
        }
    }

    private MLModelResponse convertToResponse(MLModel model) {
        return MLModelResponse.builder()
                .id(model.getId())
                .modelName(model.getModelName())
                .modelType(model.getModelType())
                .version(model.getVersion())
                .description(model.getDescription())
                .framework(model.getFramework())
                .status(model.getStatus().name())
                .isActive(model.getIsActive())
                .servingEndpoint(model.getServingEndpoint())
                .accuracy(model.getAccuracy())
                .precision(model.getPrecision())
                .recall(model.getRecall())
                .f1Score(model.getF1Score())
                .mse(model.getMse())
                .mae(model.getMae())
                .trainingStartDate(model.getTrainingStartDate())
                .trainingEndDate(model.getTrainingEndDate())
                .epochs(model.getEpochs())
                .batchSize(model.getBatchSize())
                .learningRate(model.getLearningRate())
                .deploymentDate(model.getDeploymentDate())
                .deploymentEnvironment(model.getDeploymentEnvironment())
                .lastPredictionDate(model.getLastPredictionDate())
                .totalPredictions(model.getTotalPredictions())
                .avgResponseTime(model.getAvgResponseTime())
                .driftScore(model.getDriftScore())
                .lastDriftCheck(model.getLastDriftCheck())
                .driftDetected(model.getDriftDetected())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    // Placeholder methods - actual implementation would depend on specific ML frameworks
    private String deployModelToEnvironment(MLModel model, ModelDeploymentRequest request) {
        // Implementation for deploying model to specific environment
        return "http://localhost:8094/api/ai-ml/models/" + model.getId() + "/predict";
    }

    private void undeployModelFromEnvironment(MLModel model) {
        // Implementation for undeploying model from environment
    }

    private Map<String, Object> makePredictionWithModel(MLModel model, Map<String, Object> inputData) {
        // Implementation for making predictions with the model
        return tensorFlowService.predict(model, inputData);
    }

    private BigDecimal calculateConfidence(Map<String, Object> predictions) {
        // Implementation for calculating prediction confidence
        return BigDecimal.valueOf(0.95); // Placeholder
    }

    private void updateModelStats(MLModel model, BigDecimal responseTime) {
        model.setTotalPredictions(model.getTotalPredictions() + 1);
        model.setLastPredictionDate(LocalDateTime.now());
        
        if (model.getAvgResponseTime() == null) {
            model.setAvgResponseTime(responseTime);
        } else {
            // Moving average calculation
            BigDecimal currentAvg = model.getAvgResponseTime();
            BigDecimal newAvg = currentAvg.multiply(BigDecimal.valueOf(0.9))
                    .add(responseTime.multiply(BigDecimal.valueOf(0.1)));
            model.setAvgResponseTime(newAvg);
        }
        
        mlModelRepository.save(model);
    }

    // Additional methods would be implemented based on requirements...
    public ModelPerformanceResponse getModelPerformance(Long id, String startDate, String endDate) {
        // Implementation for model performance metrics
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TrainingResponse retrainModel(Long id, ModelRetrainingRequest request) {
        // Implementation for model retraining
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<PredictionHistoryResponse> getModelPredictions(Long id, String startDate, String endDate, Pageable pageable) {
        // Implementation for prediction history
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ValidationResponse validateModel(Long id, ModelValidationRequest request) {
        // Implementation for model validation
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public DriftDetectionResponse checkModelDrift(Long id, DriftDetectionRequest request) {
        // Implementation for drift detection
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Map<String, Object> getModelsHealthStatus() {
        // Implementation for health status
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("activeModels", mlModelRepository.countByIsActiveTrue());
        health.put("totalModels", mlModelRepository.count());
        health.put("timestamp", LocalDateTime.now());
        return health;
    }
}