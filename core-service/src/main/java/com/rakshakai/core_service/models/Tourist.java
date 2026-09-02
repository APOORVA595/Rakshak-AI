package com.rakshakai.core_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tourists")
public class Tourist {

    @Id
    private String id; // The NFC or USN tag ID (e.g., pes1ug23cs095)
    
    private String fullName;
    private String bloodGroup;
    private String medicalNotes;
    private String emergencyContact;
    private String status; // SAFE, MISSING, FOUND_ALIVE, FOUND_DECEASED
    private String faceVectorId; // Links to the Python AI database

    // Empty constructor required by Spring
    public Tourist() {
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFaceVectorId() { return faceVectorId; }
    public void setFaceVectorId(String faceVectorId) { this.faceVectorId = faceVectorId; }
}