package com.rakshakai.core_service.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rakshakai.core_service.models.Tourist;
import com.rakshakai.core_service.models.TrackingHistory;
import com.rakshakai.core_service.repositories.TouristRepository;
import com.rakshakai.core_service.repositories.TrackingHistoryRepository;
import com.rakshakai.core_service.services.EmailService;

@RestController
@RequestMapping("/api/checkpoints")
public class CheckpointController {

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TrackingHistoryRepository trackingRepository;

    // --- GLOBAL DISASTER FLAG ---
    private boolean isGlobalEvacuationActive = false;

    // METHOD A: Fetch all tourists for the Global Base Camp Dashboard
    @GetMapping("/tourists")
    public List<Tourist> getAllTourists() {
        return touristRepository.findAll();
    }

    // METHOD B: Normal RFID Gate Taps
    @PostMapping("/scan")
    public Map<String, Object> rfidTap(@RequestBody Map<String, String> tapData) {
        String touristId = tapData.get("touristId");
        String location = tapData.get("location");
        Map<String, Object> response = new HashMap<>();

        Optional<Tourist> touristOpt = touristRepository.findById(touristId);

        if (touristOpt.isPresent()) {
            Tourist tourist = touristOpt.get();
            tourist.setLastKnownLocation(location);
            touristRepository.save(tourist);

            response.put("status", "ACCESS GRANTED");
            response.put("message", "Tourist " + touristId + " passed through " + location);
        } else {
            response.put("status", "ERROR");
            response.put("message", "Unregistered wristband ID: " + touristId);
        }

        return response;
    }

    // METHOD C: Active LoRaWAN / GPS Pings (Emergency Tracking)
    @PostMapping("/emergency")
    public Map<String, Object> emergencyPing(@RequestBody Map<String, String> pingData) {
        String touristId = pingData.get("touristId");
        String lat = pingData.get("latitude");
        String lon = pingData.get("longitude");
        String heartRate = pingData.getOrDefault("heartRate", "UNKNOWN");
        
        Double aiRiskScore = pingData.containsKey("aiRiskScore") ? Double.parseDouble(pingData.get("aiRiskScore")) : 0.0;
        Boolean isAnomaly = pingData.containsKey("isAnomaly") ? Boolean.parseBoolean(pingData.get("isAnomaly")) : false;

        Map<String, Object> response = new HashMap<>();

        Optional<Tourist> touristOpt = touristRepository.findById(touristId);

        if (touristOpt.isPresent()) {
            Tourist tourist = touristOpt.get();
            
            // Lock SOS State ONLY if they are currently safe (TRACKING_ACTIVE).
            // This stops automated background AI pings from overwriting commander choices like DISPATCHED!
            if (isAnomaly) {
                if ("TRACKING_ACTIVE".equals(tourist.getStatus())) {
                    System.out.println("🚨 DEBUG: NEW EMERGENCY! Locking state to SOS_ACTIVE...");
                    tourist.setStatus("SOS_ACTIVE");
                    emailService.sendSOSAlert(tourist, lat, lon);
                } else {
                    System.out.println("⚠️ DEBUG: Target is already in rescue lifecycle (" + tourist.getStatus() + "). Ignoring automated AI override.");
                }
            }
    
            touristRepository.save(tourist);

            // Save History
            TrackingHistory history = new TrackingHistory();
            history.setTourist(tourist);
            history.setLatitude(lat);
            history.setLongitude(lon);
            history.setHeartRate(heartRate);
            history.setPingType("LORA_PING");
            history.setAiRiskScore(aiRiskScore);
            history.setIsAnomaly(isAnomaly);
            history.setRecordedAt(java.time.LocalDateTime.now());
            trackingRepository.save(history);

            response.put("status", isAnomaly ? "EMERGENCY_ANOMALY_SAVED" : "TRACKING_SAVED");
            
            // --- DOWNLINK COMMAND FOR OFFLINE WRISTBANDS ---
            response.put("evacuate", isGlobalEvacuationActive); 

        } else {
            response.put("status", "ERROR");
            response.put("message", "Unregistered wristband ID: " + touristId);
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // METHOD D: Base Camp Dashboard (Fetch live path for the map)
    @GetMapping("/history/{touristId}")
    public List<TrackingHistory> getTouristHistory(@PathVariable String touristId) {
        return trackingRepository.findByTouristTouristIdOrderByRecordedAtDesc(touristId);
    }

    // --- NEW METHOD 1: Update Rescue Lifecycle Status ---
    @PostMapping("/update-status/{touristId}")
    public Map<String, Object> updateStatus(@PathVariable String touristId, @RequestParam String status, @RequestParam(required = false) String notes) {
        Map<String, Object> response = new HashMap<>();
        Optional<Tourist> touristOpt = touristRepository.findById(touristId);

        if (touristOpt.isPresent()) {
            Tourist tourist = touristOpt.get();
            tourist.setStatus(status);
            
            // If the rescue team adds medical notes, append them to the profile
            if (notes != null && !notes.isEmpty()) {
                String currentNotes = tourist.getMedicalConditions() == null ? "" : tourist.getMedicalConditions();
                tourist.setMedicalConditions(currentNotes + " | [RESCUE LOG]: " + notes);
            }
            
            touristRepository.save(tourist);
            response.put("status", "SUCCESS");
            System.out.println("✅ DEBUG: Commander updated " + tourist.getFullName() + " to " + status);
        }
        return response;
    }

    // --- NEW METHOD 2: Trigger Global Evacuation ---
    @PostMapping("/evacuate")
    public Map<String, Object> toggleGlobalEvacuation(@RequestParam boolean active) {
        this.isGlobalEvacuationActive = active;
        System.out.println("⚠️ GLOBAL EVACUATION STATUS: " + active);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("evacuationActive", active);
        return response;
    }
}