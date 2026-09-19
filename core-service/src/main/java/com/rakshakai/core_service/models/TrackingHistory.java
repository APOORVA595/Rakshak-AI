package com.rakshakai.core_service.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tracking_history")
public class TrackingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tourist_id", referencedColumnName = "tourist_id")
    private Tourist tourist;

    private String latitude;
    private String longitude;
    private String heartRate;
    private String pingType;
    
    // --- New AI Fields ---
    @Column(name = "ai_risk_score")
    private Double aiRiskScore;
    
    @Column(name = "is_anomaly")
    private Boolean isAnomaly;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // --- GETTERS ---
    public Long getId() { return id; }
    public Tourist getTourist() { return tourist; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
    public String getHeartRate() { return heartRate; }
    public String getPingType() { return pingType; }
    public Double getAiRiskScore() { return aiRiskScore; }
    public Boolean getIsAnomaly() { return isAnomaly; }
    public LocalDateTime getRecordedAt() { return recordedAt; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setTourist(Tourist tourist) { this.tourist = tourist; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public void setHeartRate(String heartRate) { this.heartRate = heartRate; }
    public void setPingType(String pingType) { this.pingType = pingType; }
    public void setAiRiskScore(Double aiRiskScore) { this.aiRiskScore = aiRiskScore; }
    public void setIsAnomaly(Boolean isAnomaly) { this.isAnomaly = isAnomaly; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}