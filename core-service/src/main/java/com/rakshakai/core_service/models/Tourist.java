package com.rakshakai.core_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tourists")
public class Tourist {

    @Id
    @Column(name = "tourist_id")
    private String touristId;
    
    private String fullName;
    private String bloodType;
    private String emergencyContact;
    private String medicalConditions;
    private String status;

    // Getters and Setters
    public String getTouristId() { return touristId; }
    public void setTouristId(String touristId) { this.touristId = touristId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    
    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}